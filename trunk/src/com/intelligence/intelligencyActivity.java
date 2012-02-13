package com.intelligence;
import intelligence.imageanalysis.CarSnapshot;
import intelligence.intelligence.Intelligence;

import java.io.IOException;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class intelligencyActivity extends Activity {
	public static Context cntxt;
	Preview mView;
	DrawCanvasView view;
	public static Intelligence systemLogic = null;
	private final String 	___FILE_NAME___  	= "./test/9.jpg";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cntxt = this;
        /*
        try {
			systemLogic 	= new Intelligence (true);
			CarSnapshot c 				= new CarSnapshot (___FILE_NAME___);
			HashSet<String> number 		= systemLogic.recognize(c);
			Log.d("intelligence_debug", "recognized: " + number);
		} catch (IOException e) {
			Log.e("intelligence_error", e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("intelligence_error", e.toString());
			e.printStackTrace();
		}
		
        */
     // Stylized window in full-screen mode
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
		// Disable animations Important! only after "addContentView"!
		this.getWindow().setWindowAnimations(0);
        /**
         * The main processing engine 
         */
        view = new DrawCanvasView(this);
        mView = new Preview(this, view);
        
        setContentView(mView);
        addContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	
    	switch (newConfig.orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				break;
		
			default:
				break;
		}    	
    }
    
    
}