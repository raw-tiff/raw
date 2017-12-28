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

package com.github.gasrios.raw.lgm2017;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.gasrios.raw.formats.ImageSRGB;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.DngProcessor;
import com.github.gasrios.raw.processor.TiffProcessorEngine;
import com.github.gasrios.raw.swing.Image;
import com.github.gasrios.raw.swing.ImageFrame;

/*
 * Compare to B&W correct brightness increase.
 */

public class SRGB extends DngProcessor<ImageSRGB> {

	String fileName;

	public SRGB(ImageSRGB image, String fileName) {
		super(image);
		this.fileName = fileName;
	}

	@Override public void end() throws TiffProcessorException {

		//DisplayableImage displayableImage = new DisplayableImage(image);
		Image displayableImage = new Image(increaseBrightness(blackAndWhite((ImageSRGB) image)));

		// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
		image = null;
		System.gc();

		new ImageFrame("sRGB", displayableImage, 1440, 900);

		try { ImageIO.write(displayableImage, "PNG", new File(fileName+".png")); }
		catch (IOException e) { throw new TiffProcessorException(e); }

	}

	private ImageSRGB blackAndWhite(ImageSRGB image) {

		double[][][] im = image.getImage();

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			double average = 0D;
			for (int k = 0; k < 3; k++) average += im[i][j][k];
			average /= 3;
			for (int k = 0; k < 3; k++) im[i][j][k] = average;
		};

		return image;

	}

	// Only works for B&W images.
	private ImageSRGB increaseBrightness(ImageSRGB image) {

		double[][][] im = image.getImage();
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			if (min > im[i][j][0]) min = im[i][j][0];
			if (max < im[i][j][0]) max = im[i][j][0];
		}

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			double b = (im[i][j][0]-min)/(max-min);
			for (int k = 0; k < 3; k ++) im[i][j][k] = b;
		}

		return image;

	}

	public static void main(String[] args) throws Exception { new TiffProcessorEngine(new FileInputStream(args[0]), new SRGB(new ImageSRGB(), args[0])).run(); }

}