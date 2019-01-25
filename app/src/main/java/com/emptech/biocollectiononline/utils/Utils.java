package com.emptech.biocollectiononline.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emptech.biocollectiononline.AppConfig;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class Utils {

    private static float density;
    private static int WIDTH;
    private static int HEIGHT;
    private static int DPI;

    public static int dip2px(Context context, float dpValue) {
        final float scale = getDensity(context);
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = getDensity(context);
        return (int) (pxValue / scale + 0.5f);
    }

    public static Dialog createImageDlg(Context context, int resId) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ImageView image = new ImageView(dialog.getContext());
        image.setImageResource(resId);
        dialog.setContentView(image);
        dialog.show();
        return dialog;
    }

    public static Dialog createDialog(Context context, View view) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(view);
        return dialog;
    }

    public static int getAsc(String st) {
        byte[] gc = st.getBytes();
        return (int) gc[0];
    }

    public static String asc2String(int asc) {
        return String.valueOf((char) asc);
    }

    public static boolean checkTextView(TextView textView, String error) {
        if (textView == null)
            return false;

        textView.setError(null);
        if (TextUtils.isEmpty(textView.getText().toString())) {
            textView.setError(error);
            return false;
        }
        return true;
    }

    public static Bitmap getBitmap(final String path, BitmapFactory.Options opt) {
        File file = new File(path);
        if (file.exists()) {
            Bitmap bmp = null;
            bmp = BitmapFactory.decodeFile(path, opt);
            return bmp;
        }
        return null;
    }

    public static Bitmap getDefaultBitmap(final String path, int inSampleSize) {
        BitmapFactory.Options opt = null;
        opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inSampleSize = inSampleSize;
        Bitmap bitmap = Utils.getBitmap(path, opt);
        return bitmap;
    }

    public static boolean saveBitmap(Bitmap bm, String path) {
        if (bm == null || path == null)
            return false;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            boolean result = bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }

        return false;
    }

    public static BitmapFactory.Options getBitmapOptions(String path) {
        File file = new File(path);
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            return options;
        }
        return null;
    }

    public static String bitmap2Base64(Bitmap bitmap) {
        String result = "";
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            byte[] encode = out.toByteArray();
            result = Base64.encodeToString(encode, Base64.DEFAULT);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }
        return null;
    }

    public static Bitmap base64ToBitmap(String base64) {
        byte[] byteIcon = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteIcon, 0, byteIcon.length);
        return bitmap;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap drawable2ARGBBMP(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap Bmp2ARGB(Bitmap bmp) {
        if (bmp != null)
            return bmp.copy(Bitmap.Config.ARGB_8888, false);
        return null;
    }

    public static Bitmap Bmp2RGB565(Bitmap bmp) {
        if (bmp != null)
            return bmp.copy(Bitmap.Config.RGB_565, false);
        return null;
    }

    public static Drawable bitmap2Drawable(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static Bitmap getBitmap(Context context, final int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    public static Bitmap getScaleBitmap(Context context, int redId, float rate) {
        Bitmap bmp1 = BitmapFactory.decodeResource(context.getResources(), redId);
        int width = (int) (bmp1.getWidth() * rate);
        int height = (int) (bmp1.getHeight() * rate);
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp1, width, height, false);
        return bmp2;
    }

    void startFrameAnim(ImageView imageview, Drawable drawable[]) {
        AnimationDrawable anim = new AnimationDrawable();
        anim.setOneShot(false);
        imageview.setImageDrawable(anim);
        anim.start();
    }

    public static void showCustomDialog(Context context, int res_id) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(res_id);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    public static View getRootView(Activity context) {
        return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
    }

    public static void showMsg(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getBeforeDate(Date date, int days, String daytype) {
        SimpleDateFormat df = new SimpleDateFormat(daytype);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - days);
        return df.format(calendar.getTime());
    }

    public static Date getBeforeDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -days);
        return calendar.getTime();
    }

    public static String formatDate(Date date, String daytype) {
        return new SimpleDateFormat(daytype).format(date);
    }

    public static String getAfterDate(Date date, int days, String daytype) {
        SimpleDateFormat df = new SimpleDateFormat(daytype);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + days);
        return df.format(calendar.getTime());
    }

    public static String getAfterHour(Date date, int hours, String dayType) {
        SimpleDateFormat df = new SimpleDateFormat(dayType);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hours);
        return df.format(calendar.getTime());
    }

    public static Date getAfterDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static String getDate(int timeticks, String datatype) {
        SimpleDateFormat sdf = new SimpleDateFormat("datatype");
        String date = sdf.format(new Date(timeticks * 1000));
        return date;
    }

    public static Date getDate(long timeticks) {
        return new Date(timeticks * 1000);
    }

    public static int getWeekofDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return w;
    }

    public static String getWeekNameofDate(Date date) {
        String weekDays[] = {"Mon", "Tues", "Wendes", "Thurs", "Fri", "Satur", "Sun",};
        int week = getWeekofDate(date);
        return weekDays[week];
    }

    public static float getDensity(Context ctx) {
        if (density == 0) {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            DPI = dm.densityDpi;
            WIDTH = dm.widthPixels;
            HEIGHT = dm.heightPixels;
            density = dm.density;
        }
        return density;
    }

    public static int getWIDTH(Context ctx) {
        if (WIDTH == 0) {
            getDensity(ctx);
        }
        return WIDTH;
    }

    public static int getHEIGHT(Context ctx) {
        if (HEIGHT == 0) {
            getDensity(ctx);
        }
        return HEIGHT;
    }

    public static int getDPI(Context ctx) {
        if (DPI == 0) {
            getDensity(ctx);
        }
        return DPI;
    }

    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }
        return date;
    }

    public static Date parseDate2(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }
        return date;
    }

    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }
        return size;
    }

    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            }
        }
    }

    public static String getVersionName(Context context) {
        String verion = "V1.0.0";
        if (context == null)
            return verion;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            return verion;
        }
    }

    public static void enableUnderlined(TextView v) {
        v.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        v.getPaint().setAntiAlias(true);
    }

    public static void runTask(Runnable runnable, long delayMillis) {
        new Handler().postDelayed(runnable, delayMillis);
    }

    public static void excuteRootCmd(String script) throws IOException, InterruptedException {
        Runtime ex = Runtime.getRuntime();
        String cmdBecomeSu = "su";
        Process runsum = null;
        try {
            runsum = ex.exec(cmdBecomeSu);
            int exitVal = 0;
            final OutputStreamWriter out = new OutputStreamWriter(runsum.getOutputStream());
            // Write the script to be executed
            out.write(script);
            // Ensure that the last character is an "enter"
            out.write("\n");
            out.flush();
            // Terminate the "su" process
            out.write("exit\n");
            out.flush();
            exitVal = runsum.waitFor();
            if (exitVal == 0) {
                Log.e("Debug", "Successfully to su");
            }
        } catch (Exception e) {
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            Log.e("Debug", "Fails to su");
        } finally {
            if (runsum != null) {
                runsum.destroy();
            }
        }
    }

    public static void execCommand(String command) throws IOException {
        // start the ls command running
        // String[] args = new String[]{"sh", "-c", command};
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command); // 这句话就是shell与高级语言间的调用
        // 如果有参数的话可以用另外一个被重载的exec方法
        // 实际上这样执行时启动了一个子进程,它没有父进程的控制台
        // 也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            // System.out.println(line);
            sb.append(line);
            sb.append('\n');
        }
        // tv.setText(sb.toString());
        // 使用exec执行不会等执行成功以后才返回,它会立即返回
        // 所以在某些情况下是很要命的(比如复制文件的时候)
        // 使用wairFor()可以等待命令执行完成以后才返回
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
            e.printStackTrace();
        }
    }

    static public void excuteShell(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                Log.i("exeShell", line);
            }

        } catch (Throwable t) {
            t.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, t.toString());
        }
    }

    static public int getCurrentVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    static public String getForGroundActivity(Context context) {
        // ActivityManager activityManager = (ActivityManager)
        // context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        // List<RunningTaskInfo> forGroundActivity =
        // activityManager.getRunningTasks(1);
        // RunningTaskInfo currentActivity;
        // currentActivity = forGroundActivity.get(0);
        // String activityName = currentActivity.topActivity.getClassName();
        // return activityName;
        return null;
    }

    static public void hideAllSystemBar(Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        // params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        window.setAttributes(params);
    }

    static public String getFilesDir(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }

    static public String getPackagePath(Context context) {
        return context.getApplicationContext().getPackageResourcePath();
    }

    static public String getDatabasePath(Context context, String dbname) {
        return context.getApplicationContext().getDatabasePath(dbname).getAbsolutePath();
    }

    static public AlertDialog.Builder newAlertDlg(Context context) {
        return new AlertDialog.Builder(context);
    }

    static public void hideSoftInput(Activity context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    static public void createFileWithByte(byte[] bytes, String dir, String fName) {
        File file = new File(dir, fName);
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    LogUtils.e(AppConfig.MODULE_CATCHERR, e2.toString());
                }
            }
        }
    }

    /**
     * 判断某个Activity 界面是否在前台
     * @param context
     * @param className 某个界面名称
     * @return
     */
    public static boolean  isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;

    }

    static public void removeFile(String fName) {
        new File(fName).delete();
    }

    static public void keepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    static public void mkDirs(String dirPath) {
        File mkFile = new File(dirPath);
        if (!mkFile.exists()) {
            Log.e(AppConfig.MODULE_APP, "文件不存在，创建：" + dirPath);
            if (!mkFile.mkdirs()) {
                Log.e(AppConfig.MODULE_APP, "创建文件夹失败");
            }
        }
    }

    static public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    static public long getAppMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @SuppressLint("MissingPermission")
    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       String imei = telephonyManager.getDeviceId();
        return imei;
    }

    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }


    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_CATCHERR, e.toString());
        }

        return pi;
    }
}
