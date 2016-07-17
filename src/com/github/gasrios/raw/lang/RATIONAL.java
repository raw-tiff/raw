package com.github.gasrios.raw.lang;

public class RATIONAL extends DngNumber {

	private static final long serialVersionUID = -2832867839922457347L;

	public static double[] asDoubleArray(RATIONAL[] array) {
		double[] buffer = null;
		if (array != null) {
			buffer = new double[array.length];
			for (int i = 0; i < array.length; i++) buffer[i] = array[i].doubleValue();
		}
		return buffer;
	}

	private long numerator, denominator;

	public RATIONAL(long numerator, long denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override public double doubleValue() { return ((double) numerator)/denominator; }

	@Override public float floatValue() { return ((float) numerator)/denominator; }

	@Override public int intValue() { return (int) (numerator/denominator); }

	@Override public long longValue() { return numerator/denominator; }

	@Override public String toString() { return numerator + "/" + denominator; }

	@Override public boolean equals(Object object) {
		return
			object instanceof RATIONAL &&
			numerator == ((RATIONAL) object).numerator &&
			denominator == ((RATIONAL) object).denominator;
	}

}