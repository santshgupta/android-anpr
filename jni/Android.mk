#############################################################
#															#
# JNI Config to work with native and NEON functions 		#
#															#
# @Andrey AnZ Zhdanov 										#
# 2011														#
# 															#
#############################################################
#APP_OPTIM := release

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# Module classes
LOCAL_MODULE    := com_graphics_NativeGraphics
LOCAL_SRC_FILES := com_graphics_NativeGraphics.cpp
LOCAL_SRC_FILES += graphics_core.cpp
LOCAL_SRC_FILES += HoughTransformation.cpp
#arm neon support
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)   
	LOCAL_ARM_NEON := true
	# For using NEON we needed declare to few options:                
	#LOCAL_CFLAGS    += -march=armv7-a -mfloat-abi=softfp -mfpu=neon                                                                                                                                                                                                                      
endif    

# declare cpufeatures
LOCAL_STATIC_LIBRARIES := cpufeatures

# We are used logger, graphics and other librarys 
LOCAL_LDLIBS    := -lm -llog -ljnigraphics

# debug options
LOCAL_LDFLAGS   := -Wl,-Map,xxx.map
LOCAL_CFLAGS    := -g

# We have using new C syntaxis
#LOCAL_CFLAGS    += -std=c99

# -----  ---- only for emulator! ---- ----- -
# Is set the __ARM_NEON__ flag to true
LOCAL_CFLAGS    += -march=armv7-a -mfloat-abi=softfp -mfpu=neon
# ---- ----- ----- ----- ----- ----- ----- --
LOCAL_CPPFLAGS += -fexceptions


include $(BUILD_SHARED_LIBRARY)

# Manualy define cpu validate futures
$(call import-module,android/cpufeatures) 