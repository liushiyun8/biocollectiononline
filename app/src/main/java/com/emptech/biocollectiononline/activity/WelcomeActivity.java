package com.emptech.biocollectiononline.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.common.AppFragmentActivity;
import com.emptech.biocollectiononline.common.CommonAdapter;
import com.emptech.biocollectiononline.common.ViewHolder;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.WelecomFragment;
import com.emptech.biocollectiononline.manager.LedManager;
import com.emptech.biocollectiononline.manager.PowerManager;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.SignatureManager;
import com.emptech.biocollectiononline.manager.VersionManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.utils.CommUtils;
import com.emptech.biocollectiononline.utils.FileUtil;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.utils.SDCardUtil;
import com.emptech.biocollectiononline.utils.UiUtils;
import com.emptech.biocollectiononline.utils.Utils;
import com.emptech.biocollectiononline.utils.VolumeControlUtil;
import com.emptech.biocollectiononline.utils.YModem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import cn.xiongdi.jni.UserDevices;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class WelcomeActivity extends AppFragmentActivity {

    @Inject
    protected App app;

    @Inject
    protected Resources mResources;

    @Inject
    protected PreferencesManager mPreferencesManager;

    int REQUEST_WRITE = 1;
    private AlertDialog dialog;
    private AlertDialog LightDialog;
    private SeekBar seekb;
    private SeekBar mediaSeek;
    private AlertDialog VolumeDialog;
    private AlertDialog languagedialog;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener;
    private Intent intentServer;
    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Action ",action);
            Log.e("result ",intent.getStringExtra("result"));
//            Toast.makeText(WelcomeActivity.this,"set success" ,Toast.LENGTH_LONG ).show();
        }
    };
    private boolean needStopServer;
    private boolean needConfig;
    String[] array;
    private long SOFTTIMEOUT=24*60*60*1000;
    private AlertDialog.Builder alertDialogBuilder;
    private PowerManager powerManager;
    private int registCount;
    private AlertDialog updatedialog;
    private AlertDialog log_dialog;
    private RadioGroup rg;
    private boolean isIpRegister;


    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {

            private ProgressDialog progressDialog;

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case YModem.MSG_PROGRESS: {
                        if(progressDialog==null)
                        progressDialog = new ProgressDialog(WelcomeActivity.this);
                        progressDialog.setMax(msg.arg2);
                        progressDialog.setProgress(msg.arg1);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        if(!progressDialog.isShowing())
                        progressDialog.show();
                        if (msg.arg1 == msg.arg2) {
                            alertDialogBuilder.setTitle(R.string.file_send_result)
                                    .setMessage(R.string.file_send_success)
                                    .setCancelable(true)
                                    .show();
                            if(powerManager!=null)
                                powerManager.update();
                        }
                        break;
                    }
                    case YModem.MSG_INFO: {
                        if("YModem download success".equals(String.valueOf(msg.arg1))){
                            if(powerManager!=null)
                            powerManager.update();
                        }
                        LogUtils.v(TAG, String.valueOf(msg.arg1));
                        break;
                    }
                    case YModem.MSG_ERROR: {
                        alertDialogBuilder.setTitle(R.string.file_send_result)
                                .setMessage((String) msg.obj)
                                .setCancelable(true)
                                .show();
                        break;
                    }
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().Component().inject(this);
        //先判断语言，语言不一致的话直接结束
        Locale locale;
        String lang = App.get().getmPreferencesManager().getStringPref(AppConfig.LANGUAGE_KEY, "ru");
        switch (lang){
            case "ru":
                locale=new Locale("ru");
                break;
            case "en":
                locale=Locale.ENGLISH;
                break;
            case "cn":
                locale=Locale.CHINESE;
                break;
            default:
                locale=new Locale("ru");
                break;
        }
        if(!setLanguage(locale)){
            return;
        }
        requestSDPermissions();
        array = getResources().getStringArray(R.array.set_array);
        startSocket();
        needStopServer=true;

        OpenUsb();
        int ret = UserDevices.uart_open();
        if (ret != 0) {
            LogUtils.v(TAG, "串口上电失败:" + ret);
        }else LogUtils.v(TAG, "串口上电成功:" + ret);

        //左右指纹下电
        UserDevices.fingerLeft_ope((byte) 0);
        UserDevices.fingerRight_ope((byte) 0);
        //open Signature power
        UserDevices.signature_ope((byte) 1);
        //关闭程序异常导致的灯常亮
        LedManager.close();
        //设置进入界面：
//        initSet();

        //设置默认声音
        int maxVolume = VolumeControlUtil.getMaxVolume(this, AudioManager.STREAM_MUSIC);
        LogUtils.v("maxVolume", maxVolume+"");
        int volume=mPreferencesManager.getIntegerPref(AppConfig.PREFERENCE_KEY_VOLUME, 0);

        VolumeControlUtil.adjustMusicVolume(this, volume,false);

        alertDialogBuilder = new AlertDialog.Builder(this);

        //注册IP设置结果广播
        IntentFilter mIntentFilter = new IntentFilter("xd.ethernet.SettingsResult");
        registerReceiver(mReceiver, mIntentFilter);
        isIpRegister=true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndUpdateApk();
            }
        }, 3000);
    }

    private void startSocket() {
        intentServer = new Intent(this, ServerService.class);
        startService(intentServer);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        int id = MusicPlayer.get(this).play(R.raw.welcome, false);
//        LogUtils.v(TAG,"musicId:"+id );
    }

    private void startCommandService(String udiskPath) {
        LogUtils.d(TAG, "startCommandService");
        Intent myintent = new Intent(this, CommandService.class);
        myintent.putExtra(AppConfig.BUNDLE_KEY1, udiskPath);
        this.startService(myintent);
    }
    private void findAndUpdateApk() {
        List<Map<String, Object>> canWriteExtSDCardPath = SDCardUtil.getCanWriteExtSDCardPath();
        Log.v(TAG,"size:" +canWriteExtSDCardPath.size());
        for (int i = 0; i < canWriteExtSDCardPath.size(); i++) {
            Map<String, Object> stringObjectMap = canWriteExtSDCardPath.get(i);
            String path = (String) stringObjectMap.get("path");
            Log.v(TAG,"path:" +path);
            LogUtils.v(TAG,"启动配置服务" );
            startCommandService(path);
//            updateApk(path);
            needConfig=true;
            if(updateSoftware(path)){
                break;
            }
        }
    }

    private boolean updateSoftware(String path) {
        String softpath = path + AppConfig.UPDATESOFTPATH;
        File file = new File(softpath);
        FileUtil.deleteDir(AppConfig.WORK_UPDATE_PATH);
        List<UpdateItem> items=new ArrayList<>();
        File[] list = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith("Collection.apk"))
                    return true;
                return false;
            }
        });
        if(list!=null&&list.length>0){
            for (int i = 0; i < list.length; i++) {
                String apkPath = list[i].getAbsolutePath();
                if(CommUtils.checkApk(this,apkPath )){
                    String destFileName = AppConfig.WORK_UPDATE_PATH + File.separator + new File(apkPath).getName();
                    if(FileUtil.copyFile(apkPath, destFileName)){
                        items.add(new UpdateItem(AppConfig.UPDATE_APP_TYPE,destFileName));
                        LogUtils.e(TAG,"Find APK version match,apkPath:"+apkPath);
                    }else
                        LogUtils.e(TAG,"APK File copy error");
                    break;
                }
                LogUtils.e(TAG,"APK version not match,apkPath:"+apkPath);
            }
        }
        File[] list1 = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(AppConfig.PMSOFTFILENAME))
                    return true;
                return false;
            }
        });
        if(list1!=null&&list1.length>0){
            for (int i = 0; i < list1.length; i++) {
                File f = list1[i];
                String version = f.getName().substring(1, 8);//V1.0.0.1
                String replace = version.replace(".", "").replace("V", "");
                String pmVersion = VersionManager.getPmVersion();
                String v = pmVersion.replace(".", "").replace("V", "");
                try {
                    LogUtils.d(TAG,"U disk:"+replace+",curent:" +v);
                    if(Integer.parseInt(replace)>Integer.parseInt(v)){
                        String pmPath=f.getAbsolutePath();
                        LogUtils.e(TAG,"Find update PM macth,pm paht:"+pmPath);
                        String destFileName = AppConfig.WORK_UPDATE_PATH + File.separator + new File(pmPath).getName();
                        if(FileUtil.copyFile(pmPath,destFileName )){
                            items.add(new UpdateItem(AppConfig.UPDATE_PM_TYPE,destFileName));
                        }else
                            LogUtils.e(TAG,"Copy pmFile error:"+pmPath);
                        break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        File[] file2=file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(AppConfig.SIGNATURESOFTFILENAME)){
                    return true;
                }
                return false;
            }
        });
        if(file2!=null&&file2.length>0){
            for (File f : file2) {
                String version = f.getName().substring(1, 6);//V1.0.0
                String replace = version.replace(".", "").replace("V", "");
                String SigVersion = VersionManager.getSignatureVersion();
                String v = SigVersion.replace(".", "").replace("V", "");
                try {
                    LogUtils.d(TAG,"U disk:"+replace+",curent:" +v);
                    if(Integer.parseInt(replace)>Integer.parseInt(v)){
                        String SigPath=f.getAbsolutePath();
                        LogUtils.e(TAG,"Find update Signature macth,signature path:"+SigPath);
                        String destFileName = AppConfig.WORK_UPDATE_PATH + File.separator + new File(SigPath).getName();
                        if(FileUtil.copyFile(SigPath,destFileName )){
                            items.add(new UpdateItem(AppConfig.UPDATE_SIGNATURE_TYPE,destFileName ));
                        }else
                            LogUtils.e(TAG,"Copy SigFile error:"+SigPath);
                        break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if(items.size()>0){
            showUpdateDialog(items);
            return true;
        }
        return false;
    }

    private void showUpdateDialog(List<UpdateItem> items) {
        if(updatedialog==null) {
            View view = LayoutInflater.from(this).inflate(R.layout.setdialog, null);
            TextView tv=view.findViewById(R.id.title);
            tv.setText(R.string.updateApp);
            Button cancle_btn=view.findViewById(R.id.cancle_btn);
            cancle_btn.setText(R.string.cancle);
            cancle_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedialog.dismiss();
                }
            });
            updatedialog = new AlertDialog.Builder(this).setView(view)
                    .setCancelable(false).create();
        }
        updatedialog.show();
        ListView lv = updatedialog.getWindow().findViewById(R.id.lv);
        CommonAdapter<UpdateItem> adapter = new CommonAdapter<UpdateItem>(WelcomeActivity.this, items, R.layout.item_lay) {
            @Override
            public void convert(ViewHolder helper, int position, final UpdateItem item) {
                String title = "";
                switch (item.type) {
                    case AppConfig.UPDATE_APP_TYPE:
                        title = getString(R.string.updateApp);
                        break;
                    case AppConfig.UPDATE_PM_TYPE:
                        title = getString(R.string.updatePmSoft);
                        break;
                    case AppConfig.UPDATE_SIGNATURE_TYPE:
                        title = getString(R.string.signature_update);
                        break;
                }
                helper.setText(R.id.tv, title);
                helper.getView(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (item.type){
                            case AppConfig.UPDATE_APP_TYPE:
                                updateAPP(item.path);
                                break;
                            case AppConfig.UPDATE_PM_TYPE:
                                updatePm(item.path);
                                break;
                            case AppConfig.UPDATE_SIGNATURE_TYPE:
                                updateSignature(item.path);
                                break;
                        }
                        updatedialog.dismiss();
                    }
                });
            }
        };
        lv.setAdapter(adapter);
        updatedialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void updateSignature(final String path) {
        final SignatureManager signatureManager = new SignatureManager();
        boolean open = signatureManager.openDevice();
        if(open)
        new Thread(){
            @Override
            public void run() {
                super.run();
                signatureManager.update(path);
            }
        }.start();
        else UiUtils.shortTip(R.string.signature_fail);
    }

