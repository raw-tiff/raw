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

import java.awt.Image;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {

	public ImageFrame(Image image, int displayWidth, int displayHeight) { this("Image Preview", image, displayWidth, displayHeight); }

	public ImageFrame(String name, Image image, int displayWidth, int displayHeight) {
		super(name);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLocation(0, 0);
		add(new ImageComponent(image, displayWidth, displayHeight));
		pack();
		setVisible(true);
	}

}