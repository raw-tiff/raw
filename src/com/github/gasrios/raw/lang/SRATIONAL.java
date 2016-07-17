package com.github.gasrios.raw.lang;

public class SRATIONAL extends DngNumber {

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