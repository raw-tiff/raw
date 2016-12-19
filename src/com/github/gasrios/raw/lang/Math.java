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
 * A note on image editing and CIE color spaces
 *
 * Not all CIE color spaces are useful when editing images. In fact, surprisingly few are. In order to effectively manipulate
 * an image you need a color space in which luminance, saturation and hue are linearly independent from one another, a condition
 * usually not met.
 *
 * Let's examine for example the case of CIE 1931 XYZ. From https://en.wikipedia.org/wiki/CIE_1931_color_space#Meaning_of_X.2C_Y.2C_and_Z
 *
 * "When judging the relative luminance (brightness) of different colors in well-lit situations, humans tend to perceive
 * light within the green parts of the spectrum as brighter than red or blue light of equal power. The luminosity function
 * that describes the perceived brightnesses of different wavelengths is thus roughly analogous to the spectral sensitivity
 * of M cones. The CIE model capitalises on this fact by defining Y as luminance."
 *
 * Y may be a decent approximation of luminance in CIE 1931 XYZ, but it also a measure of how "green" a color is. Manipulating
 * Y will affect all hues in the image.
 *
 * Similar problems happen with CIE 1976 (L*, u*, v*) and its LChuv variant: if you increase L* the image is desaturated.
 *
 * (See https://en.wikipedia.org/wiki/CIELUV, in particular session "Cylindrical representation (CIELCH)")
 *
 * CIE 1976 (L*, a*, b*) was not tested and does not look promising, given it has no saturation definition.
 *
 * One option is to replace chroma with saturation (C/L) in LChuv. The resulting LSH color space is not formally specified
 * but seems closer to providing linearly independent dimensions than any other CIE color space. I  call it "myLSH".
 */

public final class Math {

	// Image editing methods.

	public static double[][][] blackAndWhite(double[][][] image) {
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++) image[i][j][1] = 0;
		return image;
	}

	/*
	 * Color space conversions.
	 *
	 * FIXME Need to correct rounding mistakes?
	 * FIXME luv2mylsh(xyz2luv(XYZ)) should be replaced with xyz2mylsh(XYZ)
	 * FIXME mylsh2luv(luv2xyz(LSH)) should be replaced with mylsh2xyz(LSH)
	 */

	static private final double

		// u,v coordinates for D50 reference white. See ftp://law.resource.org/pub/us/cfr/ibr/003/cie.15.2004.tables.xls
		un = .209159684D,
		vn = .488082649D,

		// See http://www.brucelindbloom.com/index.html?LContinuity.html
		ε = 216D/24389D,
		κ = 24389D/27D,
		invκ = 27D/24389D;

	// http://en.wikipedia.org/wiki/CIELUV#The_forward_transformation
	public static double[] xyz2luv(double[] xyz) {

		double d = xyz[0]+15D*xyz[1]+3D*xyz[2];

		if (d == 0D) return new double[] { 0D, 0D, 0D };

		double L = xyz[1] > ε? 116D*java.lang.Math.pow(xyz[1], 1D/3D)-16D : κ*xyz[1];
		return new double[] { L, 13D*L*((4D*xyz[0]/d)-un), 13D*L*((9D*xyz[1]/d)-vn) };

	}

	// See http://en.wikipedia.org/wiki/CIELUV#The_reverse_transformation
	public static double[] luv2xyz(double[] luv) {

		if (luv[0] == 0D) return new double[] { 0D, 0D, 0D };

		double u = luv[1]/(13D*luv[0])+un;
		double v = luv[2]/(13D*luv[0])+vn;
		double Y = luv[0] > 8D? java.lang.Math.pow((luv[0]+16D)/116D, 3D) : luv[0]*invκ;

		return new double[] { Y*(9D*u)/(4D*v), Y, Y*(12D-3D*u-20D*v)/(4D*v) };

	}

	// See http://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation
	public static double[] luv2mylsh(double[] luv) {
		return (luv[0] == 0D)?
			new double[] { 0D, 0D, 0D }:
			new double[] { luv[0], java.lang.Math.pow(java.lang.Math.pow(luv[1], 2) + java.lang.Math.pow(luv[2], 2), .5D)/luv[0], java.lang.Math.atan2(luv[2], luv[1]) };
	}

	public static double[] mylsh2luv(double[] lsh) {
		return new double[] { lsh[0], lsh[0]*lsh[1]*java.lang.Math.cos(lsh[2]), lsh[0]*lsh[1]*java.lang.Math.sin(lsh[2]) };
	}

	// Matrix operations

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