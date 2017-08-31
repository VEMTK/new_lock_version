package com.weather.lock.entity;

/**
 * Created by xlc on 2017/2/6.
 */
public class Location {


    public void setLocation(String location) {
        this.location = location;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public String getLocation() {
        return location;
    }

    public String getWoeid() {
        return woeid;
    }

    String location;

    String woeid;

    public String getFrist_location() {
        return frist_location;
    }

    public void setFrist_location(String frist_location) {
        this.frist_location = frist_location;
    }

    String frist_location;


    public Location(String w, String n,String f_l)
    {
        this.woeid=w;
        this.location=n;
        this.frist_location=f_l;
    }


}
