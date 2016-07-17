** Introduction **

This is not a production ready raw reader, just a personal project I started while studying digital image processing. If you need a complete, fully functional library, check dcraw out: https://www.cybercom.net/~dcoffin/dcraw/

Currently the only format supported is uncompressed, linear (demosaiced) Adobe DNG. You can use Adobe Camera Raw and DNG Converter to convert your raw files to DNG: https://www.adobe.com/support/downloads/product.jsp?product=106&platform=Windows

I decided to support DNG first because, unlike proprietary formats such as Canon's CR2 or Nikon's .NEF, DNG is open and has its specification publicly available. Also, virtually all widely used raw formats are TIFF-based, like DNG, so if you can read it, you are more than halfway done reading any raw file format.

I am looking for contributors willing to help me implement support for reading nonlinear compressed data. While uncompressed linear is specific to DNG, nonlinear compressed is the same format used by Canon and Nikon.

** Before you begin **

Keep in mind DNG is an evolution of TIFF, a decades old file format that has been receiving extensions for as long as it exists.
It's full of idiosyncrasies and I strongly encourage you to read the following specifications before proceeding:

TIFF Revision 6.0 Final - June 3, 1992
TIFF Technical Note 1: TIFF Trees
ISO 12234-2:2001, Electronic still-picture imaging – Removable memory – Part 2: TIFF/EP image data format
Digital Negative Specification Version 1.4.0.0

** The Code **

At the highest level code is split in two parts: "Core" and "Non Core". "Core" code is additionally split into three main
categories.

** Core **

1. DNG low level reader (com.github.gasrios.raw.io.DngInputStream)

DngInputStream does two things:

 - Provides easy random reading access to content: DNG headers are processed linearly but there is a lot of jumping around
   involved in extracting data from file bodies.

 - Provides methods that convert from TIFF primitive types to Java types at read time.

In short it encapsulates the direct handling of the file and provides higher level classes a more abstract view of the harsh
reality of TIFF.

DngInputStream extends java.io.BufferedInputStream, not java.io.RandomAccessFile. The RandomAccessFile based implementation
was about four times slower and less than fifty lines of code were needed to emulate what was needed of its functionality.

2. Intermediary level representation (classes com.github.gasrios.raw.data.ImageFileDirectoryLoader and com.github.gasrios.raw.data.ImageFileDirectory)

ImageFileDirectoryLoader encapsulates all the logic used to extract DNG info from file while  ImageFileDirectory holds all
information once it is loaded.

3. Processor Engine (com.github.gasrios.raw.processor.DngProcessorEngine and com.github.gasrios.raw.processor.DngProcessor)

DngProcessorEngine uses ImageFileDirectoryLoader to load a ImageFileDirectory then navigates its contents and calls the methods
defined in DngProcessor. You implement this interface (or extend the abstract class com.github.gasrios.raw.processor.AbstractDngProcessor
which provides empty implementations of methods you do not care about) to define the actions you want performed.

** Non Core **

"Non core" is concerned with the cool stuff: class com.github.gasrios.raw.LoadHighResolutionImage provides a DngProcessor that will load
the image and its metadata then convert it to an agnostic format. Image editors will extend it, implement method end() and then
consume protected attributes "imageData" and "image".

If you need help navigating the code, check below.

** Understanding the core **

com.github.gasrios.raw.processor.DngProcessorEngine
	uses com.github.gasrios.raw.processor.DngProcessor
		implemented by com.github.gasrios.raw.processor.AbstractDngProcessor
	uses com.github.gasrios.raw.data.ImageFileDirectoryLoader
		uses com.github.gasrios.raw.data.Tag
		uses com.github.gasrios.raw.data.Type
		uses com.github.gasrios.raw.io.DngInputStream

com.github.gasrios.raw.lang.DngNumber
	extended by com.github.gasrios.raw.lang.RATIONAL
	extended by com.github.gasrios.raw.lang.SRATIONAL

** Understanding image editing **

com.github.gasrios.raw.LoadHighResolutionImage
	extends com.github.gasrios.raw.processor.AbstractDngProcessor

com.github.gasrios.raw.lang.Math
	used by com.github.gasrios.raw.display.ImageSRGB
	used by com.github.gasrios.raw.LoadHighResolutionImage

** Dependencies And Additional Licenses **

This software uses Adobe XMP Toolkit for Java, licensed under the BSD license. The original source can be downloaded here: http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

Copyright (c) 2009, Adobe Systems Incorporated  All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

* Neither the name of Adobe Systems Incorporated, nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANT ABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.