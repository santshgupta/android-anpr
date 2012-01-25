package com.intelligence;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class Console {
	protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	protected Canvas canvas = null;
	protected View mainView = null;
	public int cHeight = 20;
	public int cWidth = 20;
	
	private final int SPACING = 20;
	
	public Console (View mv, Canvas cnv) {
		this.canvas = cnv;
		this.mainView = mv;
		this.paint.setColor(Color.WHITE);
		this.paint.setTextSize(14);
		this.canvas.drawText("___Intelligence visual console___", cWidth, cHeight, paint);
		cHeight += (SPACING + 10);
        redrawMainView();
	}
	
	
	public void console(String str) {
		Log.d("intelligence_debug", str);
    	this.canvas.drawText(str, cWidth, cHeight, this.paint);
    	cHeight += SPACING;
        redrawMainView();
    }
    
    synchronized public void consoleBitmap(Bitmap bmp) {
		this.canvas.drawBitmap(bmp, cWidth, cHeight, this.paint);
    	cHeight += bmp.getHeight() + 10;
        redrawMainView();
	}
    synchronized public void consoleBitmap(Bitmap bmp, Bitmap bmp2) {
		this.canvas.drawBitmap(bmp, cWidth, cHeight, this.paint);
		this.canvas.drawBitmap(bmp2, cWidth + bmp.getWidth() + cWidth, cHeight, this.paint);
    	cHeight += bmp.getHeight() + 10;
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
