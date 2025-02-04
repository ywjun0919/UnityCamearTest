package com.abc.cameratest;

import java.io.File;

import com.unity3d.player.*;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class UnityPlayerActivity extends Activity {
	protected UnityPlayer mUnityPlayer; // don't change the name of this
										// variable; referenced from native code

	private CameraHandler m_CameraHandle = null;

	// Setup activity layout
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia
														// play happy

		UnityMessenger.AddMessageCenter("Main Camera");
		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();
		m_CameraHandle = new CameraHandler(this);
		createPath();
	}
	
	private void createPath() {
		String path = getExternalFilesDir();
		 File destDir = new File(path);
         if (!destDir.exists())
         {
                 destDir.mkdirs();
         }
	}

	public CameraHandler getCameraHander() {
		return m_CameraHandle;
		
	}
	
	// Quit Unity
	@Override
	protected void onDestroy() {
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override
	protected void onPause() {
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override
	protected void onResume() {
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mUnityPlayer.injectEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mUnityPlayer.injectEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mUnityPlayer.injectEvent(event);
	}

	/* API12 */ public boolean onGenericMotionEvent(MotionEvent event) {
		return mUnityPlayer.injectEvent(event);
	}
	
	public boolean HasExternalSDCard() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	public String getExternalStorageDirectory() {
		if (HasExternalSDCard()) {
			return Environment.getExternalStorageDirectory().toString();
		} else {
			return "null";
		}
	}

	public String getExternalFilesDir() {
		if (HasExternalSDCard()) {
			Log.v("Unity", "getExternalFilesDir hasexternalsdcard dir = " + this.getExternalFilesDir(null).getAbsolutePath());
			return this.getExternalFilesDir(null).getAbsolutePath();
		} else {
			Log.v("Unity", "getExternalFilesDir do not hasexternalsdcard dir = " + this.getFilesDir().getAbsolutePath());
			return this.getFilesDir().getAbsolutePath();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		m_CameraHandle.onActivityResult(requestCode,resultCode,data);
	}
}
