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

import java.util.HashMap;
import java.util.Map;

import com.github.gasrios.raw.data.Illuminant;

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
 * See https://en.wikipedia.org/wiki/CIELUV and https://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation_.28CIELCH.29
 *
 * CIE 1976 (L*, a*, b*) was not tested and does not look promising, given it has no saturation definition.
 *
 * One option is to replace chroma with saturation (C/L) in LChuv. The resulting LSH color space is not formally specified
 * but seems closer to providing linearly independent dimensions than any other CIE color space.
 *
 * FIXME LSH still suffers from one major drawback: saturation is neither normalized nor bounded.
 */

public final class Math {

	/*
	 * Image info that might be useful when editing.
	 */

	public static double min(double[][][] image, int dimension) {
		double buffer = Double.MAX_VALUE;
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++)
			if (buffer > image[i][j][dimension]) buffer = image[i][j][dimension];
		return buffer;
	}

	public static double median(double[][][] image, int dimension) {
		double buffer = 0D;
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++) buffer += image[i][j][dimension];
		return buffer / (image.length*image[0].length);
	}

	public static double max(double[][][] image, int dimension) {
		double buffer = Double.MIN_VALUE;
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++)
			if (buffer < image[i][j][dimension]) buffer = image[i][j][dimension];
		return buffer;
	}

	/*
	 * Image editing methods. Color space is assumed to be LSH.
	 */

	// Convert to B&W by desaturating image.
	public static double[][][] blackAndWhite(double[][][] image) {
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++) image[i][j][1] = 0;
		return image;
	}

	// Increase image saturation by given percentage
	public static double[][][] saturate(double[][][] image, double percentage) {
		for (int i = 0; i < image.length; i++) for (int j = 0; j < image[0].length; j ++) image[i][j][1] *= (1 + percentage);
		return image;
	}

	/*
	 * Color space conversions.
	 */

	/*
	 * 1. Camera to CIE 1931 XYZ
	 *
	 * See Digital Negative Specification Version 1.4.0.0, page 82.
	 *
	 * ReferenceNeutral = Inverse(AB * CC) * CameraNeutral
	 *
	 * D = Invert(AsDiagonalMatrix(ReferenceNeutral))
	 *
	 * CameraToXYZ_D50 = FM * D * Inverse(AB * CC)
	 *
	 * See Digital Negative Specification Version 1.4.0.0, page 80.
	 *
	 * Let AB be the n-by-n matrix, which is zero except for the diagonal entries, which are defined by the AnalogBalance tag.
	 *
	 * Let CC be the n-by-n matrix interpolated from the CameraCalibration1 and CameraCalibration2 tags (or identity
	 * matrices, if the signatures don’t match).
	 *
	 * Let FM be the 3-by-n matrix interpolated from the ForwardMatrix1 and ForwardMatrix2 tags.
	 *
	 * CameraCalibration1 defines a calibration matrix that transforms reference camera native space values to individual
	 * camera native space values under the first calibration illuminant. The matrix is stored in row scan order.
	 *
	 * This matrix is stored separately from the matrix specified by the ColorMatrix1 tag to allow raw converters to swap in
	 * replacement color matrices based on UniqueCameraModel tag, while still taking advantage of any per-individual camera
	 * calibration performed by the camera manufacturer.
	 *
	 * ForwardMatrix1 defines a matrix that maps white balanced camera colors to XYZ D50 colors.
	 */
	public static double[][] cameraToXYZ_D50(
			RATIONAL [] analogBalance,
			double   [] cameraNeutral,
			int			calibrationIlluminant1,
			int			calibrationIlluminant2,
			SRATIONAL[] cameraCalibration1,
			SRATIONAL[] cameraCalibration2,
			SRATIONAL[] colorMatrix1,
			SRATIONAL[] colorMatrix2,
			SRATIONAL[] forwardMatrix1,
			SRATIONAL[] forwardMatrix2
	) {

		double weight =
			interpolationWeightingFactor(
				analogBalance,
				calibrationIlluminant1,
				calibrationIlluminant2,
				cameraCalibration1,
				cameraCalibration2,
				cameraNeutral,
				colorMatrix1,
				colorMatrix2
			);

		double[][] invABxCC =
			Math.inverse(
				Math.multiply(
					Math.asDiagonalMatrix(RATIONAL.asDoubleArray(analogBalance)),
					Math.weightedAverage(
						Math.vector2Matrix(SRATIONAL.asDoubleArray(cameraCalibration1)),
						Math.vector2Matrix(SRATIONAL.asDoubleArray(cameraCalibration2)),
						weight
					)
				)
			);

		return
			Math.multiply(
				Math.multiply(
					Math.weightedAverage(
						Math.vector2Matrix(SRATIONAL.asDoubleArray(forwardMatrix1)),
						Math.vector2Matrix(SRATIONAL.asDoubleArray(forwardMatrix2)),
						weight
					),
					Math.inverse(Math.asDiagonalMatrix(Math.multiply(invABxCC, cameraNeutral)))
				),
				invABxCC
		);

	}

	/*
	 * We don't really need the White Balance xy Coordinates, just the interpolation weighting factor, but the same process
	 * calculates both.
	 *
	 * See Digital Negative Specification Version 1.4.0.0, page 80.
	 *
	 * Translating Camera Neutral Coordinates to White Balance xy Coordinates
	 *
	 * This process is slightly more complex than the transform in the other direction because it requires an iterative
	 * solution.
	 *
	 * 1. Guess an xy value. Use that guess to find the interpolation weighting factor between the color calibration tags.
	 *    Find the XYZtoCamera matrix as above.
	 *
	 * 2. Find a new xy value by computing:
	 *		XYZ = Inverse(XYZtoCamera) * CameraNeutral
	 *
	 * 3. Convert the resulting XYZ to a new xy value.
	 *
	 * 4. Iterate until the xy values converge to a solution.
	 */
	private static double interpolationWeightingFactor(
		RATIONAL [] analogBalance,
		int			calibrationIlluminant1,
		int			calibrationIlluminant2,
		SRATIONAL[] cameraCalibration1,
		SRATIONAL[] cameraCalibration2,
		double[]	cameraNeutral,
		SRATIONAL[] colorMatrix1,
		SRATIONAL[] colorMatrix2
	) {

		// This is just an initial guess. Any value will do.
		double previousWeight, weight = .5D;

		// Sometimes, due to precision issues, weight does not converge, so we have to limit the number  of iterations.
		int count = 0;

		do {
			count++;
			previousWeight = weight;
			weight =
				interpolationWeightingFactor(
					xyz2xy(
						Math.multiply(
							Math.inverse(
								xyzToCamera(weight, analogBalance, cameraCalibration1, cameraCalibration2, colorMatrix1, colorMatrix2)
							),
							cameraNeutral
						)
					),
					calibrationIlluminant1,
					calibrationIlluminant2
				);
		} while (previousWeight != weight && count < 20);

		// When previousWeight and weight do not converge, we get stuck between two oscillating values. Return their average.
		return previousWeight == weight? weight : (previousWeight+weight)/2D;

	}

	private static final Map<Integer, Illuminant> ILLUMINANTS = new HashMap<Integer, Illuminant>();

	static {
		for (Illuminant illuminant: Illuminant.values()) ILLUMINANTS.put(illuminant.value, illuminant);
	}

	/*
	 * See Digital Negative Specification Version 1.4.0.0, page 79.
	 *
	 * DNG 1.2.0.0 and later requires a specific interpolation algorithm: linear interpolation using inverse correlated
	 * color temperature.
	 *
	 * To find the interpolation weighting factor between the two tag sets, find the correlated color temperature for the
	 * user-selected white balance and the two calibration illuminants. If the white balance temperature is between two
	 * calibration illuminant temperatures, then invert all the temperatures and use linear interpolation. Otherwise, use
	 * the closest calibration tag set.
	 */
	private static double interpolationWeightingFactor(double[] xy, int calibrationIlluminant1, int calibrationIlluminant2) {
		return
			1D - normalize(
				1/ILLUMINANTS.get(calibrationIlluminant2).cct,
				1/cct(xy),
				1/ILLUMINANTS.get(calibrationIlluminant1).cct
			);
	}

	// McCamy's cubic approximation (http://en.wikipedia.org/wiki/Color_temperature#Approximation)
	private static double cct(double[] chromaticityCoordinates) {
		double n = (chromaticityCoordinates[0] - 0.3320D)/(chromaticityCoordinates[1] - 0.1858D);
		return -449D*java.lang.Math.pow(n, 3D) + 3525D*java.lang.Math.pow(n, 2D) - 6823.3D*n + 5520.33D;
	}

	public static double normalize(double b, double m, double t) { return m < b? 0 : m > t? 1 : (m-b)/(t-b); }

	/*
	 * See Digital Negative Specification Version 1.4.0.0, page 79.
	 *
	 * Translating White Balance xy Coordinates to Camera Neutral Coordinates
	 *
	 * If the white balance is specified in terms of a CIE xy coordinate, then a camera neutral coordinate can be derived
	 * by first finding the correlated color temperature for the xy value. This value determines the interpolation weighting
	 * factor between the two sets of color calibration tags.
	 *
	 * The XYZ to camera space matrix is:
	 *
	 *	XYZtoCamera = AB * CC * CM
	 *
	 * The camera neutral can be found by expanding the xy value to a 3-by-1 XYZ matrix (assuming Y = 1.0) and multiplying it
	 * by the XYZtoCamera matrix:
	 *
	 *	CameraNeutral = XYZtoCamera * XYZ
	 *
	 * See Digital Negative Specification Version 1.4.0.0, page 80.
	 *
	 * Let AB be the n-by-n matrix, which is zero except for the diagonal entries, which are defined by the AnalogBalance tag.
	 *
	 * Let CC be the n-by-n matrix interpolated from the CameraCalibration1 and CameraCalibration2 tags (or identity matrices,
	 * if the signatures don’t match).
	 *
	 * Let CM be the n-by-3 matrix interpolated from the ColorMatrix1 and ColorMatrix2 tags.
	 */
	private static double[][] xyzToCamera(
		double weight,
		RATIONAL [] analogBalance,
		SRATIONAL[] cameraCalibration1,
		SRATIONAL[] cameraCalibration2,
		SRATIONAL[] colorMatrix1,
		SRATIONAL[] colorMatrix2
	) {
		return
			Math.multiply(
				Math.multiply(
					Math.asDiagonalMatrix(RATIONAL.asDoubleArray(analogBalance)),
					Math.weightedAverage(
						Math.vector2Matrix(SRATIONAL.asDoubleArray(cameraCalibration1)),
						Math.vector2Matrix(SRATIONAL.asDoubleArray(cameraCalibration2)),
						weight
					)
				),
				Math.weightedAverage(
					Math.vector2Matrix(SRATIONAL.asDoubleArray(colorMatrix1)),
					Math.vector2Matrix(SRATIONAL.asDoubleArray(colorMatrix2)),
					weight
				)
			);
	}

	// http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_xyY.html
	private static double[] xyz2xy(double[] xyz) {
		return (xyz[0]+xyz[1]+xyz[2]) == 0?
			new double[] { 0, 0, 0 }:
			new double[] { xyz[0]/(xyz[0]+xyz[1]+xyz[2]), xyz[1]/(xyz[0]+xyz[1]+xyz[2]), xyz[1] };
	}

	/*
	 * 2. CIE 1931 XYZ <-> CIE 1976 (L*, u*, v*)
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

	/*
	 * 3. CIE 1976 (L*, u*, v*) <-> LSH (LChuv, with chroma C replaced with saturation S = C/L)
	 */

	// See http://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation
	public static double[] luv2lsh(double[] luv) {
		return (luv[0] == 0D)?
			new double[] { 0D, 0D, 0D }:
			new double[] {
				luv[0],
				java.lang.Math.pow(java.lang.Math.pow(luv[1], 2) + java.lang.Math.pow(luv[2], 2), .5D)/luv[0],
				java.lang.Math.atan2(luv[2], luv[1])
			};
	}

	public static double[] lsh2luv(double[] lsh) {
		return new double[] {
			lsh[0],
			lsh[0]*lsh[1]*java.lang.Math.cos(lsh[2]),
			lsh[0]*lsh[1]*java.lang.Math.sin(lsh[2])
		};
	}

	/*
	 * 4. CIE 1931 XYZ <-> sRGB
	 */

	/*
	 * From http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
	 *
	 * This matrix accounts for the illuminant D50, while IEC 61966-2-1:1999 uses D65.
	 */
	public static final double[][] XYZ_D50ToSRGB = new double[][] {
		new double[] {  3.1338561D, -1.6168667D, -0.4906146D },
		new double[] { -0.9787684D,  1.9161415D,  0.0334540D },
		new double[] {  0.0719453D, -0.2289914D,  1.4052427D }
	};

	public static int[] lsh2sRGB(double[] pixel) {
		return to8bits(gammaCorrection(Math.multiply(XYZ_D50ToSRGB, Math.luv2xyz(Math.lsh2luv(pixel)))));
	}

	private static double[] gammaCorrection(double[] d) {
		return new double[] { gammaCorrection(d[0]), gammaCorrection(d[1]), gammaCorrection(d[2]) };
	}

	// From http://en.wikipedia.org/wiki/SRGB#The_forward_transformation_.28CIE_xyY_or_CIE_XYZ_to_sRGB.29
	private static double gammaCorrection(double d) {
		return d <= 0.0031308D? 12.92D*d : 1.055D*java.lang.Math.pow(d, 1D/2.4D) - 0.055D;
	}

	private static int[] to8bits(double[] rgb) { return new int[] { to8Bits(rgb[0]), to8Bits(rgb[1]), to8Bits(rgb[2]) }; }

	private static int to8Bits(double n) { return (int) java.lang.Math.round((n < 0? 0 : n > 1? 1 : n)*255D); }

	/*
	 * Matrix operations
	 */

	public static double[] multiply(double[][] m, double[] v) {
		double[] m2 = new double[m.length];
		for (int i = 0; i < m2.length; i++) for (int j = 0; j < v.length; j++) m2[i] += m[i][j]* v[j];
		return m2;
	}

	private static double[][] multiply(double[][] m1, double[][] m2) {
		double[][] m3 = new double[m1.length][m2[0].length];
		for (int i = 0; i < m3.length; i++) for (int j = 0; j < m3[0].length; j++) for (int k = 0; k < m2.length; k++)
			m3[i][j] += m1[i][k]* m2[k][j];
		return m3;
	}

	private static double[][] weightedAverage(double[][] matrix1, double[][] matrix2, double weight) {
		double[][] buffer = new double[matrix1.length][matrix1[0].length];
		weight = weight < 0? 0 : weight > 1? 1 : weight;
		for (int i = 0; i < matrix1.length; i++) for (int j = 0; j < matrix1[0].length; j++)
			buffer[i][j] = matrix1[i][j]*(1-weight) + matrix2[i][j]*weight;
		return buffer;
	}

	private static double[][] asDiagonalMatrix(double[] vector) {
		double[][] buffer = new double[vector.length][vector.length];
		for (int i = 0; i < vector.length; i++) buffer[i][i] = vector[i];
		return buffer;
	}

	// TODO samplesPerPixel != 3
	private static double[][] vector2Matrix(double[] vector) {
		double[][] buffer = null;
		if (vector != null) {
			buffer = new double[3][3];
			for (int i = 0; i < 9; i++) buffer[i/3][i%3] = vector[i];
		}
		return buffer;
	}

	private static double[][] inverse(double[][] m) {
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

	private static double cofactor(int i, int j, double[][] m) { return determinant(submatrix(i, j, m))*java.lang.Math.pow(-1, i+j); }

	private static double[][] submatrix(int row, int column, double[][] m) {
		double[][] buffer = new double[m.length-1][m[0].length-1];
		for (int i = 0; i < m.length; i ++) for (int j = 0; j < m.length; j ++) if (i != row && j != column)
			buffer[i - (i < row? 0 : 1)][j - (j < column? 0 : 1)] = m[i][j];
		return buffer;
	}

	private Math() {}

}