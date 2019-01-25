package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.socket.MessageType;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class SessionStartModeSocket extends SocketSession  {

    private int previewWidth;
    private int previewHeight;
    private byte Overtime;
    private byte NFIQ;

    private byte type;

    @Override
    public byte[] getMessageToClient(byte[] isSuccess) {
        if (isSuccess == null || isSuccess.length != 1) {
            return null;
        }
        byte[] dataContent = new byte[4];
        dataContent[0] = isSuccess[0];
        dataContent[1] = MessageType.ANDROID_TYPE.TYPE_START_ACTION_MODE[0];
        dataContent[2] = MessageType.ANDROID_TYPE.TYPE_START_ACTION_MODE[1];
        dataContent[3]=getRunningMode();
        return makePacket(dataContent);
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public byte getOvertime() {
        return Overtime;
    }

    public void setOvertime(byte overtime) {
        Overtime = overtime;
    }

    public byte getNFIQ() {
        return NFIQ;
    }

    public void setNFIQ(byte NFIQ) {
        this.NFIQ = NFIQ;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
