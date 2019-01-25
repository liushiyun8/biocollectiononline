package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.emptech.biocollectiononline.socket.MessageType;

public class SessionUserAllInfoSocket extends SocketSession {

    UserInfoMsg userInfoMsg;

    public UserInfoMsg getUserInfoMsg() {
        return userInfoMsg;
    }

    public void setUserMsg(UserInfoMsg userInfoMsg) {
        this.userInfoMsg = userInfoMsg;
    }

    @Override
    public byte[] getMessageToClient(byte[] isSuccess) {
        if (isSuccess == null || isSuccess.length != 1) {
            return null;
        }
        byte[] dataContent = new byte[4];
        dataContent[0] = isSuccess[0];
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_ALL_INFO[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_ALL_INFO[1];
        dataContent[3] = runningMode;
        return makePacket(dataContent);
    }
}
