package com.github.gasrios.raw.lang;

import com.github.gasrios.raw.formats.ImageCIEXYZ;

public class ImageEditing {

	// Convert to B&W by desaturating image. Assumes pixel[1] is saturation
	public static ImageCIEXYZ blackAndWhite(ImageCIEXYZ image) { return saturate(image, -1D); }

	// Increase image saturation by given percentage. Assumes pixel[1] is saturation
	public static ImageCIEXYZ saturate(ImageCIEXYZ image, double percentage) {
		double[][][] im = image.getImage();
		for (int i = 0; i < im.length; i++) for (int j = 0; j < im[0].length; j ++) im[i][j][1] *= (1 + percentage);
		return image;
	}

}