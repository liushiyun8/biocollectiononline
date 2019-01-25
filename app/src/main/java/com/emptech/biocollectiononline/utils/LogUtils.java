package com.emptech.biocollectiononline.utils;

import android.text.TextUtils;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.common.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

public class LogUtils {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static int LEVEL = AppConfig.isDebug ? VERBOSE : ERROR;
    public static final String SEPARATOR = ",";

    public static boolean LOG2FILE = !AppConfig.isDebug;
    private static String LOGDIR=AppConfig.WORK_PATH_LOG;

    private static class LogInfo {
        public int index;
        public RandomAccessFile raf;
    }

    public static ConcurrentHashMap<String, LogInfo> logHashMap = new ConcurrentHashMap<>();

    private static int MAX_FILE_SIZE = 1024 * 512;

    public static void logConfigure(String logDir) {
        LOGDIR = logDir;
        File mkFile = new File(LOGDIR);
        if (!mkFile.exists())
            Log.v(AppConfig.MODULE_APP, "创建Log路径：" + mkFile.mkdirs());
    }

    public static void writeTxtToFile(String strcontent, RandomAccessFile raf) {

        if (raf != null) {
            String strContent = strcontent + "\r\n";
            try {
                raf.seek(raf.length());
                raf.write(strContent.getBytes());
                raf.close();
            } catch (Exception e) {
                String errString = "Error on write File:" + e;
                Log.e("TestFile", "Error on write File:" + e);
                writeTxtToFile(errString + "\r\n", LOGDIR + "/logutils.txt");
            }
        }
    }

    public static void writeTxtToFile(String strcontent, String filePath) {
        String strContent = strcontent + "\r\n";

        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
            raf.seek(raf.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile2", "Error on write File:" + e);
        }
    }

