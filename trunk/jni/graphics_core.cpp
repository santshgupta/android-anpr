/*
 *  RssBegun.cpp
 *  AWeb
 *
 *  Created by ������ ������ on 15.06.11.
 *  Copyright 2011 Begun. All rights reserved.
 *
 */
#include "graphics_core.h"

using namespace GraphicsCoreNS;


GraphicsCore :: GraphicsCore() {

}


void GraphicsCore :: fullEdgeDetector(JNIEnv* env, jclass javaThis, jobject bitmapcolor, jobject bitmapgray) {
	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	uint8_t redColor, greenColor, blueColor;
	int ret, y, x;

	LOGI("GraphicsCore::fullEdgeDetector");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);
	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap input format must be RGBA_8888, format = %d", infocolor.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap output format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *rgbData = (uint32_t *) pixelscolor;
	uint32_t *destData = (uint32_t *) pixelsgray;

	jint matrix[9] = { -1,	0, 	1,
					   -2,	0, 	2,
					   -1, 	0, 	1};

	jint matrix2[9] = {-1, -2, -1,
						0,	0,	0,
						1,	2,	1};

	int templateSize = 3;
	int sumY 	= 0;
	int sumX 	= 0;
	int sumY2 	= 0;
	int sumX2 	= 0;
	int max  	= 0;
	int max2  	= 0;
	int width 	= infocolor.width;
	int height 	= infocolor.height;
	std::vector<int> destArr(width * height);
	std::vector<int> destArrNew(width * height);
	for ( int x = (templateSize - 1) / 2; x < width - (templateSize + 1) / 2; x++) {
		for (int y = (templateSize - 1) / 2; y < height - (templateSize + 1) / 2; y++) {
			sumY = 0;
			sumY2 = 0;
			for (int x1 = 0; x1 < templateSize; x1++) {
				for (int y1 = 0; y1 < templateSize; y1++) {
					int x2 = ( x - (templateSize - 1) / 2 + x1);
					int y2 = ( y - (templateSize - 1) / 2 + y1);
					int16_t value = (rgbData[ y2 * width + x2] & 0xff) * (matrix [y1 * templateSize + x1]);
					int16_t value2 = (rgbData[ y2 * width + x2] & 0xff) * (matrix2 [y1 * templateSize + x1]);
					sumY += value;
					sumY2 += value2;
				}
			}
			sumX = 0;
			sumX2 = 0;
			for ( int x1 = 0; x1 < templateSize; x1++) {
				for (int y1 = 0; y1 < templateSize; y1++) {
					int x2 = ( x - (templateSize - 1) / 2 + x1);
					int y2 = ( y - (templateSize - 1) / 2 + y1);
					int16_t value = (rgbData[ y2 * width + x2] & 0xff) * (matrix[ x1 * templateSize + y1]);
					int16_t value2 = (rgbData[ y2 * width + x2] & 0xff) * (matrix2[ x1 * templateSize + y1]);
					sumX += value;
					sumX2 += value2;
				}
			}
			int p1 = (int)sqrt(sumX * sumX + sumY * sumY);
			int p2 = (int)sqrt(sumX2 * sumX2 + sumY2 * sumY2);
			destArr[y * width + x] = p1;
			destArrNew[y * width + x] = p2;

			if(max < p1)
				max = p1;
			if(max2 < p2)
				max2 = p2;
		}
	}

	float ratio = (float)max / 255;
	float ratio2 = (float)max2 / 255;

	for ( int x = (templateSize - 1) / 2; x < width - (templateSize + 1) / 2; x++) {
		for (int y = (templateSize - 1) / 2; y < height - (templateSize + 1) / 2; y++) {
			sumX = (int)(destArr[y * width + x] / ratio);
			sumX2 = (int)(destArrNew[y * width + x] / ratio2);
			int m = (int)fmin(255, sumX + sumX2);
			destData[y * width + x] = 0xff000000 | ((int)m << 16 | (int)m << 8 | (int)m);
		}
	}

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// YUV TO ARGB CONVERTER ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void GraphicsCore :: yuvToRGB(JNIEnv* env, jclass javaThis, jbyteArray bitmapData, jobject bitmapgray) {
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	int ret, y, x;
	LOGI("GraphicsCore::yuvToRGB");

	#ifdef __ARM_NEON__
		LOGI("Neon support - ok/ armv7: 2.6.29-g582a0f5 #38 / default: 2.6.29-00261-g0097074-dirty #20 / armv5: 2.6.29-g582a0f5 digit@lulu #37" );
	#endif

	jbyte *src = env->GetByteArrayElements(bitmapData, NULL);

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}
	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap output format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}
	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *ddPtr = (uint32_t *) pixelsgray;
	int len = infogray.width * infogray.height;
