package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.socket.MessageUtils;

/**
 * Created by linxiaohui on 2018/1/2.
 */

public class MessageCollectionHandler extends MessageHandler implements IMessageConfirmHandler {


    @Override
    public byte getRunningMode() {
        return runningMode;
    }

    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        if (!MessageUtils.checkByteLegitimate(data)) {
            return false;
        }
        byte[] userData = getMessageContentByPacket(data);
        if ((userData[0] & 0xFF) == 0x40 && (userData[1] & 0xFF) == 0x00) {
            //确认采集包
            runningMode = userData[2];
            return true;
        }
        LogUtils.e(AppConfig.MODULE_SERVER, "colletion error");
        return false;
    }
}
