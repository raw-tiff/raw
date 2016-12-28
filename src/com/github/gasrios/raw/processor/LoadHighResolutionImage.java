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
	protected double[][]   cameraToXYZ_D50;
	protected double[]	   cameraNeutral;

	private   RATIONAL[]   analogBalance;
	private   int[]		   bitsPerSample;;
	private   int		   calibrationIlluminant1;
	private   int		   calibrationIlluminant2;
	private   SRATIONAL[]  cameraCalibration1;
	private   SRATIONAL[]  cameraCalibration2;
	private   SRATIONAL[]  colorMatrix1;
	private   SRATIONAL[]  colorMatrix2;
	private   SRATIONAL[]  forwardMatrix1;
	private   SRATIONAL[]  forwardMatrix2;
	private   int		   samplesPerPixel;

	@Override public void firstIfd(ImageFileDirectory ifd) {

		analogBalance		   = (RATIONAL[])  ifd.get(Tag.AnalogBalance);
		calibrationIlluminant1 = (int)		   ifd.get(Tag.CalibrationIlluminant1);
		calibrationIlluminant2 = (int)		   ifd.get(Tag.CalibrationIlluminant2);
		cameraCalibration1	   = (SRATIONAL[]) ifd.get(Tag.CameraCalibration1);
		cameraCalibration2	   = (SRATIONAL[]) ifd.get(Tag.CameraCalibration2);
		colorMatrix1		   = (SRATIONAL[]) ifd.get(Tag.ColorMatrix1);
		colorMatrix2		   = (SRATIONAL[]) ifd.get(Tag.ColorMatrix2);
		forwardMatrix1		   = (SRATIONAL[]) ifd.get(Tag.ForwardMatrix1);
		forwardMatrix2		   = (SRATIONAL[]) ifd.get(Tag.ForwardMatrix2);

		/*
		 * See https://forums.adobe.com/message/9222350
		 *
		 * CameraNeutral in this sense is a transform of the AsShotWhiteXY. It would be same as AsShotNeutral if the camera
		 * to XYZ matrix, etc used was the same, but otherwise not. So you should be able to do a "circular" transform, so
		 * e.g., CameraNeutral to AsShotWhiteXY to CameraNeutral if you use the same matrix. But bear in mind that there's a
		 * twist here. If for example a particular camera manufacturer specifies AsShotWhiteXY in their raw file, you don't
		 * necessarily know the matrix that they used, so you can't exactly know that their CameraNeutral values were. You
		 * only know the matrix that Adobe used, so your CameraNeutral as calculated may not be the same as what the camera
		 * originally thought.
		 */
		cameraNeutral = RATIONAL.asDoubleArray((RATIONAL[])  ifd.get(Tag.AsShotNeutral));

	}

	@Override public final void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		samplesPerPixel = ((int)	   ifd.get(Tag.SamplesPerPixel));
		bitsPerSample	= (int[])	   ifd.get(Tag.BitsPerSample);

		cameraToXYZ_D50 = Math.cameraToXYZ_D50(
				analogBalance,
				cameraNeutral,
				calibrationIlluminant1,
				calibrationIlluminant2,
				cameraCalibration1,
				cameraCalibration2,
				colorMatrix1,
				colorMatrix2,
				forwardMatrix1,
				forwardMatrix2
			);

		// FIXME TIFF property is LONG, but java arrays have their size defined as int.
		int width		= (int) (long) ifd.get(Tag.ImageWidth);
		int length		= (int) (long) ifd.get(Tag.ImageLength);

		//image = new double[width][length][0];
		image = new double[length][width][0];

		int pixelSize = 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample[i]-1)/8;

		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);

		// See TIFF 6.0 Specification, page 39
		for (int i = 0; i < (int) ((length + rowsPerStrip - 1) / rowsPerStrip); i++) {
			short[] strip = ifd.getStripAsShortArray(i);
			for (int j = 0; pixelSize*j < strip.length; j = j + 1)
				//image[j%width][j/width + i*rowsPerStrip] =
				image[length - j/width + i*rowsPerStrip][width - j%width] =
					camera2lsh(strip, j*pixelSize, ifd.getByteOrder());
		}

	}

	protected double[] camera2lsh(short[] strip, int offset, ByteOrder byteOrder) {

		double[] sensorLevels = readSensorLevels(strip, offset, byteOrder);

		/*
		 * Crop before converting to CIE 1931 XYZ, otherwise only god knows what might happen to hue.
		 *
		 * See https://forums.adobe.com/message/9222350
		 *
		 * That's an inherent problem of Bayer sensor cameras. The sensitivities of the various channels are such
		 * that one channel will always saturate before others.
		 */
		for (int k = 0; k < sensorLevels.length; k++) if (sensorLevels[k] > cameraNeutral[k]) sensorLevels[k] = cameraNeutral[k];

		return Math.luv2lsh(Math.xyz2luv(Math.multiply(cameraToXYZ_D50, sensorLevels)));

	}

	// TODO Divide by WhiteLevel, not constant.
	protected final double[] readSensorLevels(short[] strip, int offset, ByteOrder byteOrder) {
		double[] sensorLevels = new double[samplesPerPixel];
		for (int i = 0; i < samplesPerPixel; i++) {
			if (bitsPerSample[i] <= 8) {
				short[] sample = new short[1];
				System.arraycopy(strip, offset, sample, 0, 1);
				sensorLevels[i] = sample[0]/255D;
			} else if (bitsPerSample[i] <= 16) {
				short[] sample = new short[2];
				System.arraycopy(strip, offset, sample, 0, 2);
				sensorLevels[i] = TiffInputStream.toInt(sample, byteOrder)/65535D;
			} else if (bitsPerSample[i] <= 32) {
				short[] sample = new short[4];
				System.arraycopy(strip, offset, sample, 0, 4);
				sensorLevels[i] = TiffInputStream.toLong(sample, byteOrder)/4294967295D;
			}
			offset += 1 + (bitsPerSample[i]-1)/8;
		}
		return sensorLevels;
	}

}