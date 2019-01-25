package com.emptech.biocollectiononline.manager;

import android.os.SystemClock;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.futronictech.AnsiSDKLib;
import com.futronictech.UsbDeviceDataExchangeImpl;

/**
 * Created by linxiaohui on 2018/1/10.
 */

public class CaptureThread extends OperationThread {


    private AnsiSDKLib ansi_lib = null;
    private boolean mUseUsbHost = true;
    private UsbDeviceDataExchangeImpl m_io_ctx = null;

    private FingerManager.onFingerTipListener fingerTipListener;
    private FingerManager.onFingerListener fingerListener;


    public CaptureThread(boolean useUsbHost, UsbDeviceDataExchangeImpl io_ctx, FingerManager.onFingerTipListener fingerTipListener, FingerManager.onFingerListener fingerListener) {
        ansi_lib = new AnsiSDKLib();
        mUseUsbHost = useUsbHost;
        m_io_ctx = io_ctx;
        this.fingerListener = fingerListener;
        this.fingerTipListener = fingerTipListener;
    }

    public void run() {
        boolean dev_open = false;
        try {
            if (mUseUsbHost) {
                if (!ansi_lib.OpenDeviceCtx(m_io_ctx)) {
                    if (fingerTipListener != null) {
                        fingerTipListener.errorFingerListener(ansi_lib.GetErrorMessage());
                        fingerTipListener.stateFingerListener(FingerManager.MESSAGE_END_OPERATION);
                    }
//                        mmHandler.obtainMessage(MESSAGE_SHOW_ERROR_MSG, -1, -1, ).sendToTarget();
//                        mmHandler.obtainMessage(MESSAGE_END_OPERATION).sendToTarget();
                    return;
                }
            } else {
                if (!ansi_lib.OpenDevice(0)) {
                    if (fingerTipListener != null) {
                        fingerTipListener.errorFingerListener(ansi_lib.GetErrorMessage());
                        fingerTipListener.stateFingerListener(FingerManager.MESSAGE_END_OPERATION);
                    }
//                        mmHandler.obtainMessage(MESSAGE_SHOW_ERROR_MSG, -1, -1, ansi_lib.GetErrorMessage()).sendToTarget();
//                        mmHandler.obtainMessage(MESSAGE_END_OPERATION).sendToTarget();
                    return;
                }
            }

            dev_open = true;

            if (!ansi_lib.FillImageSize()) {
                if (fingerTipListener != null) {
                    fingerTipListener.errorFingerListener(ansi_lib.GetErrorMessage());
                    fingerTipListener.stateFingerListener(FingerManager.MESSAGE_END_OPERATION);
                }
//                    mmHandler.obtainMessage(MESSAGE_SHOW_ERROR_MSG, -1, -1, ansi_lib.GetErrorMessage()).sendToTarget();
//                    mmHandler.obtainMessage(MESSAGE_END_OPERATION).sendToTarget();
                return;
            }

            byte[] img_buffer = new byte[ansi_lib.GetImageSize()];

            for (; ; ) {
                if (IsCanceled()) {
                    break;
                }
                Log.v(AppConfig.MODULE_APP, "线程运行：" + Thread.currentThread().toString());
                long lT1 = SystemClock.uptimeMillis();
                int nNFIQ = 0;
                if (ansi_lib.CaptureImage(img_buffer)) {
                    if (ansi_lib.GetNfiqFromImage(img_buffer, ansi_lib.GetImageWidth(), ansi_lib.GetImageHeight()))
                        nNFIQ = ansi_lib.GetNIFQValue();
                    long op_time = SystemClock.uptimeMillis() - lT1;

                    String op_info = String.format("Capture done. Time is %d(ms)", op_time);

                    if (fingerTipListener != null) {
                        fingerTipListener.messageFingerListener(op_info);
                    }
                    if (fingerListener != null) {
                        fingerListener.fingerPrintListener(ansi_lib.GetImageWidth(),
                                ansi_lib.GetImageHeight(), img_buffer, nNFIQ,ansi_lib);
                    }
                } else {
                    int lastError = ansi_lib.GetErrorCode();
//                    LogUtils.e(AppConfig.MODULE_APP,"lastError:"+Thread.currentThread().toString()+":code:"+lastError);
                    if (lastError == AnsiSDKLib.FTR_ERROR_EMPTY_FRAME ||
                            lastError == AnsiSDKLib.FTR_ERROR_NO_FRAME ||
                            lastError == AnsiSDKLib.FTR_ERROR_MOVABLE_FINGER) {
                        Thread.sleep(100);
                    } else {
                        String error = String.format("Capture failed. Error: %s.", ansi_lib.GetErrorMessage());
                        if (fingerTipListener != null) {
                            fingerTipListener.errorFingerListener(error);
                        }
                        Thread.sleep(100);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("Thread",e.getMessage() );
            e.printStackTrace();
            if (fingerTipListener != null) {
                fingerTipListener.errorFingerListener(e.getMessage());
            }
        }

        if (dev_open) {
            ansi_lib.CloseDevice();
        }
        if (fingerTipListener != null) {
            fingerTipListener.stateFingerListener(FingerManager.MESSAGE_END_OPERATION);
        }
    }
}
