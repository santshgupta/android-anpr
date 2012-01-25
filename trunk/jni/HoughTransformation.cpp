/*
 * HoughTransformation.cpp
 *
 *  Created on: 28.12.2011
 *      Author: azhdanov
 */

#include "HoughTransformation.h"

using namespace GraphicsCoreNS;

HoughTransformation :: HoughTransformation (JNIEnv* env, jclass javaThis, jobject bitmap) {
	AndroidBitmapInfo infocolor;
	uint8_t redColor, greenColor, blueColor;
	void* pixelscolor;
	int ret;

	LOGI("HoughTransformation :: HoughTransformation");

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &infocolor)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed 1 ! error=%d", ret);
		return;
	}
	LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is%d",
			infocolor.width,
			infocolor.height,
			infocolor.stride,
			infocolor.format,
			infocolor.flags);

	if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 ! format=%d", infocolor.format);
		return;
	}

	width 	= infocolor.width;
	height 	= infocolor.height;

	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixelscolor)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
	}

	uint32_t *rgbData = (uint32_t *) pixelscolor;
	BitmapData bmp(width, std::vector<uint32_t>(height, 0));
	bitmapData = bmp;
	bmp.clear();

	for (int x = 0; x < width; x++) {
		for (int y = 0; y < height; y++) {
			redColor = (uint8_t) ((rgbData[y * width + x] >> 16) & 0xFF);
			greenColor = (uint8_t) ((rgbData[y * width + x] >> 8) & 0xFF);
			blueColor = (uint8_t) ((rgbData[y * width + x]) & 0xFF);
			float brightness = (float)fmax(fmax((double)redColor, (double)greenColor), (double)blueColor) / 255;
			addLine(x,y, brightness);
		}
	}
	AndroidBitmap_unlockPixels(env, bitmap);
};


HoughTransformation :: ~HoughTransformation () {
};

/**
 * Формируем битмапу на обработку.Получаем яркость
 */
void HoughTransformation :: addLine(int x, int y, float brightness) {
	float xf = 2 * ((float)x) / width - 1;
	float yf = 2 * ((float)y) / height - 1;
	for (int a = 0; a < width; a++) {

		float af = 2 * ((float)a) / width - 1;
		float bf = yf - af * xf;
		int b = (int)((bf + 1) * height / 2);
		if (0 < b && b < height-1) {
			bitmapData[a][b] += brightness;
		}
	}
}


float HoughTransformation :: getMaxValue() {
	float maxValue = 0;
	for (int x = 0; x < width; x++)
		for (int y = 0; y < height; y++)
			maxValue = fmax(maxValue, bitmapData[x][y]);
	return maxValue;
}


Point HoughTransformation :: computeMaxPoint() {
	float max = 0;
	int maxX = 0, maxY = 0;
	for (int x = 0; x < width; x++)  {
		for (int y = 0; y < height; y++) {
			float curr = bitmapData[x][y];
			if (curr >= max) {
				maxX = x;
				maxY = y;
				max = curr;
			}
		}
	}
	Point p(maxX, maxY);
	return p;
}

Point HoughTransformation :: getMaxPoint() {
	if (maxPoint == false) {
		maxPoint = computeMaxPoint();
	}
	return maxPoint;
}

float HoughTransformation :: getAverageValue() {
	float sum = 0;
	for (int x = 0; x < width; x++)
		for (int y = 0; y < height; y++)
			sum += bitmapData[x][y];
	return sum / (width * height);
}


jfloat HoughTransformation :: render(int renderType, int colorType) {
	float average = getAverageValue();
	maxPoint = computeMaxPoint();

	float a = 2 * ((float)maxPoint.x) / width - 1;
	float b = 2 * ((float)maxPoint.y) / height - 1;

	float x0f = -1;
	float y0f = a * x0f + b;
	float x1f = 1;
	float y1f = a * x1f + b;

	int y0 = (int)( (y0f+1) * height / 2  );
	int y1 = (int)( (y1f+1) * height / 2  );

	int dxl = width;
	int dyl = y1 - y0;
	dx = dxl;
	dy = dyl;

	angle = (float) (180 * atan(dy / dx) / PI);
	return -dy / dx;
}



jfloat HoughTransformation :: transform () {
	return render(RENDER_ALL, COLOR_BW);
}

