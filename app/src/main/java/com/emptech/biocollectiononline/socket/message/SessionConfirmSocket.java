package com.emptech.biocollectiononline.socket.message;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class SessionConfirmSocket extends SocketSession {

    private boolean isSuccess;

    @Override
    public byte[] getMessageToClient(byte[] numberByte) {
        //messageType+ 采集模块+图片编号；
        if (runningMode == 0x00) {
            return null;
        }
        byte[] messageContent = new byte[2 + 1 + 8];
        int index = 0;
        messageContent[index] = 0x50;
        index++;
        messageContent[index] = 0x00;
        index++;
        messageContent[index] = runningMode;
        index++;
        System.arraycopy(numberByte, 0, messageContent, index, numberByte.length);
        return makePacket(messageContent);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
