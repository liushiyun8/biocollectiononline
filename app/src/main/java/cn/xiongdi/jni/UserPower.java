package cn.xiongdi.jni;

/**
 * Created by yzq on 2018/1/12.
 */

public class UserPower {
    /**
     * @return 0 if success, others if fail.
     */
    public static native int open();

    /*yzq: ioctl request */
    public static final int XD_POWER_CTL_VCC3V3_EN = 0;
    public static final int XD_POWER_CTL_SIGN = 1;
    public static final int XD_POWER_CTL_FINGER1 = 2;
    public static final int XD_POWER_CTL_FINGER2 = 3;
    public static final int XD_POWER_CAMERA_VCCEN = 4;
    public static final int XD_POWER_HUB_RST = 5;
    public static final int XD_POWER_AUDIO_VCCEN = 6;
    public static final int XD_POWER_LED_CTR = 6;

    /*yzq: ioctl state */
    public static final int DISABLE = 0;
    public static final int ENABLE = 1;

    /**
     * @param request
     * @param state
     * @return 0 if success, others if fail.
     */
    public static native int ioctl(int request, int state);

    /**
     * @return 0 if success, others if fail.
     */
    public static native int close();

    static {
        System.loadLibrary("jniUserPower");
    }
}
