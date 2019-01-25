package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.socket.MessageType;

public class SessionLedSocket extends SocketSession {
    byte value;
    @Override
    public byte[] getMessageToClient(byte[] data) {
        byte[] dataContent=new byte[4];
        dataContent[0] = data[0];
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_LED[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_LED[1];
        dataContent[3]=runningMode;
        return makePacket(dataContent);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }
}
