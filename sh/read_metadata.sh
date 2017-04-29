#!/bin/sh
for file do java -cp ../bin:../lib/xmpcore-5.1.3.jar com.github.gasrios.raw.ReadMetadata $file; echo "--"; done
