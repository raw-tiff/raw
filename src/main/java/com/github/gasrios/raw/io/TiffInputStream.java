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

package com.github.gasrios.raw.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import com.github.gasrios.raw.data.CanonMakerNoteTag;
import com.github.gasrios.raw.data.InteroperabilityTag;
import com.github.gasrios.raw.data.MakerNoteTag;
import com.github.gasrios.raw.data.Tag;
import com.github.gasrios.raw.data.Type;
import com.github.gasrios.raw.lang.RATIONAL;
import com.github.gasrios.raw.lang.SRATIONAL;
import com.github.gasrios.raw.lang.TiffProcessorException;

/*
 * See "TIFF Revision 6.0 Final - June 3, 1992", page 15.
 *
 * See https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
 *
 * Conventions adopted when converting data from TIFF to Java:
 *
 * - ASCII is read as java.lang.String.
 *
 * - SBYTE is read as byte.
 *
 * - SSHORT is read as short.
 *
 * - SLONG is read as int.
 *
 * - SRATIONAL is read as com.github.gasrios.raw.lang.SRATIONAL.
 *
 * - FLOAT is read as float.
 *
 * - DOUBLE is read as double.
 *
 * - BYTE is read as short to preserve sign.
 *
 * - SHORT is read as int to preserve sign.
 *
 * - LONG is read as long to preserve sign.
 *
 * - RATIONAL is read as com.github.gasrios.raw.lang.RATIONAL.
 *
 * - UNDEFINED is read as byte.
 */

public class TiffInputStream extends BufferedInputStream {

	private static final Map<Integer, Tag> TAGS = new HashMap<Integer, Tag>();
	private static final Map<Integer, InteroperabilityTag> INTEROPERABILITY_TAGS = new HashMap<Integer, InteroperabilityTag>();
	private static final Map<Integer, CanonMakerNoteTag> MAKERNOTE_TAGS = new HashMap<Integer, CanonMakerNoteTag>();

	static {
		for (Tag tag: Tag.values()) TAGS.put(tag.number, tag);
		for (InteroperabilityTag tag: InteroperabilityTag.values()) INTEROPERABILITY_TAGS.put(tag.number, tag);
		for (CanonMakerNoteTag tag: CanonMakerNoteTag.values()) MAKERNOTE_TAGS.put(tag.number, tag);
	}

	public static synchronized int toInt(short[] buffer, ByteOrder byteOrder) {
		return byteOrder.equals(ByteOrder.LITTLE_ENDIAN)? (buffer[1] << 8) + buffer[0] : (buffer[0] << 8) + buffer[1];
	}

	public static synchronized long toLong(short[] buffer, ByteOrder byteOrder) {
		return byteOrder.equals(ByteOrder.LITTLE_ENDIAN)?
			(((long) buffer[3]) << 24) + (buffer[2] << 16) + (buffer[1] << 8) + buffer[0]:
			(((long) buffer[0]) << 24) + (buffer[1] << 16) + (buffer[2] << 8) + buffer[3];
	}

	// Used to preserve state when we have to jump from file header to body and back.
	private long mark = 0, currentPosition = 0;

	private ByteOrder byteOrder;

	public ByteOrder getByteOrder() { return byteOrder; }

	public TiffInputStream(InputStream in) throws TiffProcessorException, IOException {

		super(in);

		/*
		 * See http://docs.oracle.com/javase/7/docs/api/java/io/FilterInputStream.html#mark(int)
		 *
		 * We always mark the file beginning before reading or skipping bytes, so we can always move back to the initial
		 * position and move around the file using its absolute offsets.
		 *
		 * Due to the readlimit constraint we cannot read over 2³¹-1 bytes (2GB) of data at once, as it will invalidate the
		 * mark position.
		 */
		super.mark(Integer.MAX_VALUE);

		// See TIFF 6.0 Specification, page 13
		short[] buffer = new short[2];
		if (read(buffer) != 2) throw new EOFException();
		if		(buffer[0] == 0x49 && buffer[1] == 0x49) byteOrder = ByteOrder.LITTLE_ENDIAN;
		else if (buffer[0] == 0x4D && buffer[1] == 0x4D) byteOrder = ByteOrder.BIG_ENDIAN;
		else throw new TiffProcessorException("Invalid endianness specification: " + (char) buffer[0] + (char) buffer[1]);

		short version = readSSHORT();
		if (version < 42) throw new TiffProcessorException("Failed to further identify the file as a TIFF file. Version = " + version);

		long offset = readOffset();
		if (offset < 8) throw new TiffProcessorException("Offset is smaller than header size: " + offset);

		seek(offset);

	}

