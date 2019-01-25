package com.emptech.biocollectiononline.bean;

/**
 * Created by xpn on 2018/3/20.
 */
public class XmlInfo {

    /**
     * CPU温度 (cat /sys/class/hwmon/hwmon0/device/temp1_input)、
     * CPU占用率、总内存、可用内存、总存储空间、可用存储空间、
     * 背光亮度、声音大小、IP设置、网关设置(busybox ifconfig)、
     * 进程信息、//上次系统运行信息、
     * 当前系统运行信息(logcat -v time;logcat -b radio;dmesg)、
     * GPS信息（是否定位、经纬度、定位消耗时间）、
     * 无线信息（运营商、SIM卡号、信号强度、联网消耗时间等）、
     * 系统时间、RTC时间(busybox hwclock )、系统运行时间等。
     */

    //weather ADB is enable
    public int adbFlag;//0：关闭  1：暂时打开  2：永久打开

    public int entrySetting;//进入设置

    //obtain the system information
    public int systemInfo;

    public int copyTestLog;

    public int copyStartupLog;

    public String staticip;

    public String ipmask;

    public String gateway;

    public String dns1;

    public int wifiEnable;

    public int testTimaout;
    public int testError;
    public int testClose;

    public String getStaticip() {
        return staticip;
    }

    public void setStaticip(String staticip) {
        this.staticip = staticip;
    }

    public String getIpmask() {
        return ipmask;
    }

    public void setIpmask(String ipmask) {
        this.ipmask = ipmask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public int getCameraRotation() {
        return CameraRotation;
    }

    public void setCameraRotation(int cameraRotation) {
        CameraRotation = cameraRotation;
    }

    public int getVolume() {
        return Volume;
    }

    public void setVolume(int volume) {
        Volume = volume;
    }

    public int CameraRotation;

    public int Volume;

    public int getLightvalue() {
        return lightvalue;
    }

    public void setLightvalue(int lightvalue) {
        this.lightvalue = lightvalue;
    }

    public int lightvalue;

    public void setAdbFlag(int adbFlag) {
        this.adbFlag = adbFlag;
    }

    public void setSystemInfo(int systemInfo) {
        this.systemInfo = systemInfo;
    }


    public void setEntrySetting(int entrySetting) {
        this.entrySetting = entrySetting;
    }

    public void setCopyTestLog(int copyTestLog) {
        this.copyTestLog = copyTestLog;
    }

    public void setCopyStartupLog(int copyStartupLog) {
        this.copyStartupLog = copyStartupLog;
    }

    public int getWifiEnable() {
        return wifiEnable;
    }

    public void setWifiEnable(int wifiEnable) {
        this.wifiEnable = wifiEnable;
    }
}
