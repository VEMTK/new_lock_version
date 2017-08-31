package com.weather.lock.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.weather.lock.MainActivity;
import com.weather.lock.MyApp;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.Util;

/**
 * Created by xlc on 2017/3/11.
 */

public class CheckAppThemeTask extends AsyncTask<Void,Integer,Boolean> {

    private MyApp app;

    private Activity mActivity;

    private int status;

    private SharedUtil sharedUtil=null;

    public CheckAppThemeTask(Activity activity)
    {
        this.app= (MyApp) activity.getApplication();

        this.mActivity=activity;

        sharedUtil=SharedUtil.getInstance(activity);

        status=sharedUtil.get_int(SharedUtil.APP_THEME_STAUS,0);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        if (Util.getWallpapperBitmap(app) != null) { //0

            if (status!=0) {

                sharedUtil.save_int(SharedUtil.APP_THEME_STAUS,0);

                return true;
            }
        } else { //10
            if (status!=1) {
                sharedUtil.save_int(SharedUtil.APP_THEME_STAUS,1);
                return true;
            }

        }
        return false;
    }
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if(aBoolean) {

            Log.e("tool","更改");
            mActivity.recreate();

        }
    }
}
