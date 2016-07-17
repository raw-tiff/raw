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
 * org.yoyo.dng.lang.math.RATIONAL and org.yoyo.dng.lang.math.SRATIONAL.
 */

package com.github.gasrios.raw.lang;

public abstract class DngNumber extends Number implements Comparable<DngNumber> {

	private static final long serialVersionUID = -8554025807316285334L;

	@Override public int hashCode() { return Double.valueOf(doubleValue()).hashCode(); }

	@Override public int compareTo(DngNumber other) { return Double.compare(doubleValue(), other.doubleValue()); }

}