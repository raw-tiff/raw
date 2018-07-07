#!/bin/sh
./gradlew clean final bintrayUpload --info -PbintrayApiKey=$1
