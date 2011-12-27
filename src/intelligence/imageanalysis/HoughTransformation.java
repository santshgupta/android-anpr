/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
*/
package intelligence.imageanalysis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

//import javaAndroid.awt.Color;
//import javaAndroid.awt.Graphics2D;
//import javaAndroid.awt.Point;
//import javaAndroid.awt.image.BufferedImage;

public class HoughTransformation {
    public static int RENDER_ALL = 1;
    public static int RENDER_TRANSFORMONLY = 0;
    public static int COLOR_BW = 0;
    public static int COLOR_HUE = 1;
    
    float[][] bitmap;
    Point maxPoint;
    private int width;
    private int height;
    
    public float angle = 0;
    public float dx = 0;
    public float dy = 0;
    
    public HoughTransformation(int width, int height) {
        this.maxPoint = null;
        this.bitmap = new float[width][height];
        this.width = width;
        this.height = height;
        for (int x=0; x<this.width; x++)
            for (int y=0; y<this.height; y++)
                this.bitmap[x][y] = 0;
    }

    public void addLine(int x, int y, float brightness) {
        // posunieme suradnicovu sustavu do stredu : -1 .. 1, -1 .. 1
        float xf = 2*((float)x)/this.width - 1;
        float yf = 2*((float)y)/this.height - 1;
        // y=ax + b
        // b = y - ax

        for (int a=0; a<this.width;a++) {     
            // posunieme a do stredu
            float af = 2*((float)a)/this.width - 1;
            // vypocitame b
            float bf = yf - af * xf;
            // b posumieme do povodneho suradnicoveho systemu
            int b = (int)(  (bf+1)*this.height/2  );
            
            if (0 < b && b < this.height-1) {
                bitmap[a][b] += brightness;
            }
        }
    }
  
    public float getMaxValue() {
        float maxValue = 0;
        for (int x=0; x<this.width; x++) 
            for (int y=0; y<this.height; y++)
                maxValue=Math.max(maxValue, this.bitmap[x][y]);
        return maxValue;
    }
    
    private Point computeMaxPoint() {
        float max = 0;
        int maxX = 0, maxY = 0;
        for (int x = 0; x < this.width; x++)  {
            for (int y = 0; y < this.height; y++) {
                float curr = this.bitmap[x][y];
                if (curr >= max) {
                    maxX = x;
                    maxY = y;
                    max = curr;
                }
            }
        }
        return new Point(maxX, maxY);
    }
    public Point getMaxPoint() {
        if (this.maxPoint == null) this.maxPoint = this.computeMaxPoint();
        return this.maxPoint;
    }
    
    private float getAverageValue() {
        float sum = 0;
        for (int x=0; x<this.width; x++) 
            for (int y=0; y<this.height; y++)
                sum+=this.bitmap[x][y];
        return sum/(this.width*this.height);
    }    
    
    public Bitmap render(int renderType, int colorType) {
        float average = this.getAverageValue();
        Bitmap output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[this.width * this.height];
        int elmIdx = 0;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int value = (int)(255 * this.bitmap[x][y] / average / 3);
                value = Math.max(0, Math.min(value,255));
                if (colorType == HoughTransformation.COLOR_BW) {
                	pixels[elmIdx] = Color.rgb(value, value, value);
                } else {
                	float hsv[] = {0.67f - ((float)value/255)*2/3, 1.0f, 1.0f};
                	pixels[elmIdx] = Color.HSVToColor(hsv);
                }
                elmIdx++;
            }
        }
        output.setPixels(pixels, 0, this.width, 0, 0, this.width, this.height);
        this.maxPoint = computeMaxPoint();
        
        float a = 2*((float)this.maxPoint.x)/this.width - 1;
        float b = 2*((float)this.maxPoint.y)/this.height - 1;
        
        float x0f = -1;
        float y0f = a * x0f + b;
        float x1f = 1;
        float y1f = a * x1f + b;
        
        int y0 = (int)( (y0f+1)*this.height/2  );
        int y1 = (int)( (y1f+1)*this.height/2  );

        int dx = this.width;
        int dy = y1 - y0;
        this.dx = dx;
        this.dy = dy;
        this.angle = (float) (180 * Math.atan(this.dy/this.dx) / Math.PI);
        return output;
    }
}
