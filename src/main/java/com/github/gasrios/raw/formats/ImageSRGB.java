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
 * Color space: sRGB, normalized to range [0, 1.0]
 */
public class ImageSRGB extends ImageCIEXYZ {

	public double[] fromXYZ(double[] pixel)	{ return gammaCorrection(Math.multiply(XYZ_D50ToSRGB, pixel)); }

	public int[] toSRGB(double[] pixel)		{ return to8bits(pixel); }

}