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
import java.util.Collections;
import java.util.Comparator;
import intelligence.intelligence.Intelligence;


public class PlateVerticalGraph extends Graph {
    private static double peakFootConstant =// 0.42;  /* CONSTANT*/
            Intelligence.configurator.getDoubleProperty("plateverticalgraph_peakfootconstant");
    
    Plate handle;
    
    public PlateVerticalGraph(Plate handle) {
        this.handle = handle;
    }
    
    public class PeakComparer implements Comparator<Object> {
        PlateVerticalGraph graphHandle = null;
        
        public PeakComparer(PlateVerticalGraph graph) {
            this.graphHandle = graph;
        }
        
        private float getPeakValue(Object peak) {
            return this.graphHandle.yValues.get(((Peak)peak).getCenter());
        }
        
        public int compare(Object peak1, Object peak2) { // Peak
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }
    
    public ArrayList<Peak> findPeak(int count) {
        for (int i=0; i<this.yValues.size();i++)
            this.yValues.set(i,this.yValues.get(i) - this.getMinValue());
        ArrayList<Peak> outPeaks = new ArrayList<Peak>();
        for (int c=0; c<count; c++) {
            float maxValue = .2f;
            int maxIndex = 0;
            boolean found = false;
            for (int i=0; i<this.yValues.size(); i++) {
                if (allowedInterval(outPeaks, i)) {
                    if (this.yValues.get(i) >= maxValue) {
                        maxValue = this.yValues.get(i);
                        maxIndex = i;
                        found = true;
                    }
                }
            }
            
            if (!found)
            	continue;
            
            if (yValues.get(maxIndex) < 0.05 * super.getMaxValue()) break;//0.4
            
            int leftIndex = indexOfLeftPeakRel(maxIndex, peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex, peakFootConstant);
            
            outPeaks.add(new Peak(
                    Math.max(0,leftIndex),
                    maxIndex,
                    Math.min(this.yValues.size(), rightIndex)
                    ));
        }
        
        Collections.sort(outPeaks, (Comparator<? super Graph.Peak>)
                                   new PeakComparer(this));
        super.peaks = outPeaks;
        return outPeaks;
    }       
    
    public int indexOfLeftPeakRel(int peak, double peakFootConstantRel) {
        int index=peak;
        for (int i=peak; i>=0; i--) {
            index = i;
            if (yValues.get(index) <= peakFootConstantRel*yValues.get(peak) ) 
            	break;
        }
        return Math.max(0, index);
    }
    
    public int indexOfRightPeakRel(int peak, double peakFootConstantRel) {
        int index=peak;
        for (int i=peak; i<yValues.size(); i++) {
            index = i;
            if (yValues.get(index) <= peakFootConstantRel*yValues.get(peak) ) 
            	break;
        }
        return Math.min(yValues.size(), index+1);
    }
}
