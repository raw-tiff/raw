/*
 * © 2016 Guilherme Rios All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 */

/*
 * This class bridges the gap between (1) reading a DNG file and making its information available in an easy to use fashion and
 * (2) writing code that actually processes this information.
 *
 * The first half of the job is done by classes org.yoyo.dng.data.ImageFileDirectoryLoader and org.yoyo.dng.io.DngInputStream,
 * while the second by classes implementing interface org.yoyo.dng.processor.DngProcessor or extending the convenience class
 * org.yoyo.dng.processor.AbstractDngProcessor which provides empty implementations of all methods defined in the former.
 *
 * DngProcessorEngine creates an ImageFileDirectory then sweeps across all of its contents, invoking matching methods in
 * DngProcessor. So by implementing your DngProcessor you can focus on processing the parts of the file you care about
 * while ignoring everything else.
 */

package com.github.gasrios.raw.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.adobe.xmp.XMPException;
import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.ImageFileDirectoryLoader;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.TiffProcessorException;

public final class TiffProcessorEngine {

	private ImageFileDirectory ifd0;
	private TiffProcessor listener;

	public TiffProcessorEngine(InputStream in, TiffProcessor listener) throws TiffProcessorException, IOException, XMPException {
		this(in, listener, true);
	}

	public TiffProcessorEngine(InputStream in, TiffProcessor listener, boolean ignoreUnknownTags) throws TiffProcessorException, IOException, XMPException {
		ifd0 = (new ImageFileDirectoryLoader(new TiffInputStream(in, ignoreUnknownTags))).load();
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	public void run() throws TiffProcessorException {

		// TODO NewSubFileType is found in DNG files. Canon files (.CR2) do not have it.
		if (ifd0.containsKey(Tag.NewSubFileType) && ((long) ifd0.get(Tag.NewSubFileType)) == 1) listener.thumbnailIfd(ifd0);
		listener.firstIfd(ifd0);
		processIfd(ifd0);

		/*
		 * TODO A fully compatible TIFF reader should honor "TIFF Technical Note 1: TIFF Tress" and read sub-IFDs of any depth.
		 *
		 * Having said that, this works for DNG.
		 */
		subIfds(ifd0);

		// IFD chain. So far only seen in Canon's .CR2 files
		ImageFileDirectory currentIfd = ifd0;
		while (currentIfd.getNext() != null) {
			listener.nextIfd(currentIfd.getNext());
			processIfd(currentIfd = currentIfd.getNext());
		}

		if (ifd0.containsKey(Tag.ExifIFD)) exifIfd((ImageFileDirectory) ifd0.get(Tag.ExifIFD));
		if (ifd0.containsKey(Tag.XMP)) xmp((Map<String, String>) ifd0.get(Tag.XMP));

		// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
		ifd0 = null;
		System.gc();
		listener.end();

	}

	private void processIfd(ImageFileDirectory ifd) throws TiffProcessorException {

		if (ifd.containsKey(Tag.NewSubFileType) && ((long) ifd.get(Tag.NewSubFileType)) == 0) listener.highResolutionIfd(ifd);
		if (ifd.containsKey(Tag.NewSubFileType) && ((long) ifd.get(Tag.NewSubFileType)) == 1) listener.previewIfd(ifd);

		listener.ifd(ifd);
		for (Tag tag: ifd.keySet()) if (tag != Tag.SubIFDs && tag != Tag.ExifIFD && tag != Tag.XMP) listener.tag(tag, ifd.get(tag));

	}

	@SuppressWarnings("unchecked")
	private void subIfds(ImageFileDirectory ifd) throws TiffProcessorException {
		if (ifd.containsKey(Tag.SubIFDs) && !((List<ImageFileDirectory>) ifd.get(Tag.SubIFDs)).isEmpty())
			for (ImageFileDirectory subIfd: (List<ImageFileDirectory>) ifd.get(Tag.SubIFDs)) processIfd(subIfd);
	}

	private void exifIfd(ImageFileDirectory exifIfd) throws TiffProcessorException {
		listener.exifIfd(exifIfd);
		listener.ifd(exifIfd);
		for (Tag tag: exifIfd.keySet()) listener.tag(tag, exifIfd.get(tag));
	}

	private void xmp(Map<String, String> xmp) throws TiffProcessorException {
		listener.xmp(xmp);
		for (String tag: xmp.keySet()) listener.xmpTag(tag, xmp.get(tag));
	}

}