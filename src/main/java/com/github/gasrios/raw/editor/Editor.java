/*
 * © 2018 Guilherme Rios All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package com.github.gasrios.raw.editor;

import com.github.gasrios.raw.formats.ImageLSH;

/*
 * A simples interface called from com.github.gasrios.raw.editor.CommandLineEditorInvoker. The point is to make image editing as simple as possible.
 */

public interface Editor {

	void edit(ImageLSH image);

}