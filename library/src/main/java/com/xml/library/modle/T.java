package com.xml.library.modle;

public class T {

    // atype:广告类型    c:弹出次数  in:时间间隔   aid:sdk中的appid  iid：sdk中的apiid  两个id可以互换

    private int atype;

    private int c;

    private long in;

    private String aid;

    private String iid;

    public int getAtype() {
        return atype;
    }

    public void setAtype(int atype) {
        this.atype = atype;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public long getIn() {
        return in;
    }

    public void setIn(long in) {
        this.in = in;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }


}
