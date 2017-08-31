package com.xml.library.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by aspsine on 15-4-19.
 */
public abstract class AbstractDao {

    private DBOpenHelper mHelper;

    public AbstractDao(Context context) {

        mHelper = new DBOpenHelper(context);
    }

    protected SQLiteDatabase getWritableDatabase() {

        if (mHelper != null)
            return mHelper.getWritableDatabase();
        return null;
    }

    protected SQLiteDatabase getReadableDatabase() {
        if (mHelper != null)
            return mHelper.getReadableDatabase();
        return null;
    }

    public void close() {
        mHelper.close();
    }
}
