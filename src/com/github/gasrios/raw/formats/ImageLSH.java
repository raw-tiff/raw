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
 * Color space: LSH (CIE LCHuv, with chroma C replaced with saturation S = C/L)
 */
public class ImageLSH extends ImageCIELCH {

	public double[] fromXYZ(double[] pixel) {
		double[] lch = super.fromXYZ(pixel);
		return (lch[0] == 0D)? lch : new double[] { lch[0], lch[1]/lch[0], lch[2] };
	}

	public int[] toSRGB(double[] pixel) { return super.toSRGB(new double[] { pixel[0], pixel[1]*pixel[0], pixel[2] }); }

}