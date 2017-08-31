package com.weather.lock.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xlc on 2016/12/29.
 */
public class SharedUtil {


    private static final String LIBRARY_XML = "library_xml";

    public static final String TEMP_STATUS = "temp_status";

    /**
     * 保存最后查询地址的woeid
     */
    public static final String LOCATION_CODE="location_code";
    /**
     * 保存最后查询地址
     */
    public static final String LOCATION_NAME="location_name";

    public static final String K_LOCATION="k_location";

    public static final String CLOSE_SCREEN_INDEX="close_screen_index";

    /***
     * 记录用户是否手动设置地址 0没，1有
     */
    public static final String USER_SER_LOACTION="user_set_location";

    /***
     * 记录用户是否设置温度单位 0没，1有
     */
    public static final String USER_SET_TEMP_UNIT="user_set_temp_unit";

    /***
     * 记录天气相关
     */
    public static final String WEATHER_LOW="weather_low";
    public static final String WEATHER_HEIGHT="weather_height";
    public static final String WEATHER_CODE="weather_code";

    /***
     * 记录布局是否需要左右对调
     */
    public static  final String LAYOUT_LEFT_RIGHT="layout_left_right";


    /**
     * 保存当前的温度
     */
    public static  final String CURRENT_TEMP="current_temp";


    /***
     * 保存记录app主题
     */
    public static  final String APP_THEME_STAUS="app_theme_status";




    private static SharedUtil instance = null;

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private SharedPreferences sharedPreferences = null;

    private SharedPreferences pkgPreferences = null;

    private SharedPreferences.Editor editor = null;

    private Context mContext;

    public static SharedUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedUtil(context);
        }
        return instance;
    }

    private SharedUtil(Context context) {
        this.mContext = context;
        sharedPreferences = context.getSharedPreferences(LIBRARY_XML, 0);

    }

    public void save_int(String tag, int velues) {

        editor = sharedPreferences.edit();

        editor.putInt(tag, velues);

        editor.apply();
    }

    public void save_long(String tag, long velues) {

        editor = sharedPreferences.edit();

        editor.putLong(tag, velues);

        editor.apply();
    }

    public void save_string(String tag, String velues) {

        editor = sharedPreferences.edit();

        editor.putString(tag, velues);

        editor.apply();
    }

    public int get_int(String tag, int default_velues) {

        return sharedPreferences.getInt(tag, default_velues);
    }

    public long get_long(String tag, long default_velues) {

        return sharedPreferences.getLong(tag, default_velues);
    }

    public String get_string(String tag, String default_values) {

        return sharedPreferences.getString(tag, default_values);
    }


}
