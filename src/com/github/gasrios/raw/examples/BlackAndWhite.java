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

import com.github.gasrios.raw.formats.*;
import com.github.gasrios.raw.lang.ImageEditing;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.LinearChunkyUncompressedDNG;
import com.github.gasrios.raw.processor.TiffProcessorEngine;
import com.github.gasrios.raw.swing.DisplayableImage;
import com.github.gasrios.raw.swing.ImageFrame;

/*
 * Turn image to black and white, preserving perceived luminance.
 */

public class BlackAndWhite extends LinearChunkyUncompressedDNG {

	public BlackAndWhite(ImageCIEXYZ image) { super(image); }

	public static void main(String[] args) throws Exception {
		//new TiffProcessorEngine(new FileInputStream(args[0]), new BlackAndWhite(new ImageCIEXYZ())).run();
		//new TiffProcessorEngine(new FileInputStream(args[0]), new BlackAndWhite(new ImageCIELUV())).run();
		//new TiffProcessorEngine(new FileInputStream(args[0]), new BlackAndWhite(new ImageCIELCH())).run();
		new TiffProcessorEngine(new FileInputStream(args[0]), new BlackAndWhite(new ImageLSH())).run();
		//new TiffProcessorEngine(new FileInputStream(args[0]), new BlackAndWhite(new ImageSRGB())).run();
	}

	@Override public void end() throws TiffProcessorException {

		DisplayableImage displayableImage = new DisplayableImage(ImageEditing.blackAndWhite(image));

		// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
		image = null;
		System.gc();

		new ImageFrame(displayableImage, 1075, 716);

	}

}