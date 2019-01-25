package com.emptech.biocollectiononline.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import com.emptech.biocollectiononline.AppConfig;


public class WindowUtils {
    private static String BAR_SHOW = "com.pointercn.showBar";
    private static String BAR_HIDE = "com.pointercn.hideBar";

    /**
     * function:导航栏是否可用
     */
    public static void setNavigateEnable(Context context, boolean isEnable) {
        if ((!isEnable) && context instanceof Activity) {
            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        String action = (AppConfig.isDebug ? true : isEnable) ? BAR_SHOW : BAR_HIDE;
        context.sendBroadcast(new Intent(action));
    }

}
