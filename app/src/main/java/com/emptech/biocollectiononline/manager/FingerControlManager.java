package com.emptech.biocollectiononline.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.lang.ref.WeakReference;

import cn.xiongdi.jni.UserDevices;

/**
 * Created by linxiaohui on 2018/4/3.
 */

public class FingerControlManager {
    private final String TAG = FingerControlManager.class.getSimpleName();
    private final WeakReference<Context> weakReference;
    private boolean isUnregisterFingerReceiver = false;//是否解除了指纹广播；
    private int OpenUSBCount = 0;
    private static Context mContext;

    private FingerControlCallBack mFingerControlCallBack;

    public interface FingerControlCallBack {
        void OpenSuccessed(UsbDevice device);
    }

    public void setFingerControlCallBack(FingerControlCallBack mFingerControlCallBack) {
        this.mFingerControlCallBack = mFingerControlCallBack;
    }


    public FingerControlManager(Context context) {
        weakReference = new WeakReference<>(context);
        mContext = weakReference.get();
        OpenUSBCount=0;
    }


    public boolean OpenUsb() {
        registerFingerUsb();
        LogUtils.v(TAG, "fingerprint1 is powering....");
        int ret = UserDevices.fingerRight_ope((byte)1);
        if (ret != 0) {
            LogUtils.e(TAG, "fingerprint1 power fail:" + ret);
            return false;
        }else {
            LogUtils.v(TAG, "fingerprint1 power success:" + ret);
        }
        return true;
    }

    public void closeFingerUsb() {
        LogUtils.v(TAG, "fingerprint power down....");
        UserDevices.fingerRight_ope((byte)0);
        UserDevices.fingerLeft_ope((byte)0);
        OpenUSBCount=0;
        if (!isUnregisterFingerReceiver) {
            unRegisterFingerUsb();
        }
    }

    private void registerFingerUsb() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);
    }

    private void unRegisterFingerUsb() {
        mContext.unregisterReceiver(mUsbReceiver);
        isUnregisterFingerReceiver = true;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            String deviceName = usbDevice.getDeviceName();
            Log.e(TAG, "--- 接收到广播， action: " + action);
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                LogUtils.e(TAG, "USB device is Attached: " + deviceName);
                if (OpenUSBCount == 0) {
                    mFingerControlCallBack.OpenSuccessed(usbDevice);
                    LogUtils.e(TAG, "first fingerprint recieve broadcast " + deviceName + ";start another fingerprint power");
                    int ret = UserDevices.fingerLeft_ope((byte)1);
                    if (ret != 0) {
                        LogUtils.e(TAG, "fingerprint2 power fail:" + ret);
                    }else {
                        LogUtils.v(TAG, "fingerprint2 power succcess:" + ret);
                    }
                } else if (OpenUSBCount == 1) {
                    unRegisterFingerUsb();
                    LogUtils.e(TAG, "second fingerprint recieve broadcast " + deviceName + ";unregist broadcast！");
                    if (mFingerControlCallBack != null) {
                        mFingerControlCallBack.OpenSuccessed(usbDevice);
                    }
                }
                OpenUSBCount++;
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.e(TAG, "USB device is Detached: " + deviceName);
                if(OpenUSBCount>0)
                OpenUSBCount--;
            }
        }
    };
}
