package com.emptech.biocollectiononline.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.activity.WelcomeActivity;
import com.emptech.biocollectiononline.bean.EventMsg;
import com.emptech.biocollectiononline.manager.GPRSManager;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import butterknife.BindView;
import butterknife.OnClick;
import cn.xiongdi.jni.UserDevices;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class WelecomFragment extends BaseFragment {
    @BindView(R.id.tv_server_address)
    TextView tv_server_address;
    @BindView(R.id.tipTv)
    TextView tipTv;
    @BindView(R.id.version_tv)
    TextView versionTv;
    @BindView(R.id.logo_iv)
    ImageView logoiv;
    int tipId;
    private BroadcastReceiver netWorkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetStat();
        }
    };
    private GPRSManager gprsManager;
    private int ClickCount;
    private long lastClickTime;

    private int setClickCount;
    private long lastsetClickTime;

    private int debugCount;
    private long lastBugClickTime;
    private boolean lastStatus;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMsg msg) {
        if (msg != null) {
            if(msg.what==EventMsg.EVENT_MSG_NETCHANGED){
                switch (msg.arg1){
                    case EventMsg.EVENT_NET_WAITING:
                        tipId=R.string.net_waiting;
                        //网络灯亮红灯
                        UserDevices.internet_status((byte) 0);
                        break;
                     case EventMsg.EVENT_NET_LOST:
                         tipId=R.string.net_lost;
                         //网络灯亮红灯
                         UserDevices.internet_status((byte) 0);
                        break;
                     case EventMsg.EVENT_NET_CONNECT:
                         tipId=R.string.net_connect;
                         //网络灯亮绿灯
                         UserDevices.internet_status((byte) 1);
                        break;
                     default:
                        tipId=R.string.net_waiting;
                         //网络灯亮红灯
                         UserDevices.internet_status((byte) 0);
                        break;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(tipTv!=null){
                            tipTv.setText(tipId);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_welecome;
    }

    @Override
    protected void initView(View view) {
        lastStatus=AppConfig.isTestSocket;
        EventBus.getDefault().register(this);
        //注册网络状态监听
        onEvent(new EventMsg(EventMsg.EVENT_MSG_NETCHANGED,EventMsg.CURRENT_NET_STATUS , 0,null ));
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(netWorkStateReceiver, filter);
        gprsManager = new GPRSManager(activity);
        updateNetStat();
        PackageManager pm =activity.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(activity.getPackageName(), 0);
            versionTv.setText("Version:"+info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int part = sp.getIntegerPref(AppConfig.DEPARTMENT_KAY, AppConfig.part1);
        if(part==AppConfig.part1){
            logoiv.setImageResource(R.mipmap.logo);
        }else {
            logoiv.setImageResource(R.mipmap.logo1);
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG,"x:"+event.getX()+"Y:"+event.getY() );
                if(event.getX()<=100&&event.getY()<=100){
                    if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_POINTER_DOWN){
                        if(System.currentTimeMillis()-lastsetClickTime>=2000){
                            setClickCount=1;
                        }else {
                            setClickCount++;
                        }
                        lastsetClickTime=System.currentTimeMillis();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void updateNetStat() {
        LogUtils.v(AppConfig.MODULE_SERVER, "本机IP地址：" + gprsManager.getIPAddress() + ";开始监听9898端口...");
        tv_server_address.setText(getString(R.string.server_address) + gprsManager.getIPAddress());
    }


    @OnClick({R.id.tv_server_address,R.id.logo_iv,R.id.version_tv})
    void OnClick(View view) {
        switch (view.getId()) {
            case R.id.tv_server_address:
                if(System.currentTimeMillis()-lastClickTime>=2000){
                    ClickCount=1;
                }else {
                    ClickCount++;
                }
                lastClickTime =System.currentTimeMillis();
                break;
            case R.id.version_tv:
                if(System.currentTimeMillis()-lastBugClickTime>=1500){
                    debugCount=1;
                }else {
                    debugCount++;
                }
                lastBugClickTime =System.currentTimeMillis();
                if(debugCount>=10){
                    debugCount=0;
                    LogUtils.LOG2FILE=!LogUtils.LOG2FILE;
                    LogUtils.LEVEL=(LogUtils.LEVEL==LogUtils.VERBOSE?LogUtils.ERROR:LogUtils.VERBOSE);
                    if(!LogUtils.LOG2FILE){
                        AppConfig.isTestSocket=true;
                        UiUtils.shortTip("Debug");
                    }else {
                        AppConfig.isTestSocket=lastStatus;
                        UiUtils.shortTip("Release");
                    }
                }
                break;
            case R.id.logo_iv:
                if(ClickCount==3){
                    if(System.currentTimeMillis()-lastClickTime<=2000){
                        PackageManager pm = activity.getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage("com.emp.collectionhardtest");
                        if(intent!=null)
                            startActivity(intent);
                        ClickCount=0;
                        break;
                    }
                    ClickCount=0;
                }else ClickCount=0;
                if(setClickCount==5){
                    if(System.currentTimeMillis()-lastsetClickTime<=2000){
                        if(activity instanceof WelcomeActivity)
                            ((WelcomeActivity) activity).showSetDialog();
                        setClickCount=0;
                        break;
                    }
                    setClickCount=0;
                }else setClickCount=0;
//                if(System.currentTimeMillis()-lastClickTime>=2000){
//                    ClickCount=1;
//                }else {
//                    ClickCount++;
//                }
//                lastClickTime=System.currentTimeMillis();
//                if(ClickCount>=5){
//                    ClickCount=0;
//                    swichLogo();
//                }
                break;
        }
    }

    public void swichLogo() {
        int part = sp.getIntegerPref(AppConfig.DEPARTMENT_KAY, AppConfig.part1);
        part=part==AppConfig.part1?AppConfig.part2:AppConfig.part1;
        if(part==AppConfig.part1){
            logoiv.setImageResource(R.mipmap.logo);
        }else {
            logoiv.setImageResource(R.mipmap.logo1);
        }
        sp.setIntegerPref(AppConfig.DEPARTMENT_KAY,part);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(activity!=null)
        activity.unregisterReceiver(netWorkStateReceiver);
    }
}
