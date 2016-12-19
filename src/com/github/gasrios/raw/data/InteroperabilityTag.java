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

public final class InteroperabilityTag extends Tag {

	public InteroperabilityTag(String name, int number) { super(name, number); }

	public static final InteroperabilityTag[] values() {return values; }

	// Exif, page 83. See also page 32.
	public static final InteroperabilityTag InteroperabilityIndex = new InteroperabilityTag("InteroperabilityIndex", 1);

	/*
	 * Exif, page 32.
	 *
	 * No documentation referencing this tag was found yet but most people online call it "InteroperabilityVersion" and read
	 * its value as an array of ASCII chars (at least for Canon's .CR2, its type is UNDEFINED), so I am adopting the same
	 * convention.
	 */
	public static final InteroperabilityTag InteroperabilityVersion = new InteroperabilityTag("InteroperabilityVersion", 2);

	private static final InteroperabilityTag[] values = new InteroperabilityTag[] { InteroperabilityIndex, InteroperabilityVersion };

}