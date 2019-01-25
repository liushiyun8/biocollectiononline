package com.emptech.biocollectiononline.utils;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.util.Log;
import com.emptech.biocollectiononline.AppConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommUtils {

    static final String TAG = AppConfig.MODULE;

    private CommUtils() {
    }


    public static String excuteRootCmd(String script) {
        StringBuffer resBuffer = new StringBuffer();
        Runtime ex = Runtime.getRuntime();//取得当前JVM的运行时环境
        String cmdBecomeSu = "su";
        try {
            java.lang.Process runsum = ex.exec(cmdBecomeSu);//在单独的进程中执行指定的字符串命令
            int exitVal = 0;
            final OutputStreamWriter out = new OutputStreamWriter(runsum.getOutputStream());
            out.write(script);
            out.write("\n");
            out.flush();
            out.write("exit\n");
            out.flush();
            exitVal = runsum.waitFor();
            if (exitVal == 0) {
                LogUtils.d(AppConfig.MODULE, "Successfully to su");
                BufferedReader in = new BufferedReader(new InputStreamReader(runsum.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    LogUtils.d(AppConfig.MODULE, line);
                    resBuffer.append(line);
                }
            }
        } catch (Exception e) {
            LogUtils.e(AppConfig.MODULE, "Fails to su");
        }
        return resBuffer.toString();
    }

    public static String excuteCmd(String script) {
        StringBuffer resBuffer = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(script);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                LogUtils.d(AppConfig.MODULE, line);
                resBuffer.append(line);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return resBuffer.toString();
    }

    public static long convertTimestamp(String saveTime, String format) {
        //yyyy/MM/dd HH:mm"
        SimpleDateFormat df2 = new SimpleDateFormat(format);
        long timeStemp = 0;
        Date date;
        try {
            date = df2.parse(saveTime);
            timeStemp = date.getTime();
            return timeStemp;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE, e.toString());
        }
        return 0;

    }


    public static byte[] getContent(String filePath) {

        try {
            FileInputStream in = new FileInputStream(filePath);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            System.out.println("bytes available:" + in.available());

            byte[] temp = new byte[1024];
            int size = 0;
            while ((size = in.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            in.close();

            byte[] bytes = out.toByteArray();
            System.out.println("bytes size got is:" + bytes.length);

            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 执行具体的静默安装逻辑，需要手机ROOT。
     * @param apkPath 要安装的apk文件的路径
     * @return 安装成功返回true，安装失败返回false。
     */
    public static boolean silentInstall(String apkPath) {

        Log.i("info","apkPath: " + apkPath);

        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            //  申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            //  执行pm  install命令
            String command = "pm  install  -r  " + apkPath + "\n";
            //String command = "pm  install  -r  " + apkPath + "&& am start -a android.intent.action.MAIN -n com.emp.sztmachine/.activitys.MainActivity --ei isnew 1101\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            //  读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            LogUtils.d(AppConfig.MODULE, "Install  msg=  " + msg);
            Log.i("info","Install  msg=  " + msg);
            //  如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                result = true;
            }
        } catch (Exception e) {
            LogUtils.e(AppConfig.MODULE, e.getMessage());
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                LogUtils.e(AppConfig.MODULE, e.getMessage());
            }
        }
        return result;
    }

    public static boolean checkApk1(Context context, String apkPath) {
        File fileApk = new File(apkPath);
        if (!fileApk.exists() || !fileApk.isFile()) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo apkInfo = pm.getPackageArchiveInfo(fileApk.getPath(), PackageManager.GET_ACTIVITIES);
        if (apkInfo != null) {
            PackageInfo appInfo = null;
            try {
                appInfo = pm.getPackageInfo("com.emp.collectionStartup", 0);
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (apkInfo.applicationInfo.packageName.equals(appInfo.applicationInfo.packageName)) {
                // 比较安装文件包名和当前应用包名
                if (apkInfo.versionCode > appInfo.versionCode) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断手机是否拥有Root权限。
     *
     * @return 有root权限返回true，否则返回false。
     */
    public boolean isRoot() {
        boolean bool = false;
        try {
            bool = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * 正常方式安装升级APK
     *
     * @param context
     * @param apkPath
     * @return 安装成功返回 true, 失败返回 false
     */
    public static boolean updateAPK(Activity context, String apkPath) {
        File fileApk = new File(apkPath);
        if (!fileApk.exists()) {
            LogUtils.d(AppConfig.MODULE, "升级文件：" + fileApk.getName() + " 不存在");
            return false;
        }
        boolean canUpdate = false;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(fileApk.getPath(), PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            String packageName = appInfo.packageName; // 得到安装包名称
            String appName = "";// 当前应用包名
            try {
                appName = pm.getPackageInfo(context.getPackageName(), 0).packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // if (packageName.equals(appName))
            {
                // 比较安装文件包名和当前应用包名
                canUpdate = true;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                //context.startActivityForResult(intent, 360);
            }
        }
        return canUpdate;
    }



//    public static String findOTGUSB(Context context) {
//
//        StorageManager mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
//
//        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
//        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//
//        for (VolumeInfo vol : volumes) {
//            if(vol.isUSBOTG()) {
//                final File path = vol.getPath();
//                mUpgradeLog1Preference.setSummary(path.toString());
//            }
//        }
//
//    }

    public static String getVersion(Context mContext,String packageName){
        String appVersion = "";// 当前应用包名
        PackageManager pm=mContext.getPackageManager();
        try {
            appVersion = pm.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return appVersion;
    }

    public static String getAppVersion(Context mContext){
        String appVersion = "";// 当前应用包名
        PackageManager pm=mContext.getPackageManager();
        try {
            appVersion = pm.getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return appVersion;
    }

    public static String[] getDiskPath(Context context) {
        String[] result = null;
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            try {
                result =(String[])method.invoke(storageManager);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            LogUtils.d(AppConfig.MODULE,  "getDiskPath()=");
            for (int i = 0; i < result.length; i++) {
                LogUtils.d(AppConfig.MODULE,  result[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 验证安装包是否合法,版本是否高于当前版本.
     *
     * @param context
     * @param apkPath
     * @return 合法返回true，否则返回false。
     */
    public static boolean checkApk(Context context, String apkPath) {
        File fileApk = new File(apkPath);
        if (!fileApk.exists() || !fileApk.isFile()) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo apkInfo = pm.getPackageArchiveInfo(fileApk.getPath(), PackageManager.GET_ACTIVITIES);
        if (apkInfo != null) {
            PackageInfo appInfo = null;
            try {
                appInfo = pm.getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (apkInfo.applicationInfo.packageName.equals(appInfo.applicationInfo.packageName)) {
                // 比较安装文件包名和当前应用包名
                if (apkInfo.versionCode > appInfo.versionCode) {
                    return true;
                }
            }
        }
        return false;
    }


    public static File[] searchFile(File folder, final String keyWord) {//  递归查找包含关键字的文件

        File[] subFolders = folder.listFiles(new FileFilter() {//  运用内部匿名类获得文件
            @Override
            public boolean accept(File pathname) {//  实现FileFilter类的accept方法
               // LogUtils.d(AppConfig.MODULE, "searchFile=" + pathname);
                if (pathname.isDirectory()
                        || (pathname.isFile() && pathname.getName().toLowerCase().contains(keyWord.toLowerCase())))//  目录或文件包含关键字
                    return true;
                return false;
            }
        });

        List<File> result = new ArrayList<File>();//  声明一个集合

        if(subFolders != null) {
            for (int i = 0; i < subFolders.length; i++) {//  循环显示文件夹或文件
                if (subFolders[i].isFile()) {//  如果是文件则将文件添加到结果列表中
                    result.add(subFolders[i]);
                } else {//  如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
                    File[] foldResult = searchFile(subFolders[i], keyWord);
                    for (int j = 0; j < foldResult.length; j++) {//  循环显示文件
                        result.add(foldResult[j]);//  文件保存到集合中
                    }
                }
            }
        }

        File files[] = new File[result.size()];//  声明文件数组，长度为集合的长度
        result.toArray(files);//  集合数组化
        return files;
    }


    public static String[] searcDir(String folder, final String keyWord) {//  递归查找包含关键字的文件

        LogUtils.d(AppConfig.MODULE, "searcDir folder = " + folder);
        String[] paths = new String[0];

        if(folder != null) {
            File[] files = searchFile(new File(folder), keyWord);
            paths = new String[files.length];
            for (int j = 0; j < files.length; j++) {
                paths[j] = files[j].getAbsolutePath();
            }
        }
        return paths;
    }


    public static File[] listFiles(File folder, final String keyWord) {
        if(!folder.exists()) {
            LogUtils.d(AppConfig.MODULE, "folder " + folder + " not exist");
            return null;
        }
        File[] subFiles = folder.listFiles(new FileFilter() {//  运用内部匿名类获得文件
            @Override
            public boolean accept(File pathname) {//  实现FileFilter类的accept方法
                //LogUtils.d(AppConfig.MODULE, "listFiles=" + pathname);
                if (pathname.isFile()) {//  目录或文件包含关键字
                    if (keyWord != null) {
                        if (pathname.getName().toLowerCase().contains(keyWord.toLowerCase()))
                            return true;
                    } else
                        return true;
                }
                return false;
            }
        });
        return subFiles;
    }

    public static String[] listFiles(String[] folders, final String keyWord) {

        ArrayList<String> filesList = new ArrayList<>();
        for (String folder : folders) {
            File[] subFiles = listFiles(new File(folder), keyWord);
            if (subFiles != null) {
                for (File file : subFiles) {
                    filesList.add(file.getAbsolutePath());
                }
            }
        }
        String[] files = new String[filesList.size()];
        filesList.toArray(files);
        return files;
    }

    public static void writeTxtFile(String strcontent, String filePath) throws IOException {
        String strContent = strcontent + "\r\n";
        RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
        //raf.seek(raf.length());
        raf.write(strContent.getBytes());
        raf.close();
    }


    public static String readTxtLine(String filePath) {
        String first = "";
        try {
            FileReader reader = new FileReader(filePath);
            BufferedReader br = new BufferedReader(reader);

            first = br.readLine();
            br.close();
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return first;

    }

    public static boolean isProcessRunning(Context context, String proessName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(proessName)) {
                LogUtils.d(AppConfig.MODULE, "isProcessRunning proessName=" + info.processName);
                isRunning = true;
            }
        }

        return isRunning;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> lists = am.getRunningServices(30);

        for (ActivityManager.RunningServiceInfo info : lists) {//判断服务
            if (info.service.getClassName().equals(serviceName)) {
                LogUtils.d(AppConfig.MODULE, "isServiceRunning serviceName=" + info.service.getClassName());
                isRunning = true;
            }
        }

        return isRunning;
    }

    /**
     * 获取前几分钟时间点
     *
     * @param min 前几分钟
     * @return 时间点
     */
    public static long getMinute(int min) {
        Calendar c = Calendar.getInstance();

        //过去一月
        c.setTime(new Date());
        c.add(Calendar.MINUTE, min);

        return c.getTime().getTime();
    }

    /**
     * 获取前几个小时时间点
     *
     * @param hour 前几个小时
     * @return 时间点
     */
    public static long getHour(int hour) {
        Calendar c = Calendar.getInstance();

        //过去一月
        c.setTime(new Date());
        c.add(Calendar.HOUR, hour);

        return c.getTime().getTime();
    }

    /**
     * 获取前几天时间点
     *
     * @param day 前几天
     * @return 时间点
     */
    public static long getDay(int day) {
        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.add(Calendar.DATE, day);

        return c.getTime().getTime();
    }

    /**
     * 获取前几个月时间点
     *
     * @param month 前几个月
     * @return 时间点
     */
    public static long getMonth(int month) {
        Calendar c = Calendar.getInstance();

        //过去一月
        c.setTime(new Date());
        c.add(Calendar.MONTH, month);

        return c.getTime().getTime();
    }

    /**
     * 删除文件夹中某个时间点之前的文件
     *
     * @param dirPath 文件夹路径
     * @param time    时间点
     * @return
     */
    public static boolean removeFilesByTime(String dirPath, final long time) {

        File folder = new File(dirPath);
        File[] subFiles = folder.listFiles(new FileFilter() {//  运用内部匿名类获得文件
            @Override
            public boolean accept(File f) {//  实现FileFilter类的accept方法
                if (f.isFile()) {
                    long t = f.lastModified();
                    if (t < time)
                        return true;
                }
                return false;
            }
        });

        //删除旧文件
        if (subFiles != null) {
            for (File f : subFiles) {
                f.delete();
            }
            return true;
        }
        return false;
    }

/*
    public static AlertDialog showAlertDlg(Context context, String title, String content, String confirm, String cancle,
                                           final View.OnClickListener confirmListener,
                                           final View.OnClickListener cancleListener) {
        AlertDialog.Builder builer = new AlertDialog.Builder(context);

        final AlertDialog dialog = builer.create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.theme_dialog_text);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        TextView dialog_title = (TextView) window.findViewById(R.id.text_dialog_title);
        TextView dialog_tv = (TextView) window.findViewById(R.id.text_dialog_tv);
        Button dialog_bt_ok = (Button) window.findViewById(R.id.text_dialog_button_ok);
        Button dialog_bt_cancel = (Button) window.findViewById(R.id.text_dialog_button_cancel);

        if (title != null)
            dialog_title.setText(title);
        if (content != null)
            dialog_tv.setText(content);
        if (confirm != null) {
            dialog_bt_ok.setText(confirm);
            dialog_bt_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if (confirmListener != null)
                        confirmListener.onClick(v);
                }
            });
        }
        if (cancle != null) {
            dialog_bt_cancel.setText(cancle);
            dialog_bt_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if (cancleListener != null)
                        cancleListener.onClick(v);
                }
            });
        }
        return dialog;
    }*/


    public static boolean copyFile(String srcFile, String dstFile) {
        try {

            InputStream is = new FileInputStream(srcFile);

            FileOutputStream fs = new FileOutputStream(dstFile);
            byte[] buffer = new byte[1024];
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = is.read(buffer)) != -1) {
                bytesum += byteread; // 字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
            is.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LogUtils.d(TAG, "copyFile errmsg=" + e.getMessage());
            return false;
        }
        return true;
    }

}