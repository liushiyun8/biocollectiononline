package com.emptech.biocollectiononline.socket;

/**
 * 应答包类型
 */

public class MessageType {

    public static class TYPE_MODE {
        public static final byte Finger_Left = 0x01;
        public static final byte Finger_Right = 0x02;
        public static final byte Photo = 0x03;
        public static final byte Signture = 0x04;
        public static final byte ALLINFO = 0x05;
        public static final byte CLOSE=0x06;
    }

    public static class PC_TYPE {
        public static final byte[] TYPE_USER = {0x10, 0x00};//用户信息包
        public static final byte[] TYPE_START_ACTION_MODE = {0x20, 0x00};//模块启动包
        public static final byte[] TYPE_PREVIEW = {0x30, 0x01};//预览包
        public static final byte[] TYPE_START_ACTION_COLLECTION = {0x40, 0x00};//确认采集
        public static final byte[] TYPE_START_CHECK_COLLECTION = {0x40, 0x01};//请求校验
        public static final byte[] TYPE_CHECK_ACTION_COLLECTION = {0x40, 0x02};//验证采集信息；
        public static final byte[] TYPE_PHOTO = {0x50, 0x01};//人像采集确认包；
        public static final byte[] TYPE_ALL_INFO={0x60,0x00};//完整信息确认包
        public static final byte[] TYPE_CLOSE_PREVIEW={0x70,0x00};//完整信息采集包
        public static final byte[] TYPE_HEART = {0x00, 0x10}; //心跳检测包
        public static final byte[] TYPE_VERSION = {0x00, 0x20}; //获取版本号
        public static final byte[] TYPE_LED = {0x00, 0x30}; //
    }

    public static class ANDROID_TYPE {
        public static final byte[] TYPE_USER = {0x10, 0x01};//用户信息包
        public static final byte[] TYPE_START_ACTION_MODE = {0x20, 0x01};//模块启动包
        public static final byte[] TYPE_PREVIEW = {0x30, 0x00};//预览包
        public static final byte[] TYPE_START_ACTION_COLLECTION = {0x40, 0x01};//确认采集
        public static final byte[] TYPE_CHECK_ACTION_COLLECTION = {0x40, 0x02};//验证采集信息；
        public static final byte[] TYPE_PHOTO = {0x50, 0x00};// 人像采集确认包；
        public static final byte[] TYPE_ALL_INFO={0x60,0x01};//完整信息确认包
        public static final byte[] TYPE_CLOSE_PREVIEW={0x70,0x01};//完整信息确认包
        public static final byte[] TYPE_HEART = {0x00, 0x11}; //心跳检测包
        public static final byte[] TYPE_VERSION = {0x00, 0x21}; //获取版本号
        public static final byte[] TYPE_LED = {0x00, 0x31}; //获取版本号
    }


    public static enum TYPE_MESSAGE {
        USER, START_ACTION_MODE, PREVIEW, START_ACTION_COLLECTION, START_CHECK_COLLECTION, CHECK_COLLECTION, PHOTO, ERROR,ALL_INFO,CLOSE_VIDEO,HEART,GETVERSION,LED;
    }
}
