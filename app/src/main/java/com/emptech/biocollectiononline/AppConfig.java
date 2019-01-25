package com.emptech.biocollectiononline;


import com.emptech.biocollectiononline.utils.SDCardUtil;

import java.io.File;

public class AppConfig {
    public static final String MODULE_CATCHERR = "CRASH";
    public static final String MODULE_APP = "APP";
    public static final String MODULE_SERVER = "SERVER";
    public static final String WORK_PATH = SDCardUtil.getSDCardPath() + "com.xiongdi.Biocollection";
    public static final String WORK_PATH_LOG = WORK_PATH + File.separator + "log";
//    public static final String WORK_PATH_LOG = SDCardUtil.getSDCardPath()+"crash/log";
    public static final String WORK_PATH_CRASH = WORK_PATH+"/crash";
    public static final String WORK_FINGER_PATH = WORK_PATH + File.separator + "FingerPrint";
    public static final String WORK_PHOTO_PATH = WORK_PATH + File.separator + "Photo";
    public static final String WORK_SIGNTURE_PATH = WORK_PATH + File.separator + "Signture";
    public static final String WORK_TEMP_PATH = WORK_PATH + File.separator + "Temp";
    public static final String WORK_UPDATE_PATH = WORK_PATH + File.separator + "UpdateSoftware";

    public static boolean isDebug = false;  //true 为debug模式，打印日志到控制台 ; false为release,打印日志到文件
    public static final String BUNDLE = "iSocket";

    public static final String PREFERENCE_KEY_IDNUMBER = "ID_Number";// 正在操作的人员名单ID；
    public static final String PREFERENCE_KEY_LEFTFINGER = "LeftFingerPrintFilePath";//左手指纹存储路径
    public static final String PREFERENCE_KEY_RIGHTFINGER = "RightFingerPrintFilePath";//右手指纹存储路径

    public static final String PREFERENCE_KEY_LEFTFINGER_NUMBER = "LeftFingerPrintNumber";//左手指纹编号
    public static final String PREFERENCE_KEY_RIGHTFINGER_NUMBER = "RightFingerPrintNumber";//右手指纹编号

    public static final String PREFERENCE_KEY_PHOTO = "Photo";//人物照片
    public static final String PREFERENCE_KEY_SIGNTURE = "Signture";//签名文件；
    public static final String PREFERENCE_KEY_FILLLIGHT = "FILLlight";//签名文件；

    public static final String PREFERENCE_KEY_STATICIP = "staticip";//静态IP；
    public static final String PREFERENCE_KEY_IPMASK = "ipmask";//子网掩码；
    public static final String PREFERENCE_KEY_GATEWAY = "gateway";//网关；
    public static final String PREFERENCE_KEY_DNS = "dns1";//DNS；
    public static final String PREFERENCE_KEY_VOLUME = "volum";//DNS；

    public static final String UPDATESOFTPATH="/EMP2950/2950update/";
    public static final String APKFILE =UPDATESOFTPATH+"Collection.apk";
    public static final String PMSOFTFILENAME ="_emp2950A_pm_app.bin";
    public static final String SIGNATURESOFTFILENAME ="_emp2950A_signature.bin";
    public static final String MODULE = "APP";
    public static final String BUNDLE_KEY1 = "USB_PATH";
    public static final String XD_CFG_NAME = "cfg.xml";
    public static final String XD_ROOT_DIR = "/EMP2950";
    public static final String LANGUAGE_KEY = "language_key";
    public static final String DEPARTMENT_KAY = "department_key";

    //logo
    public static final byte part1=0x01;
    public static final byte part2=0x02;
    public static boolean WIFIENABLE;

    public static int PHOTO_ROTATE = 90;

    public static final int UPDATE_APP_TYPE=1;
    public static final int UPDATE_PM_TYPE=2;
    public static final int UPDATE_SIGNATURE_TYPE=3;


    //test
    public static boolean isTestSocket;
    public static boolean isTest;
    public static boolean TestTimeout;
    public static boolean TestError;
    public static boolean TestClose;
}
