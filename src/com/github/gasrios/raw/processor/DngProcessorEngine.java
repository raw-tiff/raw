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
import com.github.gasrios.raw.io.DngInputStream;
import com.github.gasrios.raw.lang.DngProcessorException;

public final class DngProcessorEngine {

	private ImageFileDirectory ifd0;
	private DngProcessor listener;

	public DngProcessorEngine(InputStream in, DngProcessor listener) throws DngProcessorException, IOException, XMPException {
		this(in, listener, true);
	}

	public DngProcessorEngine(InputStream in, DngProcessor listener, boolean ignoreUnknownTags) throws DngProcessorException, IOException, XMPException {
		ifd0 = (new ImageFileDirectoryLoader(new DngInputStream(in, ignoreUnknownTags))).load();
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	public void run() throws DngProcessorException {

		if (((long) ifd0.get(Tag.NewSubFileType)) == 1) listener.thumbnailIfd(ifd0);
		listener.firstIfd(ifd0);
		processIfd(ifd0);

		/*
		 * TODO A fully compatible TIFF reader should honor "TIFF Technical Note 1: TIFF Tress" and read sub-IFDs of any depth.
		 *
		 * Having said that, this works for DNG.
		 */
		subIfds(ifd0);

		if (ifd0.get(Tag.ExifIFD) != null) exifIfd((ImageFileDirectory) ifd0.get(Tag.ExifIFD));
		if (ifd0.get(Tag.XMP) != null) xmp((Map<String, String>) ifd0.get(Tag.XMP));

		// Does not seem to make much of a difference in practice, but just in case let's try and free some memory here.
		ifd0 = null;
		System.gc();
		listener.end();

	}

	private void processIfd(ImageFileDirectory ifd) throws DngProcessorException {

		if (ifd.get(Tag.NewSubFileType) != null && ((long) ifd.get(Tag.NewSubFileType)) == 0) listener.highResolutionIfd(ifd);
		if (ifd.get(Tag.NewSubFileType) != null && ((long) ifd.get(Tag.NewSubFileType)) == 1) listener.previewIfd(ifd);

		listener.ifd(ifd);
		for (Tag tag: ifd.keySet()) if (tag != Tag.SubIFDs && tag != Tag.ExifIFD && tag != Tag.XMP) listener.tag(tag, ifd.get(tag));

	}

	@SuppressWarnings("unchecked")
	private void subIfds(ImageFileDirectory ifd) throws DngProcessorException {
		if (ifd.get(Tag.SubIFDs) != null && !((List<ImageFileDirectory>) ifd.get(Tag.SubIFDs)).isEmpty())
			for (ImageFileDirectory subIfd: (List<ImageFileDirectory>) ifd.get(Tag.SubIFDs)) processIfd(subIfd);
	}

	private void exifIfd(ImageFileDirectory exifIfd) throws DngProcessorException {
		listener.exifIfd(exifIfd);
		listener.ifd(exifIfd);
		for (Tag tag: exifIfd.keySet()) listener.tag(tag, exifIfd.get(tag));
	}

	private void xmp(Map<String, String> xmp) throws DngProcessorException {
		listener.xmp(xmp);
		for (String tag: xmp.keySet()) listener.xmpTag(tag, xmp.get(tag));
	}

}