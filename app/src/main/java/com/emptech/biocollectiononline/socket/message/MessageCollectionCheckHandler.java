package com.emptech.biocollectiononline.socket.message;


import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * Created by linxiaohui on 2018/1/5.
 * PC图片校验是否通过
 */

public class MessageCollectionCheckHandler extends MessageHandler implements IMessageConfirmHandler {
    private String RecPhotoNumber;//图片编号；
    private boolean isCheckSuccess = false;//图片校验是否通过；

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
        if ((userData[1] & 0xFF) == 0x40 && (userData[2] & 0xFF) == 0x02) {
            //校验应答
            handlerCheckMessage(userData);
            return true;
        }
        return null;
    }

    /**
     * 处理4002包体内容；
     *
     * @param messageContent
     */
    public void handlerCheckMessage(byte[] messageContent) {
        isCheckSuccess = false;
        int index = 0;
        if ((messageContent[index] & 0xFF) != 0x00
                || (messageContent[index + 1] & 0xFF) != 0x40
                || (messageContent[index + 2] & 0xFF) != 0x02) {
            return;
        }
        isCheckSuccess=true;
        index += 3;
        runningMode = messageContent[index];
//        index++;
//        if ((messageContent[index] & 0xFF) != 0x00) {
//            return;
//        }
//        index++;
//        isCheckSuccess = true;
//        byte[] photoNumberByte = new byte[8];
//        System.arraycopy(messageContent, index, photoNumberByte, 0, photoNumberByte.length);
//        RecPhotoNumber = Converter.BytesToHexString(photoNumberByte, photoNumberByte.length);
    }

    /**
     * 获取图片校验通过编码；
     *
     * @return
     */
    public String getRecPhotoNumber() {
        if (!isCheckSuccess) {
            return null;
        }
        return RecPhotoNumber;
    }

    /**
     * 是否通过校验
     *
     * @return
     */
    public boolean isCheckSuccess() {
        return isCheckSuccess;
    }
}
