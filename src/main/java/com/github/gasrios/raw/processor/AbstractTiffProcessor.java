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
 * See com.github.gasrios.raw.processor.TiffProcessor. Provides empty implementations of the methods you do not care about, so you can only
 * focus on the stuff you need.
 */

public abstract class AbstractTiffProcessor implements TiffProcessor {

	@Override public void ifd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void thumbnailIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void firstIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void highResolutionIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void previewIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void exifIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void interoperabilityIfd(ImageFileDirectory ifd) {}

	@Override public void makerNoteIfd(ImageFileDirectory ifd) {}

	@Override public void nextIfd(ImageFileDirectory ifd) throws TiffProcessorException {}

	@Override public void tag(Tag tag, Object value) throws TiffProcessorException {}

	@Override public void xmp(Map<String, String> xmp) throws TiffProcessorException {}

	@Override public void xmpTag(String tag, String value) throws TiffProcessorException {}

	@Override public void end() throws TiffProcessorException {}

}