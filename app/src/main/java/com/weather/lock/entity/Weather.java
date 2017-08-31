package com.weather.lock.entity;

import android.content.ContentValues;

/**
 * Created by xlc on 2017/2/20.
 */
public class Weather {

    public String getWoeid() {
        return woeid;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public int getLow_temp() {
        return low_temp;
    }

    public void setLow_temp(int low_temp) {
        this.low_temp = low_temp;
    }

    public int getHeight_temp() {
        return height_temp;
    }

    public void setHeight_temp(int height_temp) {
        this.height_temp = height_temp;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public int getWeather_code() {
        return weather_code;
    }

    public void setWeather_code(int weather_code) {
        this.weather_code = weather_code;
    }

    public String getQuery_date() {
        return query_date;
    }

    public void setQuery_date(String query_date) {
        this.query_date = query_date;
    }

    public Weather(String woeid, int low_temp, int height_temp, String city_name, int weather_code, String query_date) {
        this.woeid = woeid;
        this.low_temp = low_temp;
        this.height_temp = height_temp;
        this.city_name = city_name;
        this.weather_code = weather_code;
        this.query_date = query_date;
    }

    public Weather(){}

    private String woeid;
    private int low_temp;
    private int height_temp;
    private String city_name;
    private int weather_code;
    private String query_date;

    public static final String WOEID = "woeid";
    public static final String LOW_TEMP = "low_temp";
    public static final String HEIGHT_TEMP = "height_temp";
    public static final String CITY_NAME = "city_name";
    public static final String WEATHER_CODE = "weather_code";
    public static final String QUERY_DATE = "query_date";


    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WOEID, woeid);
        contentValues.put(LOW_TEMP, low_temp);
        contentValues.put(HEIGHT_TEMP, height_temp);
        contentValues.put(CITY_NAME, city_name);
        contentValues.put(WEATHER_CODE, weather_code);
        contentValues.put(QUERY_DATE,query_date);
        return contentValues;
    }


}
