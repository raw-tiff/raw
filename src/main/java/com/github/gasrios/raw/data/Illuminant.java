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
 * Legal values for CalibrationIlluminant1 and CalibrationIlluminant2.
 *
 * See http://self.gutenberg.org/articles/standard_illuminant#White_points_of_standard_illuminants
 *
 * Whenever the mapping from EXIF to CIE was unclear we used UFRAW as reference, then chose the closest CIE illuminant with
 * similar correlated color temperature (CCT): "Fluorescent", "Tungsten", "Flash", "Fine weather", "Cloudy weather" and "Shade".
 * "Daylight" uses CIE E, the "equal energy" illuminant. "Fine weather" uses D55 and "ISO studio tungsten" uses CIE A.
 *
 * Value	Exif Name				CIE Name	 x			 y			CCT (K)	Note
 *     0	Unknown								-1			-1			  -1	Unknown
 *     1	Daylight				E			 0,33333	 0,33333	5454	Equal energy
 *     2	Fluorescent				F9			 0,37417	 0,37281	4150	Cool white deluxe fluorescent
 *     3	Tungsten				A			 0,44757	 0,40745	2856	Incandescent
 *     4	Flash					D65			 0,31271	 0,32902	6504	Noon daylight: television, sRGB color space
 *     9	Fine weather			D55			 0,33242	 0,34743	5503	Mid-morning / mid-afternoon daylight
 *    10	Cloudy weather			F5			 0,31379	 0,34531	6350	Daylight fluorescent
 *    11	Shade					D75			 0,29902	 0,31485	7504	North sky daylight
 *    12	Daylight fluorescent	F1			 0,31310	 0,33727	6430	Daylight fluorescent
 *    13	Day white fluorescent	F5			 0,31379	 0,34531	6350	Daylight fluorescent
 *    14	Cool white fluorescent	F2			 0,37208	 0,37529	4230	Cool white fluorescent
 *    15	White fluorescent		F3			 0,40910	 0,39430	3450	White fluorescent
 *    16	Warm white fluorescent	F4			 0,44018	 0,40329	2940	Warm white fluorescent
 *    17	A						A			 0,44757	 0,40745	2856	Incandescent
 *    18	B						B			 0,34842	 0,35161	4874	Direct sunlight at noon (obsolete)
 *    19	C						C			 0,31006	 0,31616	6774	Average / north sky daylight (obsolete)
 *    20	D55						D55			 0,33242	 0,34743	5503	Mid-morning / mid-afternoon daylight
 *    21	D65						D65			 0,31271	 0,32902	6504	Noon daylight: television, sRGB color space
 *    22	D75						D75			 0,29902	 0,31485	7504	North sky daylight
 *    23	D50						D50			 0,34567	 0,35850	5003	Horizon light. ICC profile PCS
 *    24	ISO studio tungsten		A			 0,44757	 0,40745	2856	Incandescent
 *   255	Other light source					-1			-1			  -1	Other light source
 *    -1	Reserved							-1			-1			  -1	Reserved
 */

public enum Illuminant {

	Unknown(0, -1),
	Daylight(1, 5454),
	Fluorescent(2, 4150),
	Tungsten(3, 2856),
	Flash(4, 6504),
	FineWeather(9, 5503),
	CloudyWeather(10, 6350),
	Shade(11, 7504),
	DaylightFluorescent(12, 6430),
	DayWhiteFluorescent(13, 6350),
	CoolWhiteFluorescent(14, 4230),
	WhiteFluorescent(15, 3450),
	WarmWhiteFluorescent(16, 2940),
	A(17, 2856),
	B(18, 4874),
	C(19, 6774),
	D55(20, 5503),
	D65(21, 6504),
	D75(22, 7504),
	D50(23, 5003),
	ISOStudioTungsten(24, 2856),
	OtherLightSource(255, -1),
	Reserved(-1, -1);

	public final int value;
	public final double cct;

	Illuminant(int value, double cct) {
		this.value = value;
		this.cct = cct;
	}

}