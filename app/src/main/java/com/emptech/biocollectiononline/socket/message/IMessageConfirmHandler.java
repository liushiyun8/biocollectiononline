package com.emptech.biocollectiononline.socket.message;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public interface IMessageConfirmHandler {
    public byte getRunningMode();

    public byte[] getRequestID();

    public Boolean handlerMessageFromServer(byte[] data);//处理服务器接收的包
}
