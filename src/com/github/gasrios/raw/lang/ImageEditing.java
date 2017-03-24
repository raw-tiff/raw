package com.github.gasrios.raw.lang;

import com.github.gasrios.raw.formats.ImageCIELCH;
import com.github.gasrios.raw.formats.ImageSRGB;

public class ImageEditing {

	// Increase brightness. Assumes pixel[0] is brightness.
	public static ImageCIELCH increaseBrightness(ImageCIELCH image) {

		double[][][] im = image.getImage();
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			if (min > im[i][j][0]) min = im[i][j][0];
			if (max < im[i][j][0]) max = im[i][j][0];
		}

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) im[i][j][0] = 100*(im[i][j][0]-min)/(max-min);

		return image;

	}

	// Convert to B&W by desaturating image. Assumes pixel[1] is saturation
	public static ImageCIELCH blackAndWhite(ImageCIELCH image) { return saturate(image, -1D); }

	// Increase image saturation by given percentage. Assumes pixel[1] is saturation
	public static ImageCIELCH saturate(ImageCIELCH image, double percentage) {
		double[][][] im = image.getImage();
		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) im[i][j][1] *= (1 + percentage);
		return image;
	}

	// Convert RGB image to B&W.
	public static ImageSRGB blackAndWhite(ImageSRGB image) {

		double[][][] im = image.getImage();

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			double average = 0D;
			for (int k = 0; k < 3; k++) average += im[i][j][k];
			average /= 3;
			for (int k = 0; k < 3; k++) im[i][j][k] = average;
		};

		return image;

	}

	// Only works for B&W images.
	public static ImageSRGB increaseBrightness(ImageSRGB image) {

		double[][][] im = image.getImage();
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			if (min > im[i][j][0]) min = im[i][j][0];
			if (max < im[i][j][0]) max = im[i][j][0];
		}

		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) {
			double b = (im[i][j][0]-min)/(max-min);
			for (int k = 0; k < 3; k ++) im[i][j][k] = b;
		}

		return image;

	}

}