-include config.mk

# Settings
PROGRAM ?= iBeaconNav
PACKAGE ?= edu.ucla.iBeaconNav
KEYFILE ?= ~/.android/android.p12
KEYTYPE ?= pkcs12
KEYNAME ?= android
ANDROID ?= /opt/android-sdk-update-manager/platforms/android-18/android.jar
SDKLIB  ?= /opt/android-sdk-update-manager/tools/lib/sdklib.jar
MAPLIB  ?= /opt/android-sdk-update-manager/extras/google/google_play_services/libproject/google-play-services_lib/libs/google-play-services.jar
TOOLS   ?= /opt/android-sdk-update-manager/build-tools/19.0.1

# Variables
PATH    := $(PATH):$(TOOLS)
DIR     := $(subst .,/,$(PACKAGE))
RES     := $(wildcard res/*/*.*)
SRC     := $(wildcard src/$(DIR)/*.java)
GEN     := gen/$(DIR)/R.java
OBJ     := obj/$(DIR)/R.class
APK     := java -classpath $(SDKLIB) \
                com.android.sdklib.build.ApkBuilderMain

# Targets
debug: bin/$(PROGRAM).dbg

release: bin/$(PROGRAM).apk

compile: $(OBJ)

clean:
	rm -rf bin gen obj

# ADB targets
logcat:
	adb logcat $(PROGRAM):D AndroidRuntime:E '*:S'

run: bin/install.stamp
	adb shell am start -W                 \
		-a android.intent.action.MAIN \
		-n $(PACKAGE)/.Main

install bin/install.stamp: bin/$(PROGRAM).apk
	adb install -r $+
	touch bin/install.stamp

uninstall:
	adb uninstall $(PACKAGE)
	rm -f bin/install.stamp

# Rules
%.dbg: %.dex %.res | bin
	@echo "APK    $@.in"
	@$(APK) $@.in -f $*.dex -z $*.res
	@echo "ALIGN  $@"
	@zipalign -f 4 $@.in $@

%.apk: %.dex %.res | bin
	@echo "APKU   $@.in"
	@$(APK) $@.in -u -f $*.dex -z $*.res
	@echo "SIGN   $@.in"
	@jarsigner -storetype $(KEYTYPE)  \
	           -keystore  $(KEYFILE)  \
	           $@.in      $(KEYNAME)
	@echo "ALIGN  $@"
	@zipalign -f 4 $@.in $@

%.dex: $(OBJ) makefile | bin
	@echo "DEX    $@ obj $(notdir $(MAPLIB))"
	@dx --dex --output $@ obj $(MAPLIB)

%.res: AndroidManifest.xml $(RES) | bin
	@echo "RES    $@"
	@aapt package -f -m               \
		--auto-add-overlay        \
		-I $(ANDROID)             \
		-M AndroidManifest.xml    \
		-S res                    \
		-S /opt/android-sdk-update-manager/extras/google/google_play_services/libproject/google-play-services_lib/res \
		-F $*.res

$(OBJ): $(SRC) $(GEN) makefile | obj
	@echo "JAVAC  obj/*.class $+"
	@JARS=$(ANDROID):$(MAPLIB);       \
	 javac -g                         \
		-Xlint:unchecked          \
		-Xlint:deprecation        \
		-bootclasspath $$JARS     \
		-encoding      UTF-8      \
		-source        1.5        \
		-target        1.5        \
		-classpath     obj        \
		-d             obj        \
		$(filter-out makefile,$+) gen/com/google/android/gms/R.java

$(GEN): AndroidManifest.xml $(RES) | gen
	@echo "GEN    $@"
	@aapt package -f -m               \
		-I $(ANDROID)             \
		-M /opt/android-sdk-update-manager/extras/google/google_play_services/libproject/google-play-services_lib/AndroidManifest.xml \
		-S /opt/android-sdk-update-manager/extras/google/google_play_services/libproject/google-play-services_lib/res \
		-J gen
	@aapt package -f -m               \
		--auto-add-overlay        \
		-I $(ANDROID)             \
		-M AndroidManifest.xml    \
		-S res                    \
		-S /opt/android-sdk-update-manager/extras/google/google_play_services/libproject/google-play-services_lib/res \
		-J gen

# Directories
bin gen obj:
	@mkdir -p $@

# Keep intermediate files
.SECONDARY:
