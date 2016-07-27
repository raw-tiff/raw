#!/bin/sh
java -Xmx2048m -cp ../bin:../lib/xmpcore-5.1.3.jar com.github.gasrios.raw.processor.LoadNonLinearHighResolutionImage $1
