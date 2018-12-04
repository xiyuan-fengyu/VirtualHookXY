@echo off
adb exec-out run-as io.virtualhook mkdir /data/data/io.virtualhook/virtual/data/inject
adb push inject/build/outputs/apk/debug/inject-debug.apk /data/local/tmp/
adb exec-out run-as io.virtualhook cp /data/local/tmp/inject-debug.apk /data/data/io.virtualhook/virtual/data/inject/
