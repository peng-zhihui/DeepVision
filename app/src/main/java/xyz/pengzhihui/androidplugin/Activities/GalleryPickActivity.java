package xyz.pengzhihui.androidplugin.Activities;

import android.os.Bundle;

import xyz.pengzhihui.androidplugin.Envs.GalleryPickActivityBase;
import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.lib_fancy_ui_kit.DragGridView.GridItem;

public class GalleryPickActivity extends GalleryPickActivityBase
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSceneItems.put(0, new GridItem.Builder(getApplicationContext())
                .setName("灰度化")
                .setIconBitmapId(R.mipmap.galerry_pick_icon_gray)
                .build());

        mSceneItems.put(1, new GridItem.Builder(getApplicationContext())
                .setName("SNPE-风格迁移")
                .setIconBitmapId(R.mipmap.galerry_pick_icon_stylizer)
                .build());


        commitItems(mSceneItems);
    }

}
