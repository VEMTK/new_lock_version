package com.xml.library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.xml.library.services.B;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by xlc on 2016/12/29.
 */
public class Utils {

    public static final String A = "3474739D4B4329F028031BBA4CA00827";

    public static final String LOCK_WEATHER_ACTION="action.lock.weather.unlock";

    public static boolean checkNet(Context context) {

        try {
            ConnectivityManager connectivity = (ConnectivityManager) context

                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivity != null) {

                NetworkInfo networkinfo = connectivity.getActiveNetworkInfo();

                LogUtil.info("Adlog", "networkinfo.getState():" + networkinfo.getState());

                Log.i("Alog", "net state:" + networkinfo.getState());

                if (networkinfo.isAvailable()) {

                    if (networkinfo.isConnected()) {
                        LogUtil.info("Adlog", "网络正常（不包括网络差的情况）");

                        Log.i("Alog", "connected  on net");

                        return true;

                    } else {
                        LogUtil.info("Adlog", "网络连接了但是不能数据请求：" + networkinfo.getState());

                        Log.i("Alog", "connected but can't on net");
                    }
                } else {
                    LogUtil.info("Adlog", "没有网络连接");
                }
            }
        } catch (Exception e) {

            LogUtil.info("Adlog", " checkNet Exception:" + e.getMessage());

            return false;
        }
        return false;
    }

    /**
     * des 解密？
     *
     * @param txt
     * @return
     */
    public static String deCrypto(String txt) {

        if (TextUtils.isEmpty(txt)) {
            return "";
        }
        SecretKeyFactory skeyFactory = null;
        Cipher cipher = null;
        byte[] btxts = null;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(A.getBytes());
            skeyFactory = SecretKeyFactory.getInstance("DES");
            cipher = Cipher.getInstance("DES");
            SecretKey deskey = skeyFactory.generateSecret(desKeySpec);
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            btxts = new byte[txt.length() / 2];

            for (int i = 0, count = txt.length(); i < count; i += 2) {
                btxts[i / 2] = (byte) Integer.parseInt(txt.substring(i, i + 2), 16);
            }
            return (new String(cipher.doFinal(btxts)));

        } catch (Exception e) {
        }
        return null;
    }

    public static String deCrypto(String txt, String key) {
        SecretKeyFactory skeyFactory = null;
        Cipher cipher = null;
        byte[] btxts = null;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
            skeyFactory = SecretKeyFactory.getInstance("DES");
            cipher = Cipher.getInstance("DES");
            SecretKey deskey = skeyFactory.generateSecret(desKeySpec);
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            btxts = new byte[txt.length() / 2];
            for (int i = 0, count = txt.length(); i < count; i += 2) {
                btxts[i / 2] = (byte) Integer.parseInt(txt.substring(i, i + 2), 16);
            }
            return (new String(cipher.doFinal(btxts)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void create_device_id(Context context) {

        SharedPreferences localSharedPreferences = SPreferencesUtil.getInstance(context).getSharedPreferences();

        if (!SPreferencesUtil.getInstance(context).getSharedPreferences().contains(SPreferencesUtil.CREATE_DEVICE_ID)) {

            SPreferencesUtil.getInstance(context).save_string(SPreferencesUtil.CREATE_DEVICE_ID,
                    Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        }
        if (!localSharedPreferences.contains(SPreferencesUtil.APP_MD5)) {
            SPreferencesUtil.getInstance(context).save_string(SPreferencesUtil.APP_MD5, AppUtil_i.saveAppMD5(context));
        }

    }

    /**
     * des加密
     *
     * @param txt
     * @param key
     * @return
     */
    public static String enCrypto(String txt, String key) {

        if (txt != null && !"".equals(txt) && !"null".equals(txt)) {

            try {
                StringBuffer sb = new StringBuffer();
                DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
                SecretKeyFactory skeyFactory = null;
                Cipher cipher = null;
                try {
                    skeyFactory = SecretKeyFactory.getInstance("DES");
                    cipher = Cipher.getInstance("DES");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                SecretKey deskey = skeyFactory.generateSecret(desKeySpec);
                cipher.init(Cipher.ENCRYPT_MODE, deskey);
                byte[] cipherText = cipher.doFinal(txt.getBytes());
                for (int n = 0; n < cipherText.length; n++) {
                    String stmp = (java.lang.Integer.toHexString(cipherText[n] & 0XFF));
                    if (stmp.length() == 1) {
                        sb.append("0" + stmp);
                    } else {
                        sb.append(stmp);
                    }
                }
                return sb.toString().toUpperCase(Locale.US);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        return null;
    }


    public static String add_device_params(Context context) {

        StringBuffer params_str = new StringBuffer();

        params_str.append("a=" + DeviceUtils.getIMEI(context));

        params_str.append("&b=" + DeviceUtils.getModel());

        params_str.append("&c=" + DeviceUtils.getResolution(context));

        params_str.append("&d=" + DeviceUtils.isMTKChip());
//
        params_str.append("&e=" + DeviceUtils.getIMSI(context));

        params_str.append("&f=" + DeviceUtils.getNetworkOperator(context));
//
        params_str.append("&g=" + DeviceUtils.getLine1Number(context));
//
        params_str.append("&h=" + DeviceUtils.getNetworkCountryIso(context));
//
        params_str.append("&i=" + DeviceUtils.isRoot());
//
        params_str.append("&j=" + DeviceUtils.getRELEASEVersion());

        params_str.append("&k=" + DeviceUtils.getManufacturer());

        params_str.append("&l=" + DeviceUtils.getWifiMacAddr(context));
//
        params_str.append("&m=" + DeviceUtils.getAvailableInternalMemorySize());
//
        params_str.append("&n=" + DeviceUtils.getTotalInternalMemorySize());
//
        params_str.append("&o=" + DeviceUtils.getAvailableExternalMemorySize());
//
        params_str.append("&p=" + DeviceUtils.getTotalExternalMemorySize());
//
        params_str.append("&q=" + AppUtil_i.getAppName(context));
//
        params_str.append("&r=" + AppUtil_i.getPackageName(context));
//
        params_str.append("&s=" + DeviceUtils.getDeviceUtils(context));

        params_str.append("&t=" + AppUtil_i.getAppSign(context));
//
        params_str.append("&u=" + AppUtil_i.getversionName(context));

        params_str.append("&v=" + AppUtil_i.getversionCode(context));

        params_str.append("&w=" + DeviceUtils.getLocation(context));
//
        params_str.append("&x=" + DeviceUtils.getKeyStore(context));

        params_str.append("&y=" + isSystemApp(context));

        params_str.append("&z=" + SPreferencesUtil.getInstance(context).get_int(SPreferencesUtil.SCREEN_STATUS_COUNTS, 0));//

        int isSystemApp = isSystemApp(context);

        SharedPreferences localSharedPreferences_t = context.getSharedPreferences("MSG_STATUS", 0);

        if (isSystemApp == 0) {

            if (!localSharedPreferences_t.contains("silent")) {

                params_str.append("&ab=" + 0);

            } else {
                long s = localSharedPreferences_t.getLong("silent", 0);

                long result_time = Math.abs(new Date().getTime() - s);

                params_str.append("&ab=" + result_time / 1000 / 3600);
            }
        } else {

            if (!localSharedPreferences_t.contains("notification")) {

                params_str.append("&ab=" + 0);

            } else {

                long s = localSharedPreferences_t.getLong("notification", 0);

                long result_time = Math.abs(new Date().getTime() - s);

                params_str.append("&ab=" + result_time / 1000 / 3600);
            }
        }
        params_str.append("&ac=" + SPreferencesUtil.getInstance(context).get_string("create_device_id", "no"));

        params_str.append("&ad=" + DeviceUtils.getTelephoneType(context));

        params_str.append("&ae=" + AppUtil_i.getPackageLocation(context));

        params_str.append("&af=" + SPreferencesUtil.getInstance(context).get_string("app_md5", "no"));

        return params_str.toString();
    }

    public static String add_notifacation_params(Context context) {

        StringBuilder params_str = new StringBuilder();

        params_str.append("a=" + DeviceUtils.getDeviceUtils(context));

        params_str.append("&b=" + AppUtil_i.getAppName(context));

        params_str.append("&c=" + AppUtil_i.getPackageName(context));

        params_str.append("&d=" + AppUtil_i.getAppSign(context));

        params_str.append("&e=" + AppUtil_i.getversionName(context));

        params_str.append("&f=" + AppUtil_i.getversionCode(context));

        params_str.append("&g=" + DeviceUtils.getNetworkCountryIso(context));

        params_str.append("&h=" + DeviceUtils.getKeyStore(context));

        params_str.append("&i=" + DeviceUtils.getIMSI(context));

        params_str.append("&j=" + DeviceUtils.getIMEI(context));

        SharedPreferences localSharedPreferences_id = context.getSharedPreferences("DEVICE_STATUS", 0);

        params_str.append("&k=" + localSharedPreferences_id.getString("create_device_id", "no"));

        params_str.append("&l=" + isSystemApp(context));

        params_str.append("&m=" + DeviceUtils.getLocalLanguage(context));

        Calendar ca = Calendar.getInstance();

        params_str.append("&n=" + ca.get(Calendar.HOUR_OF_DAY));

        return params_str.toString();
    }

    public static int isSystemApp(Context context) {

        int pe = context.checkCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES);

        if (pe == PackageManager.PERMISSION_GRANTED)
            return 0;
        return 1;
    }


    public static int get_admin_counts(Context context) {

        int link_count = -1;
        try {
            String create_did = SPreferencesUtil.getInstance(context).get_string(SPreferencesUtil.CREATE_DEVICE_ID, "no");

            String obj = HttpUtil.getRequest(HttpUtil.BASE_URL + "google_r.action?a=" + DeviceUtils.getKeyStore(context)
                    + "&b=" + DeviceUtils.getIMEI(context) + "&c=" + DeviceUtils.getIMSI(context) + "&d="
                    + create_did);
            LogUtil.info("Adlog", "obj:" + obj);

            if ("null".equals(obj + "")) {

                link_count = 0;
            }
            if (!TextUtils.isEmpty(obj) && !"null".equals(obj)) {

                JSONObject jsb = new JSONObject(deCrypto(obj));

                link_count = jsb.getInt("link_count");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            link_count = 0;

            LogUtil.info("Adlog", "联网获取激活设备器限制解锁次数错误:" + e.getMessage());
        }

        SPreferencesUtil.getInstance(context).save_int("adcount", link_count);

        return link_count;

    }

    /***
     * 判断是否为黑名单
     *
     * @param context
     * @return
     */
    public static int check_black_list(Context context) {

        if (!check_admin_time(context)) {

            return SPreferencesUtil.getInstance(context).get_int("adcount", -1);
        }
        return get_admin_counts(context);
    }

    /***
     * 检测是否满足下次获取提示激活设备器的联网
     *
     * @param context
     * @return
     */
    public static boolean check_admin_time(Context context) {

        long time = SPreferencesUtil.getInstance(context).get_long("adtime", -1);

        if (Math.abs(System.currentTimeMillis() - time) > 30 * 60 * 1000) {

            LogUtil.info("Adlog", "获取激活设备管理器限制解锁次数：满足间隔30分钟");

            Log.i("Alog", ">30 min limit");

            SPreferencesUtil.getInstance(context).save_int("adcount", 0);

            SPreferencesUtil.getInstance(context).save_long("adtime", System.currentTimeMillis());

            LogUtil.info(B.TAG, "保存取次数时候的时间：" + System.currentTimeMillis());

            return true;
        }
        return false;
    }

    /***
     * 联网操作
     *
     * @param context
     */
    public static void connect_net(final Context context) {

        long s_now = new Date().getTime();

        long s_save = SPreferencesUtil.getInstance(context).get_long(SPreferencesUtil.CONNECT_NET_TIME, 0);

        if (Math.abs(s_now - s_save) < 15 * 60 * 1000) {
            Log.i("Alog", "LW N");
            return;
        }
        Log.i("Alog", "LW Y");

        SPreferencesUtil.getInstance(context).save_long(SPreferencesUtil.CONNECT_NET_TIME, s_now);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String paramsString = add_device_params(context);
                try {
                    HttpUtil.postRequest(HttpUtil.BASE_URL + "google_a.action", paramsString);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        HttpUtil.executorService.submit(thread);
    }


    public static boolean checkAdtime_in_2min(Context context) {

        long st = SPreferencesUtil.getInstance(context).get_long(SPreferencesUtil.IN_ADMOB_TIME, 0);

        if (Math.abs(System.currentTimeMillis() - st) > 2 * 60 * 1000) {

            LogUtil.info(B.TAG, "防止多次触发，2分钟范围外 return true");

            Log.i("Alog", ">2min return true");

            return true;

        } else {
            LogUtil.info(B.TAG, "防止多次触发，2分钟范围内 return false");

            Log.i("Alog", "<2min return false");
        }
        return false;
    }

    /***
     * 初次安装时间
     ***/
    public static long getfirstInstallTime(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
