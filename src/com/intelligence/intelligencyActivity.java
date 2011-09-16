package com.intelligence;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class intelligencyActivity extends Activity {
	public static Context cntxt;
	DrawCanvasView mView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cntxt = this;
        /**
         * The main processing engine 
         */
        mView = new DrawCanvasView(this);
        mView.refresh();
        setContentView(mView);
    }
}