	/*
	 * See http://docs.oracle.com/javase/6/docs/api/java/io/FilterInputStream.html#skip(long)
	 *
	 * "The skip method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0."
	 *
	 * Found out empirically that trying to skip over 8KiB at once does not work. This implementation fixes this problem.
	 */
	@Override public synchronized long skip(long n) throws IOException {
		long skipped = 0, buffer = n;
		while (skipped < buffer) skipped = super.skip(buffer -= skipped);
		currentPosition += n;
		return n;
	}

	@Override public boolean markSupported() { return true; }

	public synchronized void mark() { mark(0); }

	@Override public synchronized void mark(int readlimit) { mark = currentPosition; }

	@Override public synchronized void reset() throws IOException { seek(mark); }

	// Random access support.
	public synchronized void seek(long offset) throws IOException {

		// Calling reset here takes us back to the beginning of the file.
		super.reset();

		/*
		 * See http://docs.oracle.com/javase/7/docs/api/java/io/FilterInputStream.html#mark(int)
		 *
		 * We always mark the file beginning before reading or skipping bytes, so we can always move back to the initial
		 * position and move around the file using its absolute offsets.
		 *
		 * Due to the readlimit constraint we cannot read over 2³¹-1 bytes (2GB) of data at once, as it will invalidate the
		 * mark position.
		 */
		super.mark(Integer.MAX_VALUE);

		skip(offset);

		currentPosition = offset;

	}

	/*
	 * Derived from http://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html#read(byte[])
	 *
	 * Because the type "byte" is signed in Java but unsigned in TIFF it is simpler to read it as an array of short.
	 *
	 * Reads some number of bytes from the input stream and stores them as shorts into the array buffer. The number of bytes
	 * actually read is returned as an integer. This method blocks until input data is available, end of file is detected, or
	 * an exception is thrown. If the length of b is zero, then no bytes are read and 0 is returned; otherwise, there is an
	 * attempt to read at least one byte. If no byte is available because the stream is at the end of the file, the value -1
	 * is returned; otherwise, at least one byte is read and stored into b.
	 *
	 * The first byte read is stored into element b[0], the next one into b[1], and so on. The number of bytes read is, at
	 * most, equal to the length of b. Let k be the number of bytes actually read; these bytes will be stored in elements
	 * b[0] through b[k-1], leaving elements b[k] through b[b.length-1] unaffected.
	 *
	 * Parameters:
	 *
	 * buffer - the array into which the data is read.
	 *
	 * Returns: the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream
	 * has been reached.
	 */
	public synchronized int read(short[] buffer) throws IOException {

		if (buffer.length == 0) return 0;

		short byteRead = (short) read();
		if (byteRead == -1) return -1;

		buffer[0] = byteRead;

		int numberOfBytesRead = 1;
		while (numberOfBytesRead < buffer.length) {
			if ((byteRead = (short) read()) == -1) break;
			buffer[numberOfBytesRead++] = byteRead;
		}

		currentPosition += numberOfBytesRead;

		return numberOfBytesRead;

	}

	/*
	 * Methods that read numeric types
	 */

	/*
	 * TIFF's SSHORT type, a 2-byte signed integer, can be read as Java's 2-byte short.
	 */
	public synchronized short readSSHORT() throws IOException {
		short[] buffer = new short[2];
		if (read(buffer) != 2) throw new EOFException();
		return (short) (byteOrder.equals(ByteOrder.LITTLE_ENDIAN)? (buffer[1] << 8) + buffer[0] : (buffer[0] << 8) + buffer[1]);
	}

	/*
	 * TIFF's SLONG type, a 4-byte unsigned integer, can be read as Java's 4-byte int but watch out for any naming confusion
	 * here: in Java the signed long type has 8 bytes so TIFF's SLONG maps more correctly Java's int which also has 4 bytes.
	 */
	public synchronized int readSLONG() throws IOException {
		short[] buffer = new short[4];
		if (read(buffer) != 4) throw new EOFException();
		return byteOrder.equals(ByteOrder.LITTLE_ENDIAN)?
			(buffer[3] << 24) + (buffer[2] << 16) + (buffer[1] << 8) + buffer[0]:
			(buffer[0] << 24) + (buffer[1] << 16) + (buffer[2] << 8) + buffer[3];
	}