//    private void initSet() {
//        getContentView().setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.e(TAG,"x:"+event.getX()+"Y:"+event.getY() );
//                if(event.getX()<=60&&event.getY()<=60){
//                    if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_POINTER_DOWN){
//                        if(System.currentTimeMillis()-lastClickTime>=2000){
//                            ClickCount=1;
//                        }else {
//                            ClickCount++;
//                        }
//                        lastClickTime=System.currentTimeMillis();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//    }

    public void showSetDialog() {
        if(dialog==null){
            View view = LayoutInflater.from(this).inflate(R.layout.setdialog, null);
            ListView lv = view.findViewById(R.id.lv);
            List<String> list = Arrays.asList(array);
            list=new ArrayList<>(list);
            list.remove(list.size()-1);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, list);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position){
                        case 0:
                            ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$EthernetSettingsActivity");
                            Intent intent = new Intent();
                            intent.setComponent(componentName);
                            startActivity(intent);
                            if(needConfig)
                            sendConfigBroadCast();
                            break;
                        case 1:
                            setFillLight();
                            break;
                        case 2:
                            setVolume();
                            break;
                        case 3:
                            switchLanguage();
                            break;
                        case 4:
                            showLogDialog();
                            break;
                        case 5:
                            Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(wifiIntent);
                            break;

                    }
                }
            });
            View cancle = view.findViewById(R.id.cancle_btn);
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog = new AlertDialog.Builder(this).setView(view)
                    .setCancelable(false).create();
        }

        if(!dialog.isShowing()){
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.show();
            ListView lv = dialog.getWindow().findViewById(R.id.lv);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) lv.getAdapter();
            if(AppConfig.WIFIENABLE){
                adapter.clear();
                adapter.addAll(array);
                adapter.notifyDataSetChanged();
            }else {
                adapter.clear();
                List<String> list = Arrays.asList(array);
                list=new ArrayList<>(list);
                list.remove(list.size()-1);
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }

    }

    private void showLogDialog() {
        if(log_dialog==null){
            View v = LayoutInflater.from(this).inflate(R.layout.list_item, null);
            rg = v.findViewById(R.id.rg);
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(currentFragment instanceof WelecomFragment){
                        ((WelecomFragment)currentFragment).swichLogo();
                    }
                }
            });
            v.findViewById(R.id.cancle_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log_dialog.dismiss();
                }
            });
            log_dialog=new AlertDialog.Builder(this).setView(v).create();
        }
        if(!log_dialog.isShowing()){
            int part = App.get().getmPreferencesManager().getIntegerPref(AppConfig.DEPARTMENT_KAY, AppConfig.part1);
            if(part==AppConfig.part1){
                rg.check(R.id.rb1);
            }else rg.check(R.id.rb2);
            log_dialog.getWindow().setGravity(Gravity.CENTER);
            log_dialog.show();
        }
    }
    private void switchLanguage() {
        if(languagedialog==null){
            View view = LayoutInflater.from(this).inflate(R.layout.setdialog, null);
            ListView lv = view.findViewById(R.id.lv);
            TextView tv = view.findViewById(R.id.title);
            tv.setText(R.string.languagetitle);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, getResources().getStringArray(R.array.languages));
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position){
                        case 0:
                            setLanguage(new Locale("ru"));
                            break;
                        case 1:
                            setLanguage(Locale.ENGLISH);
                            break;
                        case 2:
                            setLanguage(Locale.CHINESE);
                            break;
                    }
                    languagedialog.dismiss();
