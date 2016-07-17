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

/*
 * See https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
 *
 * By the time this class comes into play we hopefully are done with all the editing and have but one concern: displaying it.
 * (of course we may not be done with *ALL* the editing; still we probably have done enough to make it worth checking out its
 * current state)
 *
 * There's very little left to do here: we just convert CIEXYZ D50 to sRGB, Java's default color space.
 */

package com.github.gasrios.raw.swing;

import java.awt.image.BufferedImage;

import com.github.gasrios.raw.lang.Math;

public class ImageSRGB extends BufferedImage {

	/*
	 * XYZ (D50) to sRGB Transform
	 *
	 * From http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
	 *
	 * This matrix accounts for the illuminant D50, while IEC 61966-2-1:1999 (sRGB spec) uses D65.
	 */
	private static final double[][] XYZ_D50ToSRGB =
		new double[][] {
			new double[] { 3.1338561D, -1.6168667D, -0.4906146D },
			new double[] {-0.9787684D,  1.9161415D,  0.0334540D },
			new double[] { 0.0719453D, -0.2289914D,  1.4052427D }
		};


	// See https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html#BufferedImage(int, int, int)
	public ImageSRGB(double[][][] image) {
		super(image.length, image[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < getWidth(); i++) for (int j = 0; j < getHeight(); j = j + 1) setRGB(i, j, toSRGB(image[i][j]));
	}

	private synchronized void setRGB(int column, int row, int[] pixel) { setRGB(column, row, (pixel[0] << 16) + (pixel[1] << 8) + pixel[2]); }

	private int[] toSRGB(double[] camera) {
		double[] srgb = Math.multiply(XYZ_D50ToSRGB, camera);
		for (int i = 0; i < srgb.length; i++) srgb[i] = gammaCorrection(srgb[i]);
		return to8bits(srgb);
	}

	// From http://en.wikipedia.org/wiki/SRGB#The_forward_transformation_.28CIE_xyY_or_CIE_XYZ_to_sRGB.29
	private static double gammaCorrection(double d) { return d <= 0.0031308D? 12.92D*d : 1.055D*java.lang.Math.pow(d, 1D/2.4D) - 0.055D; }

	private int[] to8bits(double[] rgb) { return new int[] { to8Bits(rgb[0]), to8Bits(rgb[1]), to8Bits(rgb[2]) }; }

	private int to8Bits(double n) { return (int) java.lang.Math.round((n < 0? 0 : n > 1? 1 : n)*255D); }

}