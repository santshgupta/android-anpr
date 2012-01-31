package com.intelligence;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ConsoleGraph {
	private Canvas canvas;
	private int initPosX = 0;
	private int initPosY = 0;
	protected View mainView = null;
	private int step = 0;
	private Paint paintContent = new Paint(Paint.FILTER_BITMAP_FLAG);
    
	public ConsoleGraph (View view, Canvas c, int x, int y, int step) {
		this.step = step;
		this.canvas = c;
		this.mainView = view;
		this.initPosX = x;
		this.initPosY = y;
		this.paintContent.setColor(Color.MAGENTA);
		this.paintContent.setStyle(Paint.Style.STROKE);
		this.paintContent.setStrokeWidth(2);
	}
	
	public void drawLine (int x, int y) {
		this.canvas.drawLine(initPosX + x, initPosY + y, initPosX + x + step, initPosY + y, paintContent);
		redrawMainView();
	}
	
	private void redrawMainView() {
    	((Activity)(mainView.getContext())).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mainView.invalidate();
			}
		});
    }
}
