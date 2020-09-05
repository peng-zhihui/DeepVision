package xyz.pengzhihui.androidplugin.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.ablanco.parallax.ParallaxContainer;
import com.ablanco.parallax.ParallaxView;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import flipagram.assetcopylib.AssetCopier;
import xyz.pengzhihui.androidplugin.Envs.Configuration;
import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.androidplugin.Utils.Logger;
import xyz.pengzhihui.lib_fancy_ui_kit.Envs.FullScreenActivityBase;
import xyz.pengzhihui.lib_fancy_ui_kit.FancyDialog.MaterialDialog;
import xyz.pengzhihui.lib_fancy_ui_kit.Utils.PermissionHelper.OnPermissionCallback;
import xyz.pengzhihui.lib_neural_engine.SnpeEngine;

public class MainActivity extends FullScreenActivityBase implements OnPermissionCallback
{
    static
    {
        System.loadLibrary("opencv_java3");
    }

    protected static final Logger LOGGER = new Logger();

    //---------------------Views
    private ParallaxContainer mLiveModeContainer;
    private ParallaxContainer mGalleryModeContainer;
    private ParallaxContainer m3DModeContainer;
    private MaterialDialog mMaterialDialog;

    public Handler mHandler;

    Class<?> liveActivityClass = CameraLiveActivity.class;
    Class<?> galleryActivityClass = GalleryPickActivity.class;


    private List<String> listTitles = Arrays.asList(
            "「Image Captioning」", "「Style Transform」", "「Texture Synthesis」", "「Image Retrieval」",
            "「Super-Resolution」", "「Facial Recognition」", "「3D Reconstruction」", "「Image generation」",
            "「Object Tracking」", "「Zhihui Peng」");


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        animateViews();

        CheckPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        });

        // Copy models to SD card
        CopyAssets();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        animateViews();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initViews()
    {
        mHandler = new Handler(msg -> {
            mMaterialDialog.setTitle("APP已有新版本更新");
            mMaterialDialog.setMessage("APP目前仅供内部测试，请下载最新版本使用。");
            mMaterialDialog.setPositiveButton("下载", v -> {
                mMaterialDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.pengzhihui.com"));
                startActivity(intent);

                finish();
            });

            mMaterialDialog.setNegativeButton("退出", v -> {
                mMaterialDialog.dismiss();
                finish();
            });

            mMaterialDialog.show();

            return false;
        });

        mLiveModeContainer = findViewById(R.id.mode_live_preview);
        ParallaxView mLiveModeButton = findViewById(R.id.button_live_preview);
        mLiveModeButton.setOnTouchListener(new View.OnTouchListener()
        {
            float x = 0;
            float y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    x = event.getRawX();
                    y = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (Math.sqrt((event.getRawX() - x) * (event.getRawX() - x)
                            + (event.getRawY() - y) * (event.getRawY() - y)) < 50)
                    {
                        if (liveActivityClass != null)
                        {
                            Intent intent = new Intent(MainActivity.this
                                    , liveActivityClass);
                            startActivity(intent);
                        }
                    }
                }
                return false;
            }
        });


        mGalleryModeContainer = findViewById(R.id.mode_gallery);
        ParallaxView mGalleryModeButton = findViewById(R.id.button_gallery);
        mGalleryModeButton.setOnTouchListener(new View.OnTouchListener()
        {
            float x = 0;
            float y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    x = event.getRawX();
                    y = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (Math.sqrt((event.getRawX() - x) * (event.getRawX() - x)
                            + (event.getRawY() - y) * (event.getRawY() - y)) < 50)
                    {
                        if (galleryActivityClass != null)
                        {
                            Intent intent = new Intent(MainActivity.this
                                    , galleryActivityClass);
                            startActivity(intent);
                        }
                    }
                }
                return false;
            }
        });


        m3DModeContainer = findViewById(R.id.mode_3d);
        ParallaxView m3DModeButton = findViewById(R.id.button_3d);
        m3DModeButton.setOnTouchListener(new View.OnTouchListener()
        {
            float x = 0;
            float y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    x = event.getRawX();
                    y = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (Math.sqrt((event.getRawX() - x) * (event.getRawX() - x)
                            + (event.getRawY() - y) * (event.getRawY() - y)) < 50)
                    {
                    }
                }
                return false;
            }
        });


        mMaterialDialog = new MaterialDialog(this);
    }

    private void animateViews()
    {
        final int animation_time = 450;

        mLiveModeContainer.setVisibility(View.INVISIBLE);
        mGalleryModeContainer.setVisibility(View.INVISIBLE);
        m3DModeContainer.setVisibility(View.INVISIBLE);

        mLiveModeContainer.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mLiveModeContainer.setVisibility(View.VISIBLE);

                AnimationSet animationSet = new AnimationSet(true);

                TranslateAnimation translateAnimation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_PARENT, 0.2f, Animation.RELATIVE_TO_PARENT, 0);
                translateAnimation.setDuration(animation_time);
                translateAnimation.setInterpolator(new DecelerateInterpolator(7.5f));
                translateAnimation.setFillAfter(false);
                animationSet.addAnimation(translateAnimation);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(animation_time);
                animationSet.addAnimation(alphaAnimation);

                mLiveModeContainer.startAnimation(animationSet);
            }
        }, 0);


        mGalleryModeContainer.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mGalleryModeContainer.setVisibility(View.VISIBLE);

                AnimationSet animationSet = new AnimationSet(true);

                TranslateAnimation translateAnimation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_PARENT, 0.2f, Animation.RELATIVE_TO_PARENT, 0);
                translateAnimation.setDuration(animation_time);
                translateAnimation.setInterpolator(new DecelerateInterpolator(7.5f));
                translateAnimation.setFillAfter(true);
                animationSet.addAnimation(translateAnimation);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(animation_time);
                animationSet.addAnimation(alphaAnimation);

                mGalleryModeContainer.startAnimation(animationSet);
            }
        }, 80);


        m3DModeContainer.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                m3DModeContainer.setVisibility(View.VISIBLE);

                AnimationSet animationSet = new AnimationSet(true);

                TranslateAnimation translateAnimation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_PARENT, 0.2f, Animation.RELATIVE_TO_PARENT, 0);
                translateAnimation.setDuration(animation_time);
                translateAnimation.setInterpolator(new DecelerateInterpolator(7.5f));
                translateAnimation.setFillAfter(true);
                animationSet.addAnimation(translateAnimation);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(animation_time);
                animationSet.addAnimation(alphaAnimation);

                m3DModeContainer.startAnimation(animationSet);
            }
        }, 160);
    }

    public void CopyAssets()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(
                Configuration.AppSharedPreferencesName, Context.MODE_PRIVATE);

        int asset_version = sharedPreferences.getInt("asset_version", -1);
        if (asset_version == -1) // assets not exist
        {
            try
            {
                // This will fail if the user didn't allow the permissions

                File faceModelDir = new File(Configuration.SD_ASSETS_PATH);
                faceModelDir.mkdirs();
                new AssetCopier(MainActivity.this).withFileScanning()
                        .copy("tocopy", faceModelDir);

            } catch (IOException e)
            {
                e.printStackTrace();
            }


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("asset_version", 1);
            editor.apply();
        }
    }

    public int getVersionCode(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode = "";
        try
        {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return Integer.parseInt(versionCode);
    }

}