#ifdef __ARM_NEON__
	typedef signed char __attribute__((vector_size(16))) vec8;
	static const vec8 kUVToRB[8]  = { 255, 255, 255, 255, 255, 255, 255, 255 };

	__asm__ __volatile__
	(
		// Clear memory
		"mov r5, #0 								\n"
		"mov r6, #0 								\n"

		// load in d3 RGB alpha-channel (a <- rgb)
		"vld1.8 {d3}, [%[vec]]		 				\n"

		// main cicle
		"1:											\n"
		// clear memory
		"vdup.i32 d0, r5  							\n"
		"vdup.i32 d1, r5  							\n"
		"vdup.i32 d2, r5  							\n"

		//load 8 pixels in every register
		"vld1.8 {d0}, [%[x]] 						\n"
		"vld1.8 {d1}, [%[x]] 						\n"
		"vld1.8 {d2}, [%[x]]! 						\n"

		// calculate and store to dest pointer
		"vst4.8    {d0[0], d1[0], d2[0], d3[0]}, [%[dest]]!    \n"
		"vst4.8    {d0[1], d1[1], d2[1], d3[1]}, [%[dest]]!    \n"
		"vst4.8    {d0[2], d1[2], d2[2], d3[2]}, [%[dest]]!    \n"
		"vst4.8    {d0[3], d1[3], d2[3], d3[3]}, [%[dest]]!    \n"
		"vst4.8    {d0[4], d1[4], d2[4], d3[4]}, [%[dest]]!    \n"
		"vst4.8    {d0[5], d1[5], d2[5], d3[5]}, [%[dest]]!    \n"
		"vst4.8    {d0[6], d1[6], d2[6], d3[6]}, [%[dest]]!    \n"
		"vst4.8    {d0[7], d1[7], d2[7], d3[7]}, [%[dest]]!    \n"

		// Subtract counter while it don't attempted zero
		"subs       %[l], %[l], #8     		                	\n"
		"bhi        1b											\n"
		:
		[x] "+r" (src),
		[dest] "+r" (ddPtr),
		[l] "+r" (len)
		:
		[vec] "r"(kUVToRB)

		// registers
		: "cc", "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "d10", "d11", "d12", "d13", "d14","memory"
	);
#else
	for (int i = 0; i < len; i++) {
		ddPtr[i] = 0xff000000 | (src[i]) | (src[i] << 8) | (src[i] << 16);
	}
#endif
	LOGI("unlocking pixels");
	env->ReleaseByteArrayElements(bitmapData, src, JNI_ABORT);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////W I N E R     T R A N S F O R M A T I O N ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


void GraphicsCore :: wienerTransformation(JNIEnv* env, jclass javaThis, jobject bitmapcolor, jobject bitmapgray) {
	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	uint8_t redColor, greenColor, blueColor;
	int ret, y, x;

	LOGI("GraphicsCore::WinerTransformation");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);
	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap input format must be RGBA_8888, format = %d", infocolor.format);
		return;
	}
	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap output format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *rgbData = (uint32_t *) pixelscolor;
	uint32_t *destData = (uint32_t *) pixelsgray;

	int width 	= infocolor.width;
	int height 	= infocolor.height;

	IplImage *tmp = loadPixels(rgbData, width, height);
	IplImage *tmp2 = cvCreateImage(cvSize(tmp->width, tmp->height), IPL_DEPTH_8U, 1);
	IplImage *tmp3 = cvCreateImage(cvSize(tmp->width, tmp->height), IPL_DEPTH_8U, 1);
	cvCvtColor(tmp, tmp2, CV_RGB2GRAY);
	openCVWienerFilter(tmp2, tmp3, 8, 8);
	char color = 0;
	int wDelta = (int)(tmp3->width  / 3 * 2);
	int hDelta = (int)(tmp3->height / 3 * 2);

	for ( int y = 0; y < tmp3->height; y++ ) {
		char* pt = (char*) (tmp3->imageData + y * tmp3->widthStep);
		char* ptOriginal = (char*) (tmp2->imageData + y * tmp2->widthStep);
		for (int x = 0; x < tmp3->width; x++ ) {
			if (x > wDelta && y > hDelta)
				color = pt[x];
			else
				color = ptOriginal[x];
			destData[y * width + x] = 0xff000000 | (color) | (color << 8)
													| (color << 16);
		}
	}

	cvReleaseImage(&tmp);
	cvReleaseImage(&tmp2);
	cvReleaseImage(&tmp3);

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////









