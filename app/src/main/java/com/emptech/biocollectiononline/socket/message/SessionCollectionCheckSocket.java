package com.emptech.biocollectiononline.socket.message;

/**
 * Created by linxiaohui on 2018/1/5.
 */

public class SessionCollectionCheckSocket extends SocketSession {
    private String RecPhotoNumber;//图片编号；
    private boolean isCheckSuccess = false;//图片校验是否通过；

    @Override
    public byte[] getMessageToClient(byte[] dataContent) {
        return null;
    }

    public String getRecPhotoNumber() {
        return RecPhotoNumber;
    }

    public void setRecPhotoNumber(String recPhotoNumber) {
        RecPhotoNumber = recPhotoNumber;
    }

    public boolean isCheckSuccess() {
        return isCheckSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        isCheckSuccess = checkSuccess;
    }
}
