package com.emptech.biocollectiononline.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.fragment.FingerFragment;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.futronictech.AnsiSDKLib;
import com.futronictech.UsbDeviceDataExchangeImpl;

/**
 * Created by linxiaohui on 2018/1/9.
 */

public class FingerManager {

    private final String TAG = "FingerManager";

    private UsbDeviceDataExchangeImpl usb_host_ctx = null;
    private UsbDeviceDataExchangeImpl usb_host_ctx_ext1 = null;

    private int mPendingOperation = 0;
    private int mPendingOperationExt1 = 0;

    private static Bitmap mBitmapFP = null;
    private static Bitmap mBitmapFPExt1 = null;

    private OperationThread mOperationThread = null;
    private OperationThread mOperationThreadExt1 = null;
    //Pending operations
    private static final int OPERATION_CAPTURE = 1;

    public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_IMAGE = 2;
    public static final int MESSAGE_SHOW_ERROR_MSG = 3;
    public static final int MESSAGE_END_OPERATION = 4;

    private  Context mContext;

    private onFingerListener firstFingerListener;
    private onFingerListener secondFingerListener;
    private onFingerTipListener fingerTipListener;

    private UsbDevice device1;
    private UsbDevice device2;
    private int type;

    public FingerManager(Context context) {
        mContext = context;
    }

    public interface onFingerListener {
        void fingerPrintListener(int width, int height, byte[] fingerData, int nNFIQ, AnsiSDKLib ansiLib);
    }

    public interface onFingerTipListener {
        void errorFingerListener(String errorMessage);

        void messageFingerListener(String message);

        void stateFingerListener(int deviceStateType);
    }


    /**
     * 设置指纹提示监听
     *
     * @param fingerTipListener
     */
    public void setOnFingerListener(onFingerTipListener fingerTipListener) {
        this.fingerTipListener = fingerTipListener;
    }

    public void initUSBDeviceDataExchange(UsbDevice device1, UsbDevice device2,int type) {
        this.device1 = device1;
        this.device2 = device2;
        this.type=type;
        if(type==FingerFragment.LEFTTYPE){ //只需要打开左手就行
            usb_host_ctx_ext1 = new UsbDeviceDataExchangeImpl(mContext);
            usb_host_ctx_ext1.setonUSBListener(usb_host_ctx_ext1_listener);
            openDevice2();
        }else {   //先打开右手，然后根据需要确定是否需要打开左手
            usb_host_ctx = new UsbDeviceDataExchangeImpl(mContext);
            usb_host_ctx.setonUSBListener(usb_host_ctx_listener);
//        LogUtils.e("initUSBDeviceDataExchange", usb_host_ctx+"");
            openDevice1();
        }

    }


    private UsbDeviceDataExchangeImpl.onUSBListener usb_host_ctx_listener = new UsbDeviceDataExchangeImpl.onUSBListener() {
        @Override
        public void usbState(int MessageDevice) {
            switch (MessageDevice) {
                case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
                    LogUtils.v(AppConfig.MODULE_APP, "USB申请权限1-----MESSAGE_ALLOW_DEVICE");
                    if (mPendingOperation == OPERATION_CAPTURE) {
                        LogUtils.e(TAG, "fingerprint1 recieve OPERATION_CAPTURE event");
                        if (!openDevice1()) {
                            LogUtils.e(TAG, "=============open fingerprint1============fail");
                        } else {
                            FingerFragment.isOpenSucced[0]=true;
                            LogUtils.d(TAG, "=============open fingerprint1============success");
                            usb_host_ctx.Destroy();
                            if(type!=FingerFragment.RIGHTTYPE){  //type==3时，还需要打开左手
                                usb_host_ctx_ext1 = new UsbDeviceDataExchangeImpl(mContext);
                                usb_host_ctx_ext1.setonUSBListener(usb_host_ctx_ext1_listener);
                                openDevice2();
                            }
                        }
                    }
                    break;
            }
        }
    };

    private UsbDeviceDataExchangeImpl.onUSBListener usb_host_ctx_ext1_listener = new UsbDeviceDataExchangeImpl.onUSBListener() {
        @Override
        public void usbState(int MessageDevice) {
            switch (MessageDevice) {
                case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
                    LogUtils.v(AppConfig.MODULE_APP, "USB申请权限2-----MESSAGE_ALLOW_DEVICE");
                    if (mPendingOperationExt1 == OPERATION_CAPTURE) {
                        LogUtils.e(TAG, "fingerprint2 recieve OPERATION_CAPTURE event");
                        if (!openDevice2()) {
                            LogUtils.e(TAG, "=============open fingerprint2============fail");
                        } else {
                            FingerFragment.isOpenSucced[1]=true;
                            LogUtils.d(TAG, "=============open fingerprint2=============success");
                        }
                        if(usb_host_ctx_ext1!=null)
                        usb_host_ctx_ext1.Destroy();
                    }
                    break;
            }
        }
    };

