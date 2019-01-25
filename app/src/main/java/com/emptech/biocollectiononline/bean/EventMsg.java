package com.emptech.biocollectiononline.bean;

public class EventMsg {
    public static final int EVENT_MSG_TIME_CHANGED = 0x01;
    public static final int EVENT_MSG_NETCHANGED = 0x02;

    public static final int EVENT_NET_WAITING = 0x11;
    public static final int EVENT_NET_LOST = 0x12;
    public static final int EVENT_NET_CONNECT = 0x13;

    public static int CURRENT_NET_STATUS=EVENT_NET_WAITING;

    public int what;

    public int arg1;

    public int arg2;

    public Object obj;

    public EventMsg(int what, int arg1, int arg2, Object obj) {
        super();
        this.what = what;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.obj = obj;
    }

    public static void setCurrentNetStatus(int status){
        CURRENT_NET_STATUS=status;
    }

}
