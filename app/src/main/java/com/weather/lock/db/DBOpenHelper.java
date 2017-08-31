package com.weather.lock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aspsine on 15-4-19.
 */
public class DBOpenHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "system_location.db";

    private static final int DB_VERSION = 1;

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    void createTable(SQLiteDatabase db){
        DBHelperDao.createTable(db);
    }

    void dropTable(SQLiteDatabase db){
        DBHelperDao.dropTable(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        createTable(db);
    }
}
