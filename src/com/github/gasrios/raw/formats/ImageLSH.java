package com.github.gasrios.raw.formats;

/*
 * Color space: LSH (CIE LChuv, with chroma C replaced with saturation S = C/L)
 */
public class ImageLSH extends ImageCIELCH {

	public double[] fromXYZ(double[] pixel) {
		double[] lch = super.fromXYZ(pixel);
		return (lch[0] == 0D)? lch : new double[] { lch[0], lch[1]/lch[0], lch[2] };
	}

	public int[] toSRGB(double[] pixel) { return super.toSRGB(new double[] { pixel[0], pixel[1]*pixel[0], pixel[2] }); }

}