void GraphicsCore::HSVBrightnessHorizontally(JNIEnv* env, jclass javaThis, jobject bitmapSource, jfloatArray arr) {
	AndroidBitmapInfo infoSource;
	void* pixelscolor;
	int ret;
	int y;
	int x;

	LOGI("GraphicsCore::HSVBrightnessHorizontally");
	if ((ret = AndroidBitmap_getInfo(env, bitmapSource, &infoSource)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infoSource.width,infoSource.height,infoSource.stride,infoSource.format,infoSource.flags);
	if (infoSource.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 3 ! format=%d", infoSource.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapSource, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}


	float data[infoSource.width];
	for (int i = 0; i < infoSource.width; i++) {
		data[i] = 0;
	}
	for (y = 0; y < infoSource.height; y++) {
		uint32_t *line = (uint32_t*) pixelscolor;
		for (x = 0; x < infoSource.width; x++) {
			uint8_t r = (uint8_t) ((line[x] >> 16) & 0xFF);
			uint8_t g = (uint8_t) ((line[x] >> 8) & 0xFF);
			uint8_t b = (uint8_t) ((line[x]) & 0xFF);
			data[x] += (float)fmax(fmax((double)r, (double)g), (double)b) / 255;
		}
		pixelscolor = (char *) pixelscolor + infoSource.stride;
	}
	env->SetFloatArrayRegion(arr, 0, infoSource.width, data);

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapSource);
}


void GraphicsCore :: sobel(JNIEnv* env, jclass javaThis, jobject bitmapcolor, jobject bitmapgray, jintArray krnl) {
	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	uint8_t redColor, greenColor, blueColor;
	int ret, y, x;

	LOGI("GraphicsCore :: sobel");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);
	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap input format must be RGBA_8888, format = %d", infocolor.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap output format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *rgbData = (uint32_t *) pixelscolor;
	uint32_t *destData = (uint32_t *) pixelsgray;
	jint *carr = env->GetIntArrayElements(krnl, NULL);
	jint tmpl[9];
	for (int i = 0; i < 9; i++) {
		tmpl[i] = carr[i];
	}

	this->processSobelArr(tmpl, infocolor.width, infocolor.height, rgbData, destData);

	LOGI("unlocking pixels");

	env->ReleaseIntArrayElements(krnl, carr, JNI_ABORT);
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}

void GraphicsCore :: processSobelArr (jint* tmpl, int width, int height, uint32_t *rgbData, uint32_t *destData) {
	int templateSize = 3;
	int sumY = 0;
	int sumX = 0;
	int max  = 0;

	for ( int x = (templateSize - 1) / 2; x < width - (templateSize + 1) / 2; x++) {
		for (int y = (templateSize - 1) / 2; y < height - (templateSize + 1) / 2; y++) {
			sumY = 0;
			for (int x1 = 0; x1 < templateSize; x1++) {
				for (int y1 = 0; y1 < templateSize; y1++) {
					int x2 = ( x - (templateSize - 1) / 2 + x1);
					int y2 = ( y - (templateSize - 1) / 2 + y1);
					int16_t value = (rgbData[ y2 * width + x2] & 0xff) * (tmpl [y1 * templateSize + x1]);
					sumY += value;
				}
			}
			sumX = 0;
			for ( int x1 = 0; x1 < templateSize; x1++) {
				for (int y1 = 0; y1 < templateSize; y1++) {
					int x2 = ( x - (templateSize - 1) / 2 + x1);
					int y2 = ( y - (templateSize - 1) / 2 + y1);
					int16_t value = (rgbData[ y2 * width + x2] & 0xff) * (tmpl[ x1 * templateSize + y1]);
					sumX += value;
				}
			}

			destData[y * width + x] = (int)sqrt(sumX * sumX + sumY * sumY);
			if(max < destData[ y * width + x])
				max = destData[y * width + x];
		}
	}
	float ratio = (float)max / 255;
	for (int x = 0; x < width; x++) {
		for(int y = 0; y < height; y++) {
			sumX = (int)(destData[y * width + x] / ratio);
			destData[y * width + x] = 0xff000000 | ((int)sumX << 16 | (int)sumX << 8 | (int)sumX);
		}
	}
}


