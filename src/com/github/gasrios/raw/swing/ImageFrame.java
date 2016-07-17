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