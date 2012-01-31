package intelligence.imageanalysis;

import intelligence.imageanalysis.Graph.Peak;

import java.util.ArrayList;

/**
 * @author azhdanov
 */
public class Challenger {
	

	public ArrayList< ArrayList<Integer> > elems = new ArrayList< ArrayList<Integer> >();
	public ArrayList<Integer> steps = new ArrayList<Integer>();
	
	public int minX;
	public int maxX;
	
	public int minY;
	public int maxY;
	public int delta;
	

	// < 12 - fail
	// 12 - is ideal
	// 15 - is maximum
	// > 15 - fail
	public final int COEF = 12;
	
	public Challenger(Peak p, int step, int delta) { 
		
		ArrayList<Integer> l = new ArrayList<Integer>();
		l.add(p.getCenter());
		elems.add(l);
		steps.add(step);
		minX = step;
		maxX = step + this.delta;
		minY = p.left;
		maxY = p.right;	
		this.delta = delta;
	}
	
	
	public int getStep() {
		return steps.get(steps.size() - 1);
	}


	public boolean addPeak(Peak p, int step) {
		ArrayList<Integer> lastPoints = elems.get(elems.size() - 1);
		if (getStep() == (step - this.delta)) {
			for (int lastPoint : lastPoints) {
				if (Math.abs(lastPoint - p.center) <= COEF) {
					steps.add(step);
					ArrayList<Integer> l = new ArrayList<Integer>();
					l.add(p.getCenter());
					elems.add(l);
					maxX = Math.max(maxX, step + this.delta);
					minY = Math.min(minY, p.left);
					maxY = Math.max(maxY, p.right);	
					return true;
				}
			}
		} else if (getStep() == step && elems.size() > 1) {
			ArrayList<Integer> prevPoints = elems.get(elems.size() - 2);
			for (int prevPoint : prevPoints) {
				if (Math.abs(prevPoint - p.center) <= COEF) {
					lastPoints.add(p.getCenter());
					maxX = Math.max(maxX, step + this.delta);
					minY = Math.min(minY, p.left);
					maxY = Math.max(maxY, p.right);	
					return true;
				}
			}
		}
		return false;
	}
}
