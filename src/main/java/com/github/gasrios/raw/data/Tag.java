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
 * This used to be an enum until the moment I found out the numeric identifiers were not unique for all scopes. For example,
 * 1 can be the "InteroperabilityIndex" of an Interoperability IFD, or "GPSLatitudeRef", inside a GPS IFD.
 *
 * By then the simplest way to account for that was:
 *
 * 1. Change Tag from enum to class (as enum cannot be extended);
 * 2. Extend Tag to create a separated scope for each anomalous case;
 * 3. Make the new class Tag behave like an enum, so the existing code would not break.
 *
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

public class Tag implements Comparable<Tag> {

	public final int number;

	private final String name;

	public Tag(String name, int number) {
		this.name = name;
		this.number = number;
	}

	@Override public final int compareTo(Tag tag) { return number - tag.number; }

	@Override public final String toString() { return name; }

	public static Tag[] values() { return values; }

	// DNG, page 18
	public static final Tag	NewSubFileType = new Tag("NewSubFileType", 254);

	// TIFF, page 18
	public static final Tag	ImageWidth = new Tag("ImageWidth", 256);

	// TIFF, page 18
	public static final Tag	ImageLength = new Tag("ImageLength", 257);

	// TIFF, page 29
	public static final Tag	BitsPerSample = new Tag("BitsPerSample", 258);

	// DNG, page 19
	public static final Tag	Compression = new Tag("Compression", 259);

	// DNG, page 20
	public static final Tag	PhotometricInterpretation = new Tag("PhotometricInterpretation", 262);

	public static final Tag	ImageDescription = new Tag("ImageDescription", 270);
	public static final Tag	Make = new Tag("Make", 271);
	public static final Tag	Model = new Tag("Model", 272);

	// TIFF, page 19
	public static final Tag	StripOffsets = new Tag("StripOffsets", 273);

	/*
	 * TIFF, page 36
	 * TIFF/EP, page 23
	 * DNG, page 20
	 */
	public static final Tag	Orientation = new Tag("Orientation", 274);

	// TIFF, page 39
	public static final Tag	SamplesPerPixel = new Tag("SamplesPerPixel", 277);

	// TIFF, page 19
	public static final Tag	RowsPerStrip = new Tag("RowsPerStrip", 278);

	// TIFF, page 19
	public static final Tag	StripByteCounts = new Tag("StripByteCounts", 279);

	public static final Tag	XResolution = new Tag("XResolution", 282);
	public static final Tag	YResolution = new Tag("YResolution", 283);

	// TIFF, page 19
	public static final Tag	PlanarConfiguration = new Tag("PlanarConfiguration", 284);

	public static final Tag	ResolutionUnit = new Tag("ResolutionUnit", 296);
	public static final Tag	Software = new Tag("Software", 305);
	public static final Tag	DateTime = new Tag("DateTime", 306);
	public static final Tag	Artist = new Tag("Artist", 315);

	// TIFF, page 67
	public static final Tag	TileWidth = new Tag("TileWidth", 322);

	// TIFF, page 67
	public static final Tag	TileLength = new Tag("TileLength", 323);

	// TIFF, page 68
	public static final Tag	TileOffsets = new Tag("TileOffsets", 324);

	// TIFF, page 68
	public static final Tag	TileByteCounts = new Tag("TileByteCounts", 325);

	public static final Tag	SubIFDs = new Tag("SubIFDs", 330);
	public static final Tag	JPEGTables = new Tag("JPEGTables", 347);

	/*
	 * TIFF, page 105
	 *
	 * These fields are used in Canon's .CR2 files, *HOWEVER*...
	 *
	 * http://www.awaresystems.be/imaging/tiff/tifftags/jpeginterchangeformat.html
	 *
	 * Old-style JPEG compression field. TechNote2 invalidates this part of the specification
	 *
	 * No new TIFF writer code should ever attempt to use this tag. It is part of an invalidated compression scheme, old-style
	 * JPEG, that was always unclear to begin with, and next enjoyed many mutualy exclusive implementations. The following
	 * description is for TIFF reading purposes only. 
	 */
	public static final Tag	JPEGInterchangeFormat = new Tag("JPEGInterchangeFormat", 513);
	public static final Tag	JPEGInterchangeFormatLength = new Tag("JPEGInterchangeFormatLength", 514);

