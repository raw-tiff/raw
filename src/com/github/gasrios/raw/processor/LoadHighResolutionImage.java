/*
 * © 2016 Guilherme Rios All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package com.github.gasrios.raw.processor;

import java.nio.ByteOrder;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.Math;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;
import com.github.gasrios.raw.lang.TiffProcessorException;

/*
 * This class makes all transformations deemed too complex to be at com.github.gasrios.raw.data.ImageFileDirectoryLoader when processing
 * the high resolution image IFD:
 *
 * 1. Reads image strips and converts them to a width X height pixel matrix;
 *
 * 2. Converts camera coordinates to XYZ D50 values;
 *
 * This pretty much ends all the dirty work needed to read the TIFF file and makes its information available to people whose
 * business is doing actual photo editing. Just extend this class and consume the info in attribute image.
 *
 * TODO assuming Orientation = 1
 * TODO assuming Compression = 1
 * TODO assuming PhotometricInterpretation = 34892
 * TODO assuming PlanarConfiguration = 1
 * TODO assuming SamplesPerPixel = 3. See Tags ReductionMatrix1 and ReductionMatrix2.
 */

public class LoadHighResolutionImage extends AbstractTiffProcessor {

	protected double[][][] image;

	private RATIONAL[]	analogBalance;
	private RATIONAL[]	asShotNeutral;
	private int[]		bitsPerSample;;
	private int			calibrationIlluminant1;
	private int			calibrationIlluminant2;
	private SRATIONAL[] cameraCalibration1;
	private SRATIONAL[] cameraCalibration2;
	private SRATIONAL[] colorMatrix1;
	private SRATIONAL[] colorMatrix2;
	private SRATIONAL[] forwardMatrix1;
	private SRATIONAL[] forwardMatrix2;
	private int			samplesPerPixel;

	@Override public void firstIfd(ImageFileDirectory ifd) {
		analogBalance		   = (RATIONAL[])  ifd.get(Tag.AnalogBalance);
		asShotNeutral		   = (RATIONAL[])  ifd.get(Tag.AsShotNeutral);
		calibrationIlluminant1 = (int)		   ifd.get(Tag.CalibrationIlluminant1);
		calibrationIlluminant2 = (int)		   ifd.get(Tag.CalibrationIlluminant2);
		cameraCalibration1	   = (SRATIONAL[]) ifd.get(Tag.CameraCalibration1);
		cameraCalibration2	   = (SRATIONAL[]) ifd.get(Tag.CameraCalibration2);
		colorMatrix1		   = (SRATIONAL[]) ifd.get(Tag.ColorMatrix1);
		colorMatrix2		   = (SRATIONAL[]) ifd.get(Tag.ColorMatrix2);
		forwardMatrix1		   = (SRATIONAL[]) ifd.get(Tag.ForwardMatrix1);
		forwardMatrix2		   = (SRATIONAL[]) ifd.get(Tag.ForwardMatrix2);
	}

	@Override public void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		// 3
		samplesPerPixel = ((int)  ifd.get(Tag.SamplesPerPixel));

		// 16 16 16
		bitsPerSample	= (int[]) ifd.get(Tag.BitsPerSample);

		// FIXME TIFF property is LONG, but java arrays have their size defined as int.
		int width   = (int) (long) ifd.get(Tag.ImageWidth);
		int length  = (int) (long) ifd.get(Tag.ImageLength);

		image = new double[width][length][0];

		int pixelSize = 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample[i]-1)/8;

		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);

		double[][] cameraToXYZ_D50 = Math.cameraToXYZ_D50(
			analogBalance,
			asShotNeutral,
			calibrationIlluminant1,
			calibrationIlluminant2,
			cameraCalibration1,
			cameraCalibration2,
			colorMatrix1,
			colorMatrix2,
			forwardMatrix1,
			forwardMatrix2
		);

		// See TIFF 6.0 Specification, page 39
		for (int i = 0; i < (int) ((length + rowsPerStrip - 1) / rowsPerStrip); i++) {
			short[] strip;
			strip = ifd.getStripAsShortArray(i);
			for (int j = 0; pixelSize*j < strip.length; j = j + 1)
				image[j%width][j/width + i*rowsPerStrip] =
					Math.multiply(cameraToXYZ_D50, readPixel(strip, j*pixelSize, ifd.getByteOrder()));
		}

		// Convert image to LSH (see main comment in com.github.gasrios.raw.lang.Math)
		/*for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++) {
			double[] xyz = image[i][j];
			image[i][j] = Math.luv2lsh(Math.xyz2luv(xyz));
		}*/

	}

	// TODO Assuming in the conversion pixel data is always unsigned. Double check this.
	// TODO Divide by WhiteLevel, not constant.
	private double[] readPixel(short[] strip, int offset, ByteOrder byteOrder) {

		double[] pixel = new double[samplesPerPixel];

		for (int i = 0; i < samplesPerPixel; i++) {
			if (bitsPerSample[i] <= 8) {
				short[] sample = new short[1];
				System.arraycopy(strip, offset, sample, 0, 1);
				pixel[i] = sample[0]/255D;
			} else if (bitsPerSample[i] <= 16) {
				short[] sample = new short[2];
				System.arraycopy(strip, offset, sample, 0, 2);
				pixel[i] = TiffInputStream.toInt(sample, byteOrder)/65535D;
			} else if (bitsPerSample[i] <= 32) {
				short[] sample = new short[4];
				System.arraycopy(strip, offset, sample, 0, 4);
				pixel[i] = TiffInputStream.toLong(sample, byteOrder)/4294967295D;
			}
			offset += 1 + (bitsPerSample[i]-1)/8;
		}

		return pixel;

	}

}