void GraphicsCore :: HSVBrightness (JNIEnv* env, jclass javaThis, jobject bitmapSource, jfloatArray arr) {

	AndroidBitmapInfo infoSource;
	void* pixelscolor;
	int ret;
	int y;
	int x;

	LOGI("GraphicsCore :: HSVBrightness");
	if ((ret = AndroidBitmap_getInfo(env, bitmapSource, &infoSource)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infoSource.width,infoSource.height,infoSource.stride,infoSource.format,infoSource.flags);
	if (infoSource.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 3 ! format=%d", infoSource.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapSource, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	float data[infoSource.height];
	for (y = 0; y < infoSource.height; y++) {
		data[y] = 0;
		uint32_t *line = (uint32_t*) pixelscolor;
		for (x = 0; x < infoSource.width; x++) {
			uint8_t r = (uint8_t) ((line[x] >> 16) & 0xFF);
			uint8_t g = (uint8_t) ((line[x] >> 8) & 0xFF);
			uint8_t b = (uint8_t) ((line[x]) & 0xFF);
			data[y] += (float)fmax(fmax((double)r, (double)g), (double)b) / 255;
		}
		pixelscolor = (char *) pixelscolor + infoSource.stride;
	}
	env->SetFloatArrayRegion(arr, 0, infoSource.height, data);

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapSource);

}

void GraphicsCore :: getCoefBrightness(JNIEnv* env, jclass javaThis, jobject bitmapSource, jbyteArray arr) {
	AndroidBitmapInfo infoSource;
	void* pixelscolor;
	int ret, y, x;
	LOGI("GraphicsCore::getCoefBrightness");

	#ifdef __ARM_NEON__
		LOGI("Neon support - ok/ armv7: 2.6.29-g582a0f5 #38 / default: 2.6.29-00261-g0097074-dirty #20 / armv5: 2.6.29-g582a0f5 digit@lulu #37" );
	#endif

	uint8_t *intData = (uint8_t*)env->GetByteArrayElements(arr, NULL);

	if ((ret = AndroidBitmap_getInfo(env, bitmapSource, &infoSource)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapSource, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *src = (uint32_t*) pixelscolor;
	int len = infoSource.width * infoSource.height;

	__asm__ __volatile__
	(
		// Clear memory
		"mov r5, #0 								\n"
		// main cicle
		"1:											\n"
		// clear memory
		"vdup.i32 d0, r5  							\n"
		"vdup.i32 d1, r5  							\n"
		"vdup.i32 d2, r5  							\n"
		"vdup.i32 d3, r5  							\n"
		"vdup.i32 d4, r5  							\n"
		"vdup.i32 d5, r5  							\n"
		"vld4.8 {d0, d1, d2, d3}, [%[x]]! 			\n"
		"vmax.u8 d4, d0, d1							\n"
		"vmax.u8 d5, d4, d2							\n"

		// calculate and store to dest pointer
		"vst1.8    d5, [%[dest]]!    \n"

		// Subtract counter while it don't attempted zero
		"subs       %[l], %[l], #8     		                	\n"
		"bhi        1b											\n"
		:
		[x] "+r" (src),
		[dest] "+r" (intData),
		[l] "+r" (len)
		:
		// registers
		: "cc", "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "d10", "d11", "d12", "d13", "d14","memory"
	);

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapSource);
}




void GraphicsCore :: adaptiveTreshold (JNIEnv *env, jclass javaThis, jobject bitmapcolor, jobject bitmapgray) { //

	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	uint8_t redColor, greenColor, blueColor;
	int ret, y, x;

	LOGI("GraphicsCore :: treshold");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);
	if ((infocolor.format != ANDROID_BITMAP_FORMAT_RGB_565)
			&& (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888)) {
		LOGE("Bitmap format must be RGB_565 or RGBA_8888, format = %d", infocolor.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}
	uint32_t *rgbData = (uint32_t *) pixelscolor;
	uint32_t *destData = (uint32_t *) pixelsgray;
	int width 	= infocolor.width;
	int height 	= infocolor.height;

	IplImage *tmp = loadPixels(rgbData, width, height);
	IplImage *tmp2 = cvCreateImage(cvSize(tmp->width, tmp->height), IPL_DEPTH_8U, 1);
	IplImage *tmp3 =  cvCreateImage(cvSize(tmp->width, tmp->height), IPL_DEPTH_8U, 1);

	cvCvtColor(tmp, tmp2, CV_RGB2GRAY);
	cvAdaptiveThreshold(tmp2, tmp3, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C,
				CV_THRESH_BINARY, 17, 7);

	for ( int y = 0; y < tmp3->height; y++ ) {
		char* pt = (char*) (tmp3->imageData + y * tmp2->widthStep);
		for (int x = 0; x < tmp3->width; x++ ) {
			char color = pt[x];
			destData[y * width + x] = 0xff000000 | (color) | (color << 8)
									| (color << 16);
		}
	}

	cvReleaseImage(&tmp);
	cvReleaseImage(&tmp2);
	cvReleaseImage(&tmp3);

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}

void GraphicsCore :: treshold (JNIEnv *env, jclass javaThis, jobject bitmapcolor, jobject bitmapgray, uint8_t tresh) {
	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	uint8_t redColor, greenColor, blueColor;
	int ret, y, x;

	LOGI("GraphicsCore :: treshold");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);
	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format must be RGBA_8888, format = %d", infocolor.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infogray.width,infogray.height,infogray.stride,infogray.format,infogray.flags);
	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}
	uint32_t *rgbData = (uint32_t *) pixelscolor;
	uint32_t *destData = (uint32_t *) pixelsgray;
	int w = infocolor.width;
	int h = infocolor.height;

	for (int x = 0; x < w; x++) {
		for (int y = 0; y < h; y++) {
			redColor = (uint8_t) ((rgbData[y * w + x] >> 16) & 0xFF);
			greenColor = (uint8_t) ((rgbData[y * w + x] >> 8) & 0xFF);
			blueColor = (uint8_t) ((rgbData[y * w + x]) & 0xFF);
			if (redColor <= tresh || greenColor <= tresh || blueColor <= tresh) {
				redColor = 0;
				greenColor = 0;
				blueColor = 0;
			}
			destData[y * w + x] = 0xff000000 | (redColor) | (greenColor << 8)
								| (blueColor << 16);
		}
	}
	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}


