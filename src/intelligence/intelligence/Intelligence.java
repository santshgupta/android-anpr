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


package intelligence.intelligence;

import intelligence.configurator.Configurator;
import java.io.File;
import java.util.Vector;

import com.graphics.NativeGraphics;
import com.intelligence.Console;
import com.intelligence.intelligencyActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import intelligence.imageanalysis.Band;
import intelligence.imageanalysis.CarSnapshot;
import intelligence.imageanalysis.Char;
import intelligence.imageanalysis.Plate;
import intelligence.recognizer.CharacterRecognizer;
import intelligence.recognizer.CharacterRecognizer.RecognizedChar;
import intelligence.recognizer.NeuralPatternClassificator;
import intelligence.recognizer.KnnPatternClassificator;

public class Intelligence {
	
	public static Console console;
	public static Configurator configurator = new Configurator("."+File.separator+"config.xml", intelligencyActivity.cntxt);
    public static CharacterRecognizer chrRecog;
    public static Parser parser;
    public boolean enableReportGeneration;
    protected static Canvas canvas; 
	protected static View mainView;
	protected static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private long lastProcessDuration = 0;
    
    public Intelligence(boolean enableReportGeneration, Canvas cnv, View cnvView) throws Exception {
    	int classification_method = Intelligence.configurator.getIntProperty("intelligence_classification_method");
    	
    	mainView = cnvView;
    	canvas = cnv;
    	paint.setColor(Color.WHITE);
		paint.setTextSize(40);
    	this.enableReportGeneration = enableReportGeneration;
    	
    	/**
    	 * Visual console logger
    	 */
    	console = new Console(mainView, canvas);
    	console.console("Processing was started! Please wait few minutes...");
    	if (classification_method == 0) {
            chrRecog = new KnnPatternClassificator();
    		console.console("KNN classificator has created!");
    	} else {
            chrRecog = new NeuralPatternClassificator();
            console.console("Neural classificator has created!");
    	}
        parser = new Parser();
        console.console("Parser has created!");
    }
    
    public long lastProcessDuration() {
        return this.lastProcessDuration;
    }
    
