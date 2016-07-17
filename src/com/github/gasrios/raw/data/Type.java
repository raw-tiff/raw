// See "TIFF Revision 6.0 Final - June 3, 1992", page 15.

package com.github.gasrios.raw.data;

public enum Type {

	UNEXPECTED(-1),
	BYTE(1),
	ASCII(1),
	SHORT(2),
	LONG(4),
	RATIONAL(8),
	SBYTE(1),
	UNDEFINED(1),
	SSHORT(2),
	SLONG(4),
	SRATIONAL(8),
	FLOAT(4),
	DOUBLE(8);

	public final int size;

	Type(int size) { this.size = size; }

}