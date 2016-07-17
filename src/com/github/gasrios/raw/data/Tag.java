/*
 * © 2016 Guilherme Rios All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 */

/*
 * Tags obtained from the following references:
 *
 *		TIFF Revision 6.0 Final - June 3, 1992
 *
 *		ISO 12234-2:2001, Electronic still-picture imaging – Removable memory – Part 2: TIFF/EP image data format
 *
 *		Exif Version 2.3
 *
 *		InterColor Profile Format version 3.0 - June 10, 1994
 *
 *		Digital Negative Specification Version 1.4.0.0
 */

package com.github.gasrios.raw.data;

public enum Tag {

	Unknown(-1),

	// DNG, page 18
	NewSubFileType(254),

	// TIFF, page 18
	ImageWidth(256),

	// TIFF, page 18
	ImageLength(257),

	// TIFF, page 29
	BitsPerSample(258),

	// DNG, page 19
	Compression(259),

	// DNG, page 20
	PhotometricInterpretation(262),

	ImageDescription(270),
	Make(271),
	Model(272),

	// TIFF, page 19
	StripOffsets(273),

	/*
	 * TIFF, page 36
	 * TIFF/EP, page 23
	 * DNG, page 20
	 */
	Orientation(274),

	// TIFF, page 39
	SamplesPerPixel(277),

	// TIFF, page 19
	RowsPerStrip(278),

	// TIFF, page 19
	StripByteCounts(279),

	XResolution(282),
	YResolution(283),

	// TIFF, page 19
	PlanarConfiguration(284),

	ResolutionUnit(296),
	Software(305),
	DateTime(306),
	Artist(315),

	// TIFF, page 67
	TileWidth(322),

	// TIFF, page 67
	TileLength(323),

	// TIFF, page 68
	TileOffsets(324),

	// TIFF, page 68
	TileByteCounts(325),

	SubIFDs(330),
	JPEGTables(347),
	YCbCrCoefficients(529),
	YCbCrSubSampling(530),
	YcbCrPositioning(531),
	ReferenceBlackWhite(532),

	// DNG, page 14
	XMP(700),

	// TIFF/EP, page 26
	CFARepeatPatternDim(33421),

	// TIFF/EP, page 26
	CFAPattern(33422),

	BatteryLevel(33423),
	Copyright(33432),
	ExposureTime(33434),
	FNumber(33437),
	IPTC_NAA(33723),

	// DNG, page 14
	ExifIFD(34665),

	/*
	 * TIFF/EP, page 48
	 * ICC, page 58
	 */
	InterColorProfile(34675),

	ExposureProgram(34850),
	SpectralSensitivity(34852),
	GPSInfo(34853),
	ISOSpeedRatings(34855),
	OECF(34856),
	Interlace(34857),
	TimeZoneOffset(34858),
	SelfTimerMode(34859),
	SensitivityType(34864),
	RecommendedExposureIndex(34866),

	// Exif, page 49
	ExifVersion(36864),

	DateTimeOriginal(36867),
	DateTimeDigitized(36868),
	CompressedBitsPerPixel(37122),
	ShutterSpeedValue(37377),
	ApertureValue(37378),
	BrightnessValue(37379),
	ExposureBiasValue(37380),
	MaxApertureValue(37381),
	SubjectDistance(37382),
	MeteringMode(37383),
	LightSource(37384),
	Flash(37385),
	FocalLength(37386),
	FlashEnergy(37387),
	SpatialFrequencyResponse(37388),
	Noise(37389),
	FocalPlaneXResolution(37390),
	FocalPlaneYResolution(37391),
	FocalPlaneResolutionUnit(37392),
	ImageNumber(37393),
	SecurityClassification(37394),
	ImageHistory(37395),
	SubjectLocation(37396),
	ExposureIndex(37397),
	TIFF_EPStandardID(37398),
	SensingMethod(37399),
	SubsecTimeOriginal(37521),
	SubsecTimeDigitized(37522),
	ColorSpace(40961),

	// The Exif tags below have the same name as previously declared TIFF/EP tags. Added "Exif" to name start to avoid conflict.
	ExifFocalPlaneXResolution(41486),
	ExifFocalPlaneYResolution(41487),
	ExifFocalPlaneResolutionUnit(41488),

	CustomRendered(41985),
	ExposureMode(41986),
	WhiteBalance(41987),
	SceneCaptureType(41990),
	BodySerialNumber(42033),
	LensSpecification(42034),
	LensMake(42035),
	LensModel(42036),
	LensSerialNumber(42037),

	// DNG, page 22
	DNGVersion(50706),

	// DNG, page 22
	DNGBackwardVersion(50707),

	// DNG, page 23
	UniqueCameraModel(50708),

	LocalizedCameraModel(50709),

