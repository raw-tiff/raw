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
 * Straightforward transformations are applied here, when we can assume they are sensible:
 *
 * - Offsets are replaced by real values;
 * - XMP content is processed and properties embedded in XML are extracted.
 *
 * Complex transformations and those that change the structure of the original info, for example processing of CFA tags to
 * extract a pattern, do not happen here.
 *
 * Image processing also is assumed to happen elsewhere.
 */

package com.github.gasrios.raw.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.github.gasrios.raw.io.DngInputStream;
import com.github.gasrios.raw.lang.DngProcessorException;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;

public final class ImageFileDirectoryLoader {

	private DngInputStream in;
	private ImageFileDirectory ifd;

	public ImageFileDirectoryLoader(DngInputStream in) {
		this.in = in;
		ifd = new ImageFileDirectory(in);
	}

	@SuppressWarnings("unchecked")
	public ImageFileDirectory load() throws DngProcessorException, IOException, FileNotFoundException, XMPException {

		processIfd(ifd);

		// Second pass: replace offsets to subIFDs with them and load XMP data

		/*
		 * TODO A fully compatible TIFF reader should honor "TIFF Technical Note 1: TIFF Tress" and read sub-IFDs of any depth.
		 *
		 * Having said that, this works for DNG.
		 */
		if (ifd.containsKey(Tag.SubIFDs)) {
			List<ImageFileDirectory> subIfds = new Vector<ImageFileDirectory>();
			// FIXME has to be wrong. I never load Vectors, only arrays.
			if (ifd.get(Tag.SubIFDs) instanceof List)
				for (long offset : ((List<Long>) ifd.get(Tag.SubIFDs))) subIfds.add(processIfd(offset));
			else
				subIfds.add(processIfd((long) ifd.get(Tag.SubIFDs)));
			ifd.put(Tag.SubIFDs, subIfds);
		}

		if (ifd.containsKey(Tag.ExifIFD)) {
			in.seek((long) ifd.get(Tag.ExifIFD));
			ImageFileDirectory exifIFD = new ImageFileDirectory(in);
			processIfd(exifIFD);
			exifIFD.put(Tag.ExifVersion, new String((byte[]) exifIFD.get(Tag.ExifVersion)));
			ifd.put(Tag.ExifIFD, exifIFD);
		}

		if (ifd.containsKey(Tag.XMP)) {
			short[] xmp = (short[]) ifd.get(Tag.XMP);
			byte[] buffer = new byte[xmp.length];
			for (int i = 0; i < buffer.length; i++) buffer[i] = (byte) xmp[i];
			XMPMeta xmpMeta = XMPMetaFactory.parseFromBuffer(buffer);
			Map<String, String> xmpData = new TreeMap<String, String>();
			for (XMPPropertyInfo xmpPropertyInfo: xmpMeta)
				if (xmpPropertyInfo.getPath() != null && !"".equals(xmpPropertyInfo.getValue()))
					xmpData.put(xmpPropertyInfo.getPath(), xmpPropertyInfo.getValue());
			ifd.put(Tag.XMP, xmpData);
		}

		return ifd;

	}

	private ImageFileDirectory processIfd(long offset) throws IOException, DngProcessorException {
		in.seek(offset);
		ImageFileDirectory subIfd = new ImageFileDirectory(in);
		processIfd(subIfd);
		return subIfd;
	}

	// See TIFF 6.0 Specification, page 14
	private void processIfd(ImageFileDirectory ifd) throws IOException, DngProcessorException {

		int entriescount = in.readSHORT();
		for (int i = 0; i < entriescount; i++) processIfdEntry(ifd);

		// From Digital Negative Specification Version 1.4.0.0, page 13
		if (in.readOffset() != 0) throw new DngProcessorException("SubIFD chains are not supported.");

	}

	private void processIfdEntry(ImageFileDirectory ifd) throws DngProcessorException, IOException {

		// See TIFF 6.0 Specification, page 14

		Tag tag = in.readTag();
		if (tag.equals(Tag.Unknown)) {
			in.skip(10);
			return;
		}

		// See TIFF 6.0 Specification, page 16
		Type type = in.readType();
		if (type.equals(Type.UNEXPECTED)) {
			in.skip(8);
			return;
		}

		long count = in.readLONG();
		if (count > 0xFFFFFFFFL) throw new DngProcessorException("java arrays do not support lengths out of the positive integer range: " + count);

		if (type.size * count > 4) {
			long offset = in.readOffset();
			in.mark();
			in.seek(offset);
			ifd.put(tag, processIfdEntryValue(type, (int) count));
			in.reset();
		} else {
			ifd.put(tag, processIfdEntryValue(type, (int) count));
			in.skip(4 - type.size * count);
		}

	}

	private Object processIfdEntryValue(Type type, int count) throws DngProcessorException, IOException {
		switch (type) {
			case ASCII    : return in.readASCII(count);
			case BYTE     : return in.readBYTE(count);
			case SBYTE    : return in.readSBYTE(count);
			case UNDEFINED: return in.readSBYTE(count);
			default       : return count == 1?
								processIfdEntrySingleNumericValue(type):
								processIfdEntryMultipleNumericValues(type, count);
		}
	}

	private Object processIfdEntrySingleNumericValue(Type type) throws IOException {
		switch (type) {
			case SHORT    : return in.readSHORT();
			case LONG     : return in.readLONG();
			case RATIONAL : return in.readRATIONAL();
			case SSHORT   : return in.readSSHORT();
			case SLONG    : return in.readSLONG();
			case SRATIONAL: return in.readSRATIONAL();
			case FLOAT    : return in.readFLOAT();
			case DOUBLE   : return in.readDOUBLE();
			default       : return null;
		}
	}

	private Object processIfdEntryMultipleNumericValues(Type type, int count) throws IOException {
		switch (type) {
			case SHORT:
				int[] shorts = new int[count];
				for (int i = 0; i < count; i++) shorts[i] = in.readSHORT();
				return shorts;
			case LONG:
				long[] longs = new long[count];
				for (int i = 0; i < count; i++) longs[i] = in.readLONG();
				return longs;
			case RATIONAL:
				RATIONAL[] rationals = new RATIONAL[count];
				for (int i = 0; i < count; i++) rationals[i] = in.readRATIONAL();
				return rationals;
			case SSHORT:
				short[] sshorts = new short[count];
				for (int i = 0; i < count; i++) sshorts[i] = in.readSSHORT();
				return sshorts;
			case SLONG:
				int[] ints = new int[count];
				for (int i = 0; i < count; i++) ints[i] = in.readSLONG();
				return ints;
			case SRATIONAL:
				SRATIONAL[] srationals = new SRATIONAL[count];
				for (int i = 0; i < count; i++) srationals[i] = in.readSRATIONAL();
				return srationals;
			case FLOAT:
				float[] floats = new float[count];
				for (int i = 0; i < count; i++) floats[i] = in.readFLOAT();
				return floats;
			case DOUBLE:
				double[] doubles = new double[count];
				for (int i = 0; i < count; i++) doubles[i] = in.readDOUBLE();
				return doubles;
			default:
				return null;
		}
	}

}