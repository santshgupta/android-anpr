package com.intelligence;

import intelligence.intelligence.Intelligence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.graphics.NativeGraphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";
	public boolean makeSnapshot = true;
	private ExecutorService pool;
	private Camera.Size cs = null;
	SurfaceHolder mHolder;
	public Camera camera;
	boolean retry = false;
	protected Object lock = new Object();
	public byte previewBitmapData[] = null;
	
	Preview(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
 
	public void surfaceCreated(SurfaceHolder holder) {

		camera = Camera.open();
		pool = Executors.newFixedThreadPool(5);
		try {
			camera.setPreviewDisplay(holder); 
			camera.setPreviewCallback(new PreviewCallback() {
 
				public void onPreviewFrame(final byte[] data, Camera arg1) {
					
					if (retry)
	        			  return;
					if (makeSnapshot == true) {
						makeSnapshot = false;
				
						pool.submit(new Runnable() {
							
							@Override
							public void run() {
								Camera.Parameters parameters = camera.getParameters();
								int format = parameters.getPreviewFormat();
								if (format == ImageFormat.NV21)
								{
							    	int w = parameters.getPreviewSize().width;
							    	int h = parameters.getPreviewSize().height;
							    	Intelligence.console.consoleBitmap(NativeGraphics.yuvToRGB(data, w, h));
							    	Intelligence.console.console("OK!");
							    	/*
							    	YuvImage yuv_image = new YuvImage(data, format, w, h, null);
							    	Rect rect = new Rect(0, 0, w, h);
									ByteArrayOutputStream pBmpStream = new ByteArrayOutputStream();
									yuv_image.compressToJpeg(rect, 100, pBmpStream);
									synchronized (lock) {
										previewBitmap = BitmapFactory.decodeByteArray(pBmpStream.toByteArray(), 0, pBmpStream.size());
										lock.notify();
									}*/
								}
							}
						});
					}
				}
			});
		} catch (IOException exception) {
            camera.release();
            camera = null;
        }
	}
 
	public void surfaceDestroyed(SurfaceHolder holder) {
		retry = true;
		pool.shutdown();
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
		parameters.setPreviewFrameRate(14);
		parameters.setPreviewSize(cs.width,cs.height);
		camera.setParameters(parameters);
		camera.startPreview();
	}
}
