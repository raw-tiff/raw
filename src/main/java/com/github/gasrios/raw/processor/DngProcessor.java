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
import com.github.gasrios.raw.formats.ImageCIEXYZ;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.Math;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.lang.TiffProcessorRuntimeException;

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
 * TODO assuming SamplesPerPixel = 3. See Tags ReductionMatrix1 and ReductionMatrix2.
 */

public class DngProcessor<Image extends ImageCIEXYZ> extends AbstractTiffProcessor {

	protected	double[]	cameraNeutral;
	protected	double[][]	cameraToXYZ_D50;
	protected	Image		image;

	private		int[]		bitsPerSample;
	private		int			samplesPerPixel;
	private		int[]		whiteLevel;

	public DngProcessor(Image image) { this.image = image; }

	@Override public void firstIfd(ImageFileDirectory ifd) {

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
		cameraNeutral = RATIONAL.asDoubleArray((RATIONAL[]) ifd.get(Tag.AsShotNeutral));

		cameraToXYZ_D50 = Math.cameraToXYZ_D50(
				(RATIONAL[])	ifd.get(Tag.AnalogBalance),
				cameraNeutral,
				(int)			ifd.get(Tag.CalibrationIlluminant1),
				(int)			ifd.get(Tag.CalibrationIlluminant2),
				(SRATIONAL[])	ifd.get(Tag.CameraCalibration1),
				(SRATIONAL[])	ifd.get(Tag.CameraCalibration2),
				(SRATIONAL[])	ifd.get(Tag.ColorMatrix1),
				(SRATIONAL[])	ifd.get(Tag.ColorMatrix2),
				(SRATIONAL[])	ifd.get(Tag.ForwardMatrix1),
				(SRATIONAL[])	ifd.get(Tag.ForwardMatrix2)
			);

	}

	@Override public final void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		if (
			34892	!= (int) ifd.get(Tag.PhotometricInterpretation)	||
			1		!= (int) ifd.get(Tag.Compression)				||
			1		!= (int) ifd.get(Tag.PlanarConfiguration)
		)
			// FIXME Linear, chunky and uncompressed DNG is the only format currently supported.
			throw new TiffProcessorRuntimeException("Image is not a linear, chunky and uncompressed DNG.");

		bitsPerSample	= (int[])	ifd.get(Tag.BitsPerSample);
		samplesPerPixel	= ((int)	ifd.get(Tag.SamplesPerPixel));
		whiteLevel		= (int[])	ifd.get(Tag.WhiteLevel);

		// TIFF property is LONG, but java arrays have their size defined as, and limited to, int.
		int width	= (int)(long)	ifd.get(Tag.ImageWidth);
		int length	= (int)(long)	ifd.get(Tag.ImageLength);

		image.setImage(new double[width][length][0]);

		int pixelSize = 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample[i]-1)/8;

		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);

		// See TIFF 6.0 Specification, page 39
		for (int i = 0; i < (int) ((length + rowsPerStrip - 1) / rowsPerStrip); i++) {
			short[] strip = ifd.getStripAsShortArray(i);
			for (int j = 0; pixelSize*j < strip.length; j = j + 1)
				image.getImage()[j%width][j/width + i*rowsPerStrip] =
					processConvertedPixel(
						image.fromXYZ(
								Math.multiply(
									cameraToXYZ_D50,
									crop(processRawSensorLevels(readSensorLevels(strip, j*pixelSize, ifd.getByteOrder())))
								)
						)
					);
		}

	}

	/*
	 * We may want to use raw sensor data to recover info otherwise discarded when converting to CIE 1931 XYZ. These methods
	 * provide extension points for subclasses that might want to do this.
	 */
	protected double[] processRawSensorLevels(double[] sensorLevels) { return sensorLevels; }

	protected double[] processConvertedPixel(double[] pixel) { return pixel; }

	/*
	 * Saturation is reached when sensor level exceeds its analog cameraNeutral channel, not its own physical saturation
	 * limit, otherwise it's up to the transformation matrix whether hues will be preserved when sensorLevels > cameraNeutral.
	 */
	private double[] crop(double[] sensorLevels) {
		for (int i = 0; i < sensorLevels.length; i++) if (sensorLevels[i] > cameraNeutral[i]) sensorLevels[i] = cameraNeutral[i];
		return sensorLevels;
	}

	private final double[] readSensorLevels(short[] strip, int offset, ByteOrder byteOrder) {
		double[] sensorLevels = new double[samplesPerPixel];
		for (int i = 0; i < samplesPerPixel; i++) {
			short[] sample;
			// See Digital Negative Specification Version 1.4.0.0, page 18.
			if (bitsPerSample[i] == 8) {
				sample = new short[1];
				System.arraycopy(strip, offset, sample, 0, 1);
			} else if (bitsPerSample[i] <= 16) {
				sample = new short[2];
				System.arraycopy(strip, offset, sample, 0, 2);
			} else if (bitsPerSample[i] <= 32) {
				sample = new short[4];
				System.arraycopy(strip, offset, sample, 0, 4);
			} else {
				throw new TiffProcessorRuntimeException("Invalid bitsPerSample value of " + bitsPerSample[i]);
			}
			sensorLevels[i] = TiffInputStream.toInt(sample, byteOrder)/(double) whiteLevel[i];
			offset += 1 + (bitsPerSample[i]-1)/8;
		}
		return sensorLevels;
	}

}