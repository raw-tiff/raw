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

package com.github.gasrios.raw.formats;

import com.github.gasrios.raw.lang.Math;

/*
 * Color space: CIE 1931 XYZ
 */
public class ImageCIEXYZ {

	public double[] fromXYZ(double[] pixel) { return pixel; }

	public int[] toSRGB(double[] pixel) { return to8bits(gammaCorrection(Math.multiply(XYZ_D50ToSRGB, pixel))); }

	private double[][][] image;

	public double[][][] getImage() { return image; }

	public void setImage(double[][][] image) { this.image = image; }

	/*
	 * From http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
	 *
	 * DNG uses the D50 illuminant, and this appears to be standard among raw formats.
	 */

	protected static final double[][] XYZ_D50ToSRGB = new double[][] {
		new double[] {  3.1338561D, -1.6168667D, -0.4906146D },
		new double[] { -0.9787684D,  1.9161415D,  0.0334540D },
		new double[] {  0.0719453D, -0.2289914D,  1.4052427D }
	};

	protected static double[] gammaCorrection(double[] d) {
		return new double[] { gammaCorrection(d[0]), gammaCorrection(d[1]), gammaCorrection(d[2]) };
	}

	// From http://en.wikipedia.org/wiki/SRGB#The_forward_transformation_.28CIE_xyY_or_CIE_XYZ_to_sRGB.29
	private static double gammaCorrection(double d) {
		return d <= 0.0031308D? 12.92D*d : 1.055D*java.lang.Math.pow(d, 1D/2.4D) - 0.055D;
	}

	protected static int[] to8bits(double[] rgb) { return new int[] { to8Bits(rgb[0]), to8Bits(rgb[1]), to8Bits(rgb[2]) }; }

	private static int to8Bits(double n) { return (int) java.lang.Math.round((n < 0? 0 : n > 1? 1 : n)*255D); }

}