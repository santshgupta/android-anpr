package com.intelligence;
import intelligence.intelligence.Intelligence;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

public class intelligencyActivity extends Activity {
	public static Context cntxt;
	DrawCanvasView view;
	Preview mView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cntxt = this;
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);                                                                                           
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,                                                                         
        WindowManager.LayoutParams.FLAG_FULLSCREEN);      
        
        /**
         * The main processing engine 
         */
        
        
        mView = new Preview(this);                                                                                                                                                      
        view = new DrawCanvasView(this, mView);  
        setContentView(mView);                                                                                                                                
        addContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    
    boolean retry = false;
    @Override
    protected void onPause() {
    	super.onPause();
    	view._cThread._run = false;
		view._cThread.cancel();
		view._cThread = null;
		System.gc();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	view.refresh();
    }
    
}