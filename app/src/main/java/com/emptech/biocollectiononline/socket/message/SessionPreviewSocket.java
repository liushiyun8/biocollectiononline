package com.emptech.biocollectiononline.socket.message;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class SessionPreviewSocket extends SocketSession {


    @Override
    public byte[] getMessageToClient(byte[] previewData) {
        byte mode = runningMode;
        int len = previewData.length;
        int index = 0;
        byte[] messageContent = new byte[len + 1 + 5 + 2];
        messageContent[index]=0x00;
        index++;
        //messageType
        messageContent[index] = 0x30;
        index++;
        messageContent[index] = 0x00;
        index++;
        //模块
        messageContent[index] = mode;
        index++;
        //图片长度
        messageContent[index] = (byte) (((len) >> 24) & 0xFF);
        index++;
        messageContent[index] = (byte) (((len) >> 16) & 0xFF);
        index++;
        messageContent[index] = (byte) (((len) >> 8) & 0xFF);
        index++;
        messageContent[index] = (byte) ((len) & 0xFF);
        index++;
        //图片数据
        System.arraycopy(previewData, 0, messageContent, index, len);
        return makePacket(messageContent);
    }


    public byte[] getErrorMessageToClient() {
        byte mode = runningMode;
        int index=0;
        byte[] messageContent = new byte[4];
        messageContent[index]=0x01;
        index++;
        //messageType
        messageContent[index] = 0x30;
        index++;
        messageContent[index] = 0x00;
        index++;
        //模块
        messageContent[index] = mode;
        return makePacket(messageContent);
    }

}