void GraphicsCore :: convert565to8888 (JNIEnv *env, jobject bitmapcolor, jobject bitmapgray) {

	AndroidBitmapInfo infocolor;
	void* pixelscolor;
	AndroidBitmapInfo infogray;
	void* pixelsgray;
	int ret;
	int y;
	int x;

	LOGI("GraphicsCore :: convert565to8888");
	if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapgray, &infogray)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 2 ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",
			infocolor.width,
			infocolor.height,
			infocolor.stride,
			infocolor.format,
			infocolor.flags);

	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE("Bitmap format is not RGBA_565 3 ! format=%d", infocolor.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",
			infogray.width,
			infogray.height,
			infogray.stride,
			infogray.format,
			infogray.flags);

	if (infogray.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 4 ! format=%d", infogray.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapgray, &pixelsgray)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	for (y = 0; y < infocolor.height; y++) {
		uint16_t *line 		= (uint16_t*) pixelscolor;
		uint32_t *grayline 	= (uint32_t *) pixelsgray;

		for (x = 0; x < infocolor.width; x++) {
			uint8_t redColor 	= (uint8_t) (((	line[x] & 0xF800) >> 11) << 3);
			uint8_t greenColor 	= (uint8_t) (((	line[x] & 0x07E0) >> 5) << 2);
			uint8_t blueColor 	= (uint8_t) ((	line[x] & 0x001F) << 3);
			grayline[x] = 0xff000000 | (redColor) | (greenColor << 8)
					| (blueColor << 16);
		}
		pixelscolor = (char *) pixelscolor + infocolor.stride;
		pixelsgray = (char *) pixelsgray + infogray.stride;
	}

	LOGI("unlocking pixels");
	AndroidBitmap_unlockPixels(env, bitmapcolor);
	AndroidBitmap_unlockPixels(env, bitmapgray);
}




