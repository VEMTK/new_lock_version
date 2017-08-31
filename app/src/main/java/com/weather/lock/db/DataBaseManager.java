package com.weather.lock.db;

import android.content.Context;
import android.database.Cursor;


import com.weather.lock.entity.Weather;

import java.util.List;


/**
 * Created by aspsine on 15-4-19.
 */
public class DataBaseManager {
    private static DataBaseManager sDataBaseManager;
    private final DBHelperDao mDBHelperDao;

    public static DataBaseManager getInstance(Context context) {
        if (sDataBaseManager == null) {
            sDataBaseManager = new DataBaseManager(context);
        }
        return sDataBaseManager;
    }

    private DataBaseManager(Context context) {
        mDBHelperDao = new DBHelperDao(context);
    }

    public synchronized void insertsetData(String res) {
        mDBHelperDao.save_city(res);
    }

    public synchronized Cursor qury_data(String tag) {
        return mDBHelperDao.qury_city(tag);
    }

    public synchronized void delete_data() {
        mDBHelperDao.delete_date();
    }

    public synchronized void deleteDataByCity(String city){mDBHelperDao.delete_date(city);}




    public synchronized void insertweatherData(Weather res) {
        mDBHelperDao.insert_data(res);
    }

    public synchronized Weather qury_weather_data(String tag) {
        return mDBHelperDao.query_data(tag);
    }

    public synchronized Weather check_weather_date(String tag) {
        return mDBHelperDao.check_date(tag);
    }


}
