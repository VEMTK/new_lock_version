package com.xml.library.db;

import android.content.Context;

import com.xml.library.modle.T;


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
        mDBHelperDao.saveSet(res);
    }

    public synchronized T get_setData(int type) {
        return mDBHelperDao.get_set_data(type);
    }

    public synchronized void update_counts() {
        mDBHelperDao.update_counts();
    }

    public synchronized boolean check_status(long it, int ic)
    {
        return  mDBHelperDao.check_show_counts(it,ic);
    }
}
