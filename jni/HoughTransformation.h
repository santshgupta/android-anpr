/*
 * HoughTransformation.h
 *
 *  Created on: 28.12.2011
 *      Author: azhdanov
 */

#ifndef HOUGHTRANSFORMATION_H_
#define HOUGHTRANSFORMATION_H_
#include <jni.h>
#include <math.h>
#include <stdlib.h>
#include <android/bitmap.h>
#include "logger.h"
#include <vector>




namespace GraphicsCoreNS {

typedef std::vector< std::vector<uint32_t> > BitmapData;


struct Point  {
	Point() {
		initialized = false;
	}
	Point (int a, int b) : x(a), y(b) {
		initialized = true;
	}
	bool operator==(const bool check)
	{
		if (initialized == check)
			return true;
		else
			return false;
	};
	int x, y;
	private:
		bool initialized;
};

class HoughTransformation {
	public:

		HoughTransformation (JNIEnv *, jclass, jobject);
		~HoughTransformation();

		jfloat transform();
		Point getMaxPoint();
		jfloat render(int renderType, int colorType);
		static const double PI = 3.141592653589793;
		Point maxPoint;
		float angle;
		float dx;
		float dy;

		static const int RENDER_ALL = 1;
		static int RENDER_TRANSFORMONLY;
		static const int COLOR_BW = 1;
	private:

		void addLine(int, int, float);
		float getMaxValue();
		float getAverageValue();
		Point computeMaxPoint();

		uint32_t width;
		uint32_t height;
		BitmapData bitmapData;
	};
}

#endif /* HOUGHTRANSFORMATION_H_ */
