package com.emptech.biocollectiononline.bean;

public class VersionBean {
    private String sysVersion;
    private String ServerVersion;
    private String HardTestVersion;
    private String appVersion;
    private String powerVersion;
    private String signatureVersion;

    public String getSysVersion() {
        return sysVersion;
    }

    public void setSysVersion(String sysVersion) {
        this.sysVersion = sysVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPowerVersion() {
        return powerVersion;
    }

    public void setPowerVersion(String powerVersion) {
        this.powerVersion = powerVersion;
    }

    public String getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getServerVersion() {
        return ServerVersion;
    }

    public void setServerVersion(String serverVersion) {
        ServerVersion = serverVersion;
    }

    public String getHardTestVersion() {
        return HardTestVersion;
    }

    public void setHardTestVersion(String hardTestVersion) {
        HardTestVersion = hardTestVersion;
    }
}