	public static final Tag	YCbCrCoefficients = new Tag("YCbCrCoefficients", 529);
	public static final Tag	YCbCrSubSampling = new Tag("YCbCrSubSampling", 530);
	public static final Tag	YcbCrPositioning = new Tag("YcbCrPositioning", 531);
	public static final Tag	ReferenceBlackWhite = new Tag("ReferenceBlackWhite", 532);

	// DNG, page 14
	public static final Tag	XMP = new Tag("XMP", 700);

	// TIFF/EP, page 26
	public static final Tag	CFARepeatPatternDim = new Tag("CFARepeatPatternDim", 33421);

	// TIFF/EP, page 26
	public static final Tag	CFAPattern = new Tag("CFAPattern", 33422);

	public static final Tag	BatteryLevel = new Tag("BatteryLevel", 33423);
	public static final Tag	Copyright = new Tag("Copyright", 33432);
	public static final Tag	ExposureTime = new Tag("ExposureTime", 33434);
	public static final Tag	FNumber = new Tag("FNumber", 33437);
	public static final Tag	IPTC_NAA = new Tag("IPTC_NAA", 33723);

	// DNG, page 14
	public static final Tag	ExifIFD = new Tag("ExifIFD", 34665);

	/*
	 * TIFF/EP, page 48
	 * ICC, page 58
	 */
	public static final Tag	InterColorProfile = new Tag("InterColorProfile", 34675);

	public static final Tag	ExposureProgram = new Tag("ExposureProgram", 34850);
	public static final Tag	SpectralSensitivity = new Tag("SpectralSensitivity", 34852);
	public static final Tag	GPSInfo = new Tag("GPSInfo", 34853);
	public static final Tag	ISOSpeedRatings = new Tag("ISOSpeedRatings", 34855);
	public static final Tag	OECF = new Tag("OECF", 34856);
	public static final Tag	Interlace = new Tag("Interlace", 34857);
	public static final Tag	TimeZoneOffset = new Tag("TimeZoneOffset", 34858);
	public static final Tag	SelfTimerMode = new Tag("SelfTimerMode", 34859);
	public static final Tag	SensitivityType = new Tag("SensitivityType", 34864);
	public static final Tag	RecommendedExposureIndex = new Tag("RecommendedExposureIndex", 34866);

	// Exif, page 49
	public static final Tag	ExifVersion = new Tag("ExifVersion", 36864);

	public static final Tag	DateTimeOriginal = new Tag("DateTimeOriginal", 36867);
	public static final Tag	DateTimeDigitized = new Tag("DateTimeDigitized", 36868);

	// Exif, page 50
	public static final Tag	ComponentsConfiguration = new Tag("ComponentsConfiguration", 37121);

	public static final Tag	CompressedBitsPerPixel = new Tag("CompressedBitsPerPixel", 37122);
	public static final Tag	ShutterSpeedValue = new Tag("ShutterSpeedValue", 37377);
	public static final Tag	ApertureValue = new Tag("ApertureValue", 37378);
	public static final Tag	BrightnessValue = new Tag("BrightnessValue", 37379);
	public static final Tag	ExposureBiasValue = new Tag("ExposureBiasValue", 37380);
	public static final Tag	MaxApertureValue = new Tag("MaxApertureValue", 37381);
	public static final Tag	SubjectDistance = new Tag("SubjectDistance", 37382);
	public static final Tag	MeteringMode = new Tag("MeteringMode", 37383);
	public static final Tag	LightSource = new Tag("LightSource", 37384);
	public static final Tag	Flash = new Tag("Flash", 37385);
	public static final Tag	FocalLength = new Tag("FocalLength", 37386);
	public static final Tag	FlashEnergy = new Tag("FlashEnergy", 37387);
	public static final Tag	SpatialFrequencyResponse = new Tag("SpatialFrequencyResponse", 37388);
	public static final Tag	Noise = new Tag("Noise", 37389);
	public static final Tag	FocalPlaneXResolution = new Tag("FocalPlaneXResolution", 37390);
	public static final Tag	FocalPlaneYResolution = new Tag("FocalPlaneYResolution", 37391);
	public static final Tag	FocalPlaneResolutionUnit = new Tag("FocalPlaneResolutionUnit", 37392);
	public static final Tag	ImageNumber = new Tag("ImageNumber", 37393);
	public static final Tag	SecurityClassification = new Tag("SecurityClassification", 37394);
	public static final Tag	ImageHistory = new Tag("ImageHistory", 37395);
	public static final Tag	SubjectLocation = new Tag("SubjectLocation", 37396);
	public static final Tag	ExposureIndex = new Tag("ExposureIndex", 37397);
	public static final Tag	TIFF_EPStandardID = new Tag("TIFF_EPStandardID", 37398);
	public static final Tag	SensingMethod = new Tag("SensingMethod", 37399);

