package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;

import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * Created by linxiaohui on 2018/1/5.
 */

public class MessageSigntureCheckHandler extends MessageHandler implements IMessageConfirmHandler {
    private String userNumberString;//用户编号；
    private String pictureNumberString;//图片编号；
    private byte[] picture;//图片数据；

    @Override
    public byte getRunningMode() {
        return runningMode;
    }

    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        if (!checkByteLegitimate(data)) {
            return false;
        }
        byte[] userData = getMessageContentByPacket(data);
        if ((userData[0] & 0xFF) == MessageType.PC_TYPE.TYPE_START_CHECK_COLLECTION[0] && (userData[1] & 0xFF) == MessageType.PC_TYPE.TYPE_START_CHECK_COLLECTION[1]) {
            //校验应答
            LogUtils.v(AppConfig.MODULE_APP,"开始分析包：4001包");
            handlerCheckMessage(userData);
            return true;
        }
        return false;
    }

    /**
     * 处理4002包体内容；
     *
     * @param messageContent
     */
    public void handlerCheckMessage(byte[] messageContent) {
        int index = 0;
        index += 2;
        runningMode = messageContent[index];
        index++;
        int len = (messageContent[index] & 0xFF);
        index++;
        //获取用户信息
        byte[] userNumber = new byte[len];
        System.arraycopy(messageContent, index, userNumber, 0, len);
        index += len;
        userNumberString = Converter.BytesToHexString(userNumber, userNumber.length);
        int calcPhotoLength = ((messageContent[index] & 0xff) << 24)
                + ((messageContent[index + 1] & 0xff) << 16) + ((messageContent[index + 2] & 0xff) << 8)
                + (messageContent[index + 3] & 0xff);
        index += 4;
        //移动无用的指针位；
        index += 6;
        //获取图片编号8字节；
        len = 8;
        byte[] pictureNumber = new byte[len];
        System.arraycopy(messageContent, index, pictureNumber, 0, len);
        pictureNumberString = Converter.BytesToHexString(pictureNumber, pictureNumber.length);
        index += len;
        //获取图片数据
        picture = new byte[calcPhotoLength - 16];
        System.arraycopy(messageContent, index, picture, 0, picture.length);
    }

    /**
     * 获取图片校验编码；
     *
     * @return
     */
    public String getRecPhotoNumber() {
        return pictureNumberString;
    }

    /**
     * 用户ID
     *
     * @return
     */
    public String getUseID() {
        return userNumberString;
    }

    /**
     * 图片数据
     *
     * @return
     */
    public byte[] getPicture() {
        return picture;
    }
}