    public String recognize(CarSnapshot carSnapshot) throws Exception {
    	console.console("Recognize");
    	Log.d("intelligence_debug", "recognize:");
	    int syntaxAnalysisMode = Intelligence.configurator.getIntProperty("intelligence_syntaxanalysis");
        int skewDetectionMode = Intelligence.configurator.getIntProperty("intelligence_skewdetection");
        
        if (enableReportGeneration) {
        	console.console("Automatic Number Plate Recognition Report");
        	console.console("Image width: "+carSnapshot.getWidth()+" px");
        	console.console("Image height: "+carSnapshot.getHeight()+" px");
        }
        int p = 0;
        console.console("getBands");
        for (Band b : carSnapshot.getBands()) { //doporucene 3
        	if (enableReportGeneration) {
        		console.console("Band width : "+b.getWidth()+" px");
        		console.console("Band height : "+b.getHeight()+" px");
            }
            for (Plate plate : b.getPlates()) {//doporucene 3
            	if (enableReportGeneration) {
            		console.console("Plate width : "+plate.getWidth()+" px");
            		console.console("Plate height : "+plate.getHeight()+" px");
                }   
            	//console.consoleBitmap(plate.image);
            	
            	Plate notNormalizedCopy = null;
                
            	if (skewDetectionMode != 0) {
                	notNormalizedCopy = plate.clone();
                	notNormalizedCopy.image = notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
                	float houghSkew = NativeGraphics.houghTransform(notNormalizedCopy.image);
                	Bitmap source = plate.getBi();
                	
                
                	Matrix m = new Matrix();
                	if (enableReportGeneration) {
                		console.console("skew : " + houghSkew + " px");
                	}
                	m.setSkew(0, houghSkew);
                    Bitmap core = Bitmap.createBitmap (source, 0, 0, source.getWidth(), source.getHeight(), m, false);
                    plate = new Plate(core);
                    source.recycle();
                } 
            	//console.consoleBitmap(plate.image);
                plate.normalize();
                console.consoleBitmap(plate.plateCopy.image);
                //continue;
                
                float plateWHratio = (float)plate.getWidth() / (float)plate.getHeight();
                console.console("plate w: " + plate.getWidth() + " plate h: " + plate.getHeight() + " plateWHratio: " + plateWHratio);
                if (plateWHratio < Intelligence.configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio")
                ||  plateWHratio > Intelligence.configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio")
                ) continue;
                
                Vector<Char> chars = plate.getChars();
                
                if (chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars") ||
                chars.size() > Intelligence.configurator.getIntProperty("intelligence_maximumChars")
                ) continue;
                
                if (plate.getCharsWidthDispersion(chars) > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthDispersion")
                ) continue;
                
                // SKEW-RELATED
                if (enableReportGeneration) {
                	console.console("Skew detection");
                }
                RecognizedPlate recognizedPlate = new RecognizedPlate();
                if (enableReportGeneration) {
                	console.console("Character segmentation");
                }
                for (Char chr : chars) {
                	chr.normalize();
                }
                
                float averageHeight = plate.getAveragePieceHeight(chars);
                float averageContrast = plate.getAveragePieceContrast(chars);
                float averageBrightness = plate.getAveragePieceBrightness(chars);
                float averageHue = plate.getAveragePieceHue(chars);
                float averageSaturation = plate.getAveragePieceSaturation(chars);
                for (Char chr : chars) {
                	//chr.saveImage("./test/444" + chr.toString() + ".jpg");
                	Intelligence.console.consoleBitmap(chr.image);
                    
                	boolean ok = true;
                    String errorFlags = "";
                    
                    float widthHeightRatio = (float)(chr.pieceWidth);
                    widthHeightRatio /= (float)(chr.pieceHeight);
                    
                    if (widthHeightRatio < Intelligence.configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio") ||
                        widthHeightRatio > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthHeightRatio")) {
                        
                    	errorFlags += "WHR (" + widthHeightRatio + ") ";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    } else {
                    	Intelligence.console.console("WHR: " + widthHeightRatio);
                    }
                    
                    if ((chr.positionInPlate.x1 < 2 || chr.positionInPlate.x2 > plate.getWidth() - 1) && widthHeightRatio < 0.12) {
                        errorFlags += "POS ";
                        ok = false;
                        if ( ! enableReportGeneration) 
                        	continue;
                    }
                    
                    float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
                    float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
                    float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
                    float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
                    float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;
                    
                    // Intelligence.console.console("avh: " + averageHeight);
                    if (brightnessCost > Intelligence.configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
                        errorFlags += "BRI ";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    }
                    if (contrastCost > Intelligence.configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
                        errorFlags += "CON ";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    }
                    if (hueCost > Intelligence.configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
                        errorFlags += "HUE ("+hueCost+")";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    }
                    if (saturationCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
                        errorFlags += "SAT ";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    }
                    if (heightCost < -Intelligence.configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
                        errorFlags += "HEI ";
                        ok = false;
                        if (!enableReportGeneration) 
                        	continue;
                    }
                    
                    float similarityCost = 0;
                    RecognizedChar rc = null;
                    if (ok==true) {
                    	rc = Intelligence.chrRecog.recognize(chr);
                    	//Intelligence.console.consoleBitmap(rc.render());
                        similarityCost = rc.getPatterns().elementAt(0).getCost();
                        if (similarityCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
                            errorFlags += "NEU ";
                            ok = false;
                            if (!enableReportGeneration) continue;
                        }
                    }
                    if (ok == true) {
                    	recognizedPlate.addChar(rc);
                    }
                    if (enableReportGeneration) {
                    	/*
                    	console.console("WHR = "+widthHeightRatio);
                    	console.console("HEI = "+heightCost);
                    	console.console("NEU = "+similarityCost);
                    	console.console("CON = "+contrastCost);
                    	console.console("BRI = "+brightnessCost);
                    	console.console("HUE = "+hueCost);
                    	console.console("SAT = "+saturationCost);
						*/
                        if (errorFlags.length()!=0) {
                        	console.console("errflags = "+errorFlags);
                        }
                     }
                } 
                if (recognizedPlate.chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars")) 
                	continue;
                
                String parsedOutput = parser.parse(recognizedPlate, syntaxAnalysisMode);
                if (enableReportGeneration) {
                	console.console("_RECOGNIZED_ : " + parsedOutput);
                }
                return parsedOutput; 
            }
        }
        return null;
    }
    
}