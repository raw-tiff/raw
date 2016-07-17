/*
 * See org.yoyo.dng.processor.DngProcessor. Provides empty implementations of the methods you do not care about, so you can only
 * focus on the stuff you need.
 */

package com.github.gasrios.raw.processor;

import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.lang.DngProcessorException;

public abstract class AbstractDngProcessor implements DngProcessor {

	@Override public void ifd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void thumbnailIfd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void firstIfd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void highResolutionIfd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void previewIfd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void exifIfd(ImageFileDirectory ifd) throws DngProcessorException {}

	@Override public void tag(Tag tag, Object value) throws DngProcessorException {}

	@Override public void xmp(Map<String, String> xmp) throws DngProcessorException {}

	@Override public void xmpTag(String tag, String value) throws DngProcessorException {}

	@Override public void end() throws DngProcessorException {}

}