void GraphicsCore :: convolve (JNIEnv *env, jobject bitmapSource, jobject bitmapDestination,
		jintArray krnl, uint8_t kernelCountRows, uint8_t kernelCountCols,
		uint8_t filterDiv, uint8_t offset) {

	AndroidBitmapInfo infoSource;
	void* pixelSource;
	AndroidBitmapInfo infoDestination;
	void* pixelDestination;
	int ret;

	/**
	 * Get kernel
	 */
	jint *carr = env->GetIntArrayElements(krnl, NULL);
	int kernel[kernelCountRows][kernelCountCols];
	for (int i = 0; i < kernelCountRows; i++) {
		for (int j = 0; j < kernelCountCols; j++) {
			kernel[j][i] = carr[i * kernelCountRows + j];
		}
	}

	double t0, t1, time_c, time_neon;
	int templateSize = 3;
	uint64_t features;
	LOGI("GraphicsCore :: convolve");
	if ((ret = AndroidBitmap_getInfo(env, bitmapSource, &infoSource)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	if ((ret = AndroidBitmap_getInfo(env, bitmapDestination, &infoDestination))
			< 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infoSource.width,infoSource.height,infoSource.stride,infoSource.format,infoSource.flags);
	if (infoSource.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("!Bitmap format is not RGB_8888 !- %i", infoSource.format);
		return;
	}

	LOGI("gray image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",infoDestination.width,infoDestination.height,infoDestination.stride,infoDestination.format,infoDestination.flags);
	if (infoDestination.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("!Bitmap format is not RGBA_8888 !- %i", infoDestination.format);
		return;
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapSource, &pixelSource)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	if ((ret = AndroidBitmap_lockPixels(env, bitmapDestination,
			&pixelDestination)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	/**
	 * 2 bytes RGB value
	 */
	uint32_t *rgbData = (uint32_t *) pixelSource;
	uint32_t total[infoSource.width * infoSource.height];
	int sumY = 0;
	int sumX = 0;
	int max = 0;
	LOGI("start");

	int width = infoSource.width;
	int height = infoSource.height;

	#ifdef __ARM_NEON__
		LOGI("Neon support - ok/ armv7: 2.6.29-g582a0f5 #38 / default: 2.6.29-00261-g0097074-dirty #20 / armv5: 2.6.29-g582a0f5 digit@lulu #37" );
	#endif
	if (android_getCpuFamily() != ANDROID_CPU_FAMILY_ARM) {
		LOGI("Not an ARM CPU");
	}
	features = android_getCpuFeatures();
	if ((features & ANDROID_CPU_ARM_FEATURE_ARMv7) == 0) {
		LOGI("Not an ARMv7 CPU");
	}

	uint16_t kernelData[9] = { };
	uint32_t data[9] = { };
	int pixelPosX, pixelPosY;
	for (int32_t j = 0; j < kernelCountRows; j++) {
		for (int32_t i = 0; i < kernelCountCols; i++) {
			int kernelVal = (int) kernel[i][j];
			kernelData[j * 3 + i] = kernelVal;
		}
	}

	//t0 = now_ms();
	uint32_t *destline = (uint32_t *) pixelDestination;
	for (int x = 1; x < width - 1; x++) {
		for (int y = 1; y < height - 1; y++) {
			int8_t emptyPtr = 0;
			int16_t rSum = 0, gSum = 0, bSum = 0;

#ifdef __ARM_NEON__

			int16_t *rSumPtr = &rSum, *gSumPtr = &gSum, *bSumPtr = &bSum;

			for (int32_t j = 0; j < kernelCountRows; j++) {
				for (int32_t i = 0; i < kernelCountCols; i++) {
					pixelPosX = x + (i - 1);
					pixelPosY = y + (j - 1);
					if ((pixelPosX < 0) || (pixelPosX >= width) || (pixelPosY
									< 0) || (pixelPosY >= height))
					continue;

					int posPix = width * pixelPosY + pixelPosX;
					data[j*3+i] = rgbData[posPix];
				}
			}
			uint16_t *kernelDataPtr = kernelData;
			uint32_t *dataPtr = data;
			__asm__ __volatile__
			(

					// Clear memory
					"mov r5, #0 \n\t"
					"vdup.i32 d0, r5  \n\t"
					"vdup.i32 d1, r5  \n\t"
					"vdup.i32 d2, r5  \n\t"
					"vdup.i32 d3, r5  \n\t"
					"vdup.i32 d4, r5  \n\t"
					"vdup.i32 d8, r5  \n\t"
					"vdup.i32 d9, r5  \n\t"
					"vdup.i32 d10, r5  \n\t"
					"vdup.i32 d20, r5  \n\t"
					"vdup.i32 d21, r5  \n\t"
					"vdup.i32 d22, r5  \n\t"

					// First 4 pixels
					"vld4.8 		{d0[0], d1[0], d2[0], d3[0]}, [%[x]]! \n\t"
					"vld4.8 		{d0[2], d1[2], d2[2], d3[2]}, [%[x]]! \n\t"
					"vld4.8 		{d0[4], d1[4], d2[4], d3[4]}, [%[x]]! \n\t"
					"vld4.8 		{d0[6], d1[6], d2[6], d3[6]}, [%[x]]! \n\t"

					// Load kernel matrix (First block)
					// ��� ��� ���������������� ���� ��������� ����������� � 16bit ������ ����� ������ �� ���������
					"vld1.16 		{d4[0]}, [%[kData]]! \n\t" // 1 �������
					"vld1.16 		{d4[1]}, [%[kData]]! \n\t" // 2 �������
					"vld1.16 		{d4[2]}, [%[kData]]! \n\t" // 3 �������
					"vld1.16 		{d4[3]}, [%[kData]]! \n\t" // 4 �������

					// Multiply data
					"vmul.i16    	d8, d0, d4 \n\t"
					"vmul.i16    	d9, d1, d4 \n\t"
					"vmul.i16    	d10, d2, d4 \n\t"
					//"vmul.i16    	d11, d3, d4 \n\t" // ���� ������� �������� �� �����. ������������ ��� �� �����

					// Addition
					// see http://infocenter.arm.com/help/index.jsp?topic=/com.arm.doc.dui0489c/CJAJIIGG.html
					"vpadal.u16		d20, d8 \n\t"
					"vpadal.u16		d21, d9 \n\t"
					"vpadal.u16		d22, d10 \n\t"

					// Second 4 pixels
					"vld4.8 		{d0[0], d1[0], d2[0], d3[0]}, [%[x]]! \n\t"
					"vld4.8 		{d0[2], d1[2], d2[2], d3[2]}, [%[x]]! \n\t"
					"vld4.8 		{d0[4], d1[4], d2[4], d3[4]}, [%[x]]! \n\t"
					"vld4.8 		{d0[6], d1[6], d2[6], d3[6]}, [%[x]]! \n\t"

					"vld1.16 		{d4[0]}, [%[kData]]! \n\t"
					"vld1.16 		{d4[1]}, [%[kData]]! \n\t"
					"vld1.16 		{d4[2]}, [%[kData]]! \n\t"
					"vld1.16 		{d4[3]}, [%[kData]]! \n\t"

					"vmul.i16    	d8, d0, d4 \n\t"
					"vmul.i16    	d9, d1, d4 \n\t"
					"vmul.i16    	d10, d2, d4 \n\t"

					"vpadal.u16		d20, d8 \n\t"
					"vpadal.u16		d21, d9 \n\t"
					"vpadal.u16		d22, d10 \n\t"

					// last pixel
					// clear data
					//"mov r5, #0 \n\t"
					"vdup.i32 d4, r5  \n\t"
					"vdup.i32 d8, r5  \n\t"
					"vdup.i32 d9, r5  \n\t"
					"vdup.i32 d10, r5  \n\t"

					"vld4.8 		{d0[0], d1[0], d2[0], d3[0]}, [%[x]]! \n\t"
					"vld1.16 		{d4[0]}, [%[kData]] \n\t"

					"vmul.i16    	d8, d0, d4 \n\t"
					"vmul.i16    	d9, d1, d4 \n\t"
					"vmul.i16    	d10, d2, d4 \n\t"

					"vpadal.u16		d20, d8 \n\t"
					"vpadal.u16		d21, d9 \n\t"
					"vpadal.u16		d22, d10 \n\t"

					// final result
					"vpaddl.u32		d20, d20 \n\t"
					"vpaddl.u32		d21, d21 \n\t"
					"vpaddl.u32		d22, d22 \n\t"

					// 16bit long red
					"vst1.16 	{d22[0]}, [%[rptr]] \n\t"
					// 16bit long green
					"vst1.16 	{d21[0]}, [%[gptr]] \n\t"
					// 16bit long blue
					"vst1.16 	{d20[0]}, [%[bptr]] \n\t"

					// clear accumulators
					"vdup.i32 d20, r5  \n\t"
					"vdup.i32 d21, r5  \n\t"
					"vdup.i32 d22, r5  \n\t"

					: [x] "+r" (dataPtr),
					[kData] "+r" (kernelDataPtr),
					[rptr] "+r" (rSumPtr),
					[gptr] "+r" (gSumPtr),
					[bptr] "+r" (bSumPtr)
					:
					// registers
					: "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "d10", "d11", "d12", "d13", "d14","memory"
			);

#else

			for (int32_t j = 0; j < kernelCountRows; j++) {
				for (int32_t i = 0; i < kernelCountCols; i++) {
					pixelPosX = x + (i - 1);
					pixelPosY = y + (j - 1);
					if ((pixelPosX < 0) || (pixelPosX >= width) || (pixelPosY
									< 0) || (pixelPosY >= height))
					continue;

					int posPix = width * pixelPosY + pixelPosX;
					uint8_t r = (uint8_t) ((rgbData[posPix] >> 16) & 0xFF);
					uint8_t g = (uint8_t) ((rgbData[posPix] >> 8) & 0xFF);
					uint8_t b = (uint8_t) ((rgbData[posPix]) & 0xFF);


					uint16_t kernelVal = kernelData[j * kernelCountCols + i];

					rSum += r * kernelVal;
					gSum += g * kernelVal;
					bSum += b * kernelVal;
				}
			}

#endif
			rSum = (rSum / filterDiv) + offset;
			gSum = (gSum / filterDiv) + offset;
			bSum = (bSum / filterDiv) + offset;

			rSum = (rSum > 255) ? 255 : ((rSum < 0) ? 0 : rSum);
			gSum = (gSum > 255) ? 255 : ((gSum < 0) ? 0 : gSum);
			bSum = (bSum > 255) ? 255 : ((bSum < 0) ? 0 : bSum);

			destline[y * width + x] = 0xff000000 | ((int) rSum  | (int) gSum << 8
					| (int) bSum << 16);
		}
	}
	//t1 = now_ms();
	LOGI("finish");
	//	time_c = t1 - t0;
	//LOGI("FIR Filter benchmark:C version: %g ms\n", time_c);
	env->ReleaseIntArrayElements(krnl, carr, JNI_ABORT);
	AndroidBitmap_unlockPixels(env, bitmapSource);
	AndroidBitmap_unlockPixels(env, bitmapDestination);
}


void GraphicsCore :: RGBtoHSV (double r, double g, double b, double *h, double *s, double *v) {
	double min, max, delta;

	min = fmin(fmin(r, g), b);
	max = fmax(fmax(r, g), b);
	*v = max; // v
	delta = max - min;
	if (max != 0)
		*s = delta / max; // s
	else {
		// r = g = b = 0  // s = 0, v is undefined
		*s = 0;
		*h = -1;
		return;
	}
	if (r == max)
		*h = (g - b) / delta; // between yellow &	magenta
	else if (g == max)
		*h = 2 + (b - r) / delta; // between cyan & yellow
	else
		*h = 4 + (r - g) / delta; // between magenta & cyan
	*h *= 60; // degrees
	if (*h < 0)
		*h += 360;
}



/* Adapted from:                                                            */
/*   TEXAS INSTRUMENTS, INC.                                                */
/*   IMGLIB  DSP Image/Video Processing Library                             */
void GraphicsCore :: sobelFilterTexas (uint32_t *input, int width, int height, uint32_t *output, int negative) {
        int H, O, V, i;
        int i00, i01, i02;
        int i10,      i12;
        int i20, i21, i22;
        int w = width;
        int numpx = width * (height - 1);
        for (i = 0; i < numpx - 1; ++i) {
                i00 = input[i    ] & 0xff;
                i01 = input[i    +1] & 0xff;
                i02 = input[i    +2] & 0xff;

                i10 = input[i+  w] & 0xff;
                i12 = input[i+  w+2] & 0xff;

                i20 = input[i+2*w] & 0xff;
                i21 = input[i+2*w+1] & 0xff;
                i22 = input[i+2*w+2] & 0xff;

                H = -  i00 - 2 * i01 -  i02 + i20 + 2 * i21 + i22;
                V = -  i00  +     i02
                        - 2 * i10  + 2 * i12
                        -     i20  +     i22;
                O = abs(H) + abs(V);
                if (O > 255) { O = 255; }
                if (negative) { O = 255 - O; }
                output[i + 1] = 0xff000000 | ((int) O << 16 | (int) O << 8 | (int) O);
        }
}


void GraphicsCore :: openCVWienerFilter (const void* srcArr, void* dstArr, int szWindowX, int szWindowY) {
	CV_FUNCNAME( "cvWiener2" );
	int nRows;
	int nCols;
	CvMat *p_kernel = NULL;
	CvMat srcStub, *srcMat = NULL;
	CvMat *p_tmpMat1, *p_tmpMat2, *p_tmpMat3, *p_tmpMat4;
	double noise_power;

	__BEGIN__;

	//// DO CHECKING ////

	if ( srcArr == NULL) {
		CV_ERROR( CV_StsNullPtr, "Source array null" );
	}
	if ( dstArr == NULL) {
		CV_ERROR( CV_StsNullPtr, "Dest. array null" );
	}

	nRows = szWindowY;
	nCols = szWindowX;


	p_kernel = cvCreateMat( nRows, nCols, CV_32F );
	CV_CALL( cvSet( p_kernel, cvScalar( 1.0 / (double) (nRows * nCols)) ) );

	//Convert to matrices
	srcMat = (CvMat*) srcArr;

	if ( !CV_IS_MAT(srcArr) ) {
		CV_CALL ( srcMat = cvGetMat(srcMat, &srcStub, 0, 1) ) ;
	}

	//Now create a temporary holding matrix
	p_tmpMat1 = cvCreateMat(srcMat->rows, srcMat->cols, CV_MAT_TYPE(srcMat->type));
	p_tmpMat2 = cvCreateMat(srcMat->rows, srcMat->cols, CV_MAT_TYPE(srcMat->type));
	p_tmpMat3 = cvCreateMat(srcMat->rows, srcMat->cols, CV_MAT_TYPE(srcMat->type));
	p_tmpMat4 = cvCreateMat(srcMat->rows, srcMat->cols, CV_MAT_TYPE(srcMat->type));

	//Local mean of input
	cvFilter2D( srcMat, p_tmpMat1, p_kernel, cvPoint(nCols/2, nRows/2)); //localMean

	//Local variance of input
	cvMul( srcMat, srcMat, p_tmpMat2);	//in^2
	cvFilter2D( p_tmpMat2, p_tmpMat3, p_kernel, cvPoint(nCols/2, nRows/2));

	//Subtract off local_mean^2 from local variance
	cvMul( p_tmpMat1, p_tmpMat1, p_tmpMat4 ); //localMean^2
	cvSub( p_tmpMat3, p_tmpMat4, p_tmpMat3 ); //filter(in^2) - localMean^2 ==> localVariance

	//Estimate noise power
	noise_power = cvMean(p_tmpMat3, 0);

	// result = local_mean  + ( max(0, localVar - noise) ./ max(localVar, noise)) .* (in - local_mean)

	cvSub ( srcMat, p_tmpMat1, dstArr);		     //in - local_mean
	cvMaxS( p_tmpMat3, noise_power, p_tmpMat2 ); //max(localVar, noise)

	cvAddS( p_tmpMat3, cvScalar(-noise_power), p_tmpMat3 ); //localVar - noise
	cvMaxS( p_tmpMat3, 0, p_tmpMat3 ); // max(0, localVar - noise)

	cvDiv ( p_tmpMat3, p_tmpMat2, p_tmpMat3 );  //max(0, localVar-noise) / max(localVar, noise)

	cvMul ( p_tmpMat3, dstArr, dstArr );
	cvAdd ( dstArr, p_tmpMat1, dstArr );

	cvReleaseMat( &p_kernel  );
	cvReleaseMat( &p_tmpMat1 );
	cvReleaseMat( &p_tmpMat2 );
	cvReleaseMat( &p_tmpMat3 );
	cvReleaseMat( &p_tmpMat4 );

	__END__;


}


IplImage* GraphicsCore :: loadPixels(uint32_t* pixels, int width, int height) {
	int x, y;
	IplImage *img = cvCreateImage(cvSize(width, height), IPL_DEPTH_8U, 3);

	for ( y = 0; y < height; y++ ) {
        for ( x = 0; x < width; x++ ) {
            // blue
            IMAGE( img, x, y, 0 ) = pixels[x+y*width] & 0xFF;
            // green
            IMAGE( img, x, y, 1 ) = pixels[x+y*width] >> 8 & 0xFF;
            // red
            IMAGE( img, x, y, 2 ) = pixels[x+y*width] >> 16 & 0xFF;
        }
    }

	return img;
}
