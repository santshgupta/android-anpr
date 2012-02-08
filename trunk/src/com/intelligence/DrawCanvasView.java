package com.intelligence;

import intelligence.imageanalysis.CarSnapshot;
import intelligence.intelligence.Intelligence;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawCanvasView extends View implements OnTouchListener {
	
	private Bitmap 		 	b 					= Bitmap.createBitmap(350, 2000, Bitmap.Config.ARGB_8888);
	private final String 	___FILE_NAME___  	= "./test/test007.jpg";
	private Paint 			paint 				= new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private Canvas 			mainCanvas 			= null;
	private View 			view;
	private Context 		cntxt 				= null;
	
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
	
	public DrawCanvasView(Context context) {
		super(context);
		cntxt = context;
		view  = this;
		
		Activity a = (Activity)view.getContext();
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
				Canvas cnv = new Canvas(b);
				try {
					Intelligence systemLogic 	= new Intelligence (true, cnv, view);
					CarSnapshot c 				= new CarSnapshot (___FILE_NAME___, cnv);
					String number 				= systemLogic.recognize(c);
					Log.d("intelligence_debug", "recognized: " + number);
				} catch (IOException e) {
					Log.e("intelligence_error", e.toString());
					e.printStackTrace();
				} catch (Exception e) {
					Log.e("intelligence_error", e.toString());
					e.printStackTrace();
				}
				
				synchronized (cntxt) {
					while (mainCanvas == null ) {
						Log.d("intelligence_debug", "wait canvas");
						/**
						 * We Wait until the main canvas is initialized!
						 */
						try {
							cntxt.wait();
						} catch (InterruptedException e) {
							Log.e("intelligence_error", e.toString());
						}
					}
				}	
				
				Activity a = (Activity)getContext();
				a.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Log.d("intelligence_debug", "redraw canvas!");
						invalidate();
					}
				});
			}
		});
		t.start();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		synchronized (cntxt) {
			if (mainCanvas == null) {
				mainCanvas = canvas;
				try {
					Log.d("intelligence_debug", "Canvas loaded! Notifyng!");
					cntxt.notify();
				} catch (IllegalMonitorStateException e) {
					Log.e("intelligence_error", e.toString());
				}
			}
		}
		mainCanvas.drawBitmap(b, canvasViewX, canvasViewY, paint);
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
