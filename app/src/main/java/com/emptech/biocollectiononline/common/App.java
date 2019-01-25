package com.emptech.biocollectiononline.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.activity.WelcomeActivity;
import com.emptech.biocollectiononline.dao.DatabaseOpenHelper;
import com.emptech.biocollectiononline.manager.LedManager;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.SignatureManager;
import com.emptech.biocollectiononline.receiver.TimeEventReceiver;
import com.emptech.biocollectiononline.utils.FileUtil;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import cn.xiongdi.jni.UserDevices;

public class App extends BaseApp {

    @Inject
    ExecutorService mCachedThreadPool;

    @Inject
    AppComponent appComponent;

    @Inject
    DatabaseOpenHelper databaseHelper;


    public PreferencesManager getmPreferencesManager() {
        return mPreferencesManager;
    }

    @Inject
    PreferencesManager mPreferencesManager;

    @Inject
    MusicPlayer mMusicPlayer;


    public static App get() {
        return (App) getAppContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Utils.mkDirs(AppConfig.WORK_PATH);
//        LogUtils.logConfigure(AppConfig.WORK_PATH_LOG);
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this)).build();
        appComponent.inject(this);
        InitApp();
    }


    void registerTimerReceiver() {
        TimeEventReceiver receiver = new TimeEventReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(receiver, filter);
    }

    public AppComponent Component() {
        return appComponent;
    }

    private void onAppCrached() {
        // TODO 奔溃时候的响应
        //释放为来得及关闭的硬件资源
        //1.close LED
        LedManager.close();
        //2.close fingerprint
        UserDevices.fingerLeft_ope((byte) 0);
        UserDevices.fingerRight_ope((byte) 0);
        //3.close Signature
        SignatureManager signatureManager = new SignatureManager();
        signatureManager.openDevice();
        signatureManager.CloseCapture();

            //利用系统时钟进行重启任务
            AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            try {
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // x秒钟后重启应用
            } catch (Exception e) {
                Log.e(TAG, "first class error:" + e);
            }
        finishAllActivitys1();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
//        System.gc();


//        //重启应用
//        Intent intent = new Intent(this, WelcomeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private void loadSoundResouces() {
        // TODO 加载音效
        mMusicPlayer.loadRes(R.raw.welcome);
        mMusicPlayer.loadRes(R.raw.fingerprint);
        mMusicPlayer.loadRes(R.raw.signture);
        mMusicPlayer.loadRes(R.raw.userinfoconfirm);
        mMusicPlayer.loadRes(R.raw.usercollectionconfirm);
        mMusicPlayer.loadRes(R.raw.photocollection);
        mMusicPlayer.loadRes(R.raw.finishcollection);
        mMusicPlayer.loadRes(R.raw.moveright_face);
        mMusicPlayer.loadRes(R.raw.moveleft_face);
        mMusicPlayer.loadRes(R.raw.close_face);
        mMusicPlayer.loadRes(R.raw.away_screen);
        mMusicPlayer.loadRes(R.raw.take_photo1);
        mMusicPlayer.loadRes(R.raw.leftfinger);
        mMusicPlayer.loadRes(R.raw.rightfinger);
    }



    private Runnable initThreadHandler = new Runnable() {

        @Override
        public void run() {
            loadSoundResouces();
        }
    };

    private void InitApp() {

//        CrashHandler.getInstance().init(App.this);
//        CrashHandler.getInstance().setOnExceptionCallback(new ICallback() {
//
//            public Object callback(Object obj) {
//                LogUtils.e(AppConfig.MODULE_APP, ((Throwable) obj).getMessage());
//                onAppCrached();
//                return null;
//            }
//        });
        if (!AppConfig.isDebug) {
            CrashHandler.getInstance().init(App.this);
            CrashHandler.getInstance().setOnExceptionCallback(new ICallback() {
                @Override
                public Object callback(Object obj) {
                    LogUtils.e(AppConfig.MODULE_APP, "CrashHandler callback");
                    onAppCrached();
                    return null;
                }
            });
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                FileUtil.removeOldlogs();//清除日志文件
            }
        }, 0,24*3600*1000);
        startInitializeTask();
    }


    public void startInitializeTask() {
        mCachedThreadPool.execute(initThreadHandler);
    }




}