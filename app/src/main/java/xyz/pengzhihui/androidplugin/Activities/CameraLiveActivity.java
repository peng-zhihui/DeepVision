package xyz.pengzhihui.androidplugin.Activities;

import android.os.Bundle;

import com.kyleduo.switchbutton.SwitchButton;

import org.opencv.core.Mat;

import xyz.pengzhihui.androidplugin.Algorithms.EdgeProcessing;
import xyz.pengzhihui.androidplugin.Envs.CameraLiveActivityBase;
import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyDrawerCamera.FancyItem;


public class CameraLiveActivity extends CameraLiveActivityBase
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // No effect
        mCameraSceneItems.put(0, new FancyItem.Builder(getApplicationContext())
                .setName("无效果")
                .setDetails("相机预览，无任何效果")
                .setIconBitmapId(R.mipmap.camera_live_icon_blank)
                .build());


        // Edge detect algorithm.
        EdgeProcessing mEdgeProcessing = new EdgeProcessing();
        mCameraSceneItems.put(1, new FancyItem.Builder(getApplicationContext())
                .setName("边缘检测")
                .setDetails("这是一个使用Canny算子进行多级边缘检测的算法")
                .setIconBitmapId(R.mipmap.camera_live_icon_canny)
                .setOnFrameListener(new FancyItem.onFrameListener()
                {
                    @Override
                    public Mat onFrame(Mat frame)
                    {
                        return mEdgeProcessing.doProcessing(frame);
                    }
                }).build());

        // ...


        commitItems(mCameraSceneItems);
    }


    @Override
    public void onScrollerItemSelected(int selectedItemIndex)
    {
        super.onScrollerItemSelected(selectedItemIndex);

        switch (selectedItemIndex)
        {
            case 0: // No effect
                break;

            case 1: // Edge detect
                break;

            case 2:

            //...
        }
    }

}
