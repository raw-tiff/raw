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
import java.io.IOException;

import com.github.gasrios.raw.Library;
import com.github.gasrios.raw.formats.ImageCIEXYZ;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.DngProcessor;
import com.github.gasrios.raw.processor.TiffProcessorEngine;

public class SaveImage extends DngProcessor<ImageCIEXYZ> {

	String name;

	public SaveImage(ImageCIEXYZ image, String name) {
		super(image);
		this.name = name;
	}

	@Override public void end() throws TiffProcessorException {
		try {
			Library.save(image, name, "PNG");
		} catch (IOException e) {
			throw new TiffProcessorException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		new TiffProcessorEngine(new FileInputStream(args[0]), new SaveImage(new ImageCIEXYZ(), args[0])).run();
	}

}