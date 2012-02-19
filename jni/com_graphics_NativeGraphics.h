#include <jni.h>
#include "graphics_core.h"
#include "HoughTransformation.h"

// NEON
#ifdef __ARM_NEON__
	#include <arm_neon.h>
#endif


/* Header for class com_graphics_NativeGraphics */

#ifndef _Included_com_graphics_NativeGraphics
#define _Included_com_graphics_NativeGraphics
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeConvert565to8888
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Bitmap;)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeConvert565to8888
  (JNIEnv *, jclass, jobject, jobject);

/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeTreshold
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Bitmap;I)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeTreshold
  (JNIEnv *, jclass, jobject, jobject, uint8_t);


JNIEXPORT jfloat JNICALL Java_com_graphics_NativeGraphics_nativeHoughTransform
  (JNIEnv *, jclass, jobject);


JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeAdaptiveTreshold
  (JNIEnv *, jclass, jobject, jobject);

/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeConvolve
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Bitmap;[IIIII)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeConvolve
(JNIEnv *, jclass, jobject, jobject, jintArray, uint8_t, uint8_t, uint8_t, uint8_t);

/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeGetHSVBrightness
 * Signature: (Landroid/graphics/Bitmap;[F)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeGetHSVBrightness
  (JNIEnv *, jclass, jobject, jfloatArray);

/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeGetHSVBrightnessHorizontally
 * Signature: (Landroid/graphics/Bitmap;[F)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeGetHSVBrightnessHorizontally
  (JNIEnv *, jclass, jobject, jfloatArray);

/*
 * Class:     com_graphics_NativeGraphics
 * Method:    nativeSobel
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Bitmap;[I)V
 */
JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeSobel
  (JNIEnv *, jclass, jobject, jobject, jintArray);


JNIEXPORT void JNICALL Java_com_graphics_NativeGraphics_nativeFullEdgeDetector
  (JNIEnv *, jclass, jobject, jobject);


void JNICALL Java_com_graphics_NativeGraphics_nativeWiener (JNIEnv* ,
		jclass , jobject , jobject );

void JNICALL Java_com_graphics_NativeGraphics_nativeYuvToRGB (JNIEnv* ,
		jclass , jbyteArray , jobject );

void JNICALL Java_com_graphics_NativeGraphics_nativeGetCoefBrightness (JNIEnv* ,
		jclass , jobject, jbyteArray);


#ifdef __cplusplus
}
#endif
#endif
