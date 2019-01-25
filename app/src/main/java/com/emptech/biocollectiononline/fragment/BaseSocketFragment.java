package com.emptech.biocollectiononline.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.SocketSessionManager;
import com.emptech.biocollectiononline.socket.MessageUtils;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SessionCloseSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionSocket;
import com.emptech.biocollectiononline.socket.message.SessionConfirmSocket;
import com.emptech.biocollectiononline.socket.message.SessionHeartSocket;
import com.emptech.biocollectiononline.socket.message.SessionPreviewSocket;
import com.emptech.biocollectiononline.socket.message.SessionSigntureCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionStartModeSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.apache.mina.core.session.IoSession;

import java.util.Arrays;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public abstract class BaseSocketFragment extends BaseFragment {
    SocketSession mSocketSession;
    private IPreview mIPreview;

    private boolean isInitHardWare = false;
    private SessionPreviewSocket mPreviewSocket;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated");
        SocketSession sessionSocket = SocketSessionManager.getInstance().getAndRemoveSocketSession(activity.getClass());
        if (sessionSocket != null) {
            setSocketSession(sessionSocket);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setIPreviewListener(IPreview mIPreview) {
        this.mIPreview = mIPreview;
    }

    public void removeIpreviewListener() {
        this.mIPreview = null;
    }

    /**
     * 是否需要初始化预览模块
     *
     * @return
     */
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        return false;
    }

    /**
     * 获取是否需要初始化预览模块
     *
     * @return
     */
    private boolean getIsInitPreviewMode(SocketSession mSocketSession) {
        if (mIPreview == null || !(mSocketSession instanceof SessionStartModeSocket)||activity.isFinishing()) {
            return false;
        }
        return isInitPreviewMode(mSocketSession);
    }


    /**
     * PC端是否接收到预览数据
     *
     * @param mSocketSession
     * @return
     */
    private boolean getIsPreviewPCRec(SocketSession mSocketSession) {
        if(AppConfig.isTestSocket)
        LogUtils.e(AppConfig.MODULE_SERVER, "mIPreview:"+mIPreview+"mSocketSession"+mSocketSession+"this instanceof CameraFragment:"+(this instanceof CameraFragment)+"activity.isFinishing():"+activity.isFinishing());
        if (mIPreview == null || mSocketSession == null || !(this instanceof CameraFragment) || activity.isFinishing())
            return false;
        return mSocketSession instanceof SessionPreviewSocket;
    }

    /**
     * 是否是采集信号；
     *
     * @param mSocketSession
     * @return
     */
    private boolean getIsCollection(SocketSession mSocketSession) {
        if (mIPreview == null || mSocketSession == null||activity.isFinishing()) return false;
        return mSocketSession instanceof SessionCollectionSocket && isInitPreviewMode(mSocketSession);
    }

    /**
     * 是否是采集信号；
     *
     * @param mSocketSession
     * @return
     */
    private boolean getIsCollectionCheck(SocketSession mSocketSession) {
        if (mIPreview == null || mSocketSession == null||activity.isFinishing()) return false;
        return mSocketSession instanceof SessionCollectionCheckSocket && isInitPreviewMode(mSocketSession);
    }

    /**
     * 是否是采集信号；
     *
     * @param mSocketSession
     * @return
     */
    private boolean getIsCollectionConfirm(SocketSession mSocketSession) {
        if (mIPreview == null || mSocketSession == null||activity.isFinishing()) return false;
        return mSocketSession instanceof SessionConfirmSocket && isInitPreviewMode(mSocketSession);
    }

    /**
     * 是否是申请校验信号包 ，即4001包；
     *
     * @param mSocketSession
     * @return
     */
    private boolean getIsRequestCollectionCheck(SocketSession mSocketSession) {
        if (mIPreview == null || mSocketSession == null||activity.isFinishing()) return false;
        return mSocketSession instanceof SessionSigntureCheckSocket && isInitPreviewMode(mSocketSession);
    }


    /**
     * 用于Activity给Fragment传输SocketSession信号；
     *
     * @param mSocketSession 传输的信号；
     */
    public void setSocketSession(SocketSession mSocketSession) {
        this.mSocketSession = mSocketSession;
        if (mSocketSession instanceof SessionCloseSocket) {
            LogUtils.e(AppConfig.MODULE_SERVER, "closePreview");
            closePreview();
        } else if (mSocketSession instanceof SessionHeartSocket) {
            LogUtils.e(AppConfig.MODULE_SERVER, "heartbeat");
            heartHandle(mSocketSession);
        } else if (getIsInitPreviewMode(mSocketSession)) {
            initHardWareReply(mSocketSession);
        } else if (getIsPreviewPCRec(mSocketSession)) {
            if (mSocketSession.getRunningMode() == 0x03) {
                byte[] previewData = mIPreview.getPreviewData(mSocketSession);
                if (AppConfig.isTest) {
                    if (AppConfig.TestError) {
                        previewData = null;
                    }
                    if (AppConfig.TestTimeout) {
                        return;
                    }
                }
                sendPreviewToPC(mSocketSession.getRunningMode(), previewData);
            }
            if(AppConfig.isTestSocket)
            LogUtils.e(AppConfig.MODULE_SERVER, "setSocketSession Photopreview:" + mSocketSession.getRunningMode());
        } else if (getIsPreviewPCRecNoData(mSocketSession)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "PC端接收到指纹模块预览数据" + mSocketSession.getRunningMode());
            if (mSocketSession.getRunningMode() == 0x01 || mSocketSession.getRunningMode() == 0x02) {
                byte[] preData = mIPreview.getPreviewData(mSocketSession);
                if (AppConfig.isTest) {
                    if (AppConfig.TestError) {
                        preData = null;
                    }
                    if (AppConfig.TestTimeout) {
                        return;
                    }
                }
                sendPreviewToPC(mSocketSession.getRunningMode(), preData);
            }
        } else if (getIsCollection(mSocketSession)) {
            //判定是采集包，4000包
            startCollection(mSocketSession);
        } else if (getIsCollectionCheck(mSocketSession)) {
            byte mode = mSocketSession.getRunningMode();
            String number = ((SessionCollectionCheckSocket) mSocketSession).getRecPhotoNumber();
            boolean isSuccess = ((SessionCollectionCheckSocket) mSocketSession).isCheckSuccess();
            CollectionResult(mode, isSuccess, number);
        } else if (getIsCollectionConfirm(mSocketSession)) {
            byte mode = mSocketSession.getRunningMode();
            boolean isSuccess = ((SessionConfirmSocket) mSocketSession).isSuccess();
            LogUtils.v(AppConfig.MODULE_SERVER, "确认入库模块:" + mode + ";是否成功：" + isSuccess);
            ConfirmResult(mode, isSuccess);
        } else if (getIsRequestCollectionCheck(mSocketSession)) {
            requestCollectionCheck((SessionSigntureCheckSocket) mSocketSession);
        }else {
            LogUtils.e(AppConfig.MODULE_SERVER, "setSocketSession error type:" + mSocketSession.getRunningMode());
        }
    }

    public void heartHandle(SocketSession mSocketSession) {

    }

    private boolean getIsPreviewPCRecNoData(SocketSession mSocketSession) {
        if (mIPreview == null || mSocketSession == null || !(this instanceof FingerFragment) || activity.isFinishing())
            return false;
        return mSocketSession instanceof SessionPreviewSocket;
    }

    public void closePreview() {

    }


    /**
     * 启动2000包回复，即2001包
     *
     * @param mSession
     */
    private void initHardWareReply(final SocketSession mSession) {
        //启动预览信号
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] result = {0x01};
                if (!isInitHardWare) {
                    isInitHardWare = mIPreview.initHardWare();
                }
                LogUtils.e(TAG, "initHardWareReply result:" + isInitHardWare);
                if (isInitHardWare) {
                    result[0] = 0x00;
                }
                LogUtils.e(TAG, "initHardWareReply result:" + result[0]);
                //test
                if (AppConfig.isTest) {
                    if (AppConfig.TestError) {
                        result[0] = 0x01;
                    }
                    if (AppConfig.TestTimeout) {
                        return;
                    }
                }
                sendMessageToPC(mSession, mSession.getMessageToClient(result));
            }
        }).start();
    }

    /**
     * 4001申请采集校验包
     */
    protected void requestCollectionCheck(SessionSigntureCheckSocket mSocketSession) {
        LogUtils.v(AppConfig.MODULE_SERVER, "4001申请采集校验包");
    }

    /**
     * 5000包返回值，入库是否成功
     *
     * @param mode      模块
     * @param isSuccess 是否成功入库；
     */
    protected void ConfirmResult(byte mode, boolean isSuccess) {
        LogUtils.v(TAG, "服务端入库完成模块:" + mode + ";是否成功：" + isSuccess);
    }

    /**
     * 验证结果；
     *
     * @param runningMode
     * @param isSuccess
     * @param successNumber
     */
    public void CollectionResult(byte runningMode, boolean isSuccess, String successNumber) {
        LogUtils.v(AppConfig.MODULE_SERVER, "服务端校验完成模块:" + runningMode + ";是否成功：" + isSuccess + ";校验编号：" + successNumber);
    }

    /**
     * 开始采集；
     *
     * @param mSocketSession
     */
    protected void startCollection(final SocketSession mSocketSession) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.v(AppConfig.MODULE_SERVER, "开始采集");
                byte[] collectionData = mIPreview.collection(mSocketSession.getRunningMode());
                while (collectionData == null) {
                    collectionData = mIPreview.collection(mSocketSession.getRunningMode());
                    if (collectionData != null) {
                        LogUtils.v(AppConfig.MODULE_SERVER, "等待采集。。。。:" + collectionData.length);
                    }
                    SystemClock.sleep(100);
                }
                LogUtils.v(AppConfig.MODULE_SERVER, "完成采集:" + mSocketSession.getRunningMode() + ";采集数据长度：" + collectionData.length);
                if (mSocketSession instanceof SessionConfirmSocket) {
                    ((SessionCollectionSocket) mSocketSession).setID(getID());
                    sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(collectionData));
                } else {
                    SessionCollectionSocket mSessionCollectionSocket = new SessionCollectionSocket();
                    mSessionCollectionSocket.setRunningMode(mSocketSession.getRunningMode());
                    mSessionCollectionSocket.setRequestID(mSocketSession.getRequestID());
                    mSessionCollectionSocket.setID(getID());
                    mSessionCollectionSocket.transmitIoSession(mSocketSession.getmIoSession());
                    sendMessageToPC(mSessionCollectionSocket, mSessionCollectionSocket.getMessageToClient(collectionData));
                }
            }
        }).start();
    }

    protected void sendCollectionResult(SocketSession mSocketSession, byte[] collectionData) {
        if (mSocketSession == null)
            return;
        SessionCollectionSocket mSessionCollectionSocket = new SessionCollectionSocket();
        mSessionCollectionSocket.setRunningMode(mSocketSession.getRunningMode());
        mSessionCollectionSocket.setRequestID(mSocketSession.getRequestID());
        mSessionCollectionSocket.setID(getID());
        mSessionCollectionSocket.transmitIoSession(mSocketSession.getmIoSession());
        if (AppConfig.isTest) {
            if (AppConfig.TestError) {
                collectionData = null;
            }
            if (AppConfig.TestTimeout) {
                return;
            }
        }
        sendMessageToPC(mSessionCollectionSocket, mSessionCollectionSocket.getMessageToClient(collectionData));
    }


    private byte[] getID() {
        String userid = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
        if (TextUtils.isEmpty(userid)) {
            return new byte[0];
        }
        return Converter.string2Hex(userid);
    }

    protected boolean sendMessageToPC(SocketSession mSocketSession, byte[] data) {
//        if (isRemoving()) {
//            LogUtils.e(AppConfig.MODULE_SERVER, "fragment正在被移除！");
//            return false;
//        }
        if (mSocketSession == null) {
            LogUtils.e(AppConfig.MODULE_SERVER, "socketSession is null！");
            return false;
        }
        if (data == null) {
            LogUtils.e(AppConfig.MODULE_SERVER, "send data is null！");
            return false;
        }
        if (!MessageUtils.checkByteLegitimate(data)) {
            LogUtils.e(AppConfig.MODULE_SERVER, "the packet data is error！");
            return false;
        }
        IoSession mIoSession = mSocketSession.getmIoSession();
        if (mIoSession.isConnected()) {
            mIoSession.write(data);
            return true;
        }
        LogUtils.e(AppConfig.MODULE_SERVER, "send message error,the length is:" + data.length);
        return false;
    }


    /**
     * 发送数据至PC端；
     *
     * @return
     */
    public boolean sendPreviewToPC(byte RunningMode, byte[] previewData) {
        if (mPreviewSocket == null) {
            mPreviewSocket = new SessionPreviewSocket();
        }
        if (mSocketSession == null || previewData == null) {
            if (mSocketSession == null) return false;
            LogUtils.e(TAG, "preview Socket：previewData is null" + mSocketSession.getRunningMode());
            mPreviewSocket.setRequestID(mSocketSession.getRequestID());
            mPreviewSocket.setRunningMode(RunningMode);
            mPreviewSocket.transmitIoSession(mSocketSession.getmIoSession());
            sendMessageToPC(mPreviewSocket, mPreviewSocket.getErrorMessageToClient());
            return false;
        }
        mPreviewSocket.setRequestID(mSocketSession.getRequestID());
        mPreviewSocket.setRunningMode(RunningMode);
        mPreviewSocket.transmitIoSession(mSocketSession.getmIoSession());
        return sendMessageToPC(mPreviewSocket, mPreviewSocket.getMessageToClient(previewData));
    }

    /**
     * 发送心跳至PC端；
     *
     * @return
     */
    public boolean sendHeartToPC(byte RunningMode) {
        if (mSocketSession == null) {
            LogUtils.e(TAG, "socket is：" + null);
            return false;
        }
        SessionHeartSocket mHeartSocket = new SessionHeartSocket();
        mHeartSocket.setRequestID(mSocketSession.getRequestID());
        mHeartSocket.setRunningMode(RunningMode);
        mHeartSocket.transmitIoSession(mSocketSession.getmIoSession());
        return sendMessageToPC(mHeartSocket, mHeartSocket.getMessageToClient(null));
    }


    /**
     * 确认使用采集后的资源
     *
     * @param collectionNumber 采集编号；
     * @return
     */
    public boolean sendConfirmDataToPC(byte RunningMode, String collectionNumber) {
        if (TextUtils.isEmpty(collectionNumber) || collectionNumber.length() != 16) {
            return false;
        }
        if (mSocketSession == null) {
            LogUtils.e(TAG, "socket is ：" + null);
            return false;
        }
        SessionConfirmSocket mConfirmSocket = new SessionConfirmSocket();
        mConfirmSocket.setRequestID(mSocketSession.getRequestID());
        mConfirmSocket.setRunningMode(RunningMode);
        mConfirmSocket.transmitIoSession(mSocketSession.getmIoSession());
        byte[] collectionNumberData = Converter.string2Hex(collectionNumber);
        return sendMessageToPC(mConfirmSocket, mConfirmSocket.getMessageToClient(collectionNumberData));
    }


    /**
     * 获取图片数据格式；
     *
     * @param width       图片宽度
     * @param height      图片高度
     * @param QTY         图片质量
     * @param previewData 图片数据
     * @return
     */
    public byte[] getParmPictureData(int width, int height, int QTY, byte[] previewData) {
        if (previewData == null) {
            return null;
        }
        byte[] data = new byte[16 + previewData.length];
        //图片宽度
        data[0] = (byte) ((width >> 8) & 0xFF);
        data[1] = (byte) ((width) & 0xFF);
        //图片高度
        data[2] = (byte) ((height >> 8) & 0xFF);
        data[3] = (byte) ((height) & 0xFF);
        //图片质量
        data[4] = (byte) ((QTY) & 0xFF);
        data[5] = 0x08;
        data[6] = 2;
        long len = previewData.length;
        data[7] = (byte) (((len) >> 24) & 0xFF);
        data[8] = (byte) (((len) >> 16) & 0xFF);
        data[9] = (byte) (((len) >> 8) & 0xFF);
        data[10] = (byte) ((len) & 0xFF);
        Arrays.fill(data, 11, 16, (byte) 0xFF);
        System.arraycopy(previewData, 0, data, 16, previewData.length);
        return data;
    }

    public byte[] getParmPictureData(int width, int height, int QTY, byte stutas, byte[] previewData, byte[] tampleData) {
        if (previewData == null) {
            return null;
        }
        byte[] data = new byte[16 + previewData.length + (tampleData == null ? 0 : tampleData.length)];
        //图片宽度
        data[0] = (byte) ((width >> 8) & 0xFF);
        data[1] = (byte) ((width) & 0xFF);
        //图片高度
        data[2] = (byte) ((height >> 8) & 0xFF);
        data[3] = (byte) ((height) & 0xFF);
        //图片质量
        data[4] = (byte) ((QTY) & 0xFF);
        data[5] = 0x08;
        data[6] = stutas;
        long len = previewData.length;
        data[7] = (byte) (((len) >> 24) & 0xFF);
        data[8] = (byte) (((len) >> 16) & 0xFF);
        data[9] = (byte) (((len) >> 8) & 0xFF);
        data[10] = (byte) ((len) & 0xFF);

        if (tampleData != null) {
            len = tampleData.length;
            data[11] = (byte) (((len) >> 24) & 0xFF);
            data[12] = (byte) (((len) >> 16) & 0xFF);
            data[13] = (byte) (((len) >> 8) & 0xFF);
            data[14] = (byte) ((len) & 0xFF);
        }
        Arrays.fill(data, 15, 16, (byte) 0xFF);
        System.arraycopy(previewData, 0, data, 16, previewData.length);
        if (tampleData != null) {
            System.arraycopy(tampleData, 0, data, 16 + previewData.length, tampleData.length);
        }
        return data;
    }

}
