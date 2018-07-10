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
 * Color space: CIE LCHuv
 */
public class ImageCIELCH extends ImageCIELUV {

	// See http://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation
	public double[] fromXYZ(double[] pixel) {
		double[] luv = super.fromXYZ(pixel);
		return (luv[0] == 0D)?
			new double[] { 0D, 0D, 0D }:
			new double[] {
				luv[0],
				java.lang.Math.pow(java.lang.Math.pow(luv[1], 2) + java.lang.Math.pow(luv[2], 2), .5D),
				java.lang.Math.atan2(luv[2], luv[1])
			};
	}

	public int[] toSRGB(double[] pixel) {
		return super.toSRGB(
			new double[] {
				pixel[0],
				pixel[1]*java.lang.Math.cos(pixel[2]),
				pixel[1]*java.lang.Math.sin(pixel[2])
			}
		);
	}

}