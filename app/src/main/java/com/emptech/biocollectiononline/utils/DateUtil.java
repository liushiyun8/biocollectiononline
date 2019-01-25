package com.emptech.biocollectiononline.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings.SettingNotFoundException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xiongdi on 2016/3/24. 时间日期工具类
 */
@SuppressLint("NewApi")
public class DateUtil {
    /**
     * 打印小票上的时间
     *
     * @return
     */
    public static String getPrintCurFormatTime() {
        Date todayDate = new Date();
        String[] strs = todayDate.toString().split(" ");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String str1 = df.format(todayDate);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy");
        String str2 = df2.format(todayDate);
        SimpleDateFormat df3 = new SimpleDateFormat("dd");
        String str3 = df3.format(todayDate);
        return str1 + ", " + str3 + ", " + strs[1] + ", " + str2;
    }

    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS",
                Locale.getDefault());
        return format.format(new Date());
    }


    public static boolean settDateTimeCommand(Context context, int year,
                                              int month, int day, int hour, int minute, int second) {

		/*
         * if(DateUtil.isTimeZoneAuto(context)){
		 * LogUtils.e(AppConfig.MODULE_SETTING,"关闭自动时区设置"); //关闭自动时区设置
		 * DateUtil.setAutoTimeZone(context, false); }
		 * 
		 * if(DateUtil.isDateTimeAuto(context)){ //关闭自动更新时间
		 * LogUtils.e(AppConfig.MODULE_SETTING,"关闭自动更新时间");
		 * DateUtil.setAutoDateTime(context, false); }
		 */
        String yearStr = String.format("%04d", year);
        String monthStr = String.format("%02d", month);
        String dayStr = String.format("%02d", day);
        String hourStr = String.format("%02d", hour);
        String minuteStr = String.format("%02d", minute);
        String secondStr = String.format("%02d", second);
        String datetime = yearStr + monthStr + dayStr + "." + hourStr
                + minuteStr + secondStr; // 测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
        return settDateTimeCommand(context, datetime);
    }

    /**
     * 设置系统时间，注意：有格式要求
     *
     * @param context
     * @param data_Time 时间参数 格式： yyyyMMdd.HHmmss
     * @return
     */
    public static synchronized boolean settDateTimeCommand(Context context,
                                                           String data_Time) {
        AlarmManager mAlarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setTimeZone("GMT+00:00");
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes("setprop persist.sys.timezone GMT00:00\n");
            os.writeBytes("/system/bin/date -s " + data_Time + " \n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
            os.flush();
            long now = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
            Date date = sdf.parse(data_Time);
            long when = date.getTime();
            if (Math.abs(now - when) < 1000) {
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }/*
         * catch (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
        return false;
    }

    /**
     * 修改系统时间，有些设备修改不了，弃用
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @throws IOException
     * @throws InterruptedException
     */
    public static void setDateTime(int year, int month, int day, int hour,
                                   int minute, int second) throws IOException, InterruptedException {

        requestPermission();

        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);

        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (now - when > 1000)
            throw new IOException("failed to set Date.");

    }

    private static void setDate(int year, int month, int day)
            throws IOException, InterruptedException {

        requestPermission();

        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        // Log.d(TAG, "set tm="+when + ", now tm="+now);

        if (now - when > 1000)
            throw new IOException("failed to set Date.");
    }

    private static void setTime(int hour, int minute) throws IOException,
            InterruptedException {

        requestPermission();

        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            // 需要set_time权限
            SystemClock.setCurrentTimeMillis(when);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (now - when > 1000)
            throw new IOException("failed to set Time.");
    }

    /**
     * 申请Root权限
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private static void requestPermission() throws InterruptedException,
            IOException {
        createSuProcess("chmod 666 /dev/alarm").waitFor();
    }

    private static Process createSuProcess() throws IOException {
        File rootUser = new File("/system/xbin/ru");
        if (rootUser.exists()) {
            return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
        } else {
            return Runtime.getRuntime().exec("su");
        }
    }

    static Process createSuProcess(String cmd) throws IOException {

        DataOutputStream os = null;
        Process process = createSuProcess();
        try {
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }

        return process;
    }

    public static boolean isTimeZoneAuto(Context context) {
        try {
            return android.provider.Settings.Global.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME_ZONE) > 0;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setAutoTimeZone(Context context, boolean isAuto) {
        android.provider.Settings.Global.putInt(context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME_ZONE, (isAuto ? 1
                        : 0));
    }

    public static boolean isDateTimeAuto(Context context) {
        try {
            return android.provider.Settings.Global.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME) > 0;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setAutoDateTime(Context context, boolean isAuto) {
        android.provider.Settings.Global.putInt(context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME, (isAuto ? 1 : 0));
    }

    public static long formatDateStringTolong(String dateString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
        return date.getTime();
    }

    public static String formatlongToDate(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateString = sdf.format(time);
        return dateString;
    }


}
