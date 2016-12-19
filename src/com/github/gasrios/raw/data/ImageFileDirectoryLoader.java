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

public final class ImageFileDirectoryLoader {

	private TiffInputStream in;
	private ImageFileDirectory ifd;

	public ImageFileDirectoryLoader(TiffInputStream in) {
		this.in = in;
		ifd = new ImageFileDirectory(in);
	}

	public ImageFileDirectory load() throws TiffProcessorException, IOException, FileNotFoundException, XMPException {
		ifd(ifd, Context.Main);
		return ifd;
	}

	private enum Context { Main, Interoperability, MakerNote }

	@SuppressWarnings("unchecked")
	private long ifd(ImageFileDirectory ifd, Context context) throws IOException, TiffProcessorException {

		int entriescount = in.readSHORT();

		switch (context) {
			case Main:
				for (int i = 0; i < entriescount; i++) ifdEntry(ifd, in.readTag());
			break;
			case Interoperability:
				for (int i = 0; i < entriescount; i++) ifdEntry(ifd, in.readInteroperabilityTag());
			break;
			case MakerNote:
				for (int i = 0; i < entriescount; i++) ifdEntry(ifd, in.readMakerNoteTag());
			break;
		}

		long nextOffset = in.readOffset();

		// Replace offsets with IFDs and load XMP data

		if (ifd.containsKey(Tag.SubIFDs)) {
			List<ImageFileDirectory> subIfds = new Vector<ImageFileDirectory>();
			if (ifd.get(Tag.SubIFDs) instanceof List)
				for (long subIfdOffset : ((List<Long>) ifd.get(Tag.SubIFDs))) subIfds.add(ifd(subIfdOffset, Context.Main));
			else subIfds.add(ifd((long) ifd.get(Tag.SubIFDs), Context.Main));
			ifd.put(Tag.SubIFDs, subIfds);
		}

		if (ifd.containsKey(Tag.ExifIFD)) {
			ImageFileDirectory exifIfd = ifd((long) ifd.get(Tag.ExifIFD), Context.Main);
			exifIfd.put(Tag.ExifVersion, new String((byte[]) exifIfd.get(Tag.ExifVersion)));
			if (exifIfd.containsKey(Tag.FlashPixVersion))
				exifIfd.put(Tag.FlashPixVersion, new String((byte[]) exifIfd.get(Tag.FlashPixVersion)));
			ifd.put(Tag.ExifIFD, exifIfd);
		}

		if (ifd.containsKey(Tag.Interoperability)) {
			ImageFileDirectory interoperabilityIFD = ifd((long) ifd.get(Tag.Interoperability), Context.Interoperability);
			interoperabilityIFD.put(
				InteroperabilityTag.InteroperabilityVersion,
				new String((byte[]) interoperabilityIFD.get(InteroperabilityTag.InteroperabilityVersion)));
			ifd.put(Tag.Interoperability, interoperabilityIFD);
		}

		if (ifd.containsKey(Tag.MakerNote)) {
			ImageFileDirectory makerNoteIFD = ifd((long) ifd.get(Tag.MakerNote), Context.MakerNote);
			ifd.put(Tag.MakerNote, makerNoteIFD);
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
			nextOffset = ifd(currentIfd = currentIfd.getNext(), Context.Main);
		}

		return nextOffset;

	}

	private ImageFileDirectory ifd(long offset, Context context) throws IOException, TiffProcessorException {
		in.seek(offset);
		ImageFileDirectory ifd = new ImageFileDirectory(in);
		ifd(ifd, context);
		return ifd;
	}

	private void ifdEntry(ImageFileDirectory ifd, Tag tag) throws TiffProcessorException, IOException {

		/*
		 * See Exif Version 2.3, page 46
		 *
		 * TODO works for Canon, must test other makers.
		 *
		 * Type is UNDEFINED, count the IFD size in bytes. We just ignore them and read the entry as we would an offset.
		 */
		if (tag.equals(Tag.MakerNote)) {
			in.skip(6);
			ifd.put(tag, ifdEntryValue(Type.LONG, 1));
			return;
		}

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

		// It is easier to force BitsPerSample and WhiteLevel to arrays, so we do not need to treat special cases.
		if (tag.equals(Tag.BitsPerSample) || tag.equals(Tag.WhiteLevel)) {
			if (type.size * count > 4) {
				long offset = in.readOffset();
				in.mark();
				in.seek(offset);
				ifd.put(tag, ifdEntryMultipleNumericValues(type, (int) count));
				in.reset();
			} else {
				ifd.put(tag, ifdEntryMultipleNumericValues(type, (int) count));
				in.skip(4 - type.size * count);
			}
			return;
		}

		/*
		 * See TIFF 6.0 Specification, page 15
		 *
		 * "Value Offset contains the Value instead of pointing to the Value if and only if the Value fits into 4 bytes."
		 */
		if (type.size * count > 4) {
			long offset = in.readOffset();
			in.mark();
			in.seek(offset);
			ifd.put(tag, ifdEntryValue(type, (int) count));
			in.reset();
		} else {
			ifd.put(tag, ifdEntryValue(type, (int) count));
			in.skip(4 - type.size * count);
		}

	}

	private Object ifdEntryValue(Type type, int count) throws TiffProcessorException, IOException {
		switch (type) {
			case ASCII    : return in.readASCII(count);
			case BYTE     : return in.readBYTE(count);
			case SBYTE    : return in.readSBYTE(count);
			case UNDEFINED: return in.readSBYTE(count);
			default       : return count == 1?
								ifdEntrySingleNumericValue(type):
								ifdEntryMultipleNumericValues(type, count);
		}
	}

	private Object ifdEntrySingleNumericValue(Type type) throws IOException {
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

	private Object ifdEntryMultipleNumericValues(Type type, int count) throws IOException {
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