@echo off
adb shell mkdir /data/data/io.virtualhook/virtual/data/inject
adb push inject/build/outputs/apk/debug/inject-debug.apk /data/local/tmp/
adb shell mv /data/local/tmp/inject-debug.apk /data/data/io.virtualhook/virtual/data/inject/
