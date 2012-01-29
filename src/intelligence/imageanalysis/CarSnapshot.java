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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.graphics.NativeGraphics;
import com.intelligence.Console;
import com.intelligence.ConsoleGraph;

import intelligence.imageanalysis.Challenger;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import intelligence.imageanalysis.CarSnapshotGraph;
import intelligence.imageanalysis.Graph.Peak;

public class CarSnapshot extends Photo {
	private Canvas cnv = null;
	protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static int distributor_margins = 
            Intelligence.configurator.getIntProperty("carsnapshot_distributormargins");
//    private static int carsnapshot_projectionresize_x =
//            Main.configurator.getIntProperty("carsnapshot_projectionresize_x");
//    private static int carsnapshot_projectionresize_y =
//            Main.configurator.getIntProperty("carsnapshot_projectionresize_y");
    private static int carsnapshot_graphrankfilter =
            Intelligence.configurator.getIntProperty("carsnapshot_graphrankfilter");

    static private int numberOfCandidates = Intelligence.configurator.getIntProperty("intelligence_numberOfBands");
    private CarSnapshotGraph graphHandle = null;
    
    public static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0,0,2,2);
	
    public CarSnapshot(Canvas canvas) {
    	cnv = canvas;
    	paint.setColor(Color.WHITE);
		paint.setTextSize(40);
    }
    
    public CarSnapshot(String filepath, Canvas canvas) throws IOException {
    	super(filepath);
    	cnv = canvas;
    	paint.setColor(Color.WHITE);
		paint.setTextSize(40);
    }
    
    public CarSnapshot(Bitmap bi, Canvas canvas) {
        super(bi);
        cnv = canvas;
        paint.setColor(Color.WHITE);
		paint.setTextSize(40);
    }

    //public Bitmap renderGraph() {
    //    this.computeGraph(this.image);
    //    return graphHandle.renderVertically(100, this.getHeight() - 100);
    //}    
    
    private ArrayList<Graph.Peak> computeGraph(Bitmap img) {
    	Bitmap imageCopy = Photo.duplicateImage(img);
    	graphHandle = this.histogram(imageCopy);
        graphHandle.rankFilter(carsnapshot_graphrankfilter);
        graphHandle.applyProbabilityDistributor(distributor);
        // We find two candidate
        graphHandle.findPeaks(2, 2, .20f); 
        imageCopy.recycle();
        return graphHandle.peaks;
    }
    
    
    public ArrayList<Band> getBands() {
    	ArrayList<Band> out = new ArrayList<Band>();
    	CopyOnWriteArrayList<Challenger> out2 = new CopyOnWriteArrayList<Challenger>();
    	
    	int imageWidth 	= this.image.getWidth();
    	int step 		= 20;
    	int countPlates = 4;
    	// Value in percents, show coincidence between two challenger-images
    	float stickyCoef = 0.2f;
    	int imageWidthIteration = imageWidth / step;
    	int imageLength = imageWidthIteration * step;
    	
    	
    	//Preprocessing for source image
    	Bitmap dest = NativeGraphics.convert565to8888(image);
    	dest = verticalEdgeBi(dest); 
    	dest = NativeGraphics.treshold(dest, 150);
    	
    	ConsoleGraph cGraph = Intelligence.console.createConsoleGraph(image);
    	
		for (int i = 0; i < imageLength - step; i += step) {
    		
			Bitmap bi = Bitmap.createBitmap(dest, i, 0, step, dest.getHeight());
    		
    		graphHandle = this.histogram(bi);
            graphHandle.rankFilter(carsnapshot_graphrankfilter);
            graphHandle.applyProbabilityDistributor(distributor);
            
            for (Peak p : graphHandle.findPeaks(numberOfCandidates, 6, .55f)) {
            	
            	cGraph.drawLine(i, p.center);
            	
            	boolean isValidPeak = false;
            	for (Challenger elm : out2) {
            		if (elm.addPeak(p, i)) {
            			isValidPeak = true;
            		} else if ((elm.getStep() < (i - step)) && elm.elems.size() < countPlates ) {
            			out2.remove(elm);
            		}
            	}
            	if (!isValidPeak) {
            		Challenger chlgr = new Challenger(p, i, step);
            		out2.add(chlgr);
            	}
            }
    	}
		
		LinkedList<Challenger> out3 = new LinkedList<Challenger>();
		for (Challenger elm : out2) {
			float elmSizeX = elm.maxX - elm.minX;
			float elmSizeY = elm.maxY - elm.minY;
			
			if (elm.elems.size() < countPlates)
				continue;
			if ((elm.maxX <= elm.minX) || (elm.maxY <= elm.minY)) 
				continue;			
			if (elmSizeX / elmSizeY < 1) 
				continue;
			
			boolean isOk = false;
			for (Challenger elm2 : out3) {
				float elm2SizeX = elm2.maxX - elm2.minX;
				float elm2SizeY = elm2.maxY - elm2.minY;
				float diffX 	= 0;
				float diffY 	= 0;
				if (elm2.maxY > elm.maxY) {
					diffY = elm.maxY - elm2.minY;
				} else {
					diffY = elm2.maxY - elm.minY;
				}
				if (elm2.maxX > elm.maxX) {
					diffX = elm.maxX - elm2.minX;
				} else {
					diffX = elm2.maxX - elm.minX;
				}
				if (diffY > 0 && diffX > 0 && 
				   (((diffY / elm2SizeY) > stickyCoef) || ((diffY / elmSizeY) > stickyCoef)) && 
				   (((diffX / elm2SizeX) > stickyCoef) || ((diffX / elmSizeX) > stickyCoef)) ) {
					
					elm2.maxX = Math.max(elm.maxX, elm2.maxX);
					elm2.minX = Math.min(elm.minX, elm2.minX);
					elm2.maxY = Math.max(elm.maxY, elm2.maxY);
					elm2.minY = Math.min(elm.minY, elm2.minY);
					isOk = true;
				}
			}
			if (isOk == false) {
				out3.add(elm);
			}
		}
		int amplify = 3;
		for (Challenger elm : out3) {
        	Bitmap bi = Bitmap.createBitmap( dest, 
        			Math.max(0, elm.minX - amplify), 
        			Math.max(0, elm.minY - amplify), 
        			Math.max(1,	elm.maxX - elm.minX + amplify) , 
        			Math.max(1,	elm.maxY - elm.minY + amplify));
        	for (Graph.Peak p : computeGraph(bi)) {
                Bitmap bi2 = Bitmap.createBitmap(image, elm.minX, elm.minY + p.getLeft(), Math.max(1,bi.getWidth()) , Math.max(1,p.getDiff()));
                out.add(new Band(bi2));
            }
        }
    	out2.clear();
    	out3.clear();
        
    	return out;
    }
    
    public Bitmap verticalEdgeBi(Bitmap source) {
        int template[] = {
           -1, 0, 1,
           -1, 0, 1,
           -1, 0, 1
        };
        return NativeGraphics.convolve(source, template, 3, 3, 1, 0);
    }

    public CarSnapshotGraph histogram(Bitmap bi) {
        CarSnapshotGraph graph = new CarSnapshotGraph(this);
        float[] peaks = new float[bi.getHeight()];
        /**
         * Graph at vertical position
         */
        NativeGraphics.getHSVBrightness(bi, peaks);
        graph.addPeaks(peaks);
        return graph;        
    }
}
