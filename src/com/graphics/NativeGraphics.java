/**
 * Andrey Zhdanov/2011/Native graphics/zdanchik@gmail.com
 */
package com.graphics;

import android.graphics.Bitmap;
import android.util.Log;

public class NativeGraphics {

	static {
		System.loadLibrary("com_graphics_NativeGraphics");
	}
	
	private native static void nativeConvert565to8888(Bitmap sourceBitmap, Bitmap destinationBitmap);
	private native static void nativeTreshold(Bitmap sourceBitmap, Bitmap destinationBitmap, int cnt);
	private native static void nativeAdaptiveTreshold(Bitmap sourceBitmap, Bitmap destinationBitmap);
	private native static void nativeConvolve(Bitmap sourceBitmap, Bitmap destinationBitmap, int[] template, int kernelCountRows, int kernelCountCols, int filterDiv, int offset);
	private native static void nativeGetHSVBrightness(Bitmap sourceBitmap, float[] peaks);
	private native static void nativeGetHSVBrightnessHorizontally(Bitmap sourceBitmap, float[] peaks);
	private native static void nativeSobel(Bitmap sourceBitmap, Bitmap destBitmap, int[] template);
	private native static void nativeFullEdgeDetector(Bitmap source, Bitmap destBitmap);
	private native static float nativeHoughTransform(Bitmap source);
	private native static void nativeWiener(Bitmap source, Bitmap destBitmap);
	private native static void nativeYuvToRGB(byte[] source, Bitmap destBitmap);
	
	/**
	 * Sobel - http://en.wikipedia.org/wiki/Sobel_operator
	 * @param src - source Bitmap
	 * @param template  - matrix 3x3
	 * @return Bitmap
	 */
	public static Bitmap nativeSobel(Bitmap src, int[] template) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeSobel(src, destBitmap, template);
		src.recycle();
		return destBitmap;
	}
	
	public static Bitmap convert565to8888(Bitmap src) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeConvert565to8888(src, destBitmap);
		src.recycle();
		return destBitmap;
	}
	
	/**
	 * Operations with HSV graphics color
	 */
	
	public static Bitmap getHSVBrightness(Bitmap src, float[] peaks) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeGetHSVBrightness(src, peaks);
		src.recycle();
		return destBitmap;
	}
	
	public static Bitmap getHSVBrightnessHorizontally(Bitmap src, float[] peaks) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeGetHSVBrightnessHorizontally(src, peaks);
		src.recycle();
		return destBitmap;
	}
	/**
	 * 
	 * @param src - Bitmap ARGB32, RGB16 
	 * @param cnt - density 0 - 255 
	 * @return - Bitmap ARGB32
	 */
	public static Bitmap treshold(Bitmap src, int cnt) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeTreshold(src, destBitmap, cnt);
		src.recycle();
		return destBitmap;
	}
	public static Bitmap adaptiveTreshold(Bitmap src) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeAdaptiveTreshold(src, destBitmap);
		src.recycle();
		return destBitmap;
	}
	public static Bitmap convolve(Bitmap src, int[] template, int kernelCountRows, int kernelCountCols, int filterDiv, int offset) {
		Bitmap destBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		nativeConvolve(src, destBitmap, template, kernelCountRows, kernelCountCols, filterDiv, offset);
		src.recycle();
		return destBitmap;
	}
	
	public static Bitmap fullEdgeDetector(Bitmap source) {
		Bitmap destBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		nativeFullEdgeDetector(source, destBitmap);
		source.recycle();
		return destBitmap;
	}
	
	public static Bitmap wiener(Bitmap source) {
		Bitmap destBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		nativeWiener(source, destBitmap);
		source.recycle();
		return destBitmap;
	}
	
	public static Bitmap yuvToRGB(byte[] source, int width, int height) {
		Log.d("intelligence_debug","!!!!!!ok!!7!!!");
		Bitmap destBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Log.d("intelligence_debug","!!!!!!ok!!8!!!");
		nativeYuvToRGB(source, destBitmap);
		Log.d("intelligence_debug","!!!!!!ok!!9!!!");
		System.gc();
		return destBitmap;
	}
	
	public static float houghTransform(Bitmap source) {
		return nativeHoughTransform(source);
	}
}


