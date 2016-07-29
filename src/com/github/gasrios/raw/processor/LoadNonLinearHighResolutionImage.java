/*
 * © 2016 Guilherme Rios All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 */

/*
 * TODO assuming Orientation = 1
 * TODO assuming Compression = 1
 * TODO assuming PlanarConfiguration = 1
 * TODO assuming PhotometricInterpretation = 32.803
 * TODO assuming CFALayout = 1
 */

package com.github.gasrios.raw.processor;

import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import com.github.gasrios.raw.data.Illuminant;
import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.Math;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.swing.ImageFrame;
import com.github.gasrios.raw.swing.ImageSRGB;

public class LoadNonLinearHighResolutionImage extends AbstractTiffProcessor {

	private static final Map<Integer, Illuminant> ILLUMINANTS = new HashMap<Integer, Illuminant>();
	static { for (Illuminant illuminant: Illuminant.values()) ILLUMINANTS.put(illuminant.value, illuminant); }

	protected Map<Object, Object> data;

	protected double[][][] image;

	public LoadNonLinearHighResolutionImage() { data = new HashMap<Object, Object>(); }

	@Override public void firstIfd(ImageFileDirectory ifd) {
		data.put(Tag.AsShotNeutral,				ifd.get(Tag.AsShotNeutral));
		data.put(Tag.CalibrationIlluminant1,	ifd.get(Tag.CalibrationIlluminant1));
		data.put(Tag.CalibrationIlluminant2,	ifd.get(Tag.CalibrationIlluminant2));
		data.put(Tag.CameraCalibration1,		ifd.get(Tag.CameraCalibration1));
		data.put(Tag.ForwardMatrix1,			ifd.get(Tag.ForwardMatrix1));
		data.put(Tag.ForwardMatrix2,			ifd.get(Tag.ForwardMatrix2));
	}

	@Override public void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		/*
		 * Image size, active area and crop area. See
		 *
		 * http://www.barrypearson.co.uk/articles/dng/specification.htm#areas
		 *
		 * Orientation is irrelevant here. It only matters when displaying the image (that is, cæteris paribus, two images
		 * with orientation 1 and 8 will not have any of the information below different from each other).
		 *
		 * The ActiveArea array  can be thought of as two bi-dimensional coordinates: [top, left], [bottom, right]. The values
		 * returned for bottom and right are compatible respectively with ImageLength and ImageWidth, so the X coordinate is
		 * associated with length, and Y with width. If one thinks of "width" as the extent from side to side, this may be
		 * counterintuitive, as X is usually the horizontal axis.
		 *
		 * To make matters worse, this offends the convention adopted in both DefaultCropOrigin and DefaultCropSize, which
		 * uses width for X and length for Y.
		 */

		// Image size
		int			width				= (int) (long)		ifd.get(Tag.ImageWidth);
		int			length				= (int) (long)		ifd.get(Tag.ImageLength);

		// Active sensor pixels (All others should be ignored)
		int			activeWMin			= (int) ((long[])	ifd.get(Tag.ActiveArea))[1];
		int			activeLMin			= (int) ((long[])	ifd.get(Tag.ActiveArea))[0];
		int			activeWMax			= (int) ((long[])	ifd.get(Tag.ActiveArea))[3] - activeWMin;
		int			activeLMax			= (int) ((long[])	ifd.get(Tag.ActiveArea))[2] - activeLMin;

		// Valid cropping interval
		int			cropWMin			= ((RATIONAL[])		ifd.get(Tag.DefaultCropOrigin))[0].intValue();
		int			cropLMin			= ((RATIONAL[])		ifd.get(Tag.DefaultCropOrigin))[1].intValue();
		int			cropWMax			= ((RATIONAL[])		ifd.get(Tag.DefaultCropSize))[0].intValue();
		int			cropLMax			= ((RATIONAL[])		ifd.get(Tag.DefaultCropSize))[1].intValue();