	// DNG, page 24
	CFAPlaneColor(50710),

	// DNG, page 25
	CFALayout(50711),

	LinearizationTable(50712),
	BlackLevelRepeatDim(50713),
	BlackLevel(50714),
	BlackLevelDeltaH(50715),
	BlackLevelDeltaV(50716),

	/*
	 * DNG, page 29
	 * See chapter 5, “Mapping Raw Values to Linear Reference Values” on page 77 for details of the processing model.
	 */
	WhiteLevel(50717),

	// DNG, page 29
	DefaultScale(50718),

	// DNG, page 30
	BestQualityScale(50780),

	// DNG, page 30
	DefaultCropOrigin(50719),

	/*
	 * DNG, page 31
	 * http://www.barrypearson.co.uk/articles/dng/specification.htm
	 */
	DefaultCropSize(50720),

	/*
	 * DNG, page 31
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 * Exif, page 55
	 */
	CalibrationIlluminant1(50778),

	/*
	 * DNG, page 32
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 * Exif, page 55
	 */
	CalibrationIlluminant2(50779),

	/*
	 * DNG, page 32
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	ColorMatrix1(50721),

	/*
	 * DNG, page 33
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	ColorMatrix2(50722),

	/*
	 * DNG, page 34
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	CameraCalibration1(50723),

	/*
	 * DNG, page 34
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	CameraCalibration2(50724),

	ReductionMatrix1(50725),
	ReductionMatrix2(50726),

	/*
	 * DNG, page 36
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	AnalogBalance(50727),

	/*
	 * DNG, page 37
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */	
	AsShotNeutral(50728),

	/*
	 * DNG, page 37
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	AsShotWhiteXY(50729),

	// DNG, page 38
	BaselineExposure(50730),

	// DNG, page 38
	BaselineNoise(50731),

	// DNG, page 39
	BaselineSharpness(50732),

	BayerGreenSplit(50733),

	// DNG, page 40
	LinearResponseLimit(50734),

	CameraSerialNumber(50735),
	LensInfo(50736),
	ChromaBlurRadius(50737),

	// DNG, page 42
	AntiAliasStrength(50738),

	// DNG, page 42
	ShadowScale(50739),

	// DNG, page 43
	DNGPrivateData(50740),

	MakerNoteSafety(50741),

	// DNG, page 45
	RawDataUniqueID(50781),

	OriginalRawFileName(50827),
	OriginalRawFileData(50828),

	/*
	 * DNG, page 47
	 * http://www.barrypearson.co.uk/articles/dng/specification.htm
	 */
	ActiveArea(50829),

	MaskedAreas(50830),
	AsShotICCProfile(50831),
	AsShotPreProfileMatrix(50832),
	CurrentICCProfile(50833),
	CurrentPreProfileMatrix(50834),
	ColorimetricReference(50879),
	CameraCalibrationSignature(50931),
	ProfileCalibrationSignature(50932),
	ExtraCameraProfiles(50933),
	AsShotProfileName(50934),
	NoiseReductionApplied(50935),
	ProfileName(50936),
	ProfileHueSatMapDims(50937),
	ProfileHueSatMapData1(50938),
	ProfileHueSatMapData2(50939),
	ProfileToneCurve(50940),
	ProfileEmbedPolicy(50941),
	ProfileCopyright(50942),

	/*
	 * DNG, page 58
	 * Application is described in detail in Chapter 6.
	 */
	ForwardMatrix1(50964),

	/*
	 * DNG, page 59
	 * Application is described in detail in Chapter 6.
	 */
	ForwardMatrix2(50965),

	PreviewApplicationName(50966),
	PreviewApplicationVersion(50967),
	PreviewSettingsName(50968),

	// DNG, page 61
	PreviewSettingsDigest(50969),

	// DNG, page 61
	PreviewColorSpace(50970),

	PreviewDateTime(50971),
	RawImageDigest(50972),
	OriginalRawFileDigest(50973),
	SubTileBlockSize(50974),
	RowInterleaveFactor(50975),
	ProfileLookTableDims(50981),
	ProfileLookTableData(50982),
	OpcodeList1(51008),
	OpcodeList2(51009),
	OpcodeList3(51022),

	// DNG, page 67
	NoiseProfile(51041),

	DefaultUserCrop(51125),
	DefaultBlackRender(51110),
	BaselineExposureOffset(51109),
	ProfileLookTableEncoding(51108),
	ProfileHueSatMapEncoding(51107),
	OriginalDefaultFinalSize(51089),
	OriginalBestQualityFinalSize(51090),
	OriginalDefaultCropSize(51091),

	// DNG, page 76
	NewRawImageDigest(51111),

	RawToPreviewGain(51112);

	public final int number;

	Tag(int number) { this.number = number; }

}