package com.github.gasrios.raw;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.processor.AbstractDngProcessor;
import com.github.gasrios.raw.processor.DngProcessorEngine;

public class ReadMetadata extends AbstractDngProcessor {

	private static final boolean IGNORE_UNKNOWN_TAGS = true;

	public static void main(String[] args) throws Exception {
		(new DngProcessorEngine(new FileInputStream(args[0]), new ReadMetadata(), IGNORE_UNKNOWN_TAGS)).run();
	}

	@Override public void ifd(ImageFileDirectory ifd) { System.out.println(); }

	@Override public void firstIfd(ImageFileDirectory ifd) { System.out.println("First"); }

	@Override public void highResolutionIfd(ImageFileDirectory ifd) { System.out.println("\nHigh Resolution"); }

	@Override public void exifIfd(ImageFileDirectory ifd) { System.out.println("\nExif"); }

	@Override public void xmp(Map<String, String> xmp) { System.out.println("\nXMP\n"); }

	@Override public void tag(Tag tag, Object value) {
		if (!value.getClass().isArray()) System.out.println(tag + " = " + value + " (" + value.getClass().getName() + ")");
		else {
			System.out.print(tag + " = " + Array.get(value, 0).getClass().getName() + "[" + Array.getLength(value)  + "] { ");
			System.out.print(Array.get(value, 0));
			for (int i = 1; i < (Array.getLength(value) > 10? 10 : Array.getLength(value)); i++) System.out.print(", " + Array.get(value, i));
			if (Array.getLength(value) > 10) System.out.print("...");
			System.out.println(" }");
		}
	}

	@Override public void xmpTag(String tag, String value) { System.out.println(tag + " = " + value); }

}