//                    Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    App.get().startInitializeTask();
                }
            });
            View cancle = view.findViewById(R.id.cancle_btn);
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    languagedialog.dismiss();
                }
            });
            languagedialog = new AlertDialog.Builder(this).setView(view)
                    .setCancelable(false).create();
        }

        if(!languagedialog.isShowing()){
            languagedialog.getWindow().setGravity(Gravity.CENTER);
            languagedialog.show();
        }
    }

    public boolean setLanguage(Locale locale) {
        Resources resources = app.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        LogUtils.e(TAG,"set Local:"+locale+",current local:"+config.locale);
        if(!locale.equals(config.locale)){
            config.locale = locale;
            if (locale == Locale.ENGLISH) {
                mPreferencesManager.setStringPref(AppConfig.LANGUAGE_KEY, "en");
            }else if(locale==Locale.CHINESE){
                mPreferencesManager.setStringPref(AppConfig.LANGUAGE_KEY, "cn");
            }else {
                mPreferencesManager.setStringPref(AppConfig.LANGUAGE_KEY, "ru");
            }
            resources.updateConfiguration(config, dm);
            needStopServer=false;
            App.get().startInitializeTask();
            Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return false;
        }
      return true;
    }

    private void sendConfigBroadCast() {
        PreferencesManager sp = App.get().getmPreferencesManager();
        String ip = sp.getStringPref(AppConfig.PREFERENCE_KEY_STATICIP);
        String ipmask = sp.getStringPref(AppConfig.PREFERENCE_KEY_IPMASK);
        String gateway = sp.getStringPref(AppConfig.PREFERENCE_KEY_GATEWAY);
        String dns = sp.getStringPref(AppConfig.PREFERENCE_KEY_DNS);
        if(!TextUtils.isEmpty(ip)){
            Intent intent = new Intent();
            intent.setAction("xd.ethernet.staticip");
            intent.putExtra("staticip", ip);
            intent.putExtra("ipmask", ipmask);
            intent.putExtra("gateway", gateway);
            intent.putExtra("dns1", dns);
            intent.putExtra("dns2", dns);
            LogUtils.e(TAG,"staticip:"+ip );
            LogUtils.e(TAG,"ipmask:"+ipmask );
            LogUtils.e(TAG,"gateway:"+gateway );
            LogUtils.e(TAG,"dns1:"+dns );
            //发送广播
            sendBroadcast(intent);
        }else {
            Intent intent = new Intent();
            intent.setAction("xd.ethernet.dhcp");
            //发送广播
            sendBroadcast(intent);
        }
    }

    private void setVolume() {
        if(VolumeDialog==null){
            VolumeDialog = new AlertDialog.Builder(this).setView(R.layout.volume).create();
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    }).create();
            VolumeDialog.setCanceledOnTouchOutside(false);
            VolumeDialog.getWindow().setGravity(Gravity.CENTER);
        }
        if(!VolumeDialog.isShowing()){
            VolumeDialog.show();
            //找各个View
            mediaSeek = VolumeDialog.getWindow().findViewById(R.id.mediaSeek);
            VolumeDialog.getWindow().findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(VolumeDialog.isShowing())
                        VolumeDialog.dismiss();
                }
            });
