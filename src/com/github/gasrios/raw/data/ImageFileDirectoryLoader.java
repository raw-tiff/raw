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
import com.github.gasrios.raw.io.TiffInputStream;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;

public final class ImageFileDirectoryLoader {

	private TiffInputStream in;
	private ImageFileDirectory ifd;

	public ImageFileDirectoryLoader(TiffInputStream in) {
		this.in = in;
		ifd = new ImageFileDirectory(in);
	}

	public ImageFileDirectory load() throws TiffProcessorException, IOException, FileNotFoundException, XMPException {

		long nextOffset = processIfd(ifd);

		// Second pass: replace offsets to subIFDs with them and load XMP data

		if (ifd.containsKey(Tag.ExifIFD)) {

			ImageFileDirectory exifIfd = processIfd((long) ifd.get(Tag.ExifIFD));
			exifIfd.put(Tag.ExifVersion, new String((byte[]) exifIfd.get(Tag.ExifVersion)));
			if (exifIfd.containsKey(Tag.FlashPixVersion)) exifIfd.put(Tag.FlashPixVersion, new String((byte[]) exifIfd.get(Tag.FlashPixVersion)));
			ifd.put(Tag.ExifIFD, exifIfd);

			if (exifIfd.containsKey(Tag.Interoperability)) {
				ImageFileDirectory interoperabilityIFD = processInteroperabilityIfd((long) exifIfd.get(Tag.Interoperability));
				interoperabilityIFD.put(
					InteroperabilityTag.InteroperabilityVersion,
					new String((byte[]) interoperabilityIFD.get(InteroperabilityTag.InteroperabilityVersion)));
				exifIfd.put(Tag.Interoperability, interoperabilityIFD);
			}

			/*
			 * See Exif 2.3 Specification, page 46
			 *
			 * TODO MakerNote = java.lang.Byte[44926] { 39, 0, 1, 0, 3, 0, 49, 0, 0, 0... }
			 * TODO UserComment = java.lang.Byte[264] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0... }
			 */

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

		/*
		 * TODO From Digital Negative Specification Version 1.4.0.0, page 13: IFD chains are not supported.
		 *
		 * We should have a "strict" mode that throws an exception for DNG, if offset != 0.
		 */
		ImageFileDirectory currentIfd = ifd;
		while (nextOffset != 0) {
			in.seek(nextOffset);
			currentIfd.setNext(new ImageFileDirectory(in));
			nextOffset = processIfd(currentIfd = currentIfd.getNext());
		}

		return ifd;

	}

	@SuppressWarnings("unchecked")
	private long processIfd(ImageFileDirectory ifd) throws IOException, TiffProcessorException {

		int entriescount = in.readSHORT();

		for (int i = 0; i < entriescount; i++) {
			Tag tag = in.readTag();
			if (tag.equals(Tag.Unknown)) {
				in.skip(10);
			} else processIfdEntry(ifd, tag);
		}

		long offset = in.readOffset();

		if (ifd.containsKey(Tag.SubIFDs)) {
			List<ImageFileDirectory> subIfds = new Vector<ImageFileDirectory>();
			if (ifd.get(Tag.SubIFDs) instanceof List)
				for (long subIfdOffset : ((List<Long>) ifd.get(Tag.SubIFDs))) subIfds.add(processIfd(subIfdOffset));
			else subIfds.add(processIfd((long) ifd.get(Tag.SubIFDs)));
			ifd.put(Tag.SubIFDs, subIfds);
		}

		return offset;

	}

	private ImageFileDirectory processIfd(long offset) throws IOException, TiffProcessorException {
		in.seek(offset);
		ImageFileDirectory ifd = new ImageFileDirectory(in);
		processIfd(ifd);
		return ifd;
	}

	private ImageFileDirectory processInteroperabilityIfd(long offset) throws IOException, TiffProcessorException {
		in.seek(offset);
		ImageFileDirectory ifd = new ImageFileDirectory(in);

		int entriescount = in.readSHORT();
		for (int i = 0; i < entriescount; i++) {
			InteroperabilityTag tag = in.readInteroperabilityTag();
			if (tag.equals(Tag.Unknown)) {
				in.skip(10);
			} else processIfdEntry(ifd, tag);
		}

		return ifd;
	}

	private void processIfdEntry(ImageFileDirectory ifd, Tag tag) throws TiffProcessorException, IOException {

		/*
		 * See TIFF 6.0 Specification, page 16
		 *
		 * "Readers should skip over fields containing an unexpected field type."
		 */
		Type type = in.readType();
		if (type.equals(Type.UNEXPECTED)) {
			in.skip(8);
			return;
		}

		long count = in.readLONG();
		if (count > 0xFFFFFFFFL) throw new TiffProcessorException("java arrays do not support lengths out of the positive integer range: " + count);

		/*
		 * See TIFF 6.0 Specification, page 15
		 *
		 * "Value Offset contains the Value instead of pointing to the Value if and only if the Value fits into 4 bytes."
		 */
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

	private Object processIfdEntryValue(Type type, int count) throws TiffProcessorException, IOException {
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