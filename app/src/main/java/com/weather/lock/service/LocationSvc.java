package com.weather.lock.service;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.weather.lock.util.GetYahooCityCodeSaxTools;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.SharedUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by xlc on 2017/2/10.
 */
public class LocationSvc extends Service {

    private static final String TAG = "LocationSvc";

    private static final String LOCATION_URL_2 = "http://www.ip-api.com/json";

    private static final String LOCATION_URL_1 = "http://freegeoip.net/json/";

    private String current_url;

    private SharedUtil sharedUtil = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        LogUtil.info("location_ onCreate:");

        sharedUtil = SharedUtil.getInstance(this);

    }

    class LocationTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            String string = params[0];

            LogUtil.info("查找地址的链接：" + string);

            BufferedReader in = null;

            URLConnection connection;

            try {

                URL url = new URL(string);
                //获取HttpURLConnection对象
                connection = url.openConnection();
                //                    connection.setReadTimeout(5000);
                //设置连接超时时间,毫秒为单位
                connection.setConnectTimeout(5000);

                connection.setRequestProperty("accept", "*/*");

                connection.setRequestProperty("connection", "Keep-Alive");

                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");

                //http方式

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;

                String result = "";

                while ((line = in.readLine()) != null) {
                    result += line;
                }
                LogUtil.info("sb.toString():" + result);

                JSONObject jsonObject = new JSONObject(result);

                return jsonObject.getString("city");

            } catch (Exception e) {

                LogUtil.info("定位地址错误：" + e.getMessage());

                e.printStackTrace();
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (TextUtils.isEmpty(s)) {

                if (!current_url.equals(LOCATION_URL_2)) {

                    LogUtil.info("第一个链接无法获取到地址，使用第二个");

                    new LocationTask().execute(LOCATION_URL_2);

                    current_url = LOCATION_URL_2;
                }
                return;
            }

            LogUtil.info("获取到的地址为：" + s);

            new SaxCityCodeAsync().execute(s);

        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtil.info("location_ onStartCommand:");

        sharedUtil.save_long("execute_location_time", System.currentTimeMillis());

        current_url = " ";

        new LocationTask().execute(LOCATION_URL_1);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    class SaxCityCodeAsync extends AsyncTask<String, Void, ArrayList<String>> {

        SAXParserFactory factory = null;
        SAXParser saxParser = null;
        XMLReader xmlReader = null;
        GetYahooCityCodeSaxTools tools = null;

        ArrayList<String> cityCodeHashMap;

        String location_name;

        @Override
        protected void onPreExecute() {
            try {
                factory = SAXParserFactory.newInstance();
                saxParser = factory.newSAXParser();
                xmlReader = saxParser.getXMLReader();
                cityCodeHashMap = new ArrayList<>();
                tools = new GetYahooCityCodeSaxTools(cityCodeHashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            location_name = params[0];

            String cityCodeUrl = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D'" + location_name + "'";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(cityCodeUrl);
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    xmlReader.setContentHandler(tools);
                    xmlReader.parse(new InputSource(new InputStreamReader(content)));
                } else {
                    LogUtil.info("Failed to download file");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }
            return cityCodeHashMap;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            LogUtil.info("result:" + result);

            if (result == null || result.size() <= 0) return;

            SharedUtil.getInstance(LocationSvc.this).save_string(SharedUtil.K_LOCATION, location_name);

            SharedUtil.getInstance(LocationSvc.this).save_string(SharedUtil.LOCATION_NAME, location_name);

            Intent intent = new Intent();

            intent.putStringArrayListExtra("infoList", result);

            intent.setAction("gps.location." + getPackageName());

            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.info("location Service onDestroy");
    }


}