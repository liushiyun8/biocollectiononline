package com.emptech.biocollectiononline.manager;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakReferenceHandler extends Handler {

	private final WeakReference<Activity> mActivty;
	private MyHandleMessage mhm;

	public WeakReferenceHandler(Activity activity) {
		mActivty = new WeakReference<Activity>(activity);
	}

	@Override
	public void handleMessage(Message msg) {
		Activity activity = mActivty.get();
		if (activity != null) {
			// 执行业务逻辑
			if (mhm != null) {
				mhm.handleMessage(msg);
			}
		} else {
			mhm = null;
		}
	}

	public void setHandleMessage(MyHandleMessage handleMessage) {
		mhm = handleMessage;
	}

	public interface MyHandleMessage {
		void handleMessage(Message msg);
	}
}
