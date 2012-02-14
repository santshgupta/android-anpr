package com.intelligence;
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
        this.getWindow().setWindowAnimations(0);  
        
        /**
         * The main processing engine 
         */
        
        
        mView = new Preview(this);                                                                                                                                                      
        view = new DrawCanvasView(this, mView);                                                                                                                      
        view.refresh();
        setContentView(mView);                                                                                                                                
        addContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
}