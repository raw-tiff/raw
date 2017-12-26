# Introduction

This is not a production ready raw reader, just a personal project I started while studying digital image processing. If you need a complete, fully functional library, check [dcraw](https://www.cybercom.net/~dcoffin/dcraw/) out.

Currently the only format supported is uncompressed, linear (demosaiced) Adobe DNG. Reading TIFF metadata is also supported.

I decided to support DNG first because, unlike proprietary formats such as Canon's CR2 or Nikon's .NEF, DNG is open and has its specification publicly available. Also, virtually all widely used raw formats are TIFF-based, like DNG, so if you can read it, you are more than halfway done reading any raw file format.

You can use [Adobe Camera Raw and DNG Converter](https://www.adobe.com/support/downloads/product.jsp?product=106&platform=Windows) to convert your raw files to DNG.

I am looking for contributors willing to help me implement support for reading nonlinear compressed data. While uncompressed linear is specific to DNG, nonlinear compressed is the same format used by Canon and Nikon.

# Before you begin

Keep in mind TIFF is a decades old file format that has been receiving extensions for as long as has existed. It's full of idiosyncrasies and I strongly encourage you to read the following specifications before proceeding:

* TIFF Revision 6.0 Final - June 3, 1992
* TIFF Technical Note 1: TIFF Trees
* ISO 12234-2:2001, Electronic still-picture imaging – Removable memory – Part 2: TIFF/EP image data format
* [Digital Negative Specification Version 1.4.0.0](https://wwwimages2.adobe.com/content/dam/acom/en/products/photoshop/pdfs/dng_spec_1.4.0.0.pdf)

# Color spaces

The following color spaces are currently supported:

* [CIE 1931 XYZ](https://en.wikipedia.org/wiki/CIE_1931_color_space)
* [CIE 1976 (L*, u*, v*)](https://en.wikipedia.org/wiki/CIELUV)
* [CIE LCHuv](https://en.wikipedia.org/wiki/CIELUV#Cylindrical_representation_(CIELCH))
* [sRGB](https://en.wikipedia.org/wiki/SRGB)
* LSH (CIE LCHuv, with chroma C replaced with saturation S = C/L)

# Examples

See classes in package com.github.gasrios.raw.examples. Directory "sh" has scripts to run them from the command line in Unix based systems.

# The Code

At the highest level code is split in two parts: "Core" and "Non Core". "Core" code is additionally split into three main categories.

## Core

### Low level reader

Class `com.github.gasrios.raw.io.TiffInputStream`. It does two things:

1. Provides easy random reading access to content: TIFF headers are processed linearly but there is a lot of jumping around involved in extracting data from file bodies.
2. Provides methods that convert from TIFF primitive types to Java types at read time.

In short it encapsulates the direct handling of the file and provides higher level classes a more abstract view of the harsh reality of TIFF.

TiffInputStream extends `java.io.BufferedInputStream`, not `java.io.RandomAccessFile`. The `RandomAccessFile` based implementation was about four times slower and less than fifty lines of code were needed to emulate what was needed of its functionality.

### Intermediary level representation

Class `com.github.gasrios.raw.data.ImageFileDirectoryLoader`, which encapsulates all the logic used to extract TIFF info, and class `com.github.gasrios.raw.data.ImageFileDirectory`, which holds the information once it is loaded.

### Processor Engine

Class `com.github.gasrios.raw.processor.TiffProcessorEngine` uses `ImageFileDirectoryLoader` to load an `ImageFileDirectory`, then navigates its contents and calls the methods defined in `class com.github.gasrios.raw.processor.TiffProcessor`. You implement this interface (or extend the abstract class `com.github.gasrios.raw.processor.AbstractTiffProcessor`, which provides empty implementations of methods you do not care about) to define the actions you want performed.

### Understanding the core

	com.github.gasrios.raw.processor.TiffProcessorEngine
		uses com.github.gasrios.raw.processor.TiffProcessor
			implemented by com.github.gasrios.raw.processor.AbstractDngProcessor
		uses com.github.gasrios.raw.data.ImageFileDirectoryLoader
			uses com.github.gasrios.raw.data.Tag
			uses com.github.gasrios.raw.data.Type
			uses com.github.gasrios.raw.io.TiffInputStream
	com.github.gasrios.raw.lang.TiffNumber
		extended by com.github.gasrios.raw.lang.RATIONAL
		extended by com.github.gasrios.raw.lang.SRATIONAL

## Non Core

"Non core" is concerned with the useful stuff.

Class com.github.gasrios.raw.LoadHighResolutionImage provides a TiffProcessor that will load the image, then convert it to an agnostic format. Image editors will extend it, implement method end() and then consume the protected attribute "image".

## Understanding image editing

	com.github.gasrios.raw.LoadHighResolutionImage
		extends com.github.gasrios.raw.processor.AbstractDngProcessor
	com.github.gasrios.raw.lang.Math
		used by com.github.gasrios.raw.display.ImageSRGB
		used by com.github.gasrios.raw.LoadHighResolutionImage

# Copyright & License

### © 2016 Guilherme Rios All Rights Reserved

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.

# Dependencies And Additional Licenses

This software uses [Adobe XMP Toolkit for Java](http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html), licensed under the BSD license.

Copyright (c) 2009, Adobe Systems Incorporated All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

* Neither the name of Adobe Systems Incorporated, nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANT ABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.