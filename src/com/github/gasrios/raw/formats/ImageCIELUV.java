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

/*
 * Color space: CIE 1976 (L*, u*, v*)
 */
public class ImageCIELUV extends ImageCIEXYZ {

	// http://en.wikipedia.org/wiki/CIELUV#The_forward_transformation
	public double[] fromXYZ(double[] pixel) {
		double d = pixel[0]+15D*pixel[1]+3D*pixel[2];
		if (d == 0D) return new double[] { 0D, 0D, 0D };
		double L = pixel[1] > ε? 116D*java.lang.Math.pow(pixel[1], 1D/3D)-16D : κ*pixel[1];
		return new double[] { L, 13D*L*((4D*pixel[0]/d)-un), 13D*L*((9D*pixel[1]/d)-vn) };
	}

	// http://en.wikipedia.org/wiki/CIELUV#The_reverse_transformation
	public int[] toSRGB(double[] pixel) {
		if (pixel[0] == 0D) return super.toSRGB(new double[] { 0D, 0D, 0D });
		double u = pixel[1]/(13D*pixel[0])+un;
		double v = pixel[2]/(13D*pixel[0])+vn;
		double Y = pixel[0] > 8D? java.lang.Math.pow((pixel[0]+16D)/116D, 3D) : pixel[0]*invκ;
		return super.toSRGB(new double[] { Y*(9D*u)/(4D*v), Y, Y*(12D-3D*u-20D*v)/(4D*v) });
	}

	static private final double

	// u,v coordinates for D50 reference white. See ftp://law.resource.org/pub/us/cfr/ibr/003/cie.15.2004.tables.xls
	un = .209159684D,
	vn = .488082649D,

	// See http://www.brucelindbloom.com/index.html?LContinuity.html
	ε = 216D/24389D,
	κ = 24389D/27D,
	invκ = 27D/24389D;

}