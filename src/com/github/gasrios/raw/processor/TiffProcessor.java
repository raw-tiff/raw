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

package com.github.gasrios.raw.processor;

import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.lang.TiffProcessorException;

/*
 * This interface provides consumer methods called by com.github.gasrios.raw.processor.TiffProcessorEngine when content is available and
 * ready to be processed. By implementing this interface you can manipulate TIFF images as you see fit.
 *
 * Class com.github.gasrios.raw.processor.AbstractTiffProcessor provides empty implementations of methods you do not care about for
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
 * Same applies to the high resolution and EXIF IFDs. We also provide "end()" methods called after processing an IFD.
 *
 * Calling order obeys the logic "more generic last", although this is a rather loose definition. In the case above we assume
 * "processPreviewIfd" to be more generic than "processFirstIfd" because a file may have several previews but only one first
 * IFD. On the other hand "processFirstIfd" is considered more generic than "processThumbnailIfd" because a thumbnail is also
 * a first IFD but the contrary is not true.
 *
 * Calling order of "end()" methods follows the opposite rule "more generic first" to provide proper nesting.
 */

public interface TiffProcessor {

	// Generic callback called when any IFD is available.
	void ifd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called for the first IFD if it has NewSubFileType = 1.
	void thumbnailIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	/*
	 * First IFD callback. Needed because some generic information (tags "UniqueCameraModel" and "LensInfo" for example) and
	 * information needed to process the high resolution raw image (calibration illuminant and color matrixes, among many
	 * others) are stored here, even if the image itself is inside another IFD.
	 */
	void firstIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called if this IFD has NewSubFileType = 0.
	void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called if this IFD has NewSubFileType = 1.
	void previewIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called for an Exif IFD if it exists.
	void exifIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called for an Interoperability IFD if it exists.
	void interoperabilityIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called for an Interoperability IFD if it exists.
	void makerNoteIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called if an IFD chain exists.
	void nextIfd(ImageFileDirectory ifd) throws TiffProcessorException;

	// Called for all tags except XMP (see below).
	void tag(Tag tag, Object value) throws TiffProcessorException;

	// Called after parsing XMP content using Adobe's com.adobe.xmp.XMPMetaFactory and populating a java.util.Map.
	void xmp(Map<String, String> xmp) throws TiffProcessorException;

	// Called individually for each key in the Map above.
	void xmpTag(String tag, String value) throws TiffProcessorException;

	// Called after all other methods
	void end() throws TiffProcessorException;

}