package com.emptech.biocollectiononline.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.emptech.biocollectiononline.utils.CommUtils;

import cn.xiongdi.jni.UserDevices;

public class VersionManager {
    private static String TAG="VersionManager";
    public static String pmVersion;
    public static String signatureVersion="V1.0.0";
    public static String AppVersion;

    public static String getPmVersion() {
        if(pmVersion==null){
            byte[] version = new byte[8];
            int ret1=UserDevices.get_version(version);
            Log.v(TAG,"ret:"+ ret1);
            char[] chars = new char[7];
            for (int i = 0; i < chars.length; i++) {
                chars[i]= (char) version[i];
            }
            String powerV=new String(chars);
            pmVersion="V"+(ret1!=0?"1.0.0.0":powerV);
        }
        return pmVersion;
    }

    public static void setPmVersion(String pmVersion) {
        VersionManager.pmVersion = pmVersion;
    }

    public static String getSignatureVersion() {
        SignatureManager signatureManager = new SignatureManager();
        String version=null;
        if(signatureManager.openDevice()){
            version = signatureManager.getVersion();
        }
        if(version!=null){
            if(version.startsWith("version")){
                version=version.replace("version ","V" );
                version=version.replaceAll("[\\t\\n\\r]", "");
            }
            return version;
        }
        return signatureVersion;
    }

    public static void setSignatureVersion(String signatureVersion) {
        VersionManager.signatureVersion = signatureVersion;
    }

    public static String getAppVersion() {
        return AppVersion;
    }

    public static void setAppVersion(String appVersion) {
        AppVersion = appVersion;
    }

    public static String getServerVersion(Context context) {
        return  CommUtils.getVersion(context,"com.emp.collectionStartup" );
    }

    public static String getHardVersion(Context context) {
        return  CommUtils.getVersion(context,"com.emp.collectionhardtest" );
    }
}