	public synchronized float readFLOAT() throws IOException {
		byte[] buffer = new byte[4];
		if (read(buffer) != 4) throw new EOFException();
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(byteOrder);
		currentPosition += 4;
		return byteBuffer.getFloat();
	}

	public synchronized double readDOUBLE() throws IOException {
		byte[] buffer = new byte[8];
		if (read(buffer) != 8) throw new EOFException();
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(byteOrder);
		currentPosition += 8;
		return byteBuffer.getDouble();
	}

	/*
	 * TIFF's SHORT type, a 2-byte unsigned integer, must be read as a 4-byte int in order to preserve sign.
	 */
	public synchronized int readSHORT() throws IOException {
		short[] buffer = new short[2];
		if (read(buffer) != 2) throw new EOFException();
		return toInt(buffer, byteOrder);
	}

	/*
	 * TIFF's LONG type, a 4-byte unsigned integer, must be read as a 8-byte long in order to preserve sign.
	 */
	public synchronized long readLONG() throws IOException {
		short[] buffer = new short[4];
		if (read(buffer) != 4) throw new EOFException();
		return toLong(buffer, byteOrder);
	}

	public synchronized RATIONAL readRATIONAL() throws IOException { return new RATIONAL(readLONG(), readLONG()); }

	public synchronized SRATIONAL readSRATIONAL() throws IOException { return new SRATIONAL(readSLONG(), readSLONG()); }

	public synchronized long readOffset() throws IOException, TiffProcessorException {
		long offset = readLONG();
		if (offset % 2 != 0) throw new TiffProcessorException("Offset is not even: " + offset);
		return offset;
	}

	/*
	 * Methods that read arrays of data
	 */

	/*
	 * TIFF's BYTE type, a 1-byte unsigned integer, must be read as a 2-byte short in order to preserve sign.
	 */
	public synchronized short[] readBYTE(int length) throws TiffProcessorException, IOException {
		short[] buffer = new short[length];
		if (read(buffer) != length) throw new EOFException();
		return buffer;
	}

	public synchronized byte[] readSBYTE(int length) throws TiffProcessorException, IOException {
		byte[] buffer = new byte[length];
		if (read(buffer) != length) throw new EOFException();
		currentPosition+=length;
		return buffer;
	}

	public synchronized String readASCII(int length) throws TiffProcessorException, IOException {
		byte[] buffer = new byte[length - 1];
		if (read(buffer) != length - 1) throw new EOFException();
		if (read() != 0) throw new TiffProcessorException("Non null terminated string");
		currentPosition+=length;
		return newString(buffer);
	}

	// Trim null padded ASCII sequences. Can happen in proprietary tags.
	private String newString(byte[] buffer) {
		int size = 0;
		for (int i = 0; i < buffer.length; i++) if (buffer[i] > 0) size++; else break;
		byte[] newBuffer = new byte[size];
		System.arraycopy(buffer, 0, newBuffer, 0, size);
		return new String(newBuffer);
	}

	/*
	 * Methods that read metadata
	 */

	public synchronized Tag readTag() throws TiffProcessorException, IOException {
		int tag = readSHORT();
		if (TAGS.containsKey(tag)) return TAGS.get(tag);
		else return new Tag(""+tag, tag);
	}

	public synchronized InteroperabilityTag readInteroperabilityTag() throws TiffProcessorException, IOException {
		int tag = readSHORT();
		if (INTEROPERABILITY_TAGS.containsKey(tag)) return INTEROPERABILITY_TAGS.get(tag);
		else return new InteroperabilityTag(""+tag, tag);
	}

	public synchronized MakerNoteTag readMakerNoteTag() throws TiffProcessorException, IOException {
		int tag = readSHORT();
		if (MAKERNOTE_TAGS.containsKey(tag)) return MAKERNOTE_TAGS.get(tag);
		else return new CanonMakerNoteTag(""+tag, tag);
	}

	public synchronized Type readType() throws TiffProcessorException, IOException {
		int type = readSHORT();
		if (type < 1) throw new TiffProcessorException("Invalid value for type field: " + type);
		return type > 12? Type.UNEXPECTED : Type.values()[type];
	}

}