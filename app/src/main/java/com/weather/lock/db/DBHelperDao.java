package com.weather.lock.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.weather.lock.entity.Weather;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class DBHelperDao extends AbstractDao {

    private String TAG = "Adlog";

    private Context mContext;

    /* 缓存配置表 */
    public static final String LOCATION_NAME = "location_name";

    /***
     * 记录查询地址的信息
     */
    public static final String QUERY_CITY_MSG = "query_city_msg";


    /****
     * woeid 城市代码
     * city_name 城市名称
     * query_date 查询的日期
     */
    public static final String TBL_QUETY_CITY_MSG = "create table " + QUERY_CITY_MSG + " (id integer primary key autoincrement,woeid text unique, low_temp integer,height_temp integer,city_name text,weather_code integer,query_date text)";

    /**
     *
     */
    public static final String TBL_LOCATION_CREATE = "create table " + LOCATION_NAME + " (id integer primary key autoincrement,"
            + "city_name text unique)";


    public DBHelperDao(Context context) {
        super(context);
        this.mContext = context;
    }

    public static void createTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_NAME);

        db.execSQL(TBL_LOCATION_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + QUERY_CITY_MSG);

        db.execSQL(TBL_QUETY_CITY_MSG);

    }

    public static void dropTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + QUERY_CITY_MSG);

    }

    public void save_city(String city) {

        SQLiteDatabase sd = getWritableDatabase();

        try {

            ContentValues contentValues = new ContentValues();

            contentValues.put("city_name", city);

            sd.insert(LOCATION_NAME, null, contentValues);

        } catch (Exception e) {

            Log.e("tool","保存数据错误："+e.getMessage());

            e.printStackTrace();

        }
    }

    public Cursor qury_city(String name) {

        SQLiteDatabase sd = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sd.rawQuery("select * from " + LOCATION_NAME + " where city_name like '%" + name + "%' order by id desc ", null);

        } catch (Exception e) {

            Log.e("tool","查询错误："+e.getMessage());

            e.printStackTrace();
        }
        return cursor;
    }

    public void delete_date() {


        try {

            SQLiteDatabase sd = getWritableDatabase();

            sd.execSQL("delete from " + LOCATION_NAME);

        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    public void delete_date(String city) {

        Log.e("Adlog", "do delete btn");

        try {

            SQLiteDatabase sd = getWritableDatabase();

            sd.execSQL("delete from "
                            + LOCATION_NAME
                            + " where city_name = ? ",
                    new Object[]{city});

        } catch (Exception e) {
            e.printStackTrace();

            Log.e("Adlog", "error:" + e.getMessage());
        }

    }

    /***
     * 检查是否有保存数据
     * 检查是否满足时间限制
     *
     * @param woeid
     * @return
     */
    public Weather check_date(String woeid) {

        SQLiteDatabase sd = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sd.rawQuery("select * from " + QUERY_CITY_MSG + " where " + Weather.WOEID + "= ?", new String[]{woeid});

            if (cursor.moveToNext()) {

                String date = cursor.getString(cursor.getColumnIndex(Weather.QUERY_DATE));

                LogUtil.info(cursor.getString(cursor.getColumnIndex(Weather.WOEID)) + " 最后查询报错的日期：" + date);

                LogUtil.info("当前日期：" + Util.getCurrentDate());

                if (date.equals(Util.getCurrentDate())) {

                    int low_temp = cursor.getInt(cursor.getColumnIndex(Weather.LOW_TEMP));

                    int h_temp = cursor.getInt(cursor.getColumnIndex(Weather.HEIGHT_TEMP));

                    String city_name = cursor.getString(cursor.getColumnIndex(Weather.CITY_NAME));

                    int weather_code = cursor.getInt(cursor.getColumnIndex(Weather.WEATHER_CODE));

                    String query_date = cursor.getString(cursor.getColumnIndex(Weather.QUERY_DATE));

                    return new Weather(woeid, low_temp, h_temp, city_name, weather_code, query_date);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }


    public void insert_data(Weather weather) {

        SQLiteDatabase sd = getWritableDatabase();
        try {
            sd.execSQL("replace into query_city_msg(woeid,low_temp,height_temp,city_name,weather_code,query_date)values(?,?,?,?,?,?)", new Object[]{weather.getWoeid(), weather.getLow_temp(), weather.getHeight_temp(), weather.getCity_name(), weather.getWeather_code(), weather.getQuery_date()});

        } catch (Exception e) {
            LogUtil.info("插入数据失败：" + e.getMessage());
        }


    }

    public Weather query_data(String woeid) {

        SQLiteDatabase sd = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sd.rawQuery("select * from " + QUERY_CITY_MSG + " where woeid=?", new String[]{woeid});

            if (cursor.moveToNext()) {


                int low_temp = cursor.getInt(cursor.getColumnIndex(Weather.LOW_TEMP));

                int h_temp = cursor.getInt(cursor.getColumnIndex(Weather.HEIGHT_TEMP));

                String city_name = cursor.getString(cursor.getColumnIndex(Weather.CITY_NAME));

                int weather_code = cursor.getInt(cursor.getColumnIndex(Weather.WEATHER_CODE));

                String query_date = cursor.getString(cursor.getColumnIndex(Weather.QUERY_DATE));

                return new Weather(woeid, low_temp, h_temp, city_name, weather_code, query_date);
            }

        } catch (Exception e) {

        } finally {
            if (cursor != null) cursor.close();
        }

        return null;

    }


}
