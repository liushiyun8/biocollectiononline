package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 用户信息组合
 */

public class MessageUserInfoHandler extends MessageHandler implements IMessageUserInfo {


    @Override
    public UserInfoMsg handlerMessageFromServer(byte[] data) {
        byte[] userData = getMessageContentByPacket(data);
        if (userData == null) {
            return null;
        }
        if ((userData[0] & 0xFF) != 0x10 || (userData[1] & 0xFF) != 0x00) {
            return null;
        }

        //获取用户字符串信息；
        int index = 2;
        int calcUserLength = ((userData[index] & 0xff) << 8)
                + (userData[index + 1] & 0xff);
        index += 2;
        byte[] userInfoByte = new byte[calcUserLength];
        System.arraycopy(userData, index, userInfoByte, 0, calcUserLength);
        index += calcUserLength;
        //获取用户照片信息；
        int calcPhotoLength = ((userData[index] & 0xff) << 24)
                + ((userData[index + 1] & 0xff) << 16)
                + ((userData[index + 2] & 0xff) << 8)
                + (userData[index + 3] & 0xff);
        if (calcPhotoLength > userData.length) {
            return null;
        }
        index += 4;
        byte[] userPhotoByte = new byte[calcPhotoLength];
        System.arraycopy(userData, index, userPhotoByte, 0, calcPhotoLength);
        //缓存到对象中；
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> mUserInfo = new Gson().fromJson(new String(userInfoByte), type);
        UserInfoMsg mUserInfoMsg = new UserInfoMsg();
        mUserInfoMsg.setmUserInfo(mUserInfo);
        if (userPhotoByte.length != 0) {
            mUserInfoMsg.setPhotoIconByte(userPhotoByte);
        }
        return mUserInfoMsg;
    }
}