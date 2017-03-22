package com.github.gasrios.raw.formats;

/*
 * Color space: CIE LChuv
 */
public class ImageCIELCH extends ImageCIELUV {

	// See http://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation
	public double[] fromXYZ(double[] pixel) {
		double[] luv = super.fromXYZ(pixel);
		return (luv[0] == 0D)?
			new double[] { 0D, 0D, 0D }:
			new double[] {
				luv[0],
				java.lang.Math.pow(java.lang.Math.pow(luv[1], 2) + java.lang.Math.pow(luv[2], 2), .5D),
				java.lang.Math.atan2(luv[2], luv[1])
			};
	}

	public int[] toSRGB(double[] pixel) {
		return super.toSRGB(
			new double[] {
				pixel[0],
				pixel[1]*java.lang.Math.cos(pixel[2]),
				pixel[1]*java.lang.Math.sin(pixel[2])
			}
		);
	}

}