//            alarmSeek=VolumeDialog.getWindow().findViewById(R.id.AlarmSeek);
//            notificationSeek=VolumeDialog.getWindow().findViewById(R.id.NotificationSeek);
            //设置媒体声音
            mediaSeek.setMax(VolumeControlUtil.getMaxVolume(this, AudioManager.STREAM_MUSIC));
            mediaSeek.setProgress(VolumeControlUtil.getCurVolume(this, AudioManager.STREAM_MUSIC));

            //设置响铃声音
//            alarmSeek.setMax(VolumeControlUtil.getMaxVolume(this,AudioManager.STREAM_ALARM ));
//            alarmSeek.setProgress(VolumeControlUtil.getCurVolume(this,AudioManager.STREAM_ALARM ));

            //设置通知声音
//            notificationSeek.setMax(VolumeControlUtil.getMaxVolume(this,AudioManager.STREAM_NOTIFICATION ));
//            notificationSeek.setProgress(VolumeControlUtil.getCurVolume(this,AudioManager.STREAM_NOTIFICATION ));
            seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
//                    if(seekBar==mediaSeek){
                        VolumeControlUtil.adjustMusicVolume(WelcomeActivity.this,progress ,true );
                        //记到sp
                       mPreferencesManager.setIntegerPref(AppConfig.PREFERENCE_KEY_VOLUME, progress);
