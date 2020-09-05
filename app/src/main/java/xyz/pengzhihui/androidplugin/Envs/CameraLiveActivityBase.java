package xyz.pengzhihui.androidplugin.Envs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;

import java.util.HashMap;

import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.androidplugin.Utils.Logger;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyDrawerCamera.FancyDrawerCameraActivityBase;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyDrawerCamera.FancyItem;


public abstract class CameraLiveActivityBase extends FancyDrawerCameraActivityBase
{
    protected static final Logger LOGGER = new Logger();

    protected CameraBridgeViewBase mCameraManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setCameraPreviewView(R.layout.layout_camera_preview);

        mCameraManager = findViewById(R.id.opencv_camera_view);
        mCameraManager.setVisibility(View.VISIBLE);
        mCameraManager.setScaleType(true, true); //是否可以缩放（1：1显示 or 等比放大）
        mCameraManager.disableFpsMeter();
        mCameraManager.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mCameraManager.setMaxFrameSize(Configuration.PREVIEW_WIDTH, Configuration.PREVIEW_HEIGHT);
        mCameraManager.setCvCameraViewListener(new CameraFrameListener(this, mCameraManager));

        mButtonSwitchCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int cameraID = (mCameraManager.getCameraIndex() == CameraBridgeViewBase.CAMERA_ID_FRONT) ?
                        CameraBridgeViewBase.CAMERA_ID_BACK : CameraBridgeViewBase.CAMERA_ID_FRONT;
                mCameraManager.setCameraIndex(cameraID);
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mCameraManager != null)
            mCameraManager.disableView();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mCameraManager != null)
            mCameraManager.disableView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (OpenCVLoader.initDebug())
            LOGGER.i("OpenCV library found inside package. Using it!");
        else
            LOGGER.i("Internal OpenCV library not found, initialization failed...");
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mCameraManager.isFpsMeterEnabled())
                    mCameraManager.disableFpsMeter();
                else
                    mCameraManager.enableFpsMeter();
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                break;

            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }

        return true;
    }

    protected int getCurrentSceneIndex()
    {
        return mCurrentSceneIndex;
    }

    protected HashMap<Integer, FancyItem> getSceneItems()
    {
        return mCameraSceneItems;
    }

    protected void commitItems(HashMap<Integer, FancyItem> items)
    {
        mChooseItemScroller.commitItems(items);
    }


}
