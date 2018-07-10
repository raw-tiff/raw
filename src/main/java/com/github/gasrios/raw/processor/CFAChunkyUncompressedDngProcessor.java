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
import com.github.gasrios.raw.lang.RATIONAL;
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

public class CFAChunkyUncompressedDngProcessor extends AbstractTiffProcessor {

	protected	double[][][]	image;

	private		int[]			bitsPerSample;
	private		int				samplesPerPixel;
	private		int[]			whiteLevel;

	@Override public final void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		bitsPerSample	= (int[])	ifd.get(Tag.BitsPerSample);
		samplesPerPixel	= ((int)	ifd.get(Tag.SamplesPerPixel));
		whiteLevel		= (int[])	ifd.get(Tag.WhiteLevel);

		if (
			32803	!= (int) ifd.get(Tag.PhotometricInterpretation)	||
			1		!= (int) ifd.get(Tag.Compression)				||
			1		!= (int) ifd.get(Tag.PlanarConfiguration)
		)
			throw new TiffProcessorRuntimeException("Image is not a CFA, chunky and uncompressed DNG.");

		/*
		 * See http://www.barrypearson.co.uk/articles/dng/specification.htm#areas
		 */

		/*
		 * ImageWidth, ImageLength: sensor area, in pixels.
		 *
		 * TIFF property is LONG, but java arrays have their size defined as, and limited to, int.
		 */
		int			width				= (int)(long)		ifd.get(Tag.ImageWidth);
		int			length				= (int)(long)		ifd.get(Tag.ImageLength);

		/*
		 * ActiveArea: largest area from which a useful image can be formed. This array can be thought of as two bi-dimensional
		 * coordinates: [top, left], [bottom, right]. The values returned for bottom and right are compatible respectively
		 * with ImageLength and ImageWidth, so the X coordinate is associated with length, and Y with width. If one thinks of
		 * "width" as the extent from side to side, this may be counterintuitive, as X is usually the horizontal axis. To make
		 * matters worse, this offends the convention adopted in both DefaultCropOrigin and DefaultCropSize, which uses width
		 * for X and length for Y.
		 */
		int			activeWMin			= (int) ((long[])	ifd.get(Tag.ActiveArea))[1];
		int			activeLMin			= (int) ((long[])	ifd.get(Tag.ActiveArea))[0];
		int			activeWMax			= (int) ((long[])	ifd.get(Tag.ActiveArea))[3] - activeWMin;
		int			activeLMax			= (int) ((long[])	ifd.get(Tag.ActiveArea))[2] - activeLMin;

		/*
		 * DefaultCropOrigin, DefaultCropSize: the subset of the Active Area which many raw converters convert into a useful
		 * image. The main reason why C is smaller than A is to provide some extra pixels all around for a raw converter's
		 * demosaicing algorithm to use.
		 */
		int			cropWMin			= ((RATIONAL[])		ifd.get(Tag.DefaultCropOrigin))[0].intValue();
		int			cropLMin			= ((RATIONAL[])		ifd.get(Tag.DefaultCropOrigin))[1].intValue();
		int			cropWMax			= ((RATIONAL[])		ifd.get(Tag.DefaultCropSize))[0].intValue();
		int			cropLMax			= ((RATIONAL[])		ifd.get(Tag.DefaultCropSize))[1].intValue();

		// CFA pattern description
		short[]		planeColor			= (short[])			ifd.get(Tag.CFAPlaneColor);
		short[]		pattern				= (short[])			ifd.get(Tag.CFAPattern);
		int[]		repeatPatternDim	= (int[])			ifd.get(Tag.CFARepeatPatternDim);

		// Black & white levels
		RATIONAL[]	blackLevel			= (RATIONAL[])		ifd.get(Tag.BlackLevel);
		int[]		blackLevelRepeatDim	= (int[])			ifd.get(Tag.BlackLevelRepeatDim);

		image = new double[activeWMax][activeLMax][3];

		System.out.println("Width: " + width);

		System.out.println("\nLength: " + length);

		System.out.print("\nActive area: [ ");

		System.out.print(activeWMin + " ");
		System.out.print(activeLMin + " ");
		System.out.print(activeWMax + " ");
		System.out.print(activeLMax);

		System.out.print(" ]\n\nCrop area: [ ");

		System.out.print(cropWMin + " ");
		System.out.print(cropLMin + " ");
		System.out.print(cropWMax + " ");
		System.out.print(cropLMax);

		System.out.println(" ]\n\nCFA Pattern");

