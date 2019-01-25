package com.emptech.biocollectiononline.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emptech.biocollectiononline.bean.EventMsg;

import org.greenrobot.eventbus.EventBus;

public class TimeEventReceiver extends BroadcastReceiver {

    private static String TAG = TimeEventReceiver.class.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_TIME_TICK)) {
            EventBus.getDefault().post(new EventMsg(EventMsg.EVENT_MSG_TIME_CHANGED, 0, 0, null));
            return;
        }

        if (action.equals(Intent.ACTION_TIME_CHANGED)) {
            EventBus.getDefault().post(new EventMsg(EventMsg.EVENT_MSG_TIME_CHANGED, 0, 0, null));
        }

    }
}