    /**
     * 设置指纹数据监听
     *
     * @param secondFingerListener
     */
    public void setOnFingerListener(onFingerListener firstFingerListener, onFingerListener secondFingerListener) {
        this.firstFingerListener = firstFingerListener;
        this.secondFingerListener = secondFingerListener;
    }


    private boolean openDevice1() {
        if (device1 == null|| usb_host_ctx == null) {
            LogUtils.e("TAG","device1："+device1+",,usb_host_ctx:"+usb_host_ctx );
            return false;
        }
        if (usb_host_ctx.OpenDevice(device1, true)) {
            StartCapture();
            return true;
        } else {
            if (usb_host_ctx.IsPendingOpen()) {
                mPendingOperation = OPERATION_CAPTURE;
                LogUtils.e(TAG, "requre finger1");
            } else {
                LogUtils.e(TAG, "Can not start capture operation.\nCan't open scanner device");
            }
            return false;
        }
    }

    private boolean openDevice2() {
        if (device2 == null||usb_host_ctx_ext1 == null) return false;
        if (usb_host_ctx_ext1.OpenDevice(device2, true)) {
            StartCaptureExt1();
            return true;
        } else {
            if (usb_host_ctx_ext1.IsPendingOpen()) {
                mPendingOperationExt1 = OPERATION_CAPTURE;
                LogUtils.e(TAG, "requre finger2");
            } else {
                LogUtils.e(TAG, "Can not start capture operation.\nCan't open scanner device");

            }
            return false;
        }
    }


    public void stopDevice() {
//        LogUtils.e(TAG, "关闭指纹模块！");
        stopDevice(0);
        stopDevice(1);
//        UserPower.close();
    }

    /**
     * 停止设备；
     *
     * @param deviceID 左手：0，右手：1
     */
    public void stopDevice(int deviceID) {
        switch (deviceID) {
            case 0:
                if (mOperationThread != null) {
                    mOperationThread.Cancel();
                    mOperationThread = null;
                    LogUtils.i(TAG, "线程1 cancel！");
                }
                if (usb_host_ctx != null) {
                    usb_host_ctx.CloseDevice();
                    usb_host_ctx.Destroy();
                    usb_host_ctx = null;
                    LogUtils.i(TAG, "CloseDevice 1！");
                }
//                UserPower.ioctl(UserPower.XD_POWER_CTL_FINGER1, UserPower.DISABLE);
                LogUtils.i(TAG, "关闭指纹模块==========1！");
                break;
            case 1:
                if (mOperationThreadExt1 != null) {
                    mOperationThreadExt1.Cancel();
                    mOperationThreadExt1 = null;
                    LogUtils.i(TAG, "线程2 cancel！");
                }
                if (usb_host_ctx_ext1 != null) {
                    usb_host_ctx_ext1.CloseDevice();
                    usb_host_ctx_ext1.Destroy();
                    usb_host_ctx_ext1 = null;
                    LogUtils.i(TAG, "CloseDevice 2！");
                }
//                UserPower.ioctl(UserPower.XD_POWER_CTL_FINGER2, UserPower.DISABLE);
                LogUtils.i(TAG, "关闭指纹模块==========2！");
                break;
        }
    }


    private void StartCapture() {
        LogUtils.v(TAG, "StartCapture!");
        mOperationThread = new CaptureThread(true, usb_host_ctx, fingerTipListener, firstFingerListener);
        mOperationThread.start();
    }

    private void StartCaptureExt1() {
        LogUtils.v(TAG, "StartCaptureExt1!");
        mOperationThreadExt1 = new CaptureThread(true, usb_host_ctx_ext1, fingerTipListener, secondFingerListener);
        mOperationThreadExt1.start();
    }


    public Bitmap CreateFingerBitmap(int imgWidth, int imgHeight, byte[] imgBytes) {
        int[] mPixels=new int[imgWidth*imgHeight];
//        for (int i = 0; i < imgWidth * imgHeight; i++) {
//            pixs[i]=Color.argb(255-imgBytes[i],255 ,255 ,255 );
//        }
//        return Bitmap.createBitmap(pixs,imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
//        int width, height;
//        height = emptyBmp.getHeight();
//        width = emptyBmp.getWidth();
        for( int i=0; i<imgWidth * imgHeight; i++)
        {
            mPixels[i] = Color.rgb(255-imgBytes[i],255-imgBytes[i],255-imgBytes[i]);
        }


        Bitmap result = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(mPixels, 0, imgWidth, 0, 0,  imgWidth, imgHeight, false, paint);
        return result;
    }


}
