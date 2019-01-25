package cn.xiongdi.jni;

/**
 * Created by yzq on 2018/1/17.
 */

public class UserMotor {
    /**
     * @return 0 if success, others if fail.
     */
    public static native int open();


    public static final byte actionMoveForward = 1;
    public static final byte actionMoveRollback = 2;
    public static final byte actionMoveStop = 3;

    public static final byte actionMoveUp = actionMoveForward;
    public static final byte actionMoveDown = actionMoveRollback;

    /**
     * @param action actionMoveUp, actionMoveDown, actionMoveStop
     * @param speed  [99, 65535] passispeed.
     * @return 0 if success, others if fail.
     */
    public static native int move(byte action, int speed);

    /**
     * @param state
     * @return 0 if success, others if fail.
     */
    public static native int sensor(byte[] state);

    public static boolean isMoveUpLimit(byte state) {
        return (state & (1 << actionMoveUp)) != 0;
    }

    public static boolean isMoveDownLimit(byte state) {
        return (state & (1 << actionMoveDown)) != 0;
    }

    /**
     * @return 0 if success, others if fail.
     */
    public static native int close();

    static {
        System.loadLibrary("jniUserMotor");
    }
}
