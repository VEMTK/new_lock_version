package com.weather.lock.util;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GetYahooCityCodeSaxTools extends DefaultHandler {


    private List<String> hashMap;

    private boolean isAdd = false;

    public GetYahooCityCodeSaxTools(List<String> hashMap) {
        this.hashMap = hashMap;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if ( localName == "woeid") {
            isAdd = true;
        }
    }
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (isAdd) {
            String str = new String(ch, start, length);
            Log.e("tool","woeid:"+str);
            hashMap.add(str);
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (localName == "woeid") {
            isAdd = false;
        }
    }

    @Override
    public void endDocument() throws SAXException {
    }

}
