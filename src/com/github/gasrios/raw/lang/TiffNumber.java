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

package com.github.gasrios.raw.lang;

/*
 * See "TIFF Revision 6.0 Final - June 3, 1992", page 15.
 *
 * Two TIFF numeric types do not map on any of Java's primitive numeric types:
 *
 * - RATIONAL: two 4-byte unsigned integers, numerator and denominator of a fraction.
 * - SRATIONAL: two 4-byte signed (twos-complement) integers, numerator and denominator of a fraction.
 *
 * This happens because Java has no fixed point arithmetic. Preserving RATIONAL and SRATIONAL values for as long as we can helps
 * with precision issues that conversions to double might introduce. For this reason we create this class and its two subclasses,
 * com.github.gasrios.raw.lang.RATIONAL and com.github.gasrios.raw.lang.SRATIONAL.
 */

public abstract class TiffNumber extends Number implements Comparable<TiffNumber> {

	private static final long serialVersionUID = -8554025807316285334L;

	@Override public int hashCode() { return Double.valueOf(doubleValue()).hashCode(); }

	@Override public int compareTo(TiffNumber other) { return Double.compare(doubleValue(), other.doubleValue()); }

}