package com.emptech.biocollectiononline.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.activity.WelcomeActivity;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BaseApp extends Application {

    public static final String TAG = "BaseApp";

    public static Context mContext;

    public List<WeakReference<Activity>> allActivities;


    public static Context getAppContext() {
        return mContext;
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;
        allActivities = new LinkedList<WeakReference<Activity>>();
        registerActivitysCallbacks();
    }

    public void finishActivity(Class<? extends Activity> clazz) {
        if (clazz != null) {
            Iterator<WeakReference<Activity>> iter = allActivities.iterator();
            while (iter.hasNext()) {
                WeakReference<Activity> activityWeakReference = iter.next();
                Activity activity = activityWeakReference.get();
                if(activity!=null)
                if (clazz.getSimpleName().equals(activity.getClass().getSimpleName())) {
                    activity.finish();
                    break;
                }
            }
        }
    }

    /**
     * 判定是否存在某个Activity
     *
     * @param clazz
     * @return
     */
    public Boolean hasActivity(Class<? extends Activity> clazz) {
        //判断某一个类是否存在任务栈里面
        Intent intent = new Intent(this, clazz);
        ComponentName cmpName = intent.resolveActivity(getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);  //获取从栈顶开始往下查找的10个activity
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break;  //跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    public Boolean hasthisActivity(Class<? extends Activity> clazz) {
        //判断某一个类是否存在任务栈里面
        if (clazz != null) {
            Iterator<WeakReference<Activity>> iter = allActivities.iterator();
            while (iter.hasNext()) {
                WeakReference<Activity> next = iter.next();
                Activity activity = next.get();
                if(activity!=null)
                if (clazz.getSimpleName().equals(activity.getClass().getSimpleName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取Activity数量
     *
     * @return
     */
    public int getActivityCount() {
        return allActivities.size();
    }

    /**
     * 销毁除欢迎界面以外的其他界面；
     */
    public void finishAllActivitys() {
        LogUtils.v(TAG, "开始销毁所有界面！");
        Iterator<WeakReference<Activity>> iter = allActivities.iterator();
        while (iter.hasNext()) {
            WeakReference<Activity> activityWeakReference = iter.next();
            Activity activity = activityWeakReference.get();
            if(activity!=null)
            if (activity.getClass() != WelcomeActivity.class) {
                activity.finish();
                LogUtils.v(TAG, "销毁：" + activity.getClass().getSimpleName());
            }
        }
    }

    public void finishAllActivitys1() {
        LogUtils.v(TAG, "开始销毁所有界面！");
        Iterator<WeakReference<Activity>> iter = allActivities.iterator();
        while (iter.hasNext()) {
            WeakReference<Activity> activityWeakReference = iter.next();
            Activity activity = activityWeakReference.get();
            if(activity!=null){
                activity.finish();
                LogUtils.v(TAG, "销毁：" + activity.getClass().getSimpleName());
            }
        }
    }

    private void registerActivitysCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                allActivities.add(new WeakReference<>(activity));
            }

            @Override
            public void onActivityStarted(Activity activity) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivityStopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogUtils.d(AppConfig.MODULE_APP, activity.getClass().getSimpleName() + " onActivityDestroyed");
                for (int i = 0; i < allActivities.size(); i++) {
                    if(allActivities.get(i).get()==null||allActivities.get(i).get()==activity){
                        allActivities.remove(i);
                    }
                }
            }
        });
    }
}
