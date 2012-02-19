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

import intelligence.intelligence.Intelligence;
import intelligence.recognizer.CharacterRecognizer;
import java.io.IOException;
import java.util.Vector;
import android.graphics.Bitmap;

public class Char extends Photo {
    
    public boolean normalized = false;
    public PositionInPlate positionInPlate = null;
    private PixelMap.Piece bestPiece = null;
    public int fullWidth, fullHeight, pieceWidth, pieceHeight;    
    public float statisticAverageBrightness;
    public float statisticMinimumBrightness;
    public float statisticMaximumBrightness;
    public float statisticContrast;
    public float statisticAverageHue;
    public float statisticAverageSaturation;    
    public Bitmap thresholdedImage;
    
    public Char() {
        image = null;
        init();
    }
    public Char(Bitmap bi, Bitmap thresholdedImage, PositionInPlate positionInPlate) {
        super(bi);
        this.thresholdedImage = thresholdedImage;
        this.positionInPlate = positionInPlate;
        init();
    }
    public Char(Bitmap bi) {
        this(bi,bi,null);
        init();
    }

    public Char(String filepath) throws IOException { 
        super(filepath);
        Bitmap origin = Photo.duplicateImage(this.image);
        this.adaptiveThresholding();
        this.thresholdedImage = this.image;
        this.image = origin;
        
        init();
    }
    
    public Char clone() {
        return new Char(Photo.duplicateImage(this.image),
                Photo.duplicateImage(this.thresholdedImage),
                this.positionInPlate);
    }
    
    private void init() {
        this.fullWidth = super.getWidth();
        this.fullHeight = super.getHeight();
    }
    
    public void normalize() {        
        if (normalized) 
        	return; 
        
        Bitmap colorImage = Photo.duplicateImage(this.getBi());
        this.image = this.thresholdedImage;
        PixelMap pixelMap = this.getPixelMap();        
        this.bestPiece = pixelMap.getBestPiece();        
        colorImage = getBestPieceInFullColor(colorImage, this.bestPiece);
        this.computeStatisticBrightness(colorImage);
        this.computeStatisticContrast(colorImage);
        this.computeStatisticHue(colorImage);
        this.computeStatisticSaturation(colorImage);
        
        this.image = this.bestPiece.render();        
        if (this.image == null) 
        	this.image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        
        this.pieceWidth = super.getWidth();
        this.pieceHeight = super.getHeight();        
        this.normalizeResizeOnly();
        normalized=true;
        //Intelligence.console.consoleBitmap(this.image);
    }
    
    private Bitmap getBestPieceInFullColor(Bitmap bi, PixelMap.Piece piece) {
        if (piece.width <= 0 || piece.height <= 0) return bi;
        return Bitmap.createBitmap(bi, piece.mostLeftPoint, piece.mostTopPoint, piece.width, piece.height);
    }
    
    private void normalizeResizeOnly() {
        
        int x = Intelligence.configurator.getIntProperty("char_normalizeddimensions_x");
        int y = Intelligence.configurator.getIntProperty("char_normalizeddimensions_y");
        if (x==0 || y==0) 
        	return;
        this.averageResize(x,y);
    }
    
    private void computeStatisticContrast(Bitmap bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += Math.abs(this.statisticAverageBrightness - getBrightness(bi,x,y));
            }
        }
        this.statisticContrast = sum / (w * h);
    }
    
    private void computeStatisticBrightness(Bitmap bi) {
        float sum = 0;
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                float value = getBrightness(bi,x,y);
                sum += value;
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        this.statisticAverageBrightness = sum / (w * h);
        this.statisticMinimumBrightness = min;
        this.statisticMaximumBrightness = max;
    }
    
    private void computeStatisticHue(Bitmap bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += getHue(bi,x,y);
            }
        }
        this.statisticAverageHue = sum / (w * h);
    }
    
    private void computeStatisticSaturation(Bitmap bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += getSaturation(bi,x,y);
            }
        }
        this.statisticAverageSaturation = sum / (w * h);
    }
    
    public PixelMap getPixelMap() {
        return new PixelMap(this);
    }
    
    public Vector<Double> extractEdgeFeatures() {
        int w = this.image.getWidth();
        int h = this.image.getHeight();
        double featureMatch;
        
        float[][] array = this.bitmapImageToArrayWithBounds(this.image, w, h);
        w+=2;
        h+=2;
        
        float[][] features = CharacterRecognizer.features;
        double[] output = new double[features.length*4];
        
        for (int f=0; f<features.length; f++) {
            for (int my=0; my<h-1; my++) {
                for (int mx=0; mx<w-1; mx++) {
                    featureMatch = 0;
                    featureMatch += Math.abs(array[mx][my] - features[f][0]);
                    featureMatch += Math.abs(array[mx+1][my] - features[f][1]);
                    featureMatch += Math.abs(array[mx][my+1] - features[f][2]);
                    featureMatch += Math.abs(array[mx+1][my+1] - features[f][3]);
                    
                    int bias = 0;
                    if (mx >= w/2) bias += features.length; 
                    if (my >= h/2) bias += features.length * 2;
                    output[bias+f] += featureMatch < 0.05 ? 1 : 0;
                } 
            }
        }
        Vector<Double> outputVector = new Vector<Double>();
        for (Double value : output) outputVector.add(value);
        return outputVector;
    }
    
    public Vector<Double> extractMapFeatures() {
        Vector<Double> vectorInput = new Vector<Double>();
        for (int y = 0; y<this.getHeight(); y++)
            for (int x = 0; x<this.getWidth(); x++)
                vectorInput.add(new Double(this.getBrightness(x,y)));
        return vectorInput;
    }
    
    public Vector<Double> extractFeatures() {
        int featureExtractionMethod = Intelligence.configurator.getIntProperty("char_featuresExtractionMethod");
        if (featureExtractionMethod == 0)
            return this.extractMapFeatures();
        else 
            return this.extractEdgeFeatures();
    }    
}



