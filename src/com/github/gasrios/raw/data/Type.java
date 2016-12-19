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

//See "TIFF Revision 6.0 Final - June 3, 1992", page 15.

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