		for (int i = 0; i < repeatPatternDim[0]; i++) {
			System.out.print("\t");
			for (int j = 0; j < repeatPatternDim[1]; j++) System.out.print(planeColor[pattern[(i + activeWMin)%repeatPatternDim[0]*2 + (j + activeLMin)%repeatPatternDim[1]]]);
			System.out.println();
		}

		System.out.println("\nBlack levels");

		for (int i = 0; i < blackLevelRepeatDim[0]; i++) {
			System.out.print("\t");
			for (int j = 0; j < blackLevelRepeatDim[1]; j++) System.out.print(blackLevel[i%blackLevelRepeatDim[0]*2 + j%blackLevelRepeatDim[1]].doubleValue() + "\t");
			System.out.println();
		}

		System.out.print("\nWhite level: [ ");

		for (int i = 0; i < whiteLevel.length; i++) System.out.print(whiteLevel[i] + " ");

		System.out.println("]\n\nSamples per pixel: " + samplesPerPixel);

		System.out.print("\nBits per sample: [ ");

		for (int i = 0; i < bitsPerSample.length; i++) System.out.print(bitsPerSample[i] + " ");

		System.out.println("]\n");

		int pixelSize = 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample[i]-1)/8;

		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);

		double[] minLevels = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE };
		double[] maxLevels = new double[] { Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE };

		// See TIFF 6.0 Specification, page 39
		for (int i = 0; i < (length + rowsPerStrip - 1) / rowsPerStrip; i++) {

			short[] strip = ifd.getStripAsShortArray(i);

			for (int j = 0; j*pixelSize < strip.length; j = j + 1) {

				int w = j%width - activeWMin;
				int l = j/width + i*rowsPerStrip - activeLMin;

				if (w < 0 || w > activeWMax || l < 0 || l > activeLMax) continue;

				short channel = planeColor[pattern[(w + activeWMin)%repeatPatternDim[0]*2 + (l + activeLMin)%repeatPatternDim[1]]];
				double level = readSensorLevel(strip, j*pixelSize, ifd.getByteOrder());

				// See Digital Negative Specification Version 1.4.0.0, page 27: "The origin of this pattern is the top-left corner of the ActiveArea rectangle"
				int black = blackLevel[w%blackLevelRepeatDim[0]*2 + l%blackLevelRepeatDim[1]].intValue();

				/*
				 * The black level for each pixel is then computed and subtracted. The black level for each pixel is the sum
				 * of the black levels specified by the BlackLevel, BlackLevelDeltaH and BlackLevelDeltaV tags.
				 *
				 * The black subtracted values are then rescaled to map them to a logical 0.0 to 1.0 range. The scale factor
				 * is the inverse of the difference between the value specified in the WhiteLevel tag and the maximum computed
				 * black level for the sample plane.
				 */
				level = (level-black)/(whiteLevel[0]-black);
				level = level < 0D? 0D : level > 1D? 1D : level;

				if (minLevels[channel] > level) minLevels[channel] = level;
				if (maxLevels[channel] < level) maxLevels[channel] = level;

				if (Math.random() < .0000005d) {
					System.out.print("(");
					System.out.print(w);
					System.out.print(", ");
					System.out.print(l);
					System.out.print(", ");
					System.out.print(channel);
					System.out.print(") = ");
					System.out.println(level);
				}

				// See TIFF/EP, page 26
				image[w][l][channel] = level;

			}

		}

		for (int i = 0; i < minLevels.length; i++) System.out.print(minLevels[i] + " ");

		for (int i = 0; i < maxLevels.length; i++) System.out.print(maxLevels[i] + " ");

	}

	// TODO assuming SamplesPerPixel = 1
	private final int readSensorLevel(short[] strip, int offset, ByteOrder byteOrder) {
		short[] sample;
		// See Digital Negative Specification Version 1.4.0.0, page 18.
		if (bitsPerSample[0] == 8) {
			sample = new short[1];
			System.arraycopy(strip, offset, sample, 0, 1);
		} else if (bitsPerSample[0] <= 16) {
			sample = new short[2];
			System.arraycopy(strip, offset, sample, 0, 2);
		} else if (bitsPerSample[0] <= 32) {
			sample = new short[4];
			System.arraycopy(strip, offset, sample, 0, 4);
		} else {
			throw new TiffProcessorRuntimeException("Invalid bitsPerSample value of " + bitsPerSample[0]);
		}
		return TiffInputStream.toInt(sample, byteOrder);
	}

}