		// Pixel description
		int			samplesPerPixel		= (int)				ifd.get(Tag.SamplesPerPixel);
		int[]		bitsPerSample		= (int[])			ifd.get(Tag.BitsPerSample);
		int			pixelSize			= 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample [i] - 1)/8;

		// CFA pattern description
		short[]		planeColor			= (short[])			ifd.get(Tag.CFAPlaneColor);
		short[]		pattern				= (short[])			ifd.get(Tag.CFAPattern);
		int[]		repeatPatternDim	= (int[])			ifd.get(Tag.CFARepeatPatternDim);

		// Black & white levels for linearization
		RATIONAL[]	blackLevel			= (RATIONAL[])		ifd.get(Tag.BlackLevel);
		int[]		blackLevelRepeatDim	= (int[])			ifd.get(Tag.BlackLevelRepeatDim);
		int			whiteLevel			= (int)				ifd.get(Tag.WhiteLevel);

		data.put(Tag.SamplesPerPixel,	samplesPerPixel);
		data.put(Tag.BitsPerSample,		bitsPerSample);
		data.put(ByteOrder.class,		ifd.getByteOrder());

		double minValue = Double.MAX_VALUE, maxValue = Double.MIN_VALUE;

		// See TIFF 6.0 Specification, page 39
		// TODO can be SHORT or LONG
		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);
		double[][][] image = new double[activeWMax][activeLMax][3];
		for (int i = 0; i < (length + rowsPerStrip - 1) / rowsPerStrip; i++) {
			short[] strip = ifd.getStripAsShortArray(i);
			for (int j = 0; j*pixelSize < strip.length; j = j + 1) {

				int w = j%width - activeWMin;
				int l = j/width + i*rowsPerStrip - activeLMin;

				if (w < 0 || w > activeWMax || l < 0 || l > activeLMax) continue;
				// See TIFF/EP, page 26
				// TODO Is the mask being correctly applied?
				double value = readPixel(strip, j*pixelSize)[0];
				minValue = minValue > value? value : minValue;
				maxValue = maxValue < value? value : maxValue;
				image[w][l][planeColor[pattern[(w + activeWMin)%repeatPatternDim[0]*2 + (l + activeLMin)%repeatPatternDim[1]]]] =
					// TODO assuming SamplesPerPixel = 1
					value;
			}
		}

		System.out.println("minValue: " + minValue);
		System.out.println("maxValue: " + maxValue);

		for (int w = 0; w < image.length; w++) for (int l = 0; l < image[w].length; l++) {

			double[] pixel = image[w][l];

			/*
			 * See Digital Negative Specification Version 1.4.0.0, page 77
			 *
			 * Mapping Raw Values to Linear Reference Values
			 *
			 * The section describes DNG's processing model for mapping stored raw sensor values into linear reference values.
			 *
			 * Linear reference values encode zero light as 0.0, and the maximum useful value (limited by either sensor saturation
			 * or analog to digital converter clipping) as 1.0. If SamplesPerPixel is greater than one, each sample plane should
			 * be processed independently.
			 *
			 * The processing model follows these steps:
			 *
			 * • Linearization
			 *
			 * The first step is to process the raw values through the look-up table specified by the LinearizationTable tag, if
			 * any. If the raw value is greater than the size of the table, it is mapped to the last entry of the table.
			 *
			 * TODO ignoring LinearizationTable
			 */
			

			for (int i = 0; i < pixel.length; i++) if (pixel[i] > 0) {

				/*
				 * • Black Subtraction
				 *
				 * The black level for each pixel is then computed and subtracted. The black level for each pixel is the sum of the
				 * black levels specified by the BlackLevel, BlackLevelDeltaH and BlackLevelDeltaV tags.
				 *
				 * TODO ignoring BlackLevelDeltaH and BlackLevelDeltaV
				 * TODO ignoring BlackLevelDeltaH and BlackLevelDeltaV
				 *
				 * BlackLevel = com.github.gasrios.raw.lang.RATIONAL[4] { 524288/256, 524288/256, 524288/256, 524288/256 }
				 *
				 * This tag specifies the zero light (a.k.a. thermal black or black current) encoding level, as a repeating pattern.
				 * The origin of this pattern is the top-left corner of the ActiveArea rectangle. The values are stored in
				 * row-column-sample scan order.
				 *
				 * BlackLevelRepeatDim = java.lang.Integer[2] { 2, 2 }
				 *
				 * This tag specifies repeat pattern size for the BlackLevel tag.
				 */

				pixel[i] -= blackLevel[w%blackLevelRepeatDim[0]*2 + l%blackLevelRepeatDim[1]].doubleValue();

				/*
				 * • Rescaling
				 *
				 * The black subtracted values are then rescaled to map them to a logical 0.0 to 1.0 range. The scale factor is the
				 * inverse of the difference between the value specified in the WhiteLevel tag and the maximum computed black level
				 * for the sample plane.
				 *
				 * WhiteLevel = 15000 (java.lang.Integer)
				 *
				 * 1/whiteLevel-blackLevel[0][0]
				 */

				pixel[i] /= (whiteLevel-blackLevel[0].doubleValue());

			}

			/*
			 * • Clipping
			 *
			 * The rescaled values are then clipped to a 0.0 to 1.0 logical range.
			 */

		}

		// See Digital Negative Specification Version 1.4.0.0, page 79
		double[] cameraNeutral = RATIONAL.asDoubleArray((RATIONAL[]) data.get(Tag.AsShotNeutral));

		/*
		 * Oddly enough, cameraToXYZ_D50 correctly maps AsShotNeutral to D50 white point for any interpolation weighting
		 * factor. This is very good but rather unexpected. A nice collateral effect is we do not need to iterate over weight
		 * to find the right value, just once is enough.
		 * 
		 * For the first step we assume weight = 0.5
		 */
		double[][] cameraToXYZ_D50 = cameraToXYZ_D50(
			1D - Math.normalize(
				1/ILLUMINANTS.get((int) data.get(Tag.CalibrationIlluminant2)).cct,
				1/cct(XYZ2xy(Math.multiply(cameraToXYZ_D50(0.5D, cameraNeutral), cameraNeutral))),
				1/ILLUMINANTS.get((int) data.get(Tag.CalibrationIlluminant1)).cct
			),
			cameraNeutral
		);

		this.image = new double[cropWMax][cropLMax][];
		for (int w = 0; w < cropWMax; w++) for (int l = 0; l < cropLMax; l++)
			this.image[w][l] = Math.multiply(cameraToXYZ_D50, demosaice(image, w + cropWMin, l + cropLMin));

		new ImageFrame(new ImageSRGB(this.image), 1075, 716);

	}

	// TODO implement demosaicing algorithm
	private double[] demosaice(double[][][] image, int w, int l) {
		return image[w][l];
	}

	private double[] readPixel(short[] strip, int offset) {

		int samplesPerPixel	= (int) data.get(Tag.SamplesPerPixel);
		int[] bitsPerSample	= (int[]) data.get(Tag.BitsPerSample);
		ByteOrder byteOrder	= (ByteOrder) data.get(ByteOrder.class);
		double[] pixel		= new double[samplesPerPixel];

		for (int i = 0; i < samplesPerPixel; i++) {
			// TODO Assuming pixel data is unsigned.
			if (bitsPerSample[i] <= 8) {
				short[] sample = new short[1];
				System.arraycopy(strip, offset, sample, 0, 1);
				//pixel[i] = sample[0]/(double) 0xFF;
				pixel[i] = sample[0];
			} else if (bitsPerSample[i] <= 16) {
				short[] sample = new short[2];
				System.arraycopy(strip, offset, sample, 0, 2);
				//pixel[i] = TiffInputStream.toInt(sample, byteOrder)/(double) 0xFFFF;
				pixel[i] = TiffInputStream.toInt(sample, byteOrder);
			} else if (bitsPerSample[i] <= 32) {
				short[] sample = new short[4];
				System.arraycopy(strip, offset, sample, 0, 4);
				//pixel[i] = TiffInputStream.toLong(sample, byteOrder)/(double) 0xFFFFFFFF;
				pixel[i] = TiffInputStream.toLong(sample, byteOrder);
			}
			offset += 1 + (bitsPerSample[i]-1)/8;
		}

		return pixel;

	}

	// See Digital Negative Specification Version 1.4.0.0, Chapter 6: "Mapping Camera Color Space to CIE XYZ Space"
	private double[][] cameraToXYZ_D50(double weight, double[] cameraNeutral) {
		double[][] inverseCC = Math.inverse(Math.vector2Matrix(SRATIONAL.asDoubleArray((SRATIONAL[]) data.get(Tag.CameraCalibration1))));
		return
			Math.multiply(
				Math.multiply(
					Math.weightedAverage(
						Math.vector2Matrix(SRATIONAL.asDoubleArray((SRATIONAL[]) data.get(Tag.ForwardMatrix1))),
						Math.vector2Matrix(SRATIONAL.asDoubleArray((SRATIONAL[]) data.get(Tag.ForwardMatrix2))),
						weight
					),
					Math.inverse(Math.asDiagonalMatrix(Math.multiply(inverseCC, cameraNeutral)))
				),
				inverseCC
			);
	}

	// http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_xyY.html
	private double[] XYZ2xy(double[] XYZ) { return new double[] { XYZ[0]/(XYZ[0]+XYZ[1]+XYZ[2]), XYZ[1]/(XYZ[0]+XYZ[1]+XYZ[2]), XYZ[1] }; }

	// McCamy's cubic approximation (http://en.wikipedia.org/wiki/Color_temperature#Approximation)
	private double cct(double[] chromaticityCoordinates) {
		double n = (chromaticityCoordinates[0] - 0.3320D)/(chromaticityCoordinates[1] - 0.1858D);
		return -449D*java.lang.Math.pow(n, 3D) + 3525D*java.lang.Math.pow(n, 2D) - 6823.3D*n + 5520.33D;
	}

	public static void main(String[] args) throws Exception {
		new TiffProcessorEngine(new FileInputStream(args[0]), new LoadNonLinearHighResolutionImage()).run();
	}

}