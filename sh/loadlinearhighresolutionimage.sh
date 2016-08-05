#!/bin/sh
java -Xmx4096m -cp ../bin:../lib/xmpcore-5.1.3.jar com.github.gasrios.raw.processor.LoadHighResolutionImage $1
