package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/5.
 */

public class SessionSigntureCheckSocket extends SocketSession {
    private String RecPhotoNumber;//图片编号；
    private String UserNumber;//用户信息；
    private byte[] singturePictureData;//图片数据

    @Override
    public byte[] getMessageToClient(byte[] isSuccess) {
        byte[] messageContent = new byte[13];
        int index = 0;
        messageContent[index] = isSuccess[0];
        index++;
        messageContent[index] = MessageType.ANDROID_TYPE.TYPE_CHECK_ACTION_COLLECTION[0];
        index++;
        messageContent[index] = MessageType.ANDROID_TYPE.TYPE_CHECK_ACTION_COLLECTION[1];
        index++;
        messageContent[index] = runningMode;
        index++;
        messageContent[index] = isSuccess[0];
        index++;
        byte[] photoNumber = Converter.string2Hex(RecPhotoNumber);
        if (photoNumber.length != 8) {
            LogUtils.e(AppConfig.MODULE_APP, "photoNumber'length is not 8：" + RecPhotoNumber);
            return null;
        }
        System.arraycopy(photoNumber, 0, messageContent, index, photoNumber.length);
        return makePacket(messageContent);
    }

    public String getRecPhotoNumber() {
        return RecPhotoNumber;
    }

    public void setRecPhotoNumber(String recPhotoNumber) {
        RecPhotoNumber = recPhotoNumber;
    }

    public String getUserNumber() {
        return UserNumber;
    }

    public void setUserNumber(String userNumber) {
        UserNumber = userNumber;
    }

    public byte[] getSingturePictureData() {
        return singturePictureData;
    }

    public void setSingturePictureData(byte[] singturePictureData) {
        this.singturePictureData = singturePictureData;
    }
}
