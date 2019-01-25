package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.SocketSession;

public class SessionCloseSocket extends SocketSession {
    @Override
    public byte[] getMessageToClient(byte[] data) {
        byte[] dataContent = new byte[4];
        dataContent[0] = data[0];
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_CLOSE_PREVIEW[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_CLOSE_PREVIEW[1];
        dataContent[3]=runningMode;
        return makePacket(dataContent);
    }
}
