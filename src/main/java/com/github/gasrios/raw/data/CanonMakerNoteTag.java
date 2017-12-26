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

package com.github.gasrios.raw.data;

/*
 * The tags below were collected from several online resources. Refer to them for additional info.
 *
 * http://www.exiv2.org/tags-canon.html
 * http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Canon.html
 * http://www.ozhiker.com/electronics/pjmt/jpeg_info/canon_mn.html
 * http://www.burren.cx/david/canon.html
 */

public final class CanonMakerNoteTag extends MakerNoteTag {

	public CanonMakerNoteTag(String name, int number) { super(name, number); }

	public static final CanonMakerNoteTag[] values() {return values; }

	public static final CanonMakerNoteTag CameraSettings				= new CanonMakerNoteTag("CameraSettings",					1);
	public static final CanonMakerNoteTag FocalLength					= new CanonMakerNoteTag("FocalLength",						2);
	// 3
	public static final CanonMakerNoteTag ShotInfo						= new CanonMakerNoteTag("ShotInfo",							4);
	public static final CanonMakerNoteTag ImageType						= new CanonMakerNoteTag("ImageType",						6);
	public static final CanonMakerNoteTag FirmwareVersion				= new CanonMakerNoteTag("FirmwareVersion",				    7);
	public static final CanonMakerNoteTag OwnerName						= new CanonMakerNoteTag("OwnerName",						9);
	public static final CanonMakerNoteTag SerialNumber					= new CanonMakerNoteTag("SerialNumber",					   12);
	public static final CanonMakerNoteTag CameraInfo					= new CanonMakerNoteTag("CameraInfo",					   13);
	public static final CanonMakerNoteTag ModelID						= new CanonMakerNoteTag("ModelID",						   16);
	public static final CanonMakerNoteTag ThumbnailImageValidArea		= new CanonMakerNoteTag("ThumbnailImageValidArea",		   19);
	public static final CanonMakerNoteTag SerialNumberFormat			= new CanonMakerNoteTag("SerialNumberFormat",			   21);
	// 25	0x19
	public static final CanonMakerNoteTag AFInfo						= new CanonMakerNoteTag("AFInfo",						   38);
	public static final CanonMakerNoteTag OriginalDecisionDataOffset	= new CanonMakerNoteTag("OriginalDecisionDataOffset",	  131);
	// 147	0x93
	public static final CanonMakerNoteTag LensModel						= new CanonMakerNoteTag("LensModel",					  149);
	public static final CanonMakerNoteTag InternalSerialNumber			= new CanonMakerNoteTag("InternalSerialNumber",			  150);
	public static final CanonMakerNoteTag DustRemovalData				= new CanonMakerNoteTag("DustRemovalData",				  151);
	// 152	0x98
	public static final CanonMakerNoteTag CustomFunctions				= new CanonMakerNoteTag("CustomFunctions",				  153);
	public static final CanonMakerNoteTag ProcessingInfo				= new CanonMakerNoteTag("ProcessingInfo",				  160);
	public static final CanonMakerNoteTag MeasuredColor					= new CanonMakerNoteTag("MeasuredColor",				  170);
	public static final CanonMakerNoteTag ColorSpace					= new CanonMakerNoteTag("ColorSpace",					  180);
	public static final CanonMakerNoteTag VRDOffset						= new CanonMakerNoteTag("VRDOffset",					  208);
	public static final CanonMakerNoteTag SensorInfo					= new CanonMakerNoteTag("SensorInfo",					  224);
	public static final CanonMakerNoteTag ColorData						= new CanonMakerNoteTag("ColorData",					16385);
	public static final CanonMakerNoteTag CRWParam						= new CanonMakerNoteTag("CRWParam",						16386);
	public static final CanonMakerNoteTag Flavor						= new CanonMakerNoteTag("Flavor",						16389);
	public static final CanonMakerNoteTag BlackLevel					= new CanonMakerNoteTag("BlackLevel",					16392);
	// 16393	0x4009
	// 16401	0x4011
	// 16402	0x4012
	public static final CanonMakerNoteTag CustomPictureStyleFileName	= new CanonMakerNoteTag("CustomPictureStyleFileName",	16400);
	public static final CanonMakerNoteTag VignettingCorr				= new CanonMakerNoteTag("VignettingCorr",				16405);
	public static final CanonMakerNoteTag VignettingCorr2				= new CanonMakerNoteTag("VignettingCorr2",				16406);
	// 16407	0x4017
	public static final CanonMakerNoteTag LightingOpt					= new CanonMakerNoteTag("LightingOpt",					16408);
	public static final CanonMakerNoteTag LensInfo						= new CanonMakerNoteTag("LensInfo",						16409);

	private static final CanonMakerNoteTag[] values = new CanonMakerNoteTag[] {
		CameraSettings, FocalLength, ShotInfo, ImageType, FirmwareVersion, OwnerName, SerialNumber, CameraInfo, ModelID,
		ThumbnailImageValidArea, SerialNumberFormat, AFInfo, OriginalDecisionDataOffset, LensModel, InternalSerialNumber,
		DustRemovalData, CustomFunctions, ProcessingInfo, MeasuredColor, ColorSpace, VRDOffset, SensorInfo, ColorData, CRWParam,
		Flavor, BlackLevel, CustomPictureStyleFileName, VignettingCorr, VignettingCorr2, LightingOpt, LensInfo
	};

}