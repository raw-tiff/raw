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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.gasrios.raw.formats.ImageLSH;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.LinearChunkyUncompressedDngProcessor;
import com.github.gasrios.raw.processor.TiffProcessorEngine;
import com.github.gasrios.raw.swing.DisplayableImage;

public class SaveImage extends LinearChunkyUncompressedDngProcessor {

	String fileName;

	public SaveImage(ImageLSH image, String fileName) {
		super(image);
		this.fileName = fileName;
	}

	@Override public void end() throws TiffProcessorException {

		try {

			ImageIO.write(new DisplayableImage(image), "PNG", new File(fileName+".png"));

		} catch (IOException e) { throw new TiffProcessorException(e); }

	}

	public static void main(String[] args) throws Exception { new TiffProcessorEngine(new FileInputStream(args[0]), new SaveImage(new ImageLSH(), args[0])).run(); }

}