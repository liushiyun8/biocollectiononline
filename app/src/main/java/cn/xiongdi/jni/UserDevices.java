/* 注意，请务必确保包路径为：package cn.xiongdi.jni; */
/**
 * Last edit 2018-6-22, ceekin
 * Please make sure the package path must be "package cn.xiongdi.jni;"
 */
package cn.xiongdi.jni;

public class UserDevices {
	/**
     * 注意:要控制外设，必须先使用uart_open打开串口模块
	 *		程序退出时再关闭串口模块
     */
    public static final byte actionMoveForward = 1;
    public static final byte actionMoveRollback = 2;
    public static final byte actionMoveStop = 3;

    public static final byte actionMoveUp = actionMoveForward;
    public static final byte actionMoveDown = actionMoveRollback;
    public static byte currentSate=0;

    /**
     * 描述:  打通信开串口
     * Open UART Module
     *
     * @return 0 if success, others if fail.
     * !0：打开模块失败 0：打开成功
     */
    public static native int uart_open();

   /**
     * 描述:  关闭通信开串口
     * Close UART Module
     *
     * @return 0 if success, others if fail.
     * !0：打开模块失败 0：打开成功
     */
    public static native int uart_close();

    /**
     * 描述: 电机控制指令
     * control the motor
     *
     * @param  behaviour =  1:向上转（正转）
	 *						2:向下转（反转）
	 *						3:停止
     *         speed ：值在（79-149），值越大速度越小              	
     * @return 操作命令 1:向上转（正转）
	 *					2:向下转（反转）
	 *					3:停止
     */
    public static native int motor_move(byte behaviour,int speed);
	
	
	/**
     * 描述: 检测电机是否到达临界点
     * check the motor
     *
     * @param  status =  返回的状态   0x04 为到达底部 0x02为到达顶部	 
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int motor_sensor(byte[] status);
	
	/**
     * 描述: 右指纹控制指令
     * control the fingerRight
     *
     * @param  status = 1:打开右指纹模块
	 *					0:关闭右指纹模块             	
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int fingerRight_ope(byte status);
	
	/**
     * 描述: 左指纹控制指令
     * control the fingerLeft
     *
     * @param  status = 1:打开左指纹模块
	 *					0:关闭左指纹模块             	
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int fingerLeft_ope(byte status);
	
	/**
     * 描述: 签名版控制指令
     * control the signature
     *
     * @param  status = 1:打开签名版
	 *					0:关闭签名版             	
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int signature_ope(byte status);
	
	
	
	/**
     * 描述: 状态传输指令
     * status transfer
     *
     * @param  status = 非0:系统其他异常状态
	 *					0:系统正常             	
     * @return 0 if success, others if fail.
     * !0：其他异常状态 0：电源板状态正常
     */
    public static native int status_transfer(byte status);
	
	
	
	/**
     * 描述: 网络状态指令
     * control the signature
     *
     * @param  status = 1:网络正常
	 *					0:网络异常             	
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int internet_status(byte status);

	
/**
     * 描述: 获取电源板程序版本
     * get version
     *
     * @param  status = 7位版本号  	 
     * @return 0 if success, others if fail.
     * !0：失败 0：成功
     */
    public static native int get_version(byte[] status);
	
    public static boolean isMoveUpLimit(byte state) {
        return (state & (1 << actionMoveUp)) != 0;
    }

    public static boolean isMoveDownLimit(byte state) {
        return (state & (1 << actionMoveDown)) != 0;
    }
    static {
        System.loadLibrary("jniUserDevices");/*ceekin: jniLib */
    }
}
