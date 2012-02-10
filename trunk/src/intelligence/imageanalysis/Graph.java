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

//import java.util.Collections;
//import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Graph {
    public class Peak {
        public int left, center, right;
        public Peak(int left, int center, int right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }
        public Peak(int left, int right) {
            this.left = left;
            this.center = (left+right)/2;
            this.right = right;
        }        
        public int getLeft() {
            return this.left;
        }
        public int getRight() {
            return this.right;
        }
        public int getCenter() {
            return this.center;
        }
        public int getDiff() {
            return this.right - this.left;
        }
        public void setLeft(int left) {
            this.left = left;
        }
        public void setCenter(int center) {
            this.center = center;
        }
        public void setRight(int right) {
            this.right = right;
        }
    }
    static public class ProbabilityDistributor {
        float center;
        float power;
        int leftMargin;
        int rightMargin;
        public ProbabilityDistributor(float center, float power, int leftMargin, int rightMargin) {
            this.center = center;
            this.power = power;
            this.leftMargin = Math.max(1,leftMargin);
            this.rightMargin = Math.max(1,rightMargin);
        }
        
        private float distributionFunction(float value, float positionPercentage) {
            return value * (1 - this.power * Math.abs(positionPercentage - this.center) );
        }
        
        public ArrayList<Float> distribute(ArrayList<Float> peaks) {
        	ArrayList<Float> distributedPeaks = new ArrayList<Float>();
            for (int i=0; i<peaks.size(); i++) {
                if (i < leftMargin || i > peaks.size() - rightMargin) {
                    distributedPeaks.add(0f);
                } else {
                    distributedPeaks.add(distributionFunction(peaks.get(i),
                            ((float)i/peaks.size())
                            ));
                }
            }
            
            return distributedPeaks;
        }
    }
    
    public ArrayList<Peak> peaks = null;
    public ArrayList<Float> yValues = new ArrayList<Float>();
    // statistical informations
    private boolean actualAverageValue = false; // su hodnoty aktualne ?
    private boolean actualMaximumValue = false; // su hodnoty aktualne ?
    private boolean actualMinimumValue = false; // su hodnoty aktualne ?
    private float averageValue;
    private float maximumValue;
    private float minimumValue;
    
    void deActualizeFlags() {
        this.actualAverageValue = false;
        this.actualMaximumValue = false;
        this.actualMinimumValue = false;
    }
    
    boolean allowedInterval(ArrayList<Peak> peaks, int xPosition) {
        for (Peak peak : peaks)
            if (peak.left <= xPosition && xPosition <= peak.right) return false;
        return true;
    }
    
    public void addPeaks(float[] values) {
    	Float data[] = new Float[values.length];
    	for (int i = 0; i < values.length; i++) {
			data[i] = values[i];
		}
    	Collections.addAll(yValues, data);
    	this.deActualizeFlags();
    }
    public void applyProbabilityDistributor(Graph.ProbabilityDistributor probability) {
        this.yValues = probability.distribute(this.yValues);
        this.deActualizeFlags();
    }
    public void negate() {
        float max = this.getMaxValue();
        for (int i=0; i<this.yValues.size(); i++)
            this.yValues.set(i, max - this.yValues.get(i));
        this.deActualizeFlags();
    }
    
    
    float getAverageValue() {
        if (!this.actualAverageValue) {
            this.averageValue = getAverageValue(0,this.yValues.size());
            this.actualAverageValue = true;
        }
        return this.averageValue;
    }
    
    float getAverageValue(int a, int b) {
        float sum = 0.0f;
        for (int i=a; i<b; i++) sum+= this.yValues.get(i).doubleValue();
        return sum/this.yValues.size();
    }
    

    float getMaxValue() {
        if (!this.actualMaximumValue) {
            this.maximumValue = this.getMaxValue(0, this.yValues.size());
            this.actualMaximumValue = true;
        }
        return this.maximumValue;
    }    
    float getMaxValue(int a, int b) {    
        float maxValue = 0.0f;
        for (int i=a; i<b; i++)
            maxValue = Math.max(maxValue, yValues.get(i));
        return maxValue;    
    }
    float getMaxValue(float a, float b) {
        int ia = (int)(a*yValues.size());
        int ib = (int)(b*yValues.size());
        return getMaxValue(ia, ib);
    }

    int getMaxValueIndex(int a, int b) {    
        float maxValue = 0.0f;
        int maxIndex = a;
        for (int i=a; i<b; i++) {
            if (yValues.get(i) >= maxValue) {
                maxValue = yValues.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;    
    }    
 
    float getMinValue() {
        if (!this.actualMinimumValue) {
            this.minimumValue = this.getMinValue(0, this.yValues.size());
            this.actualMinimumValue = true;
        }
        return this.minimumValue;
    }    
    float getMinValue(int a, int b) {    
        float minValue = Float.POSITIVE_INFINITY;
        for (int i=a; i<b; i++)
            minValue = Math.min(minValue, yValues.get(i));
        return minValue;    
    }    
    float getMinValue(float a, float b) {
        int ia = (int)(a*yValues.size());
        int ib = (int)(b*yValues.size());
        return getMinValue(ia, ib);
    }
    
    
    int getMinValueIndex(int a, int b) {    
        float minValue = Float.POSITIVE_INFINITY;
        int minIndex = b;
        for (int i=a; i<b; i++) {
            if (yValues.get(i) <= minValue) {
                minValue = yValues.get(i);
                minIndex = i;
            }
        }
        return minIndex;    
    }        
//    
    
    public Bitmap renderHorizontally(int width, int height) {
		Bitmap content = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap axis = Bitmap.createBitmap(width+40, height+40, Bitmap.Config.ARGB_8888);
        
        Canvas graphicContent = new Canvas(content);
        Canvas graphicAxis = new Canvas(axis);
        Paint paintAXIS = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintAXIS.setColor(Color.GRAY);
        Rect backRect = new Rect(0,0,width + 40, height + 40);
        graphicAxis.drawRect(backRect, paintAXIS);
        
        Paint paintContent = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintContent.setColor(Color.BLACK);
        backRect = new Rect(0,0,width,height);
        graphicContent.drawRect(backRect, paintContent);
        paintContent.setColor(Color.GREEN);
        
        float x,y,x0,y0;
        x=0;y=0;
        
        for (int i=0; i<this.yValues.size(); i++) {
            x0=x; y0=y;
            x =  (int) ( ( (float)i / this.yValues.size() ) * width );
            y =  (int) ( ( (float) 1 - (this.yValues.get(i) / this.getMaxValue())) * height );
            graphicContent.drawLine(x0, y0, x, y, paintContent);
        }
        
        if (this.peaks != null) { // uz boli vyhladane aj peaky, renderujeme aj tie
        	paintContent.setColor(Color.RED);
            int i = 0;
            double multConst = (double)width / this.yValues.size();
            for (Peak p : this.peaks) {
            	graphicContent.drawLine((int)(p.left * multConst),  0,  (int)(p.center * multConst),30, paintContent);
                graphicContent.drawLine((int)(p.center * multConst), 30,  (int)(p.right * multConst),0, paintContent);
                graphicContent.drawText((i++)+"." ,(int)(p.center * multConst) -5, 42, paintContent);
            }
        }
        
        graphicAxis.drawBitmap(content,35,5, paintAXIS);
        
        paintAXIS.setColor(Color.BLACK);
        
        for (int ax = 0; ax < content.getWidth(); ax += 50) {
            graphicAxis.drawText(new Integer(ax).toString() , ax + 35, axis.getHeight()-10, paintAXIS);
            graphicAxis.drawLine(ax+35, content.getHeight()+5 ,ax+35, content.getHeight()+15, paintAXIS);
        }
        
        for (int ay = 0; ay < content.getHeight(); ay += 20) {
            graphicAxis.drawText(
                    new Integer(new Float((1-(float)ay/content.getHeight())*100).intValue()).toString() + "%"
                    , 1 ,ay + 15, paintAXIS);
            graphicAxis.drawLine(25,ay+5,35,ay+5, paintAXIS);
        }        
        return axis;
    }
    
    public Bitmap renderVertically(int width, int height) {
        Bitmap content = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap axis = Bitmap.createBitmap(width+10, height+40, Bitmap.Config.ARGB_8888);
        
        Canvas graphicContent = new Canvas(content);
        Canvas graphicAxis = new Canvas(axis);
        
        
        Paint paintAXIS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paintAXIS.setColor(Color.GRAY);
        Rect backRect = new Rect(0,0,width + 40, height + 40);
        graphicAxis.drawRect(backRect, paintAXIS);
        
        Paint paintContent = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paintContent.setColor(Color.WHITE);
        backRect = new Rect(0,0,width,height);
        graphicContent.drawRect(backRect, paintContent);
        
        int x,y,x0,y0;
        x=width;y=0;

        paintContent.setColor(Color.GREEN);
        
        for (int i=0; i<this.yValues.size(); i++) {
            x0=x; y0=y;
            y = (int) ( ( (float)i / this.yValues.size() ) * height );
            x = (int) ( ( (float)  (this.yValues.get(i) / this.getMaxValue())) * width );
            graphicContent.drawLine(x0, y0, x, y, paintContent);
        }
        
        if (this.peaks != null) { 
        	paintContent.setColor(Color.RED);
            int i = 0;
            double multConst = (double)height / this.yValues.size();
            for (Peak p : this.peaks) {
                graphicContent.drawLine(width,(int)(p.left * multConst),  width-30,  (int)(p.center * multConst), paintContent);
                graphicContent.drawLine(width-30, (int)(p.center * multConst), width, (int)(p.right * multConst), paintContent);
                graphicContent.drawText((i++)+"." ,width-38, (int)(p.center * multConst)+5, paintContent);
            }
        }
        graphicAxis.drawBitmap(content, 5 ,5, paintAXIS);
        paintAXIS.setColor(Color.BLACK);
        for (int ax = 0; ax < content.getWidth(); ax += 50) {
            graphicAxis.drawText(new Integer(ax).toString() , ax + 35, axis.getHeight()-10, paintAXIS);
            graphicAxis.drawLine(ax+35, content.getHeight()+5 ,ax+35, content.getHeight()+15, paintAXIS);
        }
        
        for (int ay = 0; ay < content.getHeight(); ay += 20) {
            graphicAxis.drawText(
                    new Integer(new Float((1-(float)ay/content.getHeight())*100).intValue()).toString() + "%"
                    , 1 ,ay + 15, paintAXIS);
            graphicAxis.drawLine(25,ay+5,35,ay+5, paintAXIS);
        }        
        return axis;
    }
    
    
    public void rankFilter(int size) {
        int halfSize = size/2;
        ArrayList<Float> clone = new ArrayList<Float>(this.yValues);
        
        for (int i = halfSize; i < this.yValues.size() - halfSize;  i++) {
            float sum = 0;
            for (int ii = i - halfSize; ii<i+halfSize; ii++) {
                sum+=clone.get(ii);
            }
            this.yValues.set(i, sum / size);
        }
        /**
         * Clear not averaged values
         */
        for (int i = 0; i < halfSize;  i++) {
        	this.yValues.set(i, .0f);
        }
        for (int i = (this.yValues.size() - 1); i >= this.yValues.size() - halfSize;  i--) {
        	this.yValues.set(i, .0f);
        }
    }
    
    public int indexOfLeftPeakRel(int peak, double peakFootConstantRel, int koef) {
        int index = peak;
        int endl = 0;
       
        for (int i = peak; i > endl; i -= koef) {
        	float averageIndex = 0;
        	int delim = 0;
        	if (i >= endl + koef) {
        		for (int i2 = i; i2 > i-koef; i2--) {
        			averageIndex += yValues.get(i2);
        			delim++;
        		}
        	} else {
        		for (int i2 = i; i2 > endl; i2--) {
        			averageIndex += yValues.get(i2);
        			delim++;
        		}
        	}
        	averageIndex /= Math.max(1, delim);
            if (averageIndex < peakFootConstantRel * yValues.get(peak) ) {
            	index = i;
                break;
            } else {
            	index = Math.max(endl, i - koef);
            }
        }
        return Math.max(endl, index);
    }
    public int indexOfRightPeakRel(int peak, double peakFootConstantRel, int koef) {
        int index=peak;
        int endl = yValues.size() - 1;
        
        for (int i = peak; i < endl; i += koef) {
        	float averageIndex = 0;
        	int delim = 0;
        	if (i <= endl - koef) {
        		for (int i2 = i; i2 < i+koef; i2++) {
        			averageIndex += yValues.get(i2);
        			delim++;
        		}
        	} else {
        		for (int i2 = i; i2 < endl; i2++) {
        			averageIndex += yValues.get(i2);
        			delim++;
        		}
        	}
        	averageIndex /= Math.max(1, delim);
            index = i;
            if (averageIndex < peakFootConstantRel * yValues.get(peak) ) {
            	index = i;
                break;
            } else {
            	index = Math.min(endl, i + koef);
            }
        }
        return Math.min(endl, index);
    }

    
    public float averagePeakDiff(Vector<Peak> peaks) { // not used
        float sum = 0;
        for (Peak p : peaks)
            sum+= p.getDiff();
        return sum/peaks.size();
    }
    public float maximumPeakDiff(Vector<Peak> peaks, int from, int to) { 
        float max = 0;
        for (int i=from; i<=to; i++)
            max = Math.max(max,peaks.elementAt(i).getDiff());
        return max;
    }
    
    
}


