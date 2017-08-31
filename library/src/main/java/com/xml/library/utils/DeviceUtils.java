package com.xml.library.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("ResourceType")
public class DeviceUtils {

//	public static String getBuletoothMacAddr(Context paramContext) {
//		String btMac = "";
//		try {
//			BluetoothAdapter m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//			if (m_BluetoothAdapter != null) {
//				btMac = m_BluetoothAdapter.getAddress();
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			LogUtil.info("err", e.getMessage());
//		}
//		return btMac;
//	}


    public static String getIMEI(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        String res = Utils.enCrypto(localTelephonyManager.getDeviceId(), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static String getIMEI_1(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        return localTelephonyManager.getDeviceId();
    }

    public static String getIMSI_1(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        return localTelephonyManager.getSubscriberId();
    }

    public static String getIMSI(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");

        String s = localTelephonyManager.getSubscriberId();

        String res = Utils.enCrypto(s, Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }

    }

    public static String getNetworkOperator(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        String res = Utils.enCrypto(localTelephonyManager.getNetworkOperator(), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }

    }

    public static String getLine1Number(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        String res = Utils.enCrypto(localTelephonyManager.getLine1Number(), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static String getNetworkCountryIso(Context paramContext) {
        TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
        String res = Utils.enCrypto(localTelephonyManager.getNetworkCountryIso(), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static int getScreenHeight(Context paramContext) {
        return paramContext.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(Context paramContext) {
        return paramContext.getResources().getDisplayMetrics().widthPixels;
    }

    public static int isRoot() {
        try {

            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                return 1;
            }

        } catch (Exception e) {

        }
        return 0;
    }

    public static String getRELEASEVersion() {

        return Utils.enCrypto(android.os.Build.VERSION.RELEASE + "", Utils.A);
    }

    public static String getManufacturer() {

        String res = Utils.enCrypto(Build.MANUFACTURER, Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static int getTelephoneType(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return telephonyManager.getPhoneType();

    }

    public static String getModel() {

        String res = Utils.enCrypto(Build.MODEL, Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static String getResolution(Context paramContext) {
        Resources localResources = paramContext.getResources();
        int i = localResources.getDisplayMetrics().widthPixels;
        int j = localResources.getDisplayMetrics().heightPixels;
        String res = Utils.enCrypto(i + "x" + j, Utils.A);
        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    //
    public static boolean isMTKChip() {
        boolean bool = true;
        try {
            Class.forName("com.mediatek.featureoption.FeatureOption");
            Log.d("DeviceUtils", "isMTKChip() isMtk=" + bool);
            return bool;
        } catch (ClassNotFoundException localClassNotFoundException) {
            bool = false;
        }
        return bool;
    }

    public static String getWifiMacAddr(Context paramContext) {
        WifiManager localWifiManager = (WifiManager) paramContext.getSystemService("wifi");
        WifiInfo wifiInfo = localWifiManager.getConnectionInfo();
        String res = Utils.enCrypto(wifiInfo.getMacAddress(), Utils.A);
        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        String res = Utils.enCrypto(String.valueOf(availableBlocks * blockSize), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        String res = Utils.enCrypto(String.valueOf(totalBlocks * blockSize), Utils.A);

        if (res == null) {
            return "CD6D40F84F547C00";
        } else {
            return res;
        }
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();

            String res = Utils.enCrypto(String.valueOf(availableBlocks * blockSize), Utils.A);

            if (res == null) {
                return "CD6D40F84F547C00";
            } else {
                return res;
            }

        } else {
            return "CD6D40F84F547C00";
        }
    }

    public static long getAvailableExternalMemorySize_long() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();

            return availableBlocks * blockSize;

        } else {
            return 0;
        }
    }

    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();

            String res = Utils.enCrypto(String.valueOf(totalBlocks * blockSize), Utils.A);

            if (res == null) {
                return "CD6D40F84F547C00";
            } else {
                return res;
            }
        } else {
            return Utils.enCrypto("-1", Utils.A);
        }
    }

    public static String getAndroid(Context context) {

        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    public static String getDevIDShort() {
        String m_szDevIDShort = "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10
                + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
                + Build.TYPE.length() % 10 + Build.USER.length() % 10;
        return m_szDevIDShort;
    }

    //
    public static String getDeviceUtils(Context paramContext) {

        String m_szLongID = Utils.deCrypto(getIMEI(paramContext), Utils.A) + getDevIDShort()
                + getAndroid(paramContext) + Utils.deCrypto(getWifiMacAddr(paramContext), Utils.A);
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        byte p_md5Data[] = m.digest();
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            if (b <= 0xF)
                m_szUniqueID += "0";
            m_szUniqueID += Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID;
    }

    public static String getKeyStore(Context context) {

        ApplicationInfo appInfo;

        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            String keystore = appInfo.metaData.getString("cid");

            String res = Utils.enCrypto(keystore, Utils.A);

            if (res == null) {
                return "CD6D40F84F547C00";
            } else {
                return res;
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getKeyStore_1(Context context) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            String keystore = appInfo.metaData.getString("cid");

            return keystore;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFlurryKey(Context context) {

        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            String keystore = appInfo.metaData.getString("flurry_id");

            return keystore;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getLocation(Context context) {

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        String result = "";

        if (l != null) {
            result = l.getLatitude() + "," + l.getLongitude();
        }
        return result;
    }

    public static String getLocalLanguage(Context context) {

        return Utils.enCrypto(context.getResources().getConfiguration().locale.getLanguage(), Utils.A);
    }

}