package com.github.gasrios.raw.formats;

import com.github.gasrios.raw.lang.Math;

/*
 * Color space: normalized sRGB
 */
public class ImageSRGB extends ImageCIEXYZ {

	public double[] fromXYZ(double[] pixel) { return gammaCorrection(Math.multiply(XYZ_D50ToSRGB, pixel)); }

	public int[] toSRGB(double[] pixel) { return to8bits(pixel); }

}