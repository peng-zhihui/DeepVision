package xyz.pengzhihui.androidplugin.Envs;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.pengzhihui.androidplugin.Activities.GalleryPickResultActivity;
import xyz.pengzhihui.androidplugin.R;
import xyz.pengzhihui.androidplugin.Utils.Logger;
import xyz.pengzhihui.lib_fancy_ui_kit.DragGridView.DragAdapter;
import xyz.pengzhihui.lib_fancy_ui_kit.DragGridView.DragGridView;
import xyz.pengzhihui.lib_fancy_ui_kit.DragGridView.GridItem;
import xyz.pengzhihui.lib_fancy_ui_kit.Envs.FullScreenActivityBase;
import xyz.pengzhihui.lib_fancy_ui_kit.Utils.ScreenUtil;

public class GalleryPickActivityBase extends FullScreenActivityBase
{
    protected static final Logger LOGGER = new Logger();

    private List<HashMap<String, Object>> mSceneList = new ArrayList<HashMap<String, Object>>();
    protected HashMap<Integer, GridItem> mSceneItems;

    public static int mCurrentSelectedScene = -1;

    public static int getCurrentSceneIndex()
    {
        return mCurrentSelectedScene;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_pick);

        mSceneItems = new HashMap<>();


        SharedPreferences sharedPreferences = getSharedPreferences(
                Configuration.AppSharedPreferencesName, Context.MODE_PRIVATE);

        boolean is_first_time_open_app = sharedPreferences.getBoolean("first_time", true);
        if (is_first_time_open_app)
        {
            ImageView guide = findViewById(R.id.gallery_pick_guide);
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
                    editor.putBoolean("first_time", false);
                    editor.apply();
                    return false;
                }
            });
        }
    }

    public void commitItems(HashMap<Integer, GridItem> items)
    {
        final DragGridView mDragGridView = findViewById(R.id.drag_grid_view);
        for (Map.Entry<Integer, GridItem> itemMap : items.entrySet())
        {
            GridItem gItem = itemMap.getValue();
            HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
            itemHashMap.put("item_image", gItem.getIconBitmapId());
            itemHashMap.put("item_name", gItem.getName());
            itemHashMap.put("item_is_cloud", gItem.isCloud());
            itemHashMap.put("item_is_last", false);

            mSceneList.add(itemHashMap);
        }

        HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
        itemHashMap.put("item_image", R.mipmap.more);
        itemHashMap.put("item_name", "更多算法待添加");
        itemHashMap.put("item_is_cloud", false);
        itemHashMap.put("item_is_last", true);


        mSceneList.add(itemHashMap);

        final DragAdapter mDragAdapter = new DragAdapter(this, mSceneList);
        mDragGridView.setAdapter(mDragAdapter);
        mDragGridView.setNumColumns(2);

        mDragGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < parent.getChildCount() - 1)
            {
                View image = view.findViewById(R.id.item_image);
                image.setElevation(ScreenUtil.Dp2Pixel(30));
                image.setScaleX(1);
                image.setScaleY(1);

                mCurrentSelectedScene = position;

                GridItem.onItemSelectedListener l = mSceneItems.get(mCurrentSelectedScene).mOnItemSelectedListener;
                if (l != null)
                    l.onSelected();

                Intent intent = new Intent(GalleryPickActivityBase.this
                        , GalleryPickResultActivity.class);
                startActivity(intent);
            }
        });
    }

}