	// Exif, page 51
	public static final Tag	MakerNote = new Tag("MakerNote", 37500);
	public static final Tag	UserComment = new Tag("UserComment", 37510);

	// Exif, page 54
	public static final Tag	SubSecTime = new Tag("SubSecTime", 37520);

	public static final Tag	SubsecTimeOriginal = new Tag("SubsecTimeOriginal", 37521);
	public static final Tag	SubsecTimeDigitized = new Tag("SubsecTimeDigitized", 37522);

	// Exif, page 49
	public static final Tag	FlashPixVersion = new Tag("FlashPixVersion", 40960);

	public static final Tag	ColorSpace = new Tag("ColorSpace", 40961);

	// Exif, page 50
	public static final Tag	PixelXDimension = new Tag("PixelXDimension", 40962);
	public static final Tag	PixelYDimension = new Tag("PixelYDimension", 40963);

	// Exif, page 32
	public static final Tag	Interoperability = new Tag("Interoperability", 40965);

	// The Exif tags below have the same name as previously declared TIFF/EP tags. Added "Exif" to name start to avoid conflict.
	public static final Tag	ExifFocalPlaneXResolution = new Tag("ExifFocalPlaneXResolution", 41486);
	public static final Tag	ExifFocalPlaneYResolution = new Tag("ExifFocalPlaneYResolution", 41487);
	public static final Tag	ExifFocalPlaneResolutionUnit = new Tag("ExifFocalPlaneResolutionUnit", 41488);

	public static final Tag	CustomRendered = new Tag("CustomRendered", 41985);
	public static final Tag	ExposureMode = new Tag("ExposureMode", 41986);
	public static final Tag	WhiteBalance = new Tag("WhiteBalance", 41987);
	public static final Tag	SceneCaptureType = new Tag("SceneCaptureType", 41990);
	public static final Tag	BodySerialNumber = new Tag("BodySerialNumber", 42033);
	public static final Tag	LensSpecification = new Tag("LensSpecification", 42034);
	public static final Tag	LensMake = new Tag("LensMake", 42035);
	public static final Tag	LensModel = new Tag("LensModel", 42036);
	public static final Tag	LensSerialNumber = new Tag("LensSerialNumber", 42037);

	// DNG, page 22
	public static final Tag	DNGVersion = new Tag("DNGVersion", 50706);

	// DNG, page 22
	public static final Tag	DNGBackwardVersion = new Tag("DNGBackwardVersion", 50707);

	// DNG, page 23
	public static final Tag	UniqueCameraModel = new Tag("UniqueCameraModel", 50708);

	public static final Tag	LocalizedCameraModel = new Tag("LocalizedCameraModel", 50709);

	// DNG, page 24
	public static final Tag	CFAPlaneColor = new Tag("CFAPlaneColor", 50710);

	// DNG, page 25
	public static final Tag	CFALayout = new Tag("CFALayout", 50711);

	public static final Tag	LinearizationTable = new Tag("LinearizationTable", 50712);

	/*
	 * DNG, page 26
	 * See chapter 5, “Mapping Raw Values to Linear Reference Values” on page 77 for details of the processing model.
	 */
	public static final Tag	BlackLevelRepeatDim = new Tag("BlackLevelRepeatDim", 50713);

	/*
	 * DNG, page 27
	 * See chapter 5, “Mapping Raw Values to Linear Reference Values” on page 77 for details of the processing model.
	 */
	public static final Tag	BlackLevel = new Tag("BlackLevel", 50714);

	public static final Tag	BlackLevelDeltaH = new Tag("BlackLevelDeltaH", 50715);
	public static final Tag	BlackLevelDeltaV = new Tag("BlackLevelDeltaV", 50716);

	/*
	 * DNG, page 29
	 * See chapter 5, “Mapping Raw Values to Linear Reference Values” on page 77 for details of the processing model.
	 */
	public static final Tag	WhiteLevel = new Tag("WhiteLevel", 50717);

	// DNG, page 29
	public static final Tag	DefaultScale = new Tag("DefaultScale", 50718);

