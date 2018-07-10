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

package com.github.gasrios.raw.swing;

import java.awt.image.BufferedImage;

import com.github.gasrios.raw.formats.ImageCIEXYZ;

/*
 * See https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
 *
 * By the time this class comes into play we hopefully are done with all the editing and have but one concern: displaying it.
 * (of course we may not be done with *ALL* the editing; still we probably have done enough to make it worth checking out its
 * current state)
 *
 * There's very little left to do here: we just convert CIEXYZ D50 to sRGB, Java's default color space.
 */

public class Image extends BufferedImage {

	public enum Orientation { HORIZONTAL, VERTICAL }

	/*
	 * See https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html#BufferedImage(int, int, int)
	 */

	public Image(ImageCIEXYZ image) { this(image, Orientation.HORIZONTAL); }

	public Image(ImageCIEXYZ image, Orientation orientation) {

		super(
			orientation == Orientation.HORIZONTAL? image.getImage().length : image.getImage()[0].length,
			orientation == Orientation.HORIZONTAL? image.getImage()[0].length : image.getImage().length,
			// sRGB
			BufferedImage.TYPE_INT_RGB
		);

		double [][][] im = image.getImage();

		if (orientation == Orientation.HORIZONTAL)
			for (int i = 0; i < getWidth(); i++) for (int j = 0; j < getHeight(); j++) setRGB(i, j, image.toSRGB(im[i][j]));
		else
			for (int i = 0; i < getWidth(); i++) for (int j = 0; j < getHeight(); j++) setRGB(i, getHeight()-(j+1), image.toSRGB(im[j][i]));

	}

	private synchronized void setRGB(int column, int row, int[] pixel) { setRGB(column, row, (pixel[0] << 16) + (pixel[1] << 8) + pixel[2]); }

}