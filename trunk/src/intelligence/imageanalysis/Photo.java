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
import java.io.FileOutputStream;
import java.io.IOException;
import com.graphics.NativeGraphics;
import intelligence.intelligence.Intelligence;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;

public class Photo {
	public Bitmap image;
	public Bitmap originalImage = null;

	public Photo() {
        this.image = null;
    }
	
	public Photo(Bitmap bi) {
        this.image = bi;
    }
	
	public Photo(String filepath) throws IOException {
        this.loadImage(filepath);
    }
	
	public Photo(Bitmap bmp, int i) {
		
		int width_tmp = bmp.getWidth();
        int height_tmp = bmp.getHeight();
        
        //float coef = 0.7f;
        //int width_resized = (int)(width_tmp * coef);
        //int height_resized = (int)(height_tmp * coef);
        //int x_resized = (width_tmp - width_resized) / 2;
        //int y_resized = (height_tmp - height_resized) / 2;
        //bmp = Bitmap.createBitmap (bmp, x_resized, y_resized, width_resized, height_resized);
        
        if (width_tmp > 800 || height_tmp > 600) {
        	double averageImg = (double)width_tmp / (double)height_tmp; 
        	if (averageImg > 1) {
        		width_tmp = 800;
            	height_tmp = (int)((double)width_tmp / averageImg);
        	} else {
        		height_tmp = 600;
        		width_tmp = (int)((double)height_tmp * averageImg);
        	}
        	this.originalImage = duplicateImage(bmp);
        }
        this.image = averageResizeBi(bmp, width_tmp, height_tmp);
        bmp.recycle();
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
    
    
    public void saveImage(String fName) {
    	String path = Environment.getExternalStorageDirectory().toString();
    	File file = new File(path, fName);	
    	FileOutputStream out = null;
		try {
			file.createNewFile();
			out = new FileOutputStream(file);
			this.image.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			//Intelligence.console.console(e.toString());
		} catch (IOException e) {
			//Intelligence.console.console(e.toString());
		}
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
            
            float coef = 0.7f;
            int width_resized = (int)(width_tmp * coef);
            int height_resized = (int)(height_tmp * coef);
            int x_resized = (width_tmp - width_resized) / 2;
            int y_resized = (height_tmp - height_resized) / 2;
            bmp = Bitmap.createBitmap (bmp, x_resized, y_resized, width_resized, height_resized);
            
            if (width_tmp > 800 || height_tmp > 600) {
            	double averageImg = (double)width_tmp / (double)height_tmp; 
            	if (averageImg > 1) {
            		width_tmp = 800;
                	height_tmp = (int)((double)width_tmp / averageImg);
            	} else {
            		height_tmp = 600;
            		width_tmp = (int)((double)height_tmp * averageImg);
            	}
            	this.originalImage = duplicateImage(bmp);
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
    
    public void adaptiveThresholding() { 	
        this.image = NativeGraphics.adaptiveTreshold(this.image);
    }
    
    public void winner() { 	
    	this.image = NativeGraphics.wiener(this.image);
    }
    
    public float[][] bitmapImageToArrayWithBounds(Bitmap image, int w, int h) {
        float[][] array = new float[w+2][h+2];

        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                array[x+1][y+1] = Photo.getBrightness(image,x,y);
            }
        }
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

	
	

