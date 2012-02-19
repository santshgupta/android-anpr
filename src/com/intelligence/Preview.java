package com.intelligence;

import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	public boolean makeSnapshot = true;
	public Camera.Size cs = null;
	private SurfaceHolder mHolder;
	public Camera camera;
	protected Object lock = new Object();
	public byte[] previewBitmapData;
	
	Preview(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
 
	public void surfaceCreated(SurfaceHolder holder) {

		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder); 
			camera.setPreviewCallback(new PreviewCallback() {
 
				public void onPreviewFrame(final byte[] data, Camera arg1) {
					synchronized (lock) {
						previewBitmapData = data;
					}
				}
			});
		} catch (IOException exception) {
            camera.release();
            camera = null;
        }
	}
 
	public void surfaceDestroyed(SurfaceHolder holder) {
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
		for (Camera.Size s : sizes) {
			if (s.width > cs.width) {
				cs = s;
			}
		}
		parameters.setPreviewFormat(ImageFormat.NV21);
		parameters.setPreviewFrameRate(14);
		parameters.setPreviewSize(cs.width,cs.height);
		camera.setParameters(parameters);
		camera.startPreview();
	}
}
