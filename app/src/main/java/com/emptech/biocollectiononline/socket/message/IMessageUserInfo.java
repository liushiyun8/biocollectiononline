package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.bean.UserInfoMsg;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public interface IMessageUserInfo {
    public byte[] getRequestID();

    public UserInfoMsg handlerMessageFromServer(byte[] data);//处理服务器接收的包
}