	// DNG, page 30
	public static final Tag	BestQualityScale = new Tag("BestQualityScale", 50780);

	// DNG, page 30
	public static final Tag	DefaultCropOrigin = new Tag("DefaultCropOrigin", 50719);

	/*
	 * DNG, page 31
	 * http://www.barrypearson.co.uk/articles/dng/specification.htm
	 */
	public static final Tag	DefaultCropSize = new Tag("DefaultCropSize", 50720);

	/*
	 * DNG, page 31
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 * Exif, page 55
	 */
	public static final Tag	CalibrationIlluminant1 = new Tag("CalibrationIlluminant1", 50778);

	/*
	 * DNG, page 32
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 * Exif, page 55
	 */
	public static final Tag	CalibrationIlluminant2 = new Tag("CalibrationIlluminant2", 50779);

	/*
	 * DNG, page 32
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	ColorMatrix1 = new Tag("ColorMatrix1", 50721);

	/*
	 * DNG, page 33
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	ColorMatrix2 = new Tag("ColorMatrix2", 50722);

	/*
	 * DNG, page 34
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	CameraCalibration1 = new Tag("CameraCalibration1", 50723);

	/*
	 * DNG, page 34
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	CameraCalibration2 = new Tag("CameraCalibration2", 50724);

	public static final Tag	ReductionMatrix1 = new Tag("ReductionMatrix1", 50725);
	public static final Tag	ReductionMatrix2 = new Tag("ReductionMatrix2", 50726);

	/*
	 * DNG, page 36
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	AnalogBalance = new Tag("AnalogBalance", 50727);

	/*
	 * DNG, page 37
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	AsShotNeutral = new Tag("AsShotNeutral", 50728);

	/*
	 * DNG, page 37
	 * See chapter 6, “Mapping Camera Color Space to CIE XYZ Space” on page 79 for details of the color-processing model.
	 */
	public static final Tag	AsShotWhiteXY = new Tag("AsShotWhiteXY", 50729);

	// DNG, page 38
	public static final Tag	BaselineExposure = new Tag("BaselineExposure", 50730);

	// DNG, page 38
	public static final Tag	BaselineNoise = new Tag("BaselineNoise", 50731);

	// DNG, page 39
	public static final Tag	BaselineSharpness = new Tag("BaselineSharpness", 50732);

	public static final Tag	BayerGreenSplit = new Tag("BayerGreenSplit", 50733);

	// DNG, page 40
	public static final Tag	LinearResponseLimit = new Tag("LinearResponseLimit", 50734);

	public static final Tag	CameraSerialNumber = new Tag("CameraSerialNumber", 50735);
	public static final Tag	LensInfo = new Tag("LensInfo", 50736);
	public static final Tag	ChromaBlurRadius = new Tag("ChromaBlurRadius", 50737);

	// DNG, page 42
	public static final Tag	AntiAliasStrength = new Tag("AntiAliasStrength", 50738);

	// DNG, page 42
	public static final Tag	ShadowScale = new Tag("ShadowScale", 50739);

	// DNG, page 43
	public static final Tag	DNGPrivateData = new Tag("DNGPrivateData", 50740);

	public static final Tag	MakerNoteSafety = new Tag("MakerNoteSafety", 50741);

	// DNG, page 45
	public static final Tag	RawDataUniqueID = new Tag("RawDataUniqueID", 50781);

	public static final Tag	OriginalRawFileName = new Tag("OriginalRawFileName", 50827);
	public static final Tag	OriginalRawFileData = new Tag("OriginalRawFileData", 50828);

	/*
	 * DNG, page 47
	 * http://www.barrypearson.co.uk/articles/dng/specification.htm
	 */
	public static final Tag	ActiveArea = new Tag("ActiveArea", 50829);