//                    }else if(seekBar==alarmSeek){
//                        VolumeControlUtil.adjustRingVolume(WelcomeActivity.this,progress ,false );
//
//                    }else if(seekBar==notificationSeek){
//                        VolumeControlUtil.adjustNotificationVolume(WelcomeActivity.this,progress ,false );
//                    }
                }
            };
            mediaSeek.setOnSeekBarChangeListener(seekBarChangeListener);
//            alarmSeek.setOnSeekBarChangeListener(seekBarChangeListener);
//            notificationSeek.setOnSeekBarChangeListener(seekBarChangeListener);
        }

    }

    private void setFillLight() {
        if(LightDialog==null){
            LightDialog = new AlertDialog.Builder(this).setView(R.layout.filllight).
                    create();
//                    setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                                LedManager.close();
//                                dialog.dismiss();
//                        }
//                    })
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            LedManager.close();
//                            App.get().getmPreferencesManager().setIntegerPref(AppConfig.PREFERENCE_KEY_FILLLIGHT,seekb.getProgress() );
//                            dialog.dismiss();
//                        }
//                    })
            LightDialog.setCanceledOnTouchOutside(false);
            LightDialog.getWindow().setGravity(Gravity.CENTER);

        }
        if(!LightDialog.isShowing()){
            LightDialog.show();
            seekb = LightDialog.getWindow().findViewById(R.id.seek);
            LightDialog.getWindow().findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LedManager.close();
                    App.get().getmPreferencesManager().setIntegerPref(AppConfig.PREFERENCE_KEY_FILLLIGHT,seekb.getProgress() );
                    LightDialog.dismiss();
                }
            });
            seekb.setMax(255);
            seekb.setProgress(LedManager.getCurrentValue());
            seekb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekb.getProgress();
                    LedManager.open(progress);
                }
            });
        }

    }


    @Override
    protected void setupTopBar() {
        hideActionBar();
    }

    @Override
    protected BaseFragment setFragment() {
        return new WelecomFragment();
    }


    private void requestSDPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            //判断是否有这个权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //2、申请权限: 参数二：权限的数组；参数三：请求码
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
            }
        }
        Utils.mkDirs(AppConfig.WORK_PATH);
        LogUtils.logConfigure(AppConfig.WORK_PATH_LOG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Utils.mkDirs(AppConfig.WORK_PATH);
            LogUtils.logConfigure(AppConfig.WORK_PATH_LOG);
        }
    }

    private void OpenUsb() {
        registerFingerUsb();
        registCount++;
    }


    private void registerFingerUsb() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        registerReceiver(mUsbReceiver, filter);
        Log.e(TAG,"已经注册USB监听" );

    }

    private boolean isUnregisterFingerReceiver = false;//是否解除了指纹广播；

    private void unRegisterFingerUsb() {
        unregisterReceiver(mUsbReceiver);
        isUnregisterFingerReceiver = true;
    }

    private void closeFingerUsb() {
        Log.e(TAG,"关闭USB广播" );
        registCount--;
        if (!isUnregisterFingerReceiver&&registCount==0) {
            unRegisterFingerUsb();
            Log.e(TAG,"已经解除USB广播" );
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)/* || action.equals(Intent.ACTION_MEDIA_CHECKING)*/) {
                //获取文件路径
                String pathString = intent.getData().getPath();
                LogUtils.e(TAG,"mountPath = "+pathString);
                File file = new File(pathString);
                File[] files = file.listFiles();
                if(files.length>0){
//                    String usbPath = files[0].getAbsolutePath();
                    findAndUpdateApk();
//                    updateApk(usbPath);
                }
//                String path = intent.getDataString();
//                LogUtils.e(TAG,"path:" +path);
//                //当前usb路径
//                String pathString = path.split("file://")[1];
                //有U盘插入，找到APK路径并更新
//                findAndUpdateApk();
                LogUtils.v(AppConfig.MODULE_APP, "U盘插入");
            }else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)||action.equals(Intent.ACTION_MEDIA_EJECT)){
                LogUtils.v(AppConfig.MODULE_APP, "U盘拔出");
                needConfig=false;
                AppConfig.isTest=false;
                if(updatedialog!=null&&updatedialog.isShowing())
                    updatedialog.dismiss();
            }
        }
    };

    private boolean updateApk(String pathString) {
        String softpath = pathString + AppConfig.UPDATESOFTPATH;
        File file = new File(softpath);
        File[] list = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith("Startup.apk"))
                    return true;
                return false;
            }
        });
        if(list==null){
            return false;
        }
        for (int i = 0; i < list.length; i++) {
            String apkPath = list[i].getAbsolutePath();
            if(CommUtils.checkApk1(this,apkPath )){
                LogUtils.e(TAG,"startUp version match,apkPath:"+apkPath);
                return CommUtils.silentInstall(apkPath);
            }
            LogUtils.e(TAG,"APK version not match,apkPath:"+apkPath);
        }
       return false;
    }

    private void updateAPP(String apkPath){
        CommUtils.updateAPK(WelcomeActivity.this,apkPath );
    }

    private void updatePm(String pmpath){
        powerManager = new PowerManager(WelcomeActivity.this, mWeakReferenceHandler);
        boolean open = powerManager.openUart();
        LogUtils.e(TAG,"uartOpen:" +open);
        if(open){
            powerManager.update(pmpath);
        }
    }

    private void enterMainProcess() {
        Log.i("mainPackageName","========mainPackageName:"+getPackageName());
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            startActivity(intent);
        } else {
            LogUtils.d(TAG, "mainPackageName not exist");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null&&isIpRegister){
            unregisterReceiver(mReceiver);
            mReceiver=null;
            isIpRegister=false;
        }
        if(needStopServer&&intentServer!=null){
            stopService(intentServer);
            //网络灯亮红灯
            UserDevices.internet_status((byte) 0);
        }
        if(dialog!=null&&dialog.isShowing())
            dialog.dismiss();
        if(LightDialog!=null&&LightDialog.isShowing())
            LightDialog.dismiss();
        if(languagedialog!=null&&languagedialog.isShowing())
            languagedialog.dismiss();
        if(VolumeDialog!=null&&VolumeDialog.isShowing())
            VolumeDialog.dismiss();
        if(updatedialog!=null&&updatedialog.isShowing())
            updatedialog.dismiss();
        closeFingerUsb();
    }

    private class UpdateItem {
        int type;
        String path;
        public UpdateItem(int type, String path) {
            this.type = type;
            this.path = path;
        }
    }
}