    // tag请传模块名message打印消息
    public static void log2File(String tag, String message) {
        LogInfo logInfo = logHashMap.get(tag);

        if (logInfo == null) {
            logInfo = new LogInfo();
            logHashMap.put(tag, logInfo);
        }

        synchronized (logInfo) {

            try {
//                if(logInfo.raf!=null)
//                Log.e("raf","raf:"+logInfo.raf.getFD().valid());
                if (logInfo.raf != null &&logInfo.raf.getFD().valid()&&logInfo.raf.length() < MAX_FILE_SIZE) {
                } else {
                    int index = 0;
                    String filepath = LOGDIR + "/" + tag;
                    File logFile = new File(filepath);
                    if (!logFile.exists())
                        logFile.mkdirs();
                    do {
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
                        String date = sDateFormat.format(new java.util.Date());
                        filepath = LOGDIR + "/" + tag + "/" + tag + "_" + date + "_" + index + ".log";
                        logFile = new File(filepath);
                        index++;
                    } while (logFile.exists() && logFile.length() > MAX_FILE_SIZE);
                    RandomAccessFile raf = new RandomAccessFile(filepath, "rwd");
                    if (logInfo.raf != null&&logInfo.raf.getFD().valid())
                            logInfo.raf.close();
                    logInfo.index = index;
                    logInfo.raf = raf;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                writeTxtToFile(e.toString() + "\r\n", LOGDIR + "/logutils.txt");
            } catch (IOException e) {
                e.printStackTrace();
                writeTxtToFile(e.toString() + "\r\n", LOGDIR + "/logutils.txt");
            }

            if (logInfo.raf != null) {
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                String time = sDateFormat.format(new java.util.Date());
                writeTxtToFile("[" + time + "]" + "[" + tag + "]" + "["+ Utils.getVersionName(App.get())+"]"+message, logInfo.raf);
            }
        }

    }

    // tag请传模块名message打印消息
    public static void v(String tag, String message) {
        try {
            if (LEVEL <= VERBOSE) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                StackTraceElement stackTraceElement4 = Thread.currentThread().getStackTrace()[4];
                if (TextUtils.isEmpty(tag)) {
                    tag = getDefaultTag(stackTraceElement);
                }
                if (LOG2FILE)
                    if (stackTraceElement4 != null) {
                        log2File(tag, getLogInfo(stackTraceElement) + getLogInfo(stackTraceElement4) + message);
                    } else {
                        log2File(tag, getLogInfo(stackTraceElement) + message);
                    }
                else {
                    Log.v(tag, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tag请传模块名message打印消息
    public static void d(String tag, String message) {
        try {
            if (LEVEL <= DEBUG) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                StackTraceElement stackTraceElement4 = Thread.currentThread().getStackTrace()[4];
                if (TextUtils.isEmpty(tag)) {
                    tag = getDefaultTag(stackTraceElement);
                }
                if (LOG2FILE)
                    if (stackTraceElement4 != null) {
                        log2File(tag, getLogInfo(stackTraceElement) + getLogInfo(stackTraceElement4) + message);
                    } else {
                        log2File(tag, getLogInfo(stackTraceElement) + message);
                    }
                else {
                    Log.d(tag, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tag请传模块名message打印消息
    public static void i(String tag, String message) {
        try {
            if (LEVEL <= INFO) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                StackTraceElement stackTraceElement4 = Thread.currentThread().getStackTrace()[4];
                if (TextUtils.isEmpty(tag)) {
                    tag = getDefaultTag(stackTraceElement);
                }
                if (LOG2FILE)
                    if (stackTraceElement4 != null) {
                        log2File(tag, getLogInfo(stackTraceElement) + getLogInfo(stackTraceElement4) + message);
                    } else {
                        log2File(tag, getLogInfo(stackTraceElement) + message);
                    }
                else {
                    Log.i(tag, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tag请传模块名message打印消息
    public static void w(String tag, String message) {
        try {
            if (LEVEL <= WARN) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                StackTraceElement stackTraceElement4 = Thread.currentThread().getStackTrace()[4];
                if (TextUtils.isEmpty(tag)) {
                    tag = getDefaultTag(stackTraceElement);
                }
                if (LOG2FILE)
                    if (stackTraceElement4 != null) {
                        log2File(tag, getLogInfo(stackTraceElement) + getLogInfo(stackTraceElement4) + message);
                    } else {
                        log2File(tag, getLogInfo(stackTraceElement) + message);
                    }
                else {
                    Log.w(tag, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tag请传模块名message打印消息
    public static void e(String tag, String message) {
        try {
            if (LEVEL <= ERROR) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                StackTraceElement stackTraceElement=null;
                StackTraceElement stackTraceElement4=null;
                if(stackTrace.length>0){
                    stackTraceElement = stackTrace[0];
                    stackTraceElement4 = Thread.currentThread().getStackTrace()[stackTrace.length-1];
                }
                if (TextUtils.isEmpty(tag)) {
                    tag = getDefaultTag(stackTraceElement);
                }
                if (LOG2FILE)
                    if (stackTraceElement4 != null) {
                        log2File(tag, getLogInfo(stackTraceElement) + getLogInfo(stackTraceElement4) + message);
                    } else {
                        log2File(tag, stackTraceElement!=null?getLogInfo(stackTraceElement):"" + message);
                    }
                else {
                    Log.e(tag, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDefaultTag(StackTraceElement stackTraceElement) {

        return LogUtils.class.getSimpleName();
    }

    public static String getLogInfo(StackTraceElement stackTraceElement) {
        StringBuilder logInfoStringBuilder = new StringBuilder();
        String threadName = Thread.currentThread().getName();
        long threadID = Thread.currentThread().getId();
        String fileName = stackTraceElement.getFileName();
        String className = stackTraceElement.getClassName();
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();

        logInfoStringBuilder.append("[");
        logInfoStringBuilder.append("" + threadID).append(SEPARATOR);
        logInfoStringBuilder.append("" + threadName).append(SEPARATOR);
        logInfoStringBuilder.append("" + fileName).append(SEPARATOR);
        logInfoStringBuilder.append("" + className).append(SEPARATOR);
        logInfoStringBuilder.append("" + methodName).append(SEPARATOR);
        logInfoStringBuilder.append("" + lineNumber);
        logInfoStringBuilder.append("] ");
        return logInfoStringBuilder.toString();
    }


}