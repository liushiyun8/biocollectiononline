package com.emptech.biocollectiononline.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    private UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE;
    private Context mContext;

    private Map<String, String> infos = new HashMap<String, String>();
    private static final String VERSION_NAME = "VersionName";
    private static final String VERSION_CODE = "VersionCode";
    private static final String CRASH_REPORTER_EXTENSION = ".txt";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    private ICallback callback;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void setOnExceptionCallback(ICallback cb) {
        callback = cb;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        if (callback != null)
            callback.callback(ex);
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "uncaughtException");
                LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "Application Crashed", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        collectCrashDeviceInfo(mContext);
        saveCrashInfoToFile(ex);
        //sendCrashReportsToServer(mContext);
        return true;
    }

    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(mContext);
    }

    private void sendCrashReportsToServer(Context ctx) {
        File dir = getCrashReportFiles(ctx);
        String[] crFiles = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(CRASH_REPORTER_EXTENSION);
            }
        });
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(crFiles));

            for (String fileName : sortedFiles) {
                File cr = new File(dir, fileName);
                postReport(cr);
                cr.delete();
            }
        }
    }

    private void postReport(File file) {
    }

    private File getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
            filesDir = new File(path);
        }
        return filesDir;
    }

    private boolean saveCrashInfoToFile(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = writer.toString();
        sb.append(result);
        printWriter.close();

        LogUtils.e("CRASH", sb.toString());

		long timestamp = System.currentTimeMillis();
        String fileName = "crash-" + sdf.format(timestamp) + CRASH_REPORTER_EXTENSION;
		FileOutputStream trace = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String path = AppConfig.WORK_PATH_CRASH;
				File crashDir = new File(path);
				if (!crashDir.exists()) {
					crashDir.mkdirs();
				}
				trace = new FileOutputStream(path +File.separator+fileName);
			} else {
				trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			}
			trace.write(sb.toString().getBytes());
			trace.flush();
			return true;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException");
		} catch (IOException e) {
			Log.e(TAG, "IOException");
		} finally {
			if (trace != null) {
				try {
					trace.close();
				} catch (IOException e) {
					Log.e(TAG, "IOException");
				}
			}
		}
        return false;
    }

    public void collectCrashDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                infos.put(VERSION_NAME, pi.versionName);
                infos.put(VERSION_CODE, String.valueOf(pi.versionCode));
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException");
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "Exception");
                LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            }
        }
    }
}