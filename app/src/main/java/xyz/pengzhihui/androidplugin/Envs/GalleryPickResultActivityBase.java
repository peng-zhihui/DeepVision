package xyz.pengzhihui.androidplugin.Envs;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;
import com.winfo.photoselector.PhotoSelector;

import org.opencv.utils.ImageUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.androidplugin.Utils.FileUtils;
import xyz.pengzhihui.androidplugin.Utils.Logger;
import xyz.pengzhihui.lib_fancy_ui_kit.Envs.FullScreenActivityBase;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyDropDownMessage.DropDownMessage;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyImageViewer.ImageSource;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyImageViewer.SubsamplingScaleImageView;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyUploadButton.SlideLayout;

public abstract class GalleryPickResultActivityBase extends FullScreenActivityBase
        implements SlideLayout.onProgressChangedListener
{
    protected static final Logger LOGGER = new Logger();

    protected SubsamplingScaleImageView mImageCanvas;

    protected SlideLayout mSlideLayout;
    protected ImageView mImageProcessingMask;
    protected ImageView mImageProcessingArrow;
    protected ImageView mSlideButton;
    protected TextView mStatusText;
    protected ImageView mCompareButton;

    protected DropDownMessage mDropDownMessage;

    protected AVLoadingIndicatorView mLoadingAnimationView;

    protected boolean mIsProcessing = false;

    protected Uri tmpUri;
    protected String mSelectedImagePath;

    protected Bitmap mOrigBitmap;
    protected Bitmap mResultBitmap;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_pick_result);

        PhotoSelector.builder()
                .setSingle(true)
                .setCrop(true)
                .setShowCamera(true)
                .setCropMode(PhotoSelector.CROP_RECTANG)
                .start(GalleryPickResultActivityBase.this, Configuration.REQUEST_CODE.CROP_CODE);

        mImageCanvas = findViewById(R.id.image_view_canvas);
        mImageCanvas.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mImageCanvas.setMinimumDpi(20);
        mImageCanvas.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        mImageCanvas.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);

        mSlideLayout = findViewById(R.id.slider);
        mSlideLayout.setOnProgressChangedListener(this);

        mImageProcessingMask = findViewById(R.id.image_process_mask);
        mImageProcessingMask.setAlpha(0.f);

        mImageProcessingArrow = findViewById(R.id.image_process_arrow);
        mImageProcessingArrow.setAlpha(0.8f);

        mSlideButton = findViewById(R.id.slide_button);
        mCompareButton = findViewById(R.id.image_compare_button);

        mCompareButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        if (mOrigBitmap != null)
                            mImageCanvas.post(() -> mImageCanvas
                                    .setImage(ImageSource.bitmap(mOrigBitmap.copy(Bitmap.Config.ARGB_8888, true))));
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mResultBitmap != null)
                            mImageCanvas.post(() -> mImageCanvas
                                    .setImage(ImageSource.bitmap(mResultBitmap.copy(Bitmap.Config.ARGB_8888, true))));
                        break;
                }
                return true;
            }
        });

        mStatusText = findViewById(R.id.network_status);
        mStatusText.setText("");

        mLoadingAnimationView = findViewById(R.id.loading_indicator_view);
        mLoadingAnimationView.hide();

        mDropDownMessage = new DropDownMessage.Builder(getApplicationContext(),
                findViewById(R.id.root))
                .message("mDropDownMessage")
                .backgroundColor(0xff1976D2)
                .foregroundColor(0xffffffff)
                .interpolatorIn(new BounceInterpolator())
                .interpolatorOut(new AnticipateOvershootInterpolator())
                .textHeight(80)
                .build();


        playArrowAnimation();


        SharedPreferences sharedPreferences = getSharedPreferences(
                Configuration.AppSharedPreferencesName, Context.MODE_PRIVATE);

        boolean is_first_time_open_app = sharedPreferences.getBoolean("first_time_pick", true);
        if (is_first_time_open_app)
        {
            ImageView guide = findViewById(R.id.gallery_pick_result_guide);
            guide.setVisibility(View.VISIBLE);
            guide.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    guide.animate().alpha(0).translationY(2000)
                            .setListener(new Animator.AnimatorListener()
                            {
                                @Override
                                public void onAnimationStart(Animator animation)
                                {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    guide.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation)
                                {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation)
                                {

                                }
                            }).start();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("first_time_pick", false);
                    editor.apply();
                    return false;
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null)
        {
            switch (requestCode)
            {
                case Configuration.REQUEST_CODE.CROP_CODE:
                    Uri resultUri = PhotoSelector.getCropImageUri(data);

                    if (resultUri != null)
                    {
                        mImageCanvas.setImage(ImageSource.uri(resultUri));

                        tmpUri = resultUri;
                        mSelectedImagePath = FileUtils.getPathFromUri(this, tmpUri);

                        mOrigBitmap = decodeUriAsBitmap(resultUri);
                        int angle = readImageExif(mSelectedImagePath);
                        if (readImageExif(mSelectedImagePath) != 0)
                            mOrigBitmap = rotateImage(mOrigBitmap, angle);

                        onImageSelected(mSelectedImagePath);
                    }
            }
        }
    }

    private Bitmap decodeUriAsBitmap(Uri uri)
    {
        Bitmap bitmap = null;
        if (null == uri)
        {
            return null;
        }
        try
        {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    private long time_last;
    private boolean confirm_exit = false;

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (tmpUri != null)
                {
                    ImageUtils.saveBitmap(ImageUtils.getBitmapFromUri(this, tmpUri),
                            "sdcard/saved_image.jpg");

                    mDropDownMessage.setMessage("图片已保存为 \"sdcard/saved_image.jpg\"")
                            .show(5000);
                }
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                break;

            case KeyEvent.KEYCODE_BACK:
                if (!confirm_exit)
                {
                    time_last = System.currentTimeMillis();
                    confirm_exit = true;
                    Toast.makeText(getApplicationContext(), "再次按返回退出", Toast.LENGTH_SHORT).show();
                } else
                {
                    if (System.currentTimeMillis() - time_last < 2000)
                    {
                        finish();
                    }

                    confirm_exit = false;
                }
                break;
        }

        return true;
    }


    @Override
    public void onProgressChanged(float progress)
    {
        if (progress >= mSlideLayout.mThreshold)
        {
            mIsProcessing = true;

            playButtonAnimation(true);
            mSlideLayout.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mSelectedImagePath != null)
                        onSliderTriggered(mSelectedImagePath);
                    else
                    {
                        mDropDownMessage.setMessage("未选择图片!").show(3000);
                        notifyProcessingProgress(1, "");
                    }
                }
            }, 500);
        } else if (progress == 0)
        {
            if (mIsProcessing)
            {
                mIsProcessing = false;
                playButtonAnimation(false);
            }
        }

        if (progress > 0.2f)
        {
            mImageProcessingArrow.animate()
                    .alpha(0)
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(Animator animator)
                        {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator)
                        {
                            mImageProcessingArrow.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator)
                        {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator)
                        {

                        }
                    });
        }
    }


    protected abstract void onSliderTriggered(String path);

    protected abstract void onImageSelected(String path);

    protected void notifyProcessingProgress(float progress)
    {
        mSlideLayout.post(() -> mSlideLayout.setProgress(1 - progress));

        if (progress == 1)
            playShatterAnimation();
        else if (progress > 0.8f)
            mStatusText.post(() -> mStatusText.setText(""));
    }

    protected void notifyProcessingProgress(float progress, String text)
    {
        mSlideLayout.post(() -> mSlideLayout.setProgress(1 - progress));

        mSlideLayout.post(() -> mStatusText.setText(text));

        if (progress == 1)
            playShatterAnimation();
        else if (progress > 0.8f)
            mStatusText.post(() -> mStatusText.setText(""));
    }

    protected void setSliderText(String text)
    {
        mStatusText.post(() -> mStatusText.setText(text));
    }

    protected void showToast(String msg)
    {
        mSlideLayout.post(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    protected void updateCanvas(final Bitmap bitmap, final String _imagePath)
    {
        mImageCanvas.post(() -> {
            int angle = readImageExif(_imagePath);

            if (angle != 0)
                mResultBitmap = rotateImage(bitmap, angle);
            else
                mResultBitmap = bitmap;

            mImageCanvas.setImage(ImageSource.bitmap(mResultBitmap.copy(Bitmap.Config.ARGB_8888, true)));
        });
    }


    public int readImageExif(String path)
    {
        int degree = 0;
        try
        {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateImage(Bitmap bm, final int orientationDegree)
    {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90)
        {
            targetX = bm.getHeight();
            targetY = 0;
        } else
        {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }


    @Override
    public void onButtonClick()
    {
        PhotoSelector.builder()
                .setSingle(true)
                .setCrop(true)
                .setShowCamera(true)
                .setCropMode(PhotoSelector.CROP_RECTANG)
                .start(GalleryPickResultActivityBase.this, Configuration.REQUEST_CODE.CROP_CODE);
    }

    public void playShatterAnimation()
    {
        mImageProcessingMask.postDelayed(() -> {
            mImageProcessingMask.setAlpha(1.f);
            mImageProcessingMask.animate().cancel();
            mImageProcessingMask.animate().alpha(0)
                    .setDuration(500)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }, 100);
    }

    public void playArrowAnimation()
    {
        AnimationSet ams = new AnimationSet(true);
        AlphaAnimation ama = new AlphaAnimation(1, 0);
        ama.setDuration(2000);
        ama.setInterpolator(new FastOutSlowInInterpolator());
        ama.setRepeatMode(Animation.RESTART);
        ama.setRepeatCount(Animation.INFINITE);
        TranslateAnimation amt = new TranslateAnimation(0, 300, 0, 0);
        amt.setDuration(2000);
        amt.setInterpolator(new FastOutSlowInInterpolator());
        amt.setRepeatMode(Animation.RESTART);
        amt.setRepeatCount(Animation.INFINITE);

        ams.addAnimation(ama);
        ams.addAnimation(amt);

        mImageProcessingArrow.startAnimation(ams);
    }

    public void playButtonAnimation(boolean isProcessing)
    {
        if (isProcessing)
        {
            mSlideButton.animate().alpha(0).setDuration(500).start();
            mLoadingAnimationView.show();
        } else
        {
            mLoadingAnimationView.hide();
            mSlideButton.animate().alpha(1).setDuration(500).start();
        }
    }
}
