package intelligence.imageanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import intelligence.intelligence.Intelligence;


    public class BandGraph extends Graph {/* TODO - BEGIN */
        Band handle;
        
        private static double peakFootConstant = 
                Intelligence.configurator.getDoubleProperty("bandgraph_peakfootconstant"); //0.75
        private static double peakDiffMultiplicationConstant = 
                Intelligence.configurator.getDoubleProperty("bandgraph_peakDiffMultiplicationConstant");  // 0.2

        
        public BandGraph(Band handle) {
            this.handle = handle;
        }
        
        public class PeakComparer implements Comparator<Object> {
        	ArrayList<Float> yValues = null;
            
            public PeakComparer(ArrayList<Float> yValues) {
                this.yValues = yValues;
            }
            
            private float getPeakValue(Object peak) {
                return this.yValues.get( ((Peak)peak).getCenter()  );
            }
            
            public int compare(Object peak1, Object peak2) { 
                double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
                if (comparison < 0) return -1;
                if (comparison > 0) return 1;
                return 0;
            }
        }
        
        public ArrayList<Peak> findPeaks(int count, int useNearValue) {
        	ArrayList<Graph.Peak> outPeaks = new ArrayList<Peak>();
            
            for (int c = 0; c < count; c++) {
                float maxValue = .3f;
                int maxIndex = 0;
                boolean found = false;
                for (int i=0; i<this.yValues.size(); i++) {
                    if (allowedInterval(outPeaks, i)) {
                    	Float p = this.yValues.get(i);
                        if (p > maxValue) {
                            maxValue = p;
                            maxIndex = i;
                            found = true;
                        }
                    }
                } 
                
                if (!found)
                	continue;
                
                int leftIndex = indexOfLeftPeakRel(maxIndex, outPeaks, 0.20, 6);
                int rightIndex = indexOfRightPeakRel(maxIndex, outPeaks, 0.20, 6);
               
                leftIndex  -= leftIndex * 0.1;
                rightIndex += rightIndex * 0.1;
                outPeaks.add(new Peak(
                        Math.max(0,leftIndex),
                        maxIndex,
                        Math.min(this.yValues.size() - 1, rightIndex)
                        ));
            }
            
            ArrayList<Peak> outPeaksFiltered = new ArrayList<Peak>();
            
            for (Peak p : outPeaks) {
            	if ((p.getDiff() > 2 * this.handle.getHeight()) &&
                    (p.getDiff() < 8 * this.handle.getHeight()))  {
            		outPeaksFiltered.add(p);
                }
            }
            
            Collections.sort(outPeaksFiltered, (Comparator<? super Graph.Peak>)
                                               new PeakComparer(this.yValues));
            super.peaks = outPeaksFiltered;
            return outPeaksFiltered;
            
        }
        public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
            int index=peak;
            for (int i=peak; i>=0; i--) {
                index = i;
                if (yValues.get(index) < peakFootConstantAbs  ) break;
            }
            return Math.max(0,index);
        }
        public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
            int index=peak;
            for (int i=peak; i<yValues.size(); i++) {
                index = i;
                if (yValues.get(index) < peakFootConstantAbs ) break;
            }
            return Math.min(yValues.size(), index);
        }
    }
