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

import java.util.ArrayList;
import java.util.Vector;
import com.graphics.NativeGraphics;
import android.graphics.Bitmap;
import intelligence.intelligence.Intelligence;

public class Plate extends Photo {
    static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0,0,4,4);
    static private int numberOfCandidates = Intelligence.configurator.getIntProperty("intelligence_numberOfChars");
    private static int horizontalDetectionType = 
            Intelligence.configurator.getIntProperty("platehorizontalgraph_detectionType");    
    
    private PlateGraph graphHandle = null;
    public Plate plateCopy;
    
    /** Creates a new instance of Character */
    public Plate() {
        image = null;
    }
    
    public Plate(Bitmap bi) {
        super(bi);
        this.plateCopy = new Plate(duplicateImage(this.image), true);
        this.plateCopy.winner();
        this.plateCopy.adaptiveThresholding();
    }
    
    public Plate(Bitmap bi, boolean isCopy) {
        super(bi);
    }
    
    public Bitmap renderGraph() {
        this.computeGraph();
        return graphHandle.renderVertically(100, this.getHeight());
    }
    
    private ArrayList<Graph.Peak> computeGraph() {
        if (graphHandle != null) return graphHandle.peaks; // graf uz bol vypocitany
        graphHandle = histogram(plateCopy.getBi()); //PlateGraph graph = histogram(imageCopy); 
        graphHandle.applyProbabilityDistributor(distributor);
        graphHandle.findPeaks(numberOfCandidates);        
        //Intelligence.console.consoleBitmap(graphHandle.renderHorizontally(300, 80));
        return graphHandle.peaks;
    }    
    
    public Vector<Char> getChars() {
        Vector<Char> out = new Vector<Char>();
        for (Graph.Peak p : computeGraph()) {
            if (p.getDiff() <= 3) 
            	continue;
            out.add(new Char(
	            		Bitmap.createBitmap(image, p.getLeft(), 0, p.getDiff(), image.getHeight())  ,
	            		Bitmap.createBitmap(this.plateCopy.image, p.getLeft(), 0, p.getDiff(), image.getHeight()),  
	                    new PositionInPlate(p.getLeft(), p.getRight())
                    )
                    );
            //saveImage("./test/" + p.toString() + ".jpg");
            //Intelligence.console.consoleBitmap(Bitmap.createBitmap(this.plateCopy.image, p.getLeft(), 0, p.getDiff(), image.getHeight()));
        }
        
        return out;
    }

    public Plate clone() {
        return new Plate(Photo.duplicateImage(this.image));
    }   
         
    public void horizontalEdgeBi(Bitmap source) {
        int template[] = {
          -1,0,1
        };
        source = NativeGraphics.convolve(source, template, 1, 3, 1, 0); 
    }    
    
    public static int ppp = 0;
    public void normalize() {
        Plate clone1 = this.clone();
        clone1.verticalEdgeDetector(clone1.getBi());
        PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getBi());
        this.image = cutTopBottom(this.image, vertical);

       // Intelligence.console.consoleBitmap(vertical.renderVertically(60, 100));
        this.plateCopy.image = cutTopBottom(this.plateCopy.image, vertical);
        Plate clone2 = this.clone();
        if (horizontalDetectionType == 1) 
        	clone2.horizontalEdgeDetector(clone2.getBi());
        PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getBi());
        
        
        
        this.image = cutLeftRight(this.image, horizontal);    
        this.plateCopy.image = cutLeftRight(this.plateCopy.image, horizontal);
        clone1.image.recycle();
        clone2.image.recycle();
        clone1 = null;
        clone2 = null;        
    }
    
    private Bitmap cutTopBottom(Bitmap origin, PlateVerticalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,0f,0,0));
        Graph.Peak p = graph.findPeak(1).get(0); 
       // Intelligence.console.consoleBitmap(origin);
       // Intelligence.console.console("w: " + origin.getWidth() + " h: " + origin.getHeight() + " d: " + p.getDiff());
        
        Bitmap b = Bitmap.createBitmap(origin, 0, p.getLeft(), this.image.getWidth(), p.getDiff());
        origin.recycle();
        return b;
    }
    
    private Bitmap cutLeftRight(Bitmap origin, PlateHorizontalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,0f,2,2));
        ArrayList<Graph.Peak> peaks = graph.findPeak(3);        
        if (peaks.size()!=0) {
        	Graph.Peak p = peaks.get(0);
            Bitmap b = Bitmap.createBitmap(origin, p.getLeft(), 0, p.getDiff(), image.getHeight());
            origin.recycle();
            return b;
        }
        return origin;
    }

    
    public PlateGraph histogram(Bitmap bi) {
        PlateGraph graph = new PlateGraph(this);
        /**
         * Graph at horizontal position
         */
        float[] peaks = new float[bi.getWidth()];
        NativeGraphics.getHSVBrightnessHorizontally(bi, peaks);
        graph.addPeaks(peaks);
        return graph;
    }
    
    private PlateVerticalGraph histogramYaxis(Bitmap bi) {
        PlateVerticalGraph graph = new PlateVerticalGraph(this);
        float[] peaks = new float[bi.getHeight()];
        /**
         * Graph at vertical position
         */
        NativeGraphics.getHSVBrightness(bi, peaks);
        graph.addPeaks(peaks);
        return graph;        
    }
    
    private PlateHorizontalGraph histogramXaxis(Bitmap bi) {
        PlateHorizontalGraph graph = new PlateHorizontalGraph(this);
        /**
         * Graph at horizontal position
         */
        float[] peaks = new float[bi.getWidth()];
        NativeGraphics.getHSVBrightnessHorizontally(bi, peaks);
        graph.addPeaks(peaks);
        return graph;
    }   

    public void verticalEdgeDetector(Bitmap source) {
        int template[] = {
        	 -1,0,1,
             -2,0,2,
             -1,0,1
        };
        source = NativeGraphics.convolve(source, template, 3, 3, 1, 0);
    } 
    
    public Bitmap horizontalEdgeDetector(Bitmap source) {
       int template[] = {
    		 -1,-2,-1,
             0,0,0,
             1,2,1
        };
        return NativeGraphics.convolve(source, template, 3, 3, 1, 0);
    }    
    
    public float getCharsWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAverageCharWidth(chars);
       
        for (Char chr : chars) 
            averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
        averageDispersion /= chars.size();
        
        return averageDispersion / averageWidth;
    }
    public float getPiecesWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAveragePieceWidth(chars);
       
        for (Char chr : chars) 
            averageDispersion += (Math.abs(averageWidth - chr.pieceWidth));
        averageDispersion /= chars.size();
        
        return averageDispersion / averageWidth;
    }    
    
    public float getAverageCharWidth(Vector<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) 
            averageWidth += chr.fullWidth;
        averageWidth /= chars.size();
        return averageWidth;
    }
    
    public float getAveragePieceWidth(Vector<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) 
            averageWidth += chr.pieceWidth;
        averageWidth /= chars.size();
        return averageWidth;
    }    

    public float getAveragePieceHue(Vector<Char> chars) throws Exception {
        float averageHue = 0;
        for (Char chr : chars) {
            averageHue += chr.statisticAverageHue;
        }
        averageHue /= chars.size();
        return averageHue;
    }  
    
    public float getAveragePieceContrast(Vector<Char> chars) throws Exception {
        float averageContrast = 0;
        for (Char chr : chars) 
            averageContrast += chr.statisticContrast;
        averageContrast /= chars.size();
        return averageContrast;
    }    
    
    public float getAveragePieceBrightness(Vector<Char> chars) throws Exception {
        float averageBrightness = 0;
        for (Char chr : chars) 
            averageBrightness += chr.statisticAverageBrightness;
        averageBrightness /= chars.size();
        return averageBrightness;
    }     
    
    public float getAveragePieceMinBrightness(Vector<Char> chars) throws Exception {
        float averageMinBrightness = 0;
        for (Char chr : chars) 
            averageMinBrightness += chr.statisticMinimumBrightness;
        averageMinBrightness /= chars.size();
        return averageMinBrightness;
    }   
    
    public float getAveragePieceMaxBrightness(Vector<Char> chars) throws Exception {
        float averageMaxBrightness = 0;
        for (Char chr : chars) 
            averageMaxBrightness += chr.statisticMaximumBrightness;
        averageMaxBrightness /= chars.size();
        return averageMaxBrightness;
    }       
    
    public float getAveragePieceSaturation(Vector<Char> chars) throws Exception {
        float averageSaturation = 0;
        for (Char chr : chars) 
            averageSaturation += chr.statisticAverageSaturation;
        averageSaturation /= chars.size();
        return averageSaturation;
    }        
    
    public float getAverageCharHeight(Vector<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars)
            averageHeight += chr.fullHeight;
        averageHeight /= chars.size();
        return averageHeight;
    }
    
    public float getAveragePieceHeight(Vector<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars)
            averageHeight += chr.pieceHeight;
        averageHeight /= chars.size();
        return averageHeight;
    }    

}
