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

import java.io.FileInputStream;
import java.io.IOException;

import com.github.gasrios.raw.formats.ImageLSH;
import com.github.gasrios.raw.lang.TiffProcessorException;
import com.github.gasrios.raw.processor.DngProcessor;
import com.github.gasrios.raw.processor.TiffProcessorEngine;
import com.github.gasrios.xmp.XMPException;

/*
 * DngProcessor subclass that provides an easy to use command line caller for image editors.
 *
 * Usage: java com.github.gasrios.raw.CommandLineEditorInvoker <AnEditor implements com.github.gasrios.raw.editor.Editor> image.dng
 */

public class CommandLineEditorInvoker extends DngProcessor<ImageLSH> {

	public static void main(String[] args) throws IOException, ReflectiveOperationException, TiffProcessorException, XMPException {
		new TiffProcessorEngine(new FileInputStream(args[1]), new CommandLineEditorInvoker((Editor) Class.forName(args[0]).newInstance())).run();
	}

	private Editor editor;

	public CommandLineEditorInvoker(Editor editor) {
		super(new ImageLSH());
		this.editor = editor;
	}

	@Override public void end() throws TiffProcessorException { editor.edit(image); }

}