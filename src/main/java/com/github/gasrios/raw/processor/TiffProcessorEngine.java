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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.github.gasrios.raw.data.ImageFileDirectory;
import com.github.gasrios.raw.data.ImageFileDirectoryLoader;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.xmp.XMPException;

/*
 * This class bridges the gap between (1) reading a TIFF file and making its information available in an easy to use fashion
 * and (2) writing code that actually processes this information.
 *
 * The first half of the job is done by classes ImageFileDirectoryLoader and TiffInputStream, while the second by classes
 * implementing interface TiffProcessor or extending the convenience class AbstractTiffProcessor, which provides empty
 * implementations of all methods defined in the former.
 *
 * TiffProcessorEngine creates an ImageFileDirectory then sweeps across all of its contents, invoking matching methods in
 * TiffProcessor. So by implementing your TiffProcessor you can focus on processing the parts of the file you care about
 * while ignoring everything else.
 */

public final class TiffProcessorEngine {

	private ImageFileDirectory ifd;
	private TiffProcessor listener;

	public TiffProcessorEngine(InputStream in, TiffProcessor listener) throws TiffProcessorException, IOException, XMPException {
		ifd = (new ImageFileDirectoryLoader(new TiffInputStream(in))).load();
		this.listener = listener;
	}

	public void run() throws TiffProcessorException {

		if (ifd.containsKey(Tag.NewSubFileType) && ((long) ifd.get(Tag.NewSubFileType)) == 1) listener.thumbnailIfd(ifd);
		listener.firstIfd(ifd);
		ifdChain(ifd);

		// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
		ifd = null;
		System.gc();
		listener.end();

	}

	private void ifdChain(ImageFileDirectory ifd) throws TiffProcessorException {
		ifd(ifd);
		ImageFileDirectory nextIfd = ifd;
		while ((nextIfd = nextIfd.getNext()) != null) {
			listener.nextIfd(nextIfd);
			ifd(nextIfd);
		}
	}

	private void ifd(ImageFileDirectory ifd) throws TiffProcessorException {
		if (ifd.containsKey(Tag.NewSubFileType))
			if (((long) ifd.get(Tag.NewSubFileType)) == 0) listener.highResolutionIfd(ifd);
			else if (((long) ifd.get(Tag.NewSubFileType)) == 1) listener.previewIfd(ifd);
		tags(ifd);
	}

	@SuppressWarnings("unchecked")
	private void tags(ImageFileDirectory ifd) throws TiffProcessorException {

		listener.ifd(ifd);

		for (Tag tag: ifd.keySet())
			if (tag != Tag.SubIFDs && tag != Tag.ExifIFD && tag != Tag.XMP && tag != Tag.Interoperability && tag != Tag.MakerNote)
				listener.tag(tag, ifd.get(tag));

		if (ifd.containsKey(Tag.SubIFDs) && !((List<ImageFileDirectory>) ifd.get(Tag.SubIFDs)).isEmpty())
			ifds((List<ImageFileDirectory>) ifd.get(Tag.SubIFDs));

		if (ifd.containsKey(Tag.ExifIFD)) exif((ImageFileDirectory) ifd.get(Tag.ExifIFD));

		if (ifd.containsKey(Tag.XMP)) xmp((Map<String, String>) ifd.get(Tag.XMP));

		if (ifd.containsKey(Tag.Interoperability)) interoperability((ImageFileDirectory) ifd.get(Tag.Interoperability));

		if (ifd.containsKey(Tag.MakerNote)) makerNote((ImageFileDirectory) ifd.get(Tag.MakerNote));

	}

	private void ifds(List<ImageFileDirectory> ifds) throws TiffProcessorException {
		for (ImageFileDirectory ifd: ifds) ifdChain(ifd);
	}

	private void exif(ImageFileDirectory ifd) throws TiffProcessorException {
		listener.exifIfd(ifd);
		ifdChain(ifd);
	}

	private void interoperability(ImageFileDirectory ifd) throws TiffProcessorException {
		listener.interoperabilityIfd(ifd);
		ifdChain(ifd);
	}

	private void makerNote(ImageFileDirectory ifd) throws TiffProcessorException {
		listener.makerNoteIfd(ifd);
		ifdChain(ifd);
	}

	private void xmp(Map<String, String> xmp) throws TiffProcessorException {
		listener.xmp(xmp);
		for (String tag: xmp.keySet()) listener.xmpTag(tag, xmp.get(tag));
	}

}