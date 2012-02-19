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

import java.util.Stack;
import java.util.Vector;

import com.graphics.NativeGraphics;

import android.graphics.Bitmap;
import android.graphics.Color;

public class PixelMap {
    private class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        boolean equals(Point p2) {
            if (p2.x == this.x && p2.y == this.y) 
            	return true;
            return false;
        }
    }
    
    private class PointSet extends Stack<Point> {
        static final long serialVersionUID = 0;
        public void removePoint(Point p) {
            Point toRemove = null;
            for (Point px : this) {
                if (px.equals(p)) toRemove = px;
            }
            this.remove(toRemove);
        }
        
    }
    
    public class PieceSet extends Vector<Piece> {
        static final long serialVersionUID = 0;
    }
    private Piece bestPiece = null;
   
   
    public class Piece extends PointSet {
        static final long serialVersionUID = 0;
        public int mostLeftPoint;
        public int mostRightPoint;
        public int mostTopPoint;
        public int mostBottomPoint;
        public int width;
        public int height;
        public int centerX;
        public int centerY;
        public float magnitude;
        public int numberOfBlackPoints;
        public int numberOfAllPoints;
        
        public Bitmap render() {
            if (numberOfAllPoints==0) return null;
            Bitmap image = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
            for (int x=this.mostLeftPoint; x<=this.mostRightPoint; x++) {
                for (int y=this.mostTopPoint; y<=this.mostBottomPoint; y++) {
                    if (matrix[x][y]) {
                    	image.setPixel(
                    			x - this.mostLeftPoint, 
                    			y - this.mostTopPoint, Color.BLACK);
                    } else {
                    	image.setPixel(
                    			x - this.mostLeftPoint, 
                    			y - this.mostTopPoint, Color.WHITE);
                    }
                }
            }
            return image;
        }

        public void createStatistics() {
            this.mostLeftPoint = this.mostLeftPoint();
            this.mostRightPoint = this.mostRightPoint();
            this.mostTopPoint = this.mostTopPoint();
            this.mostBottomPoint = this.mostBottomPoint();
            this.width = this.mostRightPoint - this.mostLeftPoint + 1;
            this.height = this.mostBottomPoint - this.mostTopPoint + 1;
            this.centerX = (this.mostLeftPoint + this.mostRightPoint) / 2;
            this.centerY = (this.mostTopPoint + this.mostBottomPoint) / 2;
            this.numberOfBlackPoints = this.numberOfBlackPoints();
            this.numberOfAllPoints = this.numberOfAllPoints();
            this.magnitude = this.magnitude();
        }
        
        public int cost() { 
            return this.numberOfAllPoints - this.numberOfBlackPoints();
        }
        
        public void bleachPiece() {
            for (Point p : this) {
                matrix[p.x][p.y] = false;
            }
        }
        
        private float magnitude() {
            return ((float)this.numberOfBlackPoints / this.numberOfAllPoints);
        }
        
        private int numberOfBlackPoints() {
            return this.size();
        }
        
        private int numberOfAllPoints() {
            return this.width * this.height;
        }
       
        private int mostLeftPoint() {
            int position = Integer.MAX_VALUE;
            for (Point p : this) position = Math.min(position, p.x);
            return position;
        }
        
        private int mostRightPoint() {
            int position = 0;
            for (Point p : this) 
            	position = Math.max(position, p.x);
            return position;
        }   
        
        private int mostTopPoint() {
            int position = Integer.MAX_VALUE;
            for (Point p : this) position = Math.min(position, p.y);
            return position;
        }
        
        private int mostBottomPoint() {
            int position = 0;
            for (Point p : this) position = Math.max(position, p.y);
            return position;
        }   
    } 
   
    boolean[][] matrix;
    private int width;
    private int height;
    
    public PixelMap(Photo bi) {
        this.matrixInit(bi);
    }
    
    void matrixInit(Photo bi) {
        this.width = bi.getWidth();
        this.height = bi.getHeight();                

        this.matrix = new boolean[this.width][this.height];
        for (int x=0; x<this.width; x++) {
    		for (int y=0; y<this.height; y++) {
                this.matrix[x][y] =  bi.getBrightness(x,y) < 0.5;
            }
        }
    }
    
    public Bitmap render() {
    	Bitmap image = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        for (int x=0; x<this.width; x++) {
            for (int y=0; y<this.height; y++) {
                if (this.matrix[x][y]) {
                    image.setPixel(x,y, Color.BLACK);
                } else {
                    image.setPixel(x,y, Color.WHITE);
                }
            }
        }
        return image;
    }
    
    public Piece getBestPiece() {
        this.reduceOtherPieces();
        if (this.bestPiece == null) return new Piece();
        return this.bestPiece;
    }
    
    private boolean seedShouldBeAdded(Piece piece, Point p) {
        if (p.x<0 || p.y<0 || p.x>=this.width || p.y>=this.height) 
        	return false;
        if (!this.matrix[p.x][p.y]) 
        	return false;
        for (Point piecePoint : piece)
            if (piecePoint.equals(p)) return false;
        return true;
    }
    
    private Piece createPiece(PointSet unsorted) {
        Piece piece = new Piece();        
        PointSet stack = new PointSet();
        stack.push(unsorted.lastElement());
        
        while(!stack.isEmpty()) {
            Point p = stack.pop();
            if (seedShouldBeAdded(piece, p)) {
                piece.add(p);
                unsorted.removePoint(p);
                stack.push(new Point(p.x+1, p.y));
                stack.push(new Point(p.x-1, p.y));
                stack.push(new Point(p.x, p.y+1));
                stack.push(new Point(p.x, p.y-1));
            }
        }
        piece.createStatistics();
        return piece;
    }
    
    public PieceSet findPieces() {
        PieceSet pieces = new PieceSet();
        PointSet unsorted = new PointSet();
        for (int x = 0; x < this.width; x++) 
            for (int y = 0; y < this.height; y++)
                if (this.matrix[x][y]) 
                	unsorted.add(new Point(x,y));
        
        while (!unsorted.isEmpty()) {
        	Piece p = createPiece(unsorted);
        	//Intelligence.console.consoleBitmap(p.render());
            pieces.add(p);
        }
        return pieces;
    }
       
    public PixelMap reduceOtherPieces() {
        if (this.bestPiece != null) return this;
        PieceSet pieces = findPieces();
        int maxCost = 0;
        int maxIndex = 0;
        for (int i=0; i<pieces.size(); i++) {
        	//Intelligence.console.consoleBitmap(pieces.elementAt(i).render());
            if (pieces.elementAt(i).cost() > maxCost) {
                maxCost = pieces.elementAt(i).cost();
                maxIndex = i;
            }
        }
        
        // a ostatne zmazeme
        for (int i = 0; i < pieces.size(); i++) {
            if (i != maxIndex) 
            	pieces.elementAt(i).bleachPiece();
        }
        if (pieces.size() != 0) 
        	this.bestPiece = pieces.elementAt(maxIndex);
        return this;
    }
}
