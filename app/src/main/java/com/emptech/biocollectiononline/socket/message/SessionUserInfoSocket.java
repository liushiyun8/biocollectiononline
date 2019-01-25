package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.emptech.biocollectiononline.socket.MessageType;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class SessionUserInfoSocket extends SocketSession {
    private UserInfoMsg mUserInfoMsg;

    @Override
    public void setUserMsg(UserInfoMsg mUserInfoMsg) {
        this.mUserInfoMsg = mUserInfoMsg;
    }

    public UserInfoMsg getmUserInfoMsg() {
        return mUserInfoMsg;
    }

    @Override
    public byte[] getMessageToClient(byte[] isSuccess) {
        if (isSuccess == null || isSuccess.length != 1) {
            return null;
        }
        byte[] dataContent = new byte[4];
        dataContent[0] = isSuccess[0];
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_USER[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_USER[1];
        dataContent[3] = isSuccess[0];
        return makePacket(dataContent);
    }
}
