package intelligence.imageanalysis;

/**
 * 
 * http://code.google.com/p/jjil/
 * http://www.faqs.org/faqs/graphics/colorspace-faq/
 * Adapted by zdanchik.ru
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.graphics.NativeGraphics;

import intelligence.intelligence.Intelligence;
import jjil.android.RgbImageAndroid;
import jjil.core.RgbImage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class Photo {
	public Bitmap image;
	// RGB to Luminance conversion constants as found on
	// Charles A. Poynton's colorspace-faq:
	// http://www.faqs.org/faqs/graphics/colorspace-faq/
	private static float r_lum = .212671F;
	private static float g_lum = .715160F;
	private static float b_lum = .072169F;
	//private RgbImage prevRgbImage = null;
	//private static RgbImage mRgbImage = null;
	//private static RgbImage mSecRgbImage = null;

	
	public Photo() {
        this.image = null;
    }
	
	public Photo(Bitmap bi) {
        this.image = bi;
    }
	
	public Photo(String filepath) throws IOException {
        this.loadImage(filepath);
    }
	
	public Photo clone() {
        return new Photo(Photo.duplicateImage(this.image));
    }
	
	public int getWidth() {
        return this.image.getWidth();
    }
    public int getHeight() {
        return this.image.getHeight();
    }
    public int getSquare() {
        return this.getWidth() * this.getHeight();
    }
    
    public Bitmap getBi() {
        return this.image;
    }
    
    public void setBrightness(int x, int y, float value) {
    	int r = image.getPixel(x, y);
        float[] hsv = new float[3];
        Color.colorToHSV(r, hsv);
        float[] hsv2 = { hsv[0], hsv[1], value };
    	image.setPixel(x, y, Color.HSVToColor(hsv2));
    }
    
    static public void setBrightness(Bitmap image, int x, int y, float value) {
    	int r = image.getPixel(x, y);
        float[] hsv = new float[3];
        Color.colorToHSV(r, hsv);
        float[] hsv2 = { hsv[0], hsv[1], value };
        int c = Color.HSVToColor(hsv2);
    	image.setPixel(x, y, c);
    }
    
    /**
     * http://en.wikipedia.org/wiki/HSL_and_HSV
     * @param image
     * @param x
     * @param y
     * @return
     */
    static public float getBrightness(Bitmap image, int x, int y) {
        int r = image.getPixel(x, y);
        float[] hsv = new float[3];
        Color.colorToHSV(r, hsv);
        return hsv[2];
    }
    
    /**
     * http://en.wikipedia.org/wiki/HSL_and_HSV
     * @param image
     * @param x
     * @param y
     * @return
     */
    static public float getSaturation(Bitmap image, int x, int y) {
    	int r = image.getPixel(x, y);
        float[] hsv = new float[3];
        Color.colorToHSV(r, hsv);
        return hsv[1];
    }
	
    static public float getHue(Bitmap image, int x, int y) {
    	int r = image.getPixel(x, y);
        float[] hsv = new float[3];
        Color.colorToHSV(r, hsv);
        return hsv[0] / 360;
    }
    
    public float getBrightness(int x, int y) {
        return getBrightness(image, x, y);
    }
    
    public float getSaturation(int x, int y) {
        return getSaturation(image, x, y);
    }
    
    public float getHue(int x, int y) {
        return getHue(image, x, y);
    }
    
    /**
     * Adapted britness.
     * @param coef
     */
    public void normalizeBrightness(float coef) {
        Statistics stats = new Statistics(this);
        for (int x=0; x < this.getWidth(); x++) {
            for (int y=0; y < this.getHeight(); y++) {
                Photo.setBrightness(this.image, x, y,
                        stats.thresholdBrightness(Photo.getBrightness(this.image,x,y), coef)
                        );
            }
        }
    }
    
    /**
     * TODO Convolving 
     * @param mat
     * @param rows
     * @param cols
     */
    /*
    public void convolve(RgbImage i, double[] mat, int rows, int cols, int filterDiv, int offset) {
    	int w = i.getWidth();
    	int h = i.getHeight();
    	if((rows % 2) == 0 || (cols % 2) == 0) {
    	} else {
    		int[] rgbData = i.getData();
    		int[] conv = new int[w * h];
    		int sumR = 0;
    		int sumG = 0;
    		int sumB = 0;
    		for (int x = (cols-1) / 2; x < w - (cols + 1) / 2; x++) {
    			for (int y = (rows-1) / 2; y < h - (rows + 1) / 2; y++) {
				    sumR=0;
				    sumG=0;
				    sumB=0;
				    for (int x1 = 0; x1 < cols; x1++) {
			    		for (int y1 = 0; y1 < rows; y1++) {
		    				int x2 = (x - (cols - 1) / 2 + x1);
						    int y2 = (y-(rows-1)/2+y1);
						    int R = ((rgbData[y2 * w + x2] >> 16) & 0xff);
						    int G = ((rgbData[y2 * w + x2] >> 8) & 0xff);
						    int B = ((rgbData[y2 * w + x2]) & 0xff);
						    sumR += R * (mat[y1*cols+x1]);
						    sumG += G * (mat[y1*cols+x1]);
						    sumB += B * (mat[y1*cols+x1]);
					    }
			    	}
				    sumR = (sumR / filterDiv) + offset; 
				    sumG = (sumG / filterDiv) + offset; 
				    sumB = (sumB / filterDiv) + offset; 

				    sumR = (sumR > 255) ? 255 : ((sumR < 0) ? 0:sumR); 
				    sumG = (sumG > 255)? 255 : ((sumG < 0)? 0:sumG); 
				    sumB = (sumB > 255)? 255 : ((sumB < 0)? 0:sumB); 
				    
			    	conv[y * w + x] = 0xff000000 | ((int)sumR << 16 | (int)sumG << 8 |
					(int)sumB);
    			}
    		}
    		System.arraycopy(
    				conv,
    				0,
    				rgbData,
    				0,
    				w * h);
    	}
    }
    */
    
    public static void sobel(RgbImage i, float [] template) {
	    int templateSize=3;
	    int[] rgbData = i.getData();
	    int[] total = new int[i.getWidth() * i.getHeight()];
	    int sumY=0;
	    int sumX=0;
	    int max=0;
	    //for ( int n = 0; n<1; n++) {
		    for ( int x = (templateSize - 1) / 2; x < i.getWidth() - (templateSize + 1) / 2; x++) {
		    	for (int y = (templateSize - 1) / 2; y < i.getHeight() - (templateSize + 1) / 2; y++) {
		    		sumY=0;
		    		for (int x1 = 0; x1 < templateSize; x1++) {
		    			for (int y1 = 0; y1 < templateSize; y1++) {
		    				int x2 = ( x - (templateSize - 1) / 2 + x1);
		    				int y2 = ( y - (templateSize - 1) / 2 + y1);
		    				float value = (rgbData[ y2 * i.getWidth() + x2] & 0xff) * (template [y1 * templateSize + x1]);
		    				sumY += value;
		    			}
		    		}
		    		sumX = 0;
		    		for ( int x1 = 0; x1 < templateSize; x1++) {
		    			for (int y1 = 0; y1 < templateSize; y1++) {
		    				int x2 = ( x - (templateSize - 1) / 2 + x1);
		    				int y2 = ( y - (templateSize - 1) / 2 + y1);
		    				float value = (rgbData[ y2 * i.getWidth() + x2] & 0xff) * (template[ x1 * templateSize + y1]);
		    				sumX += value;
		    			}
		    		}
		    		total[y * i.getWidth() + x] = (int)Math.sqrt(sumX * sumX + sumY * sumY);
		    		if(max < total[ y * i.getWidth() + x])
		    			max = total[y * i.getWidth() + x];
		    	}
		    }
		    float ratio = (float)max /255;
		    for (int x = 0; x < i.getWidth(); x++) {
		    	for(int y = 0; y < i.getHeight(); y++) {
		    		sumX = (int)(total[y * i.getWidth() + x] / ratio);
		    		total[y * i.getWidth() + x] = 0xff000000 | ((int)sumX << 16 | (int)sumX << 8 | (int)sumX);
		    	}
		    }
	   // }
	    System.arraycopy(
			total,
			0,
			rgbData,
			0,
			i.getWidth() * i.getHeight());
    }


    
    public void verticalEdgeDetector(Bitmap source) {
    	int[] template={	-1, 0, 1,
				    	 	-2, 0, 2,
				    	 	-1, 0, 1};
    	source = NativeGraphics.convolve(source, template, 3, 3, 1, 0);
    }
    
	public void loadImage(String filepath) throws IOException {
		
		try {
			File source = new File(android.os.Environment.getExternalStorageDirectory(), filepath);
            BitmapFactory.Options o = new BitmapFactory.Options();
            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(source), null, o);
            int width_tmp = bmp.getWidth();
            int height_tmp = bmp.getHeight();
            if (width_tmp > 640 || height_tmp > 480) {
            	double averageImg = (double)width_tmp / (double)height_tmp;    
            	width_tmp = 640;
            	height_tmp = (int)((double)width_tmp / averageImg);
            }
            this.image = averageResizeBi(bmp, width_tmp, height_tmp);
            bmp.recycle();
        }
		catch (FileNotFoundException e) {
        	Intelligence.console.console("Input image file not found: " + filepath);
        	throw new FileNotFoundException("file not found!");
        }
	}
	
	public static Bitmap duplicateImage(Bitmap image) {
		return image.copy(Bitmap.Config.ARGB_8888, true);
    }
	
	public void averageResize(int width, int height) {
        this.image = averageResizeBi(this.image, width, height);
    }

	public Bitmap averageResizeBi(Bitmap origin, int width, int height) {
        return Bitmap.createScaledBitmap(origin, width, height, false);                   
    }
	
	static public Bitmap createBlankBi(Bitmap b) {
	    return Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
	}
	public Bitmap createBlankBi(int width, int height) {
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}
	
	    
    public void plainThresholding(Statistics stat) {
        int w = this.getWidth();
        int h = this.getHeight();
        for (int x=0; x<w; x++) {
            for (int y=0;y<h; y++) {
                this.setBrightness(x,y,stat.thresholdBrightness(this.getBrightness(x,y),1.0f));
            }
        }
    }
    
    public void adaptiveThresholding() { // 	
        this.image = NativeGraphics.adaptiveTreshold(this.image);
    }
    
    public float[][] bitmapImageToArrayWithBounds(Bitmap image, int w, int h) {
        float[][] array = new float[w+2][h+2];

        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                array[x+1][y+1] = Photo.getBrightness(image,x,y);
            }
        }
        // vynulovat hrany :
        for (int x=0; x<w+2; x++) {
            array[x][0] = 1;
            array[x][h+1] = 1;
        }
        for (int y=0; y<h+2; y++) {
            array[0][y] = 1;
            array[w+1][y] = 1;
        }
        return array;
    } 
    
    public float[][] bitmapImageToArray(Bitmap image, int w, int h) {
        float[][] array = new float[w][h];
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                array[x][y] = Photo.getBrightness(image,x,y);
            }
        }
        return array;
    }
    
    static public Bitmap arrayToBitmapImage(float[][] array, int w, int h) {
        Bitmap bi = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                Photo.setBrightness(bi,x,y,array[x][y]);
            }
        }
        return bi;
    }    
}

	
	

