package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.socket.MessageType;

public class SessionVersionSocket extends SocketSession {
    @Override
    public byte[] getMessageToClient(byte[] data) {
        byte[] dataContent;
        if(data!=null&&data.length!=0){
            dataContent= new byte[4+data.length];
            dataContent[0] = 0x00;
            dataContent[1] = MessageType.ANDROID_TYPE.TYPE_VERSION[0];
            dataContent[2] = MessageType.ANDROID_TYPE.TYPE_VERSION[1];
            dataContent[3]=runningMode;
            System.arraycopy(data,0 ,dataContent ,4 ,data.length );
        }else {
            dataContent = new byte[4];
            dataContent[0] = 0x01;
            dataContent[1] = MessageType.ANDROID_TYPE.TYPE_VERSION[0];
            dataContent[2] = MessageType.ANDROID_TYPE.TYPE_VERSION[1];
            dataContent[3]=runningMode;
        }
        return makePacket(dataContent);
    }
}
