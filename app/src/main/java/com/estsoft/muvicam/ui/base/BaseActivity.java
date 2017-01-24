package com.estsoft.muvicam.ui.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estsoft.muvicam.MuvicamApplication;
import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.injection.module.ActivityModule;

import timber.log.Timber;

/**
 * Created by jaylim on 12/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

  ActivityComponent mActivityComponent;

  protected ActivityComponent getActivityComponent() {
    return mActivityComponent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set fullscreen mode
    setupFullscreen();
    setUpDecorView();

    mActivityComponent = MuvicamApplication.get(this)
        .getApplicationComponent()
        .plus(new ActivityModule(this));
  }

  @Override
  protected void onResume() {
    super.onResume();
    startBackgroundThread();
    hideDecorView();
  }

  @Override
  protected void onPause() {
    stopBackgroundThread();
    super.onPause();
  }

  public void setupFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  public void setFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  View mDecorView;
  private final static int DEFAULT_UI_SETTING = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
      | View.SYSTEM_UI_FLAG_FULLSCREEN      // hide status bar
      | View.SYSTEM_UI_FLAG_IMMERSIVE;

  Runnable hideDecorView;

  public void setUpDecorView() {
    mDecorView = getWindow().getDecorView();

    mDecorView.setOnSystemUiVisibilityChangeListener(visibility -> {
      Timber.e("onSystemUiVisibilityChange %08x / %08x", visibility, DEFAULT_UI_SETTING);
      int xor = DEFAULT_UI_SETTING ^ visibility;
      if (xor != 0 && mBackgroundHandler != null) {
        mBackgroundHandler.postDelayed(this::hideDecorView, 1500);
        // TODO - sync with top scroll bar.
      }
    });
  }

  public void hideDecorView() {
    new Handler(getMainLooper()).post(
        () -> mDecorView.setSystemUiVisibility(DEFAULT_UI_SETTING)
    );
  }

  // HANDLER ////////////////////////////////////////////////////////////////////////

  private final static String BACKGROUND_HANDLER_THREAD = "BACKGROUND_HANDLER";

  HandlerThread mBackgroundThread;
  Handler mBackgroundHandler;

  private void startBackgroundThread() {
    mBackgroundThread = new HandlerThread(BACKGROUND_HANDLER_THREAD);
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  private void stopBackgroundThread() {
    mBackgroundThread.quitSafely();
    try {
      // Waits forever for this thread to die.
      mBackgroundThread.join();
      mBackgroundThread = null;
      mBackgroundHandler.removeCallbacksAndMessages(null);
      mBackgroundHandler = null;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
