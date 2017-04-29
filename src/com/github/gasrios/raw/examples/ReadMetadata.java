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
import java.lang.reflect.Array;
import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.processor.AbstractTiffProcessor;
import com.github.gasrios.raw.processor.TiffProcessorEngine;

/*
 * Read & display metadata from TIFF file.
 */

public class ReadMetadata extends AbstractTiffProcessor {

	@Override public void ifd(ImageFileDirectory ifd) { System.out.println(); }

	@Override public void firstIfd(ImageFileDirectory ifd) { System.out.println("First"); }

	@Override public void highResolutionIfd(ImageFileDirectory ifd) { System.out.println("\nHigh Resolution"); }

	@Override public void exifIfd(ImageFileDirectory ifd) { System.out.println("\nExif"); }

	@Override public void interoperabilityIfd(ImageFileDirectory ifd) { System.out.println("\nInteroperability"); }

	@Override public void makerNoteIfd(ImageFileDirectory ifd) { System.out.println("\nMakerNote"); }

	@Override public void nextIfd(ImageFileDirectory ifd) { System.out.println("\nNext"); }

	@Override public void xmp(Map<String, String> xmp) { System.out.println("\nXMP\n"); }

	@Override public void tag(Tag tag, Object value) {
		if (!value.getClass().isArray()) System.out.println(tag + " = " + value + " (" + value.getClass().getName() + ")");
		else {
			System.out.print(tag + " = " + Array.get(value, 0).getClass().getName() + "[" + Array.getLength(value) + "] { ");
			System.out.print(Array.get(value, 0));
			for (int i = 1; i < (Array.getLength(value) > 10? 10 : Array.getLength(value)); i++) System.out.print(", " + Array.get(value, i));
			if (Array.getLength(value) > 10) System.out.print("…");
			System.out.println(" }");
		}
	}

	@Override public void xmpTag(String tag, String value) { System.out.println(tag + " = " + value); }

	public static void main(String[] args) throws Exception { (new TiffProcessorEngine(new FileInputStream(args[0]), new ReadMetadata())).run(); }

}