	public static final Tag	MaskedAreas = new Tag("MaskedAreas", 50830);
	public static final Tag	AsShotICCProfile = new Tag("AsShotICCProfile", 50831);
	public static final Tag	AsShotPreProfileMatrix = new Tag("AsShotPreProfileMatrix", 50832);
	public static final Tag	CurrentICCProfile = new Tag("CurrentICCProfile", 50833);
	public static final Tag	CurrentPreProfileMatrix = new Tag("CurrentPreProfileMatrix", 50834);
	public static final Tag	ColorimetricReference = new Tag("ColorimetricReference", 50879);
	public static final Tag	CameraCalibrationSignature = new Tag("CameraCalibrationSignature", 50931);
	public static final Tag	ProfileCalibrationSignature = new Tag("ProfileCalibrationSignature", 50932);
	public static final Tag	ExtraCameraProfiles = new Tag("ExtraCameraProfiles", 50933);
	public static final Tag	AsShotProfileName = new Tag("AsShotProfileName", 50934);
	public static final Tag	NoiseReductionApplied = new Tag("NoiseReductionApplied", 50935);
	public static final Tag	ProfileName = new Tag("ProfileName", 50936);
	public static final Tag	ProfileHueSatMapDims = new Tag("ProfileHueSatMapDims", 50937);
	public static final Tag	ProfileHueSatMapData1 = new Tag("ProfileHueSatMapData1", 50938);
	public static final Tag	ProfileHueSatMapData2 = new Tag("ProfileHueSatMapData2", 50939);
	public static final Tag	ProfileToneCurve = new Tag("ProfileToneCurve", 50940);
	public static final Tag	ProfileEmbedPolicy = new Tag("ProfileEmbedPolicy", 50941);
	public static final Tag	ProfileCopyright = new Tag("ProfileCopyright", 50942);

	/*
	 * DNG, page 58
	 * Application is described in detail in Chapter 6.
	 */
	public static final Tag	ForwardMatrix1 = new Tag("ForwardMatrix1", 50964);

	/*
	 * DNG, page 59
	 * Application is described in detail in Chapter 6.
	 */
	public static final Tag	ForwardMatrix2 = new Tag("ForwardMatrix2", 50965);

	public static final Tag	PreviewApplicationName = new Tag("PreviewApplicationName", 50966);
	public static final Tag	PreviewApplicationVersion = new Tag("PreviewApplicationVersion", 50967);
	public static final Tag	PreviewSettingsName = new Tag("PreviewSettingsName", 50968);

	// DNG, page 61
	public static final Tag	PreviewSettingsDigest = new Tag("PreviewSettingsDigest", 50969);

	// DNG, page 61
	public static final Tag	PreviewColorSpace = new Tag("PreviewColorSpace", 50970);

	public static final Tag	PreviewDateTime = new Tag("PreviewDateTime", 50971);
	public static final Tag	RawImageDigest = new Tag("RawImageDigest", 50972);
	public static final Tag	OriginalRawFileDigest = new Tag("OriginalRawFileDigest", 50973);
	public static final Tag	SubTileBlockSize = new Tag("SubTileBlockSize", 50974);
	public static final Tag	RowInterleaveFactor = new Tag("RowInterleaveFactor", 50975);
	public static final Tag	ProfileLookTableDims = new Tag("ProfileLookTableDims", 50981);
	public static final Tag	ProfileLookTableData = new Tag("ProfileLookTableData", 50982);
	public static final Tag	OpcodeList1 = new Tag("OpcodeList1", 51008);
	public static final Tag	OpcodeList2 = new Tag("OpcodeList2", 51009);
	public static final Tag	OpcodeList3 = new Tag("OpcodeList3", 51022);

	// DNG, page 67
	public static final Tag	NoiseProfile = new Tag("NoiseProfile", 51041);

	public static final Tag	DefaultUserCrop = new Tag("DefaultUserCrop", 51125);
	public static final Tag	DefaultBlackRender = new Tag("DefaultBlackRender", 51110);
	public static final Tag	BaselineExposureOffset = new Tag("BaselineExposureOffset", 51109);
	public static final Tag	ProfileLookTableEncoding = new Tag("ProfileLookTableEncoding", 51108);
	public static final Tag	ProfileHueSatMapEncoding = new Tag("ProfileHueSatMapEncoding", 51107);
	public static final Tag	OriginalDefaultFinalSize = new Tag("OriginalDefaultFinalSize", 51089);
	public static final Tag	OriginalBestQualityFinalSize = new Tag("OriginalBestQualityFinalSize", 51090);
	public static final Tag	OriginalDefaultCropSize = new Tag("OriginalDefaultCropSize", 51091);

	// DNG, page 76
	public static final Tag	NewRawImageDigest = new Tag("NewRawImageDigest", 51111);

	public static final Tag	RawToPreviewGain = new Tag("RawToPreviewGain", 51112);

	private static final Tag[] values = new Tag[] {
		NewSubFileType, ImageWidth, ImageLength, BitsPerSample, Compression, PhotometricInterpretation, ImageDescription,
		Make, Model, StripOffsets, Orientation, SamplesPerPixel, RowsPerStrip, StripByteCounts, XResolution, YResolution,
		PlanarConfiguration, ResolutionUnit, Software, DateTime, Artist, TileWidth, TileLength, TileOffsets, TileByteCounts,
		SubIFDs, JPEGTables, JPEGInterchangeFormat, JPEGInterchangeFormatLength, YCbCrCoefficients, YCbCrSubSampling,
		YcbCrPositioning, ReferenceBlackWhite, XMP, CFARepeatPatternDim, CFAPattern, BatteryLevel, Copyright, ExposureTime,
		FNumber, IPTC_NAA, ExifIFD, InterColorProfile, ExposureProgram, SpectralSensitivity, GPSInfo, ISOSpeedRatings, OECF,
		Interlace, TimeZoneOffset, SelfTimerMode, SensitivityType, RecommendedExposureIndex, ExifVersion, DateTimeOriginal,
		DateTimeDigitized, ComponentsConfiguration, CompressedBitsPerPixel, ShutterSpeedValue, ApertureValue, BrightnessValue,
		ExposureBiasValue, MaxApertureValue, SubjectDistance, MeteringMode, LightSource, Flash, FocalLength, FlashEnergy,
		SpatialFrequencyResponse, Noise, FocalPlaneXResolution, FocalPlaneYResolution, FocalPlaneResolutionUnit, ImageNumber,
		SecurityClassification, ImageHistory, SubjectLocation, ExposureIndex, TIFF_EPStandardID, SensingMethod, MakerNote,
		UserComment, SubSecTime, SubsecTimeOriginal, SubsecTimeDigitized, FlashPixVersion, ColorSpace, PixelXDimension,
		PixelYDimension, Interoperability, ExifFocalPlaneXResolution, ExifFocalPlaneYResolution, ExifFocalPlaneResolutionUnit,
		CustomRendered, ExposureMode, WhiteBalance, SceneCaptureType, BodySerialNumber, LensSpecification, LensMake, LensModel,
		LensSerialNumber, DNGVersion, DNGBackwardVersion, UniqueCameraModel, LocalizedCameraModel, CFAPlaneColor, CFALayout,
		LinearizationTable, BlackLevelRepeatDim, BlackLevel, BlackLevelDeltaH, BlackLevelDeltaV, WhiteLevel, DefaultScale,
		DefaultCropOrigin, DefaultCropSize, ColorMatrix1, ColorMatrix2, CameraCalibration1, CameraCalibration2, ReductionMatrix1,
		ReductionMatrix2, AnalogBalance, AsShotNeutral, AsShotWhiteXY, BaselineExposure, BaselineNoise, BaselineSharpness,
		BayerGreenSplit, LinearResponseLimit, CameraSerialNumber, LensInfo, ChromaBlurRadius, AntiAliasStrength, ShadowScale,
		DNGPrivateData, MakerNoteSafety, CalibrationIlluminant1, CalibrationIlluminant2, BestQualityScale, RawDataUniqueID,
		OriginalRawFileName, OriginalRawFileData, ActiveArea, MaskedAreas, AsShotICCProfile, AsShotPreProfileMatrix,
		CurrentICCProfile, CurrentPreProfileMatrix, ColorimetricReference, CameraCalibrationSignature, ProfileCalibrationSignature,
		ExtraCameraProfiles, AsShotProfileName, NoiseReductionApplied, ProfileName, ProfileHueSatMapDims, ProfileHueSatMapData1,
		ProfileHueSatMapData2, ProfileToneCurve, ProfileEmbedPolicy, ProfileCopyright, ForwardMatrix1, ForwardMatrix2,
		PreviewApplicationName, PreviewApplicationVersion, PreviewSettingsName, PreviewSettingsDigest, PreviewColorSpace,
		PreviewDateTime, RawImageDigest, OriginalRawFileDigest, SubTileBlockSize, RowInterleaveFactor, ProfileLookTableDims,
		ProfileLookTableData, OpcodeList1, OpcodeList2, OpcodeList3, NoiseProfile, OriginalDefaultFinalSize,
		OriginalBestQualityFinalSize, OriginalDefaultCropSize, ProfileHueSatMapEncoding, ProfileLookTableEncoding,
		BaselineExposureOffset, DefaultBlackRender, NewRawImageDigest, RawToPreviewGain, DefaultUserCrop
	};

}