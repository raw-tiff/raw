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

package com.github.gasrios.raw.lang;

public final class Math {

	public static double normalize(double b, double m, double t) { return m < b? b : m > t? t : (m-b)/(t-b); }

	public static double[] multiply(double[][] m, double[] v) {
		double[] m2 = new double[m.length];
		for (int i = 0; i < m2.length; i++) for (int j = 0; j < v.length; j++) m2[i] += m[i][j]* v[j];
		return m2;
	}

	public static double[][] multiply(double[][] m1, double[][] m2) {
		double[][] m3 = new double[m1.length][m2[0].length];
		for (int i = 0; i < m3.length; i++) for (int j = 0; j < m3[0].length; j++) for (int k = 0; k < m2.length; k++)
			m3[i][j] += m1[i][k]* m2[k][j];
		return m3;
	}

	public static double[][] weightedAverage(double[][] matrix1, double[][] matrix2, double weight) {
		double[][] buffer = new double[matrix1.length][matrix1[0].length];
		weight = weight < 0? 0 : weight > 1? 1 : weight;
		for (int i = 0; i < matrix1.length; i++) for (int j = 0; j < matrix1[0].length; j++)
			buffer[i][j] = matrix1[i][j]*(1-weight) + matrix2[i][j]*weight;
		return buffer;
	}

	public static double[][] asDiagonalMatrix(double[] vector) {
		double[][] buffer = new double[vector.length][vector.length];
		for (int i = 0; i < vector.length; i++) buffer[i][i] = vector[i];
		return buffer;
	}

	// TODO samplesPerPixel != 3
	public static double[][] vector2Matrix(double[] vector) {
		double[][] buffer = null;
		if (vector != null) {
			buffer = new double[3][3];
			for (int i = 0; i < 9; i++) buffer[i/3][i%3] = vector[i];
		}
		return buffer;
	}

	public static double[][] inverse(double[][] m) {
		double[][] buffer = new double[m.length][m[0].length];
		double determinant = determinant(m);
		for (int i = 0; i < m.length; i++) for (int j = 0; j < m.length; j++) buffer[i][j] = cofactor(j, i, m)/determinant;
		return buffer;
	}

	private static double determinant(double[][] m) {
		if (m.length == 1) return m[0][0];
		double d = 0.0D;
		for (int i = 0; i < m.length; i++) d += m[i][0]*cofactor(i, 0, m);
		return d;
	}

	private static double cofactor(int i, int j, double[][] m) {
		return determinant(submatrix(i, j, m))*java.lang.Math.pow(-1, i+j);
	}

	private static double[][] submatrix(int row, int column, double[][] m) {
		double[][] buffer = new double[m.length-1][m[0].length-1];
		for (int i = 0; i < m.length; i ++) for (int j = 0; j < m.length; j ++)
			if (i != row && j != column) buffer[i - (i < row? 0 : 1)][j - (j < column? 0 : 1)] = m[i][j];
		return buffer;
	}

	private Math() {}

}