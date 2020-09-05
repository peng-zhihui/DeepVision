package xyz.pengzhihui.androidplugin.Envs;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.ImageUtils;

import java.util.Objects;

import xyz.pengzhihui.lib_fancy_ui_kit.FancyDrawerCamera.FancyItem;


public class CameraFrameListener implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static final String TAG = "pzh::CameraFrameListener";

    private int mFlip = 1;
    private Mat mRgba;

    private CameraLiveActivityBase mContext;
    private CameraBridgeViewBase mCameraManager;

    private FancyItem.onFrameListener mItem;
    private int mCurrentIndex = -1;

    CameraFrameListener(CameraLiveActivityBase _context, CameraBridgeViewBase _mOpenCvCameraManager)
    {
        mContext = _context;
        mCameraManager = _mOpenCvCameraManager;
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mFlip = (mCameraManager.getCameraIndex() == CameraBridgeViewBase.CAMERA_ID_BACK) ? 1 : -1;
    }

    @Override
    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRgba = inputFrame.rgba();

        // Rotate 90 degrees.
        ImageUtils.RotateMat90Native(mRgba.getNativeObjAddr(), mFlip);


        //------------------------ Process image here -----------------------
        int sceneIndex = mContext.getCurrentSceneIndex();
        if (sceneIndex >= 0 && sceneIndex != mCurrentIndex)
        {
            mCurrentIndex = sceneIndex;
            mItem = Objects.requireNonNull(mContext.getSceneItems().get(sceneIndex)).mOnFrameListener;
        }

        if (mItem != null)
        {
            int frameWidth = mRgba.cols();
            int frameHeight = mRgba.rows();

            mRgba = mItem.onFrame(mRgba);

            // Ensure the mat returned by algorithm is same as mRgba
            if (mRgba.cols() != frameWidth || mRgba.rows() != frameHeight)
                Imgproc.resize(mRgba, mRgba, new Size(frameWidth, frameHeight));
        }

        //-------------------------------------------------------------------

        return mRgba;
    }

}
