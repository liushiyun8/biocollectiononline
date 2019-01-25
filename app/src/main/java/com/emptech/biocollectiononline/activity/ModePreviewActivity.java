package com.emptech.biocollectiononline.activity;

import android.os.Bundle;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.common.AppSocketFragmentActivity;
import com.emptech.biocollectiononline.socket.message.SessionCloseSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionSocket;
import com.emptech.biocollectiononline.socket.message.SessionConfirmSocket;
import com.emptech.biocollectiononline.socket.message.SessionPreviewSocket;
import com.emptech.biocollectiononline.socket.message.SessionSigntureCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionStartModeSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by linxiaohui on 2018/1/5.
 */

public abstract class ModePreviewActivity extends AppSocketFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(SocketSession sessionSocket) {
        if(AppConfig.isTestSocket)
        LogUtils.e(TAG, "recieve data");
        if (sessionSocket != null) {
            if(sessionSocket instanceof SessionStartModeSocket)
                LogUtils.v(TAG, "接收到启动模块信号,传输给Fragment");
            else if(sessionSocket instanceof SessionPreviewSocket){
                LogUtils.v(TAG, "服务器接收到预览信号,传输给Fragment");
            }else if(sessionSocket instanceof SessionCollectionSocket){
                LogUtils.v(TAG, "接收到启动采集信号,传输给Fragment");
            }else if(sessionSocket instanceof SessionCollectionCheckSocket){
                LogUtils.v(TAG, "接收到采集验证信号，传输给Fragment");
            }else if(sessionSocket instanceof SessionConfirmSocket){
                LogUtils.v(TAG, "接收到用户确认信号，传输给Fragment");
            }else if(sessionSocket instanceof SessionSigntureCheckSocket){
                LogUtils.v(TAG, "接收到签名验证信号，传输给Fragment");
            }else if(sessionSocket instanceof SessionCloseSocket){
                LogUtils.v(TAG, "接收到关闭信号，传输给Fragment");
            }
            setSocketSession(sessionSocket);
        }
    }


//    @Subscribe
//    public void onEventMainThread(SessionStartModeSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到启动采集信号,传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }

//    @Subscribe
//    public void onEventMainThread(SessionPreviewSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "服务器接收到预览信号,传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }

//    @Subscribe
//    public void onEventMainThread(SessionCollectionSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到采集信号，传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }

//    @Subscribe
//    public void onEventMainThread(SessionCollectionCheckSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到验证信号，传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }
//
//    @Subscribe
//    public void onEventMainThread(SessionConfirmSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到验证信号，传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }
//
//    @Subscribe
//    public void onEventMainThread(SessionSigntureCheckSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到验证信号，传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }
//
//    @Subscribe
//    public void onEventMainThread(SessionCloseSocket sessionSocket) {
//        if (sessionSocket != null) {
//            LogUtils.v(TAG, "接收到关闭信号，传输给Fragment");
//            setSocketSession(sessionSocket);
//        }
//    }

    @Override
    public void finish() {
        super.finish();
        EventBus.getDefault().unregister(this);
    }
}
