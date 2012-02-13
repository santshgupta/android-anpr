package com.intelligence;

import intelligence.imageanalysis.CarSnapshot;
import intelligence.intelligence.Intelligence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import com.intelligence.Preview.CommonThread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";
	private Camera.Size cs = null;
	SurfaceHolder mHolder;
	public Camera camera;
	private CommonThread _cThread;
	private Intelligence systemLogic;
	protected Bitmap bmpData = null;
	private DrawCanvasView dcv = null;
	boolean retry = false;
	
	/**
	 * Дополнительный поток, в котором необходимо осуществлять работу с SurfaceView
	 */
	public class CommonThread extends Thread {
	    public boolean _run = false;
	 
	    public CommonThread() {
	    }
	 
	    @Override
	    public void run() {
	        //Canvas c;
	        while (_run) {
	            //c = null;
	            try {
	            	//if (bmpData != null) {
	        			//try {
	        				//CarSnapshot cs = new CarSnapshot ("./test/9.jpg");
		        			//if (cs.image == null) {
		        			//	return;
		        			//}
		        			//HashSet<String> number 		= systemLogic.recognize(cs);
							//Log.d(TAG, "__RECOGNIZED: " + number.toString());
						//} catch (Exception e) {
							/// TODO Auto-generated catch block
							//e.printStackTrace();
						//}
	            	//}
	                //c = Preview.this.getHolder().lockCanvas(null);
	                //synchronized (Preview.this.getHolder()) {
	                //	//Preview.this.onDraw(c);
	               // }
	            } finally {
	                //if (c != null) {
	                //	Preview.this.getHolder().unlockCanvasAndPost(c);
	                //}
	            }
	        }
	    }
	}
	
	Preview(Context context, DrawCanvasView dcv) {
		super(context);
		Preview.this.dcv = dcv; 
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
 
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();
		
		//try {
		//	systemLogic 	= new Intelligence (true);
		//} catch (Exception e) {
		//	Log.e("intelligence_error", e.toString());
		//	e.printStackTrace();
		//}
		
		//this._cThread = new CommonThread();
		//this._cThread._run = true;
		//this._cThread.start();
		try {
			camera.setPreviewDisplay(holder);
 
			camera.setPreviewCallback(new PreviewCallback() {
 
				public void onPreviewFrame(byte[] data, Camera arg1) {
					 if ( (dcv == null) || retry )
	        			  return;
					//Camera.Parameters parameters = camera.getParameters();
					//if (parameters == null)
					//	return;
					
					//int format = parameters.getPreviewFormat();

					// YUV formats require more conversion
					/*
					if (format == ImageFormat.NV21)
					{
				    	int w = parameters.getPreviewSize().width;
				    	int h = parameters.getPreviewSize().height;
				    	// Get the YuV image
				    	YuvImage yuv_image = new YuvImage(data, format, w, h, null);
				    	// Convert YuV to Jpeg
						Rect rect = new Rect(0, 0, w, h);
						ByteArrayOutputStream pBmpStream = new ByteArrayOutputStream();
						yuv_image.compressToJpeg(rect, 100, pBmpStream);
						bmpData  = BitmapFactory.decodeByteArray(pBmpStream.toByteArray(), 0, pBmpStream.size());
					}*/
					Log.d(TAG, "!!!!!! OK !!!!!");
					/*// Jpeg and RGB565 are supported by BitmapFactory.decodeByteArray
					else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565)
					{
						mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					}*/
					dcv.invalidate();
				}
			});
		} catch (IOException exception) {
            camera.release();
            camera = null;
        }
	}
 
	public void surfaceDestroyed(SurfaceHolder holder) {
		retry = true;
        //_cThread._run = false;
        //while (retry) {
        //  try {
            //    _cThread.join();
              //  retry = false;
            //} catch (InterruptedException e) {}
        //}
        camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}
 
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes(); 
		cs = sizes.get(0); 
		parameters.setPreviewFormat(ImageFormat.NV21);
		parameters.setPreviewSize(cs.width,cs.height);
		camera.setParameters(parameters);
		camera.startPreview();
	}
}
