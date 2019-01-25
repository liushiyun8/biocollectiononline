package com.emptech.biocollectiononline.bean;

import java.util.Map;

/**
 * Created by linxiaohui on 2018/1/2.
 */

public class UserInfoMsg {
    private Map<String, String> mUserInfo;
    private byte[] photoIconByte;

    public Map<String, String> getmUserInfo() {
        return mUserInfo;
    }

    public void setmUserInfo(Map<String, String> mUserInfo) {
        this.mUserInfo = mUserInfo;
    }

    public byte[] getPhotoIconByte() {
        return photoIconByte;
    }

    public void setPhotoIconByte(byte[] photoIcon) {
        this.photoIconByte = photoIcon;
    }
}
