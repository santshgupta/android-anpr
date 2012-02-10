/*
 * HoughTransformation.cpp
 *
 *  Created on: 28.12.2011
 *      Author: azhdanov
 */

#include "HoughTransformation.h"

using namespace GraphicsCoreNS;

HoughTransformation :: HoughTransformation () {
};

HoughTransformation :: ~HoughTransformation () {
};


IplImage* HoughTransformation :: loadPixels(uint32_t* pixels, int width, int height) {
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

jfloat HoughTransformation :: transform (JNIEnv* env, jclass javaThis, jobject bitmap) {
	AndroidBitmapInfo infocolor;
	uint8_t redColor, greenColor, blueColor;
	void* pixelscolor;
	int ret;

	LOGI("HoughTransformation :: HoughTransformation");

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return 0;
	}
	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",
			infocolor.width,
			infocolor.height,
			infocolor.stride,
			infocolor.format,
			infocolor.flags);

	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 ! format=%d", infocolor.format);
		return 0;
	}

	width 	= infocolor.width;
	height 	= infocolor.height;

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *rgbData = (uint32_t *) pixelscolor;

	CvMemStorage *storage = cvCreateMemStorage(0);
	IplImage *src 	= loadPixels(rgbData, width, height);
	IplImage *tmp2 	= cvCreateImage(cvSize(src->width, src->height), IPL_DEPTH_8U, 1);
	IplImage* dst 	= 0;
	CvSeq *lines 	= 0;
	jfloat dd 		= 0;

	cvCvtColor(src, tmp2, CV_RGB2GRAY);
	dst = cvCreateImage( cvGetSize(src), IPL_DEPTH_8U, 1 );
	cvCanny( tmp2, dst, 50, 200, 3 );
	lines = cvHoughLines2( dst, storage, CV_HOUGH_PROBABILISTIC, 1, CV_PI/180, 50, 50, 10 );

	if (lines -> total > 0) {
		CvPoint* line = (CvPoint*)cvGetSeqElem(lines,0);
		LOGE("point 1| x: %i y: %i", line[0].x, line[0].y);
		LOGE("point 2| x: %i y: %i", line[1].x, line[1].y);

		dx = line[1].x - line[0].x;
		dy = line[1].y - line[0].y;

		angle = (float) (180 * atan(dy / dx) / PI);
		LOGE("line angle: %f", angle);
		LOGE("skew: %f", -dy / dx );
		if (dx > 0) {
			dd = -dy / dx;
		}
	}

	cvReleaseImage(&src);
	cvReleaseImage(&dst);
	cvReleaseImage(&tmp2);
	cvReleaseMemStorage(&storage);

	AndroidBitmap_unlockPixels(env, bitmap);
	return dd;
};

