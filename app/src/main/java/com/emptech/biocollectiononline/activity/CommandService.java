package com.emptech.biocollectiononline.activity;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.widget.Toast;
import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.bean.XmlInfo;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.common.PullParser;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.utils.CommUtils;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.utils.VolumeControlUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xpn on 2018/3/20.
 */
public class CommandService extends IntentService {


    static final String TAG = AppConfig.MODULE;

    String IMEI = null;
    String IMSI = null;
    int voiceLevel = 0;

//    LocationManager mLocationManager = null;
//    TelephonyManager mTelephonyManager = null;

    boolean isLocationFlag = false;
    double latitude = 0.12345678;
    double longitude = 0.12345678;

    Handler mHandler = new Handler();

    /**
     * 手机定位监听
     */
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LogUtils.d(TAG, "onLocationChanged");
            isLocationFlag = true;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            locatedTime = System.currentTimeMillis();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 无参构造方法
     */
    public CommandService() {
        super("CommandService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        SystemClock.sleep(1000);
        String udiskPath = intent.getStringExtra(AppConfig.BUNDLE_KEY1);
        // udiskPath = "/sdcard";
        String xmlPath = udiskPath + AppConfig.XD_ROOT_DIR + "/" + AppConfig.XD_CFG_NAME;

        if (!new File(xmlPath).exists()) {
            LogUtils.d(AppConfig.MODULE, xmlPath + " not exist");
            return;
        }

        //解析xml文件
        XmlInfo info = null;
        try {
            info = parseXML(xmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(info!=null)
        excuteCommands(udiskPath, info);
    }


    XmlInfo parseXML(final String xmlPath) {

        //<staticip>192.168.1.1113</staticip>
        //	<ipmask>192.168.1.1</ipmask>
        //	<gateway>192.168.1.5666</gateway>
        //	<dns1>192.168.1.1</dns1>

        final XmlInfo xmlInfos[] = new XmlInfo[1];
        FileInputStream inputstream=null;
        try {
            inputstream = new FileInputStream(new File(xmlPath));
            PullParser.readXML(new PullParser.ICallback() {
                @Override
                public void callback(int eventCode, XmlPullParser parser) throws XmlPullParserException, IOException {
                    // TODO Auto-generated method stub
                    XmlInfo xmlInfo = xmlInfos[0];
                    switch (eventCode) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if ("configure".equals(parser.getName())) {
                                xmlInfos[0] = new XmlInfo();//创建一个对象
                            } else if (xmlInfo != null) {
                                String s = parser.nextText();
                                int flag=0;
                                if ("adbFlag".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setAdbFlag(flag);
                                } else if ("entrySetting".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setEntrySetting(flag);
                                } else if ("systemInfo".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setSystemInfo(flag);
                                } else if ("photoRotation".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setCameraRotation(flag);
                                } else if ("copyTestLog".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setCopyTestLog(flag);
                                } else if ("copyStartupLog".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setCopyStartupLog(flag);
                                } else if ("lightvalue".equals(parser.getName())) {
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setLightvalue(flag);
                                }else if("volume".equals(parser.getName())){
                                    flag=Integer.parseInt(s);
                                    xmlInfo.setVolume(flag);
                                }else if("staticip".equals(parser.getName())){
                                    xmlInfo.setStaticip(s);
                                }else if("ipmask".equals(parser.getName())){
                                    xmlInfo.setIpmask(s);
                                }else if("gateway".equals(parser.getName())){
                                    xmlInfo.setGateway(s);
                                }else if("dns1".equals(parser.getName())){
                                    xmlInfo.setDns1(s);
                                }else if("wifi".equals(parser.getName())){
                                    xmlInfo.setWifiEnable(Integer.parseInt(s));
                                }else if("testTimeout".equals(parser.getName())){
                                    xmlInfo.testTimaout=Integer.parseInt(s);
                                }else if("testError".equals(parser.getName())){
                                    xmlInfo.testError=Integer.parseInt(s);
                                }else if("testClose".equals(parser.getName())){
                                    xmlInfo.testClose=Integer.parseInt(s);
                                }

                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if ("configure".equals(parser.getName()) && xmlInfo != null) {
                                LogUtils.d(AppConfig.MODULE, "parseXMLTask parse XML=" + xmlPath + " OK");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }, "UTF-8", inputstream);
        } catch (XmlPullParserException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }finally {
            if(inputstream!=null) {
                try {
                    inputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return xmlInfos[0];
    }

    String logPath = "";

    void excuteCommand(String info, String cmd) {
        if (info != null)
            CommUtils.excuteRootCmd("echo " + info + " >> " + logPath);
        if (cmd != null)
            CommUtils.excuteRootCmd(cmd + " >> " + logPath);
    }

    // StringBuffer signalInfo = new StringBuffer();

    PhoneStateListener mPhonelistener = null;

    /**
     * 获取当前手机信号强度
     * @param signalPath
     */
    public void getCurrentNetDBM(final String signalPath) {

        mPhonelistener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String cotent = signalStrength.toString();
                String part[] = cotent.split(" ");
                String LteSignalStrength = part[8];
                int level = 0;
                //LTE ：4G网络
//                int progress = mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE ?
//                        Integer.parseInt(LteSignalStrength) : signalStrength.getGsmSignalStrength();
                int signal_level = 0;
                try {
                    signal_level = (Integer) signalStrength.getClass().getMethod("getLevel").invoke(signalStrength);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

//                signalInfo.append(cotent + "\r");
//                if (signalInfo.length() > 4096) {
//                    signalInfo.setLength(0);
//                }

                try {
                    CommUtils.writeTxtFile(cotent, signalPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        //开始监听
//        LogUtils.d(TAG, "mTelephonyManager.listen");
//        mTelephonyManager.listen(mPhonelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public String getProvidersName(String IMEI) {
        String ProvidersName = "UNKNOW";
        if (IMEI == null) {
            return ProvidersName;
        }

        if (IMEI.startsWith("46000") || IMEI.startsWith("46002") || IMEI.startsWith("46004")) {
            ProvidersName = "中国移动";
        } else if (IMEI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMEI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }

    /*    try {
            ProvidersName = URLEncoder.encode(""+ProvidersName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        Log.e("TAG_IMSI", "==== 当前卡为：" + ProvidersName);
        return ProvidersName;
    }

    void startSystemSetting() {
        //启动到手机的设置页面
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    long startLocateTime = 0;
    long locatedTime = 0;

    @SuppressLint("MissingPermission")
    void excuteCommands(String udiskPath, XmlInfo info) {

        AppConfig.PHOTO_ROTATE=info.getCameraRotation();
        PreferencesManager sp = App.get().getmPreferencesManager();
        sp.setIntegerPref(AppConfig.PREFERENCE_KEY_FILLLIGHT,info.getLightvalue());
        AppConfig.WIFIENABLE=info.getWifiEnable()==1;
        if(info.getVolume()!=0){
            VolumeControlUtil.adjustMusicVolume(this,info.getVolume() ,true );
        }

        //测试类型配置
        if(info.testTimaout==1){
            AppConfig.isTest=true;
            AppConfig.TestError=false;
            AppConfig.TestTimeout=true;
        }
        if(info.testError==1){
            AppConfig.isTest=true;
            AppConfig.TestError=true;
            AppConfig.TestTimeout=false;
        }
        if(info.testError==0&&info.testTimaout==0){
            AppConfig.isTest=false;
            AppConfig.TestError=false;
            AppConfig.TestTimeout=false;
        }
        if(info.testClose==1){
            AppConfig.isTest=true;
            AppConfig.TestError=false;
            AppConfig.TestTimeout=false;
            AppConfig.TestClose=true;
        }

        //获取Ethnet配置，并写入sp
        if(info.getStaticip()!=null){
            sp.setStringPref(AppConfig.PREFERENCE_KEY_STATICIP,info.getStaticip() );
        }
        if(info.getIpmask()!=null){
            sp.setStringPref(AppConfig.PREFERENCE_KEY_IPMASK,info.getIpmask() );
        }
        if(info.getGateway()!=null){
            sp.setStringPref(AppConfig.PREFERENCE_KEY_GATEWAY,info.getGateway() );
        }
        if(info.getDns1()!=null){
            sp.setStringPref(AppConfig.PREFERENCE_KEY_DNS,info.getDns1() );
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = format.format(new Date());

        String logDir = udiskPath + AppConfig.XD_ROOT_DIR+File.separator+"log"+File.separator+time;
        long oldTicks = System.currentTimeMillis();
        if (!new File(logDir).exists()) {
            new File(logDir).mkdirs();
        }

        if (info.entrySetting == 1) {
            startSystemSetting();
        }

        logPath = logDir + "/" + "info.txt";


            if (info.adbFlag == 0) {
                CommUtils.excuteRootCmd("setprop persist.sys.usb.config mtp &");
                showToast("close ADB");
            }
            if (info.adbFlag == 1) {
                CommUtils.excuteRootCmd("setprop sys.usb.config mtp,adb & ");
                showToast("open moment ADB");
            }
//            if (info.adbFlag == 2) {
//                CommUtils.excuteRootCmd("setprop persist.sys.usb.config mtp,adb &");
//                showToast("open ADB");
//            }
//            //手持机
//            if (info.adbFlag == 0) {
//                Intent intent = new Intent();
//                intent.setAction("com.android.settings.action.DISABLE_ADB");
//                sendBroadcast(intent);
//            }
//            if (info.adbFlag == 1 || info.adbFlag == 2) {
//                Intent intent = new Intent();
//                intent.setAction("com.android.settings.action.ENABLE_ADB");
//                sendBroadcast(intent);
//            }
//
//        }
//
//        if (AppConfig.isHandDevice) {
//            //手持机不抓取系统信息
//            return;
//        }

//        try{
//            if (info.systemInfo == 1) {
//                LogUtils.d(TAG, "requestLocationUpdates");
//                startLocateTime = System.currentTimeMillis();
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener);
//                String signalPath = logDir + "/" + "signal.txt";
//                getCurrentNetDBM(signalPath);
//            }
//        }catch (Exception e){
//            LogUtils.e(AppConfig.MODULE,"执行抓取定位信息出错，错误信息："+e.getMessage());
//        }


//        if (info.runningSysLog == 1) {
//
//            String logcatPath = logDir + "/" + "logcat.txt";
//            CommUtils.excuteRootCmd("logcat -v time > " + logcatPath + " &");//logcat -v time:实时输出日志到文件
//
//            if (!AppConfig.isHandDevice) {
//                String radioPath = logDir + "/" + "radio.txt";
//                CommUtils.excuteRootCmd("logcat -b radio > " + radioPath + " &");//logcat -b radio:输出通讯系统的logcat
//            }
//            showToast("抓取系统LOG");
//        }

        if (info.copyTestLog == 1) {

            //将sdcard/SZTMachine里面的文件复制到U盘的CLOG文件下
            CommUtils.excuteRootCmd("cp -rf "+AppConfig.WORK_PATH_CRASH +" "+ logDir + "/");
            showToast("copy crashlog to udisk");
        }

        if (info.copyStartupLog == 1) {
            CommUtils.excuteRootCmd("cp -rf "+AppConfig.WORK_PATH_LOG +" "+ logDir + "/");
            showToast("copy LOG to udisk");
        }

        String temp = "\r\r";// \r:换行
        if (info.systemInfo == 1) {

            excuteCommand("设备：" + IMEI, null);
            excuteCommand(temp + "系统时间:", "date");

//            if(!AppConfig.isHandDevice)
//                excuteCommand(temp + "RTC时间:", "busybox hwclock");

            long runTime = SystemClock.elapsedRealtime() /1000;
            excuteCommand(temp + "系统运行时间:", "echo " + runTime + "秒");
            String provider = getProvidersName(IMSI);
            excuteCommand(temp + "运营商:", "echo " + provider);
            //获取屏幕亮度
//            int lightLevel = SysUtils.getScreenBrightness(this);
//            excuteCommand(temp + "背光强度:", "echo " + lightLevel);
            excuteCommand(temp + "声音大小:", "echo " + voiceLevel);
//            if (!AppConfig.isHandDevice) {
//                excuteCommand(temp + "CPU 温度:", "cat /sys/class/hwmon/hwmon0/device/temp1_input");
//                excuteCommand(temp + "CPU 运行频率:", "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
//            }else {
//                excuteCommand(temp + "CPU 温度:", "cat /sys/class/thermal/thermal_zone1/temp");
//                excuteCommand(temp + "CPU 运行频率:", "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
//            }
            excuteCommand(temp + "存储空间:", "df");
            excuteCommand(temp + "进程信息:", "top -n 5 -d 1 -m 10");

//            if (!AppConfig.isHandDevice)
//                excuteCommand(temp + "IP网关信息:", "busybox ifconfig");
//            else
//                excuteCommand(temp + "IP网关信息:", "ifconfig");

//            String dmesgPath = logDir + "/" + "demsg.txt";
//            CommUtils.excuteRootCmd("dmesg > " + dmesgPath);

//            long sleepTime = info.locateWaitTime - (System.currentTimeMillis() - oldTicks);
//            if (sleepTime > 0) {
//                SystemClock.sleep(sleepTime);
//            }

//            if (mLocationListener != null) {
//                LogUtils.d(TAG, "removeUpdates");
//                mLocationManager.removeUpdates(mLocationListener);
//            }
            //isLocationFlag = true;
            if (!isLocationFlag)
                // excuteCommand(temp + "GPS定位信息:", "echo 未定位，等待时间" + info.locateWaitTime + "秒");
                excuteCommand(temp + "GPS定位信息:", "echo 未定位");
            else {
                excuteCommand(temp + "GPS定位信息:", "echo [" + latitude + "," + longitude + "]");
                excuteCommand(temp + "定位用时:", "echo " + (locatedTime - startLocateTime));
            }

            excuteCommand("\r\rTHE END", null);

//            if (mPhonelistener != null) {
//                LogUtils.d(TAG, "PhoneStateListener.LISTEN_NONE");
//                mTelephonyManager.listen(mPhonelistener, PhoneStateListener.LISTEN_NONE);
//            }
//            showToast("获取系统信息");
        }

        showToast("task finish");

    }

    void showToast(final String info) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getAppContext(), info, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
