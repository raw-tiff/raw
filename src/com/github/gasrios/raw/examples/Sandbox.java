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

package com.github.gasrios.raw.examples;

import java.io.FileInputStream;

import com.github.gasrios.raw.formats.ImageCIEXYZ;
import com.github.gasrios.raw.formats.ImageLSH;
import com.github.gasrios.raw.formats.ImageSRGB;
import com.github.gasrios.raw.lang.ImageEditing;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.LinearChunkyUncompressedDNG;
import com.github.gasrios.raw.processor.TiffProcessorEngine;
import com.github.gasrios.raw.swing.DisplayableImage;
import com.github.gasrios.raw.swing.ImageFrame;

/*
 * Increase image saturation (makes it more colorful).
 */

public class Sandbox extends LinearChunkyUncompressedDNG {

	public Sandbox(ImageCIEXYZ image) { super(image); }

	public static void main(String[] args) throws Exception {

		// Original image
		/*new TiffProcessorEngine(new FileInputStream(args[0]), new Sandbox(new ImageCIEXYZ()) {
			
			@Override public void end() throws TiffProcessorException {

				long start = System.currentTimeMillis();
				DisplayableImage displayableImage = new DisplayableImage(image);
				System.out.println(System.currentTimeMillis() - start);

				// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
				image = null;
				System.gc();

				new ImageFrame(displayableImage, 1075, 716);

			}

		}).run();*/

		// Increase brightness, LSH style
		new TiffProcessorEngine(new FileInputStream(args[0]), new Sandbox(new ImageLSH()) {
			
			@Override public void end() throws TiffProcessorException {

				long start = System.currentTimeMillis();
				DisplayableImage displayableImage = new DisplayableImage(ImageEditing.increaseBrightness((ImageLSH) image));
				System.out.println(System.currentTimeMillis() - start);

				// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
				image = null;
				System.gc();

				new ImageFrame("Increase brightness, LSH style", displayableImage, 1075, 716);

			}

		}).run();

		// B&W, LSH
		new TiffProcessorEngine(new FileInputStream(args[0]), new Sandbox(new ImageLSH()) {
			
			@Override public void end() throws TiffProcessorException {

				long start = System.currentTimeMillis();
				DisplayableImage displayableImage =
					new DisplayableImage(ImageEditing.increaseBrightness(ImageEditing.blackAndWhite((ImageLSH) image)));
				System.out.println(System.currentTimeMillis() - start);

				// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
				image = null;
				System.gc();

				new ImageFrame("B&W, LSH", displayableImage, 1075, 716);

			}

		}).run();

		// B&W, sRGB
		new TiffProcessorEngine(new FileInputStream(args[0]), new Sandbox(new ImageSRGB()) {
			
			@Override public void end() throws TiffProcessorException {

				long start = System.currentTimeMillis();
				image = ImageEditing.increaseBrightness(ImageEditing.blackAndWhite((ImageSRGB) image));
				double[][][] im = image.getImage();
				double
					min = Double.MAX_VALUE,
					max = Double.MIN_VALUE;

				for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
					if (min > im[i][j][0]) min = im[i][j][0];
					if (max < im[i][j][0]) max = im[i][j][0];
				}

				for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
					double b = (im[i][j][0]-min)/(max-min);
					for (int k = 0; k < 3; k ++) im[i][j][k] = b;
				}

				DisplayableImage displayableImage = new DisplayableImage(image);
				System.out.println(System.currentTimeMillis() - start);

				// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
				image = null;
				System.gc();

				new ImageFrame("B&W, sRGB", displayableImage, 1075, 716);

			}

		}).run();

	}

}