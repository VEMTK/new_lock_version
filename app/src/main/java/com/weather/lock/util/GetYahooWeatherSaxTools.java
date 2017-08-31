package com.weather.lock.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class GetYahooWeatherSaxTools extends DefaultHandler {
    private ArrayList<HashMap<String, String>> arrayList;
    private HashMap<String, String> hashMap;
    private boolean isfirst = true;

    public GetYahooWeatherSaxTools() {
        isfirst = true;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    int code;

    private int temp;

    private String week_day;

    private int height_temp;

    public int getHeight_temp() {
        return height_temp;
    }

    public void setHeight_temp(int height_temp) {
        this.height_temp = height_temp;
    }

    public int getLow_temp() {
        return low_temp;
    }

    public void setLow_temp(int low_temp) {
        this.low_temp = low_temp;
    }

    private int low_temp;

    private String text;

    private String date;

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getWeek_day() {
        return week_day;
    }

    public void setWeek_day(String week_day) {
        this.week_day = week_day;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String img_url;

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (qName.equals("yweather:condition")) {


            String string = attributes.getValue("temp").trim();

            this.setTemp(Integer.valueOf(string));

        } else if (qName.equals("yweather:forecast") && isfirst) {

            isfirst = false;

            this.setWeek_day(attributes.getValue("day"));

            this.setDate(attributes.getValue("date"));

            this.setHeight_temp(Integer.valueOf(attributes.getValue("high")));

            this.setLow_temp(Integer.valueOf(attributes.getValue("low")));

            this.setText(attributes.getValue("text"));

            this.setCode(Integer.valueOf(attributes.getValue("code")));


//			hashMap.put("day",attributes.getValue("day"));
//			hashMap.put("date",attributes.getValue("date"));
//			hashMap.put("low",attributes.getValue("low"));
//			hashMap.put("high",attributes.getValue("high"));
//			hashMap.put("text",attributes.getValue("text"));
//			hashMap.put("code",attributes.getValue("code"));
//			arrayList.add(hashMap);
        }
    }

   

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        String str = new String(ch, start, length);

        if (str.startsWith("img src="))  //判断字符串str 是不是以字符串"<img src="开头
        {
            String s2 = str.substring(str.indexOf("\"") + 1, str.length() - 2);

            Log.d("Adlog", "链接：" + s2);

            this.setImg_url(s2);
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

}
