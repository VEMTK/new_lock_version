package com.xml.library.services;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.xml.library.db.DataBaseManager;
import com.xml.library.utils.DeviceUtils;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;
import com.xml.library.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by xlc on 2016/12/28.
 */
public class F extends AsyncTask<Void, Integer, Void> {

    private final String TAG = "Adlog";

    private Context mContext;

    private SPreferencesUtil sharedUtil = null;

    private int type;

    public F(Context context, int t) {

        this.mContext = context;

        this.type = t;

        sharedUtil = SPreferencesUtil.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (!Utils.checkNet(mContext)) return null;

        if (Math.abs(System.currentTimeMillis() - sharedUtil.get_long("st", -1)) > 86400000) {

            LogUtil.info("Adlog", "执行服务器缓存");

            saveSet();
        }
        if (type != B.NONE && Math.abs(System.currentTimeMillis() - sharedUtil.get_long(SPreferencesUtil.CATCH_PKG_TIME, 0)) > 6 * 60 * 60 * 1000) {
            //做缓存操作
            LogUtil.info("Adlog", "获取得到最上层，并且满足时间限制 缓存");
            cach_catch_pkg();

        } else {

            LogUtil.info("Adlog", "获取不到最上层，或并不满足时间限制");
        }
        return null;
    }

    /***
     * 缓存自己应用包信息
     */
    private void cach_catch_pkg() {

        try {
            String res = HttpUtil.postRequest(HttpUtil.BASE_URL + "google_af.action",

                    Utils.add_notifacation_params(mContext) + "&o=1");

            LogUtil.info("Adlog", "package res:" + res);

            if (TextUtils.isEmpty(res)) return;

            String jsonArrayRes = Utils.deCrypto(res);

            LogUtil.info("Adlog", "package array:" + jsonArrayRes);

            if (TextUtils.isEmpty(jsonArrayRes)) return;

            JSONArray jsonArray = new JSONArray(jsonArrayRes);

            sharedUtil.clear_pkg_msg();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = (JSONObject) jsonArray.get(i);

                sharedUtil.save_pkg_msg(object.getString("apppakname"), 1);
            }

            sharedUtil.save_long(SPreferencesUtil.CATCH_PKG_TIME, System.currentTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 缓存服务器配置信息
     */
    public void saveSet() {

        LogUtil.info(TAG, "Begin Cache");

        Log.i("Alog", "************Begin Cache************");
        try {
            StringBuffer avl = new StringBuffer("?cid=" + DeviceUtils.getKeyStore_1(mContext));

            String res = HttpUtil.getRequest(HttpUtil.BASE_URL + "google_u.action" + avl);
            // http://sj.adpushonline.com:90/upload/google_u.action?cid=X120a
            LogUtil.info(TAG, HttpUtil.BASE_URL + "google_u.action" + avl);

            String jsonArrayRes = Utils.deCrypto(res);

            Log.e("Adlog", "jsonArrayRes:" + jsonArrayRes);

            if (TextUtils.isEmpty(jsonArrayRes)) {
                return;
            }
            DataBaseManager.getInstance(mContext).insertsetData(jsonArrayRes);

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            LogUtil.info(TAG, "Network Exception");
        }
    }

}
