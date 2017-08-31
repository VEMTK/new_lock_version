package com.weather.lock.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by xlc on 2017/2/7.
 */
public class ToolUtil {

    private static ToolUtil instance = null;

    private WifiManager wifiManager = null;

    private Context mContext;

    private BluetoothAdapter bluetoothAdapter = null;


    public boolean isWifi_on() {
        return wifi_on;
    }

    public void setWifi_on(boolean wifi_on_off) {
        this.wifi_on = wifi_on_off;
    }

    private boolean wifi_on = false;


    public static ToolUtil getInstance(Context context) {
        if (instance == null) {
            instance = new ToolUtil(context);
        }
        return instance;
    }

    private ToolUtil(Context context) {
        this.mContext = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /**
         * 初始化wifi状态
         */
        if (getWifiStatus()) {
            setWifi_on(true);
        } else {
            setWifi_on(false);
        }
    }


    public void setMobileDataStatus(boolean enabled) {

        ConnectivityManager conMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        //ConnectivityManager类
        Class<?> conMgrClass = null;
        //ConnectivityManager类中的字段
        Field iConMgrField = null;
        //IConnectivityManager类的引用
        Object iConMgr = null;
        //IConnectivityManager类
        Class<?> iConMgrClass = null;
        //setMobileDataEnabled方法
        Method setMobileDataEnabledMethod = null;
        try {
            //取得ConnectivityManager类
            conMgrClass = Class.forName(conMgr.getClass().getName());
            //取得ConnectivityManager类中的对象Mservice
            iConMgrField = conMgrClass.getDeclaredField("mService");
            //设置mService可访问
            iConMgrField.setAccessible(true);
            //取得mService的实例化类IConnectivityManager
            iConMgr = iConMgrField.get(conMgr);
            //取得IConnectivityManager类
            iConMgrClass = Class.forName(iConMgr.getClass().getName());

            //取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

            //设置setMobileDataEnabled方法是否可访问
            setMobileDataEnabledMethod.setAccessible(true);
            //调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);

        } catch (Exception e) {
            Intent intent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName clean = new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
            intent.setComponent(clean);
            mContext.startActivity(intent);
            e.printStackTrace();
        }
    }


    public boolean getMobileDataState(Object[] arg) {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class ownerClass = mConnectivityManager.getClass();

            Class[] argsClass = null;
            if (arg != null) {
                argsClass = new Class[1];
                argsClass[0] = arg.getClass();
            }
            Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);

            Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

            return isOpen;

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("得到移动数据状态出错");
            return false;
        }
    }

    public boolean getWifiStatus() {
        if (wifiManager.getWifiState() == 3) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 开关wifi
     */
    public void changeWifiStatus() {

        if (getWifiStatus()) {
            setWifi_on(false);
            wifiManager.setWifiEnabled(false);
        } else {
            setWifi_on(true);
            wifiManager.setWifiEnabled(true);
        }
    }

    public boolean getBlueStatus() {
        if(null == bluetoothAdapter){
            return false;
        }else{
            return bluetoothAdapter.isEnabled();
        }
    }

    public void setBlueStatus() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (getBlueStatus()) {
            bluetoothAdapter.disable();
        } else {
            bluetoothAdapter.enable();
        }
    }

    public boolean getWifiapStatus() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    private WIFI_AP_STATE getWifiApState() {
        int tmp;
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            tmp = ((Integer) method.invoke(wifiManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING, WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }


    public boolean setWifiApEnabled() {

        boolean enabled;

        if (getWifiapStatus()) enabled = false;

        else enabled = true;

        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);

            setWifi_on(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();

            apConfig.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "YRCCONNECTION";
            //配置热点的密码
            apConfig.preSharedKey = "12122112";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {

            e.printStackTrace();

            Log.e("tool", "open wifi hot error:" + e.getMessage());

        }
        return enabled;
    }


    private boolean check_exist_flash() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private Camera sCamera = null;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean openLight() {

        boolean open = true;

        if (!check_exist_flash()) {
            open = false;
        } else {
            try {
                sCamera = Camera.open();
                int textureId = 0;
                sCamera.setPreviewTexture(new SurfaceTexture(textureId));
                sCamera.startPreview();
                Camera.Parameters parameters = sCamera.getParameters();
                parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
                sCamera.setParameters(parameters);
            } catch (Exception e) {
                sCamera = null;
                Log.i("Adlog", "打开闪光灯失败：" + e.toString() + "");
                open = false;
            }
        }
        return open;
    }

    public void close_flash() {
        if (sCamera != null) {
            sCamera.stopPreview();
            sCamera.release();
            sCamera = null;
        }

    }
}
