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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

/*
 * Generates a viewable - correctly scaled - image to display from the image to be processed.
 */
@SuppressWarnings("serial")
public class ImageComponent extends JComponent {

	private static Image imageToDisplay;

	public ImageComponent(Image image, int displayWidth, int displayHeight) {

		super();

		if (image.getHeight(null) <= displayHeight && image.getWidth(null) <= displayWidth) imageToDisplay = image;

		else {

			int height = image.getHeight(null);
			int width = image.getWidth(null);

			if (((float) height) / width > ((float) displayHeight) / displayWidth) {
				width  = (displayHeight * width) / height;
				height = displayHeight;
			} else {
				height = (displayWidth * height) / width;
				width  = displayWidth;
			}

			imageToDisplay = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		}

	}

	public void paint(Graphics g) { g.drawImage(imageToDisplay, 0, 0, null); }

	public Dimension getPreferredSize() {
		if (imageToDisplay == null) throw new NullPointerException("No image loaded!");
		else return new Dimension(imageToDisplay.getWidth(null), imageToDisplay.getHeight(null));
	}

}