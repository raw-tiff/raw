/*
 * This interface provides consumer methods called by org.yoyo.dng.processor.DngProcessorEngine when content is available and
 * ready to be processed. By implementing this interface you can manipulate DNG images as you see fit.
 *
 * Class org.yoyo.dng.processor.AbstractDngProcessor provides empty implementations of methods you do not care about for
 * convenience.
 *
 * Sometimes different methods may be called for the same IFD; for example if the first IFD has a thumbnail - which it should
 * - the following methods will all be called, in this order:
 *
 * - processThumbnailIfd()
 * - processFirstIfd()
 * - processPreviewIfd()
 * - processIfd()
 *
 *  Same applies to the high resolution and EXIF IFDs. We also provide "end()" methods called after processing an IFD.
 *
 * Calling order obeys the logic "more generic last", although this is a rather loose definition. In the case above we assume
 * "processPreviewIfd" to be more generic than "processFirstIfd" because a file may have several previews but only one first
 * IFD. On the other hand "processFirstIfd" is considered more generic than  "processThumbnailIfd" because a thumbnail is also
 * a first IFD but the contrary is not true.
 *
 * Calling order of "end()" methods follow the opposite rule "more generic first" to provide proper nesting.
 */

package com.github.gasrios.raw.processor;

import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.lang.DngProcessorException;

public interface DngProcessor {

	// Generic callback called when any IFD is available.
	void ifd(ImageFileDirectory ifd) throws DngProcessorException;

	// Called for the first IFD if it has NewSubFileType = 1.
	void thumbnailIfd(ImageFileDirectory ifd) throws DngProcessorException;

	/*
	 * First IFD callback. Needed because some generic information (tags "UniqueCameraModel" and "LensInfo" for example) and
	 * information needed to process the high resolution raw image (calibration illuminant and color matrixes, among many
	 * others) are stored here, even if the image itself is inside another IFD.
	 */
	void firstIfd(ImageFileDirectory ifd) throws DngProcessorException;

	// Called if this IFD has NewSubFileType = 0.
	void highResolutionIfd(ImageFileDirectory ifd) throws DngProcessorException;

	// Called if this IFD has NewSubFileType = 1.
	void previewIfd(ImageFileDirectory ifd) throws DngProcessorException;

	// Called for an Exif IFD if it exists.
	void exifIfd(ImageFileDirectory ifd) throws DngProcessorException;

	// Called for all tags except XMP (see below).
	void tag(Tag tag, Object value) throws DngProcessorException;

	// Called after parsing XMP content using Adobe's com.adobe.xmp.XMPMetaFactory and populating a java.util.Map.
	void xmp(Map<String, String> xmp) throws DngProcessorException;

	// Called individually for each key in the Map above.
	void xmpTag(String tag, String value) throws DngProcessorException;

	// Called after all other methods
	void end() throws DngProcessorException;

}