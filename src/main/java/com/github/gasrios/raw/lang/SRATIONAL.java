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

public class SRATIONAL extends TiffNumber {

	private static final long serialVersionUID = 1021237428351807022L;

	public static double[] asDoubleArray(SRATIONAL[] array) {
		double[] buffer = null;
		if (array != null) {
			buffer = new double[array.length];
			for (int i = 0; i < array.length; i++) buffer[i] = array[i].doubleValue();
		}
		return buffer;
	}

	private int numerator, denominator;

	public SRATIONAL(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override public double doubleValue() { return ((double) numerator)/denominator; }

	@Override public float floatValue() { return ((float) numerator)/denominator; }

	@Override public int intValue() { return numerator/denominator; }

	@Override public long longValue() { return numerator/denominator; }

	@Override public String toString() { return numerator + "/" + denominator; }

	@Override public boolean equals(Object object) {
		return
			object instanceof SRATIONAL &&
			numerator == ((SRATIONAL) object).numerator &&
			denominator == ((SRATIONAL) object).denominator;
	}

}