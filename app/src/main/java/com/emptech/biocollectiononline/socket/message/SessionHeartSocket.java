package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.socket.MessageType;

public class SessionHeartSocket extends SocketSession {

    @Override
    public byte[] getMessageToClient(byte[] content) {
        byte[] dataContent = new byte[4];
        dataContent[0] = 0x00;
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_HEART[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_HEART[1];
        dataContent[3]=runningMode;
        return makePacket(dataContent);
    }

}
