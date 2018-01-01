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

package com.github.gasrios.raw.examples;

import com.github.gasrios.raw.Editor;
import com.github.gasrios.raw.Library;
import com.github.gasrios.raw.formats.ImageLSH;

/*
 * Turn image to black and white, preserving perceived luminance.
 */

public class BlackAndWhite implements Editor {

	@Override public void edit(ImageLSH image) {
		Library.display(Library.blackAndWhite(image));
	}

}