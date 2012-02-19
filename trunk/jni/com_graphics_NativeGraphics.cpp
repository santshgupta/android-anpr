#include "com_graphics_NativeGraphics.h"


static double now_ms(void) {
	struct timespec res;
	clock_gettime(CLOCK_REALTIME, &res);
	return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;
}


void JNICALL Java_com_graphics_NativeGraphics_nativeConvolve(JNIEnv *env,
		jclass javaThis, jobject bitmapSource, jobject bitmapDestination,
		jintArray krnl, uint8_t kernelCountRows, uint8_t kernelCountCols,
		uint8_t filterDiv, uint8_t offset) {

	GraphicsCoreNS :: GraphicsCore core;
	core.convolve(env,bitmapSource, bitmapDestination,krnl,kernelCountRows, kernelCountCols,filterDiv,offset);

}

void JNICALL Java_com_graphics_NativeGraphics_nativeTreshold (JNIEnv *env,
		jclass javaThis, jobject bitmapcolor, jobject bitmapgray, uint8_t tresh) {
	GraphicsCoreNS :: GraphicsCore core;
	core.treshold(env, javaThis, bitmapcolor, bitmapgray, tresh);
}

jfloat JNICALL Java_com_graphics_NativeGraphics_nativeHoughTransform (JNIEnv *env,
		jclass javaThis, jobject bitmapcolor) {
	GraphicsCoreNS::HoughTransformation hough;
	return hough.transform(env, javaThis, bitmapcolor);
}


void JNICALL Java_com_graphics_NativeGraphics_nativeAdaptiveTreshold (JNIEnv *env,
		jclass javaThis, jobject bitmapcolor, jobject bitmapgray) {
	GraphicsCoreNS :: GraphicsCore core;
	core.adaptiveTreshold(env, javaThis, bitmapcolor, bitmapgray);
}


void JNICALL Java_com_graphics_NativeGraphics_nativeConvert565to8888 (JNIEnv *env,
		jclass javaThis, jobject bitmapcolor, jobject bitmapgray) {

	GraphicsCoreNS :: GraphicsCore core;
	core.convert565to8888(env, bitmapcolor, bitmapgray);
}


/**
 * Vertical graphic
 */
void JNICALL Java_com_graphics_NativeGraphics_nativeGetHSVBrightness (JNIEnv* env,
		jclass javaThis, jobject bitmapSource, jfloatArray arr) {
	GraphicsCoreNS :: GraphicsCore core;
	core.HSVBrightness(env, javaThis, bitmapSource, arr);
}



/**
 * Horizontal graphic
 */
void JNICALL Java_com_graphics_NativeGraphics_nativeGetHSVBrightnessHorizontally(JNIEnv* env,
		jclass javaThis, jobject bitmapSource, jfloatArray arr) {
	GraphicsCoreNS :: GraphicsCore core;
	core.HSVBrightnessHorizontally(env, javaThis, bitmapSource, arr);
}


void JNICALL Java_com_graphics_NativeGraphics_nativeSobel(JNIEnv* env,
		jclass javaThis, jobject bitmapcolor, jobject bitmapgray, jintArray krnl) {
	GraphicsCoreNS :: GraphicsCore core;
	core.sobel(env, javaThis, bitmapcolor, bitmapgray, krnl);
}

void JNICALL Java_com_graphics_NativeGraphics_nativeFullEdgeDetector (JNIEnv* env,
		jclass javaThis, jobject bitmapSource, jobject bitmapDest) {
	GraphicsCoreNS :: GraphicsCore core;
	core.fullEdgeDetector(env, javaThis, bitmapSource, bitmapDest);
}

void JNICALL Java_com_graphics_NativeGraphics_nativeWiener (JNIEnv* env,
		jclass javaThis, jobject bitmapSource, jobject bitmapDest)
{
	GraphicsCoreNS :: GraphicsCore core;
	core.wienerTransformation(env, javaThis, bitmapSource, bitmapDest);
}

void JNICALL Java_com_graphics_NativeGraphics_nativeYuvToRGB (JNIEnv* env,
		jclass javaThis, jbyteArray krnl, jobject bitmapDest)
{
	GraphicsCoreNS :: GraphicsCore core;
	core.yuvToRGB(env, javaThis, krnl, bitmapDest);
}

void JNICALL Java_com_graphics_NativeGraphics_nativeGetCoefBrightness (JNIEnv* env,
		jclass javaThis, jobject bitmapSource, jbyteArray result)
{
	GraphicsCoreNS :: GraphicsCore core;
	core.getCoefBrightness(env, javaThis, bitmapSource, result);
}


