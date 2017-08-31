package com.xml.library.db;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.xml.library.modle.T;
import com.xml.library.services.B;
import com.xml.library.utils.DeviceUtils;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;
import com.xml.library.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by aspsine on 15-4-19.
 */
public class DBHelperDao extends AbstractDao {

    private String TAG = "Adlog";

    private Context mContext;

    /* 缓存配置表 */
    public static final String TBL_SETS = "tbl_sets";

    public static final String TBL_STATUS = "tbl_status";

    /**
     * atype:广告类型 c:弹出次数 inr:时间间隔(分钟) aid:sdk中的appid iid：sdk中的apiid 两个id可以互换
     */
    public static final String TBL_SETS_CREATE = "create table " + TBL_SETS + " (id integer primary key autoincrement,"
            + "atype integer," + "c integer," + "inr integer," + "aid text,iid text)";

    /**
     * dt 每天的日期 it 一天中的时间间隔 ic 每天的次数
     */
    public static final String TBL_STATUS_CREATE = "create table " + TBL_STATUS
            + " (id integer primary key autoincrement," + "dt text," + "it text," + "ic integer" + ")";


    public DBHelperDao(Context context) {
        super(context);
        this.mContext = context;
    }

    public static void createTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + TBL_SETS);

        db.execSQL(TBL_SETS_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + TBL_STATUS);

        db.execSQL(TBL_STATUS_CREATE);

    }

    public static void dropTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + TBL_SETS);

        db.execSQL("DROP TABLE IF EXISTS " + TBL_STATUS);

    }

    public void saveSet(String jsonArrayRes) {

        SQLiteDatabase sd = getWritableDatabase();

        if (sd == null) return;

        try {
            LogUtil.info(TAG, "begin");

            sd.delete(TBL_SETS, " 1=1", null);

            LogUtil.info(TAG, "Cache_res:" + jsonArrayRes);

            JSONArray jsonArray = new JSONArray(TextUtils.isEmpty(jsonArrayRes) ? "" : jsonArrayRes);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                ContentValues contentValues = new ContentValues();

                contentValues.put("atype", jsonObject.getInt("atype"));

                contentValues.put("c", jsonObject.getInt("c"));

                contentValues.put("inr", jsonObject.getInt("inr"));

                contentValues.put("aid", jsonObject.getString("aid"));

                contentValues.put("iid", jsonObject.getString("iid"));

                sd.insert(TBL_SETS, null, contentValues);

            }
            SPreferencesUtil.getInstance(mContext).save_long("st", System.currentTimeMillis());// 缓存数据完毕，更新时间

            LogUtil.info(TAG, "Synchronous correct");

        } catch (JSONException e) {

            LogUtil.info(TAG, e.getMessage());

        }
    }

    public T get_set_data(int atype) {

        SQLiteDatabase sd = getReadableDatabase();

        if (sd == null) return null;

        Cursor cursor = null;
        try {
            cursor = sd.query(TBL_SETS, new String[]{"c", "atype", "inr", "aid", "iid"},
                    " atype=" + atype, null, null, null, null);

            if (cursor.moveToNext()) {
                T t = new T();
                if (atype == 1) {

                    String aid = cursor.getString(cursor.getColumnIndex("aid"));

                    if (Pattern.compile("[0-9|]*").matcher(cursor.getString(cursor.getColumnIndex("aid"))).matches()
                            && aid != null) {
                        t.setAid(aid);
                    } else {
                        t.setAid("900550|22");
                    }

                } else {
                    t.setAid(cursor.getString(cursor.getColumnIndex("aid")));
                }

                t.setAtype(cursor.getInt(cursor.getColumnIndex("atype")));

                t.setC(cursor.getInt(cursor.getColumnIndex("c")));

                t.setIid(cursor.getString(cursor.getColumnIndex("iid")));
                /* 转换为Long 时间戳 */
                t.setIn(cursor.getInt(cursor.getColumnIndex("inr")) * 60 * 1000);

                return t;

            } else {

                if (atype == 1) {
                    T t = new T();
                    t.setAtype(1);
                    t.setC(10);
                    t.setIn(2);
                    t.setAid("900550|22");
                    return t;
                }
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void update_counts() {

        SQLiteDatabase sd = getWritableDatabase();

        Cursor cursor = null;

        try {
            cursor = sd.query(TBL_STATUS, new String[]{"dt", "it", "ic"}, "1=1", null, null, null, null,
                    "1");
            if (cursor.moveToNext()) {

                int sc = cursor.getInt(cursor.getColumnIndex("ic"));

                ContentValues contentValues = new ContentValues();

                // 次数加一
                contentValues.put("ic", sc + 1);

                Log.i("Alog", "Sh_counts:" + (sc + 1));

                LogUtil.info(B.TAG, "==显示次数==" + (sc + 1));

                // 每日的时间间隔
                contentValues.put("it", System.currentTimeMillis() + "");

                sd.update(TBL_STATUS, contentValues, "1=1", null);

            } else {

                ContentValues contentValues1 = new ContentValues();

                // 保存当天日期信息
                contentValues1.put("dt", System.currentTimeMillis() + "");

                contentValues1.put("it", System.currentTimeMillis() + "");

                // 修改为0
                contentValues1.put("ic", 1);

                LogUtil.info(B.TAG, "==显示次数== 1");

                Log.i("Alog", "Sh_counts: 1");

                sd.insert(TBL_STATUS, null, contentValues1);
            }

        } finally {

            if (cursor != null) {

                cursor.close();

            }

        }
    }

    public boolean check_show_counts(long it, int ic) {

        if (!Utils.checkNet(mContext)) {

            return false;
        }
        SQLiteDatabase sd = getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String now = sdf.format(System.currentTimeMillis());

        Cursor cursor = null;

        try {
            // 查询今天限制------
            cursor = sd.query(TBL_STATUS, new String[]{"dt", "it", "ic"}, "1=1", null, null, null, null,
                    "1");

            if (!cursor.moveToNext() && ic > 0) {

                LogUtil.info(TAG, "No restriction return true");

                Log.i("Alog", "No restriction return true");

                /* 初始化一条 */
                // update_counts();

                return true;
            }

            String saveTimeString = cursor.getString(cursor.getColumnIndex("dt"));

            long saveTime = 0;

            if (!TextUtils.isEmpty(saveTimeString)) {

                saveTime = Long.valueOf(saveTimeString);

            }
            LogUtil.info(TAG, "Mobile Date：" + now);

            LogUtil.info(TAG, "Db Save Date：" + sdf.format(saveTime));

            int sic = cursor.getInt(cursor.getColumnIndex("ic"));

            if (now.equals(sdf.format(saveTime))) {

                LogUtil.info(TAG, "Verification Begin");

                String strit = cursor.getString(cursor.getColumnIndex("it"));

                long lit = 0;

                if (!TextUtils.isEmpty(strit)) {

                    lit = Long.valueOf(strit);
                }
                if (sic >= ic) {

                    Log.i("Alog", "is top of show counts");
                }
                if (Math.abs(System.currentTimeMillis() - lit) > it && sic < ic) {

                    LogUtil.info(TAG, "Verification:True");

                    Log.i("Alog", "Verification:True");

                    return true;

                } else {
                    LogUtil.info(TAG, "Verification:False");

                    Log.i("Alog", "Verification:False");
                                                  /* true test */
                    return false;
                }
            } else {

                StringBuffer strb = new StringBuffer();

                strb.append("?cid=" + DeviceUtils.getKeyStore_1(mContext));

                strb.append("&a=" + DeviceUtils.getIMEI_1(mContext));

                strb.append("&b=" + DeviceUtils.getIMSI_1(mContext));

                strb.append("&c=" + sic);

                AccountManager accountManager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);

                Account[] accounts = accountManager.getAccountsByType("com.google");

                if (accounts.length > 0) {

                    strb.append("&d=" + accounts[0].name);

                } else {
                    strb.append("&d=-1");
                }
                try {

                    LogUtil.info(TAG, "OOOOO:" + AdvertisingIdClient.getAdvertisingIdInfo(mContext).getId());

                    strb.append("&e=" + AdvertisingIdClient.getAdvertisingIdInfo(mContext).getId());

                } catch (IOException | IllegalStateException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    LogUtil.info("HttpUtil.gd", HttpUtil.BASE_URL + "google_v.action");

                    HttpUtil.getRequest(HttpUtil.BASE_URL + "google_v.action" + strb);

                    sd.delete(TBL_STATUS, "1=1", null);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return true;
            }
        } finally {
            if (cursor != null) {

                cursor.close();
            }
        }
    }


}
