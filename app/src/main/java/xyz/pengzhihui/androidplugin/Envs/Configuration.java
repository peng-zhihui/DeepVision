package xyz.pengzhihui.androidplugin.Envs;

public class Configuration
{
    public static final String AppSharedPreferencesName = "pzh::InfinityCam";

    //------------REQUEST Codes
    public static class REQUEST_CODE
    {
        public static final int SINGLE_CODE = 1;//单选
        public static final int LIMIT_CODE = 2;//多选限制数量
        public static final int CROP_CODE = 3;//剪切裁剪
        public static final int UN_LIMITT_CODE = 4;//多选不限制数量
    }

    public static final String SD_ASSETS_PATH = "sdcard/InfinityCam";

    public static final String SERVER_ADDRESS = "http://xxx.xxx.xxx.xxx";

    public static final int  PREVIEW_WIDTH = 1280;
    public static final int  PREVIEW_HEIGHT = 720;

}
