# Build both ARMv5TE and ARMv7-A machine code.
APP_ABI := armeabi armeabi-v7a
APP_STL := gnustl_static
APP_MODULES := cxcore cv com_graphics_NativeGraphics
STLPORT_FORCE_REBUILD := true