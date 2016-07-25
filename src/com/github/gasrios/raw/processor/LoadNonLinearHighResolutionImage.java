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

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.swing.ImageFrame;
import com.github.gasrios.raw.swing.ImageSRGB;

public class LoadNonLinearHighResolutionImage extends AbstractTiffProcessor {

	protected Map<Object, Object> data;

	protected double[][][] image;

	public LoadNonLinearHighResolutionImage() { data = new HashMap<Object, Object>(); }

	@Override public void firstIfd(ImageFileDirectory ifd) {}

	/*
	 * Read active area and determine crop area. All coordinates are [top, left, bottom, right], following the convention
	 * stablished by ActiveArea. Orientation does not affect this and is only relevant when displaying the image (that
	 * is, cæteris paribus, two images with orientation 1 and 8 will not have any of the information below different from
	 * each other).
	 *
	 * ActiveArea can be thought of as two bi-dimensional coordinates: [top, left], [bottom, right]. The values
	 * returned for bottom and right are compatible respectively with ImageLength and ImageWidth, so the X coordinate
	 * is associated with length, and Y with width. If one thinks of "width" as the extent from side to side, this may
	 * be counterintuitive, as X is usually the horizontal axis.
	 *
	 * To make matters worse, this offends the convention adopted in both DefaultCropOrigin and DefaultCropSize, which
	 * uses width for X and length for Y.
	 */
	@Override public void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		int width   = (int) (long) ifd.get(Tag.ImageWidth);
		int length  = (int) (long) ifd.get(Tag.ImageLength);

		System.out.println("Width: " + width);
		System.out.println("Length: " + length);

		long[] activeArea = (long[]) ifd.get(Tag.ActiveArea);
		for (int i = 0; i < activeArea.length; i++) activeArea[i]--;

		System.out.print("Active area: ");
		for (int i = 0; i < activeArea.length; i++) System.out.print(activeArea[i] + ", ");
		System.out.println();

		RATIONAL[] defaultCropOrigin = (RATIONAL[]) ifd.get(Tag.DefaultCropOrigin);
		RATIONAL[] defaultCropSize = (RATIONAL[]) ifd.get(Tag.DefaultCropSize);

		long[] cropArea = new long[] {
			activeArea[0] + defaultCropOrigin[1].longValue(),
			activeArea[1] + defaultCropOrigin[0].longValue(),
			activeArea[0] + defaultCropOrigin[1].longValue() + defaultCropSize[1].longValue(),
			activeArea[1] + defaultCropOrigin[0].longValue() + defaultCropSize[0].longValue()
		};

		System.out.print("Crop area: ");
		for (int i = 0; i < cropArea.length; i++) System.out.print(cropArea[i] + ", ");
		System.out.println();

		/*
		 * CFA pattern
		 *	RG
		 *	GB
		 */

		short[]	planeColor			= (short[])	ifd.get(Tag.CFAPlaneColor);
		short[]	cfaPattern			= (short[])	ifd.get(Tag.CFAPattern);
		int[]	repeatPatternDim	= (int[])	ifd.get(Tag.CFARepeatPatternDim);
		int[][]	pattern				= new int[repeatPatternDim[0]][repeatPatternDim[1]];

		for (int i = 0; i < pattern.length; i++)  for (int j = 0; j < pattern[i].length; j++)
			pattern[i][j] = planeColor[cfaPattern[2*i+j]];

		System.out.println("Pattern");
		for (int i = 0; i < pattern.length; i++) {
			System.out.print("\t");
			for (int j = 0; j < pattern[i].length; j++)
				System.out.print(pattern[i%pattern.length][j%pattern[i%pattern.length].length]);
			System.out.println();
		}

		/*
		 * Start here
		 */

		int		samplesPerPixel	= (int)		ifd.get(Tag.SamplesPerPixel);
		int[]	bitsPerSample	= (int[])	ifd.get(Tag.BitsPerSample);

		int pixelSize = 0;
		for (int i = 0; i < samplesPerPixel; i++) pixelSize += 1 + (bitsPerSample [i]-1)/8;

		System.out.println("Pixel size: " + pixelSize);

		// TODO size image to active area. read only useful pixels in it.
		image = new double[(int) (1+activeArea[3]-activeArea[1])][(int) (1+activeArea[2]-activeArea[0])][];

		System.out.println("Image: 0, 0, " + (image[0].length) + ", " + (image.length));

		// See TIFF 6.0 Specification, page 39
		int minW = width, minL = length, maxW = 0, maxL = 0;
		int rowsPerStrip = (int) (long) ifd.get(Tag.RowsPerStrip);
		for (int i = 0; i < (int) ((length + rowsPerStrip - 1) / rowsPerStrip); i++) {
			short[] strip = ifd.getStripAsShortArray(i);
			for (int j = 0; pixelSize*j < strip.length; j = j + 1) {
				int w = j%width - (int) activeArea[1];
				int l = j/width+i*rowsPerStrip - (int) activeArea[0];
				// Active area: 51, 142, 3516, 5344 [top, left, bottom, right] [minL, minW, maxL, maxW]
				if (l < 0 || w < 0 || l > activeArea[2] || w > activeArea[3]) continue;
				image[w][l] = new double[3];
				minL = minL>l? l: minL;
				minW = minW>w? w: minW;
				maxL = maxL<l? l: maxL;
				maxW = maxW<w? w: maxW;
				image[w][l][pattern[w%pattern.length][l%pattern[w%pattern.length].length]] =
					// TODO assuming SamplesPerPixel = 1
					readPixel(
						strip,
						j*pixelSize,
						samplesPerPixel,
						bitsPerSample,
						ifd.getByteOrder()
					)[0];
			}
		}

		System.out.println("Min length: " + minL);
		System.out.println("Min width: " + minW);
		System.out.println("Max length: " + maxL);
		System.out.println("Max width: " + maxW);

		new ImageFrame(new ImageSRGB(image), 1075, 716);

	}

	private double[] readPixel(short[] strip, int offset, int samplesPerPixel, int[] bitsPerSample, ByteOrder byteOrder) {
		double[] pixel = new double[samplesPerPixel];
		for (int i = 0; i < samplesPerPixel; i++) {
			// TODO Assuming pixel data is unsigned.
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

	public static void main(String[] args) throws Exception {
		new TiffProcessorEngine(new FileInputStream(args[0]), new LoadNonLinearHighResolutionImage()).run();
	}

}