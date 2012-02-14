package com.intelligence;

import intelligence.imageanalysis.CarSnapshot;
import intelligence.intelligence.Intelligence;

import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawCanvasView extends View implements OnTouchListener {
	
	private Bitmap 		 	b 					= Bitmap.createBitmap(450, 5000, Bitmap.Config.ARGB_8888);
	private final String 	___FILE_NAME___  	= "./test/9.jpg";
	private Paint 			paint 				= new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	
	private final static int NONE = 0; 
	private final static int DRAG = 1; 
	private int touchState = NONE;
	private int canvasViewX = 0;
	private float canvasViewY = 0;
	private float cPointerX	= 0;
	private float cPointerY	= 0;
	private float startY = 0;
	private static int  displayWidth = 0;
	private static int displayHeight = 0;
	private Intelligence systemLogic;
	private Preview preview;
	public DrawCanvasView(Context context, Preview preview) {
		super(context);
		Canvas cnv = new Canvas(b);
		DrawCanvasView.this.preview = preview;
		
		try {
			systemLogic 	= new Intelligence (true, cnv, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Activity a = (Activity)this.getContext();
		Display d = a.getWindowManager().getDefaultDisplay();
		displayWidth = d.getWidth();
		displayHeight = d.getHeight();
		
		setFocusable(true);
		setOnTouchListener(this);
	}
	

	public void refresh() {		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("intelligence_debug", "The thread was run");
				try {
					synchronized (preview.lock) {
						
						if (preview.previewBitmap == null) {
							preview.lock.wait();
						}
						//	Bitmap bmpTmp = Preview.bmp.copy(Config.ARGB_8888, true);
					
						CarSnapshot c 				= new CarSnapshot (preview.previewBitmap, 1);
						HashSet<String> number 		= systemLogic.recognize(c);
						Log.d("intelligence_debug", "recognized: " + number);
							//bmpTmp.recycle();
						
					}
				} catch (Exception e) {
					Log.e("intelligence_error", e.toString());
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(b, canvasViewX, canvasViewY, paint);
	}

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchState = DRAG;
			cPointerX = event.getX();
			cPointerY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (touchState == DRAG) {
				float pY = event.getY();
				float diffY = pY - cPointerY;
				canvasViewY = startY + diffY;
				invalidate();
			}
			break;	
		case MotionEvent.ACTION_POINTER_DOWN:
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			touchState = NONE;
			startY = canvasViewY;
			break;
		
		default:
			break;
		}
		return true;
	}
}
