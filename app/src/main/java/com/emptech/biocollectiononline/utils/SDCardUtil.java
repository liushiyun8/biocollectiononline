package com.emptech.biocollectiononline.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SD卡相关的辅助类
 */
public class SDCardUtil {
    private SDCardUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     *
     * @return
     */
    public static long getSDCardAllSize() {
        if (isSDCardEnable()) {
            StatFs stat = new StatFs(getSDCardPath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getBlockSize();
            // 获取单个数据块的大小（byte）

            long freeBlocks = stat.getBlockCount();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static long getSDAvailableSize() {
        try {
            return getFreeBytes(getSDCardPath());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    public static long getRomTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount() * blockSize;
        return totalBlocks;
    }

    /**
     * 获得机身内存总大小
     * author xbp
     *
     * @return
     */
    @SuppressLint("NewApi")
    public static long getAllRomTotalSize() {
        Process process = null;
        try {
            Runtime.getRuntime().exec("su root");
            process = Runtime.getRuntime().exec("df");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (process == null) {
            return 0;
        }
        Pattern pattern = Pattern.compile("^((\\/)(\\w+))*");
        ArrayList<String> list = new ArrayList<>();
        InputStreamReader is = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(is);
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                if (line.contains("usb")) {
                    continue;
                }
                if (line.contains("external_sd")) {
                    continue;
                }
                Log.e("xubaipei", "list:" + line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find() && !matcher.group().equals("")) {
                    list.add(matcher.group());
                }
            }
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Long> sizeList = new ArrayList<>();
        for (String path : list) {
            StatFs stat = null;
            try {
                stat = new StatFs(path);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stat == null) {
                    continue;
                }
            }
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong() * blockSize;
            for (Long size : sizeList) {
                if (size == totalBlocks) {
                    sizeList.remove(totalBlocks);
                }
            }
            sizeList.add(totalBlocks);
        }
        long totalSize = 0;
        for (Long size : sizeList) {
            Log.e("xubaipei", "list:" + size);
            totalSize += size;
        }
        return totalSize;
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    public static long getRomAvailableSize() {
        try {
            return getFreeBytes(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    @SuppressLint("NewApi")
    public static long getAllRomAvaliableSize() {
        Process process = null;
        try {
            Runtime.getRuntime().exec("su root");
            process = Runtime.getRuntime().exec("df");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (process == null) {
            return 0;
        }
        Pattern pattern = Pattern.compile("^((\\/)(\\w+))*");
        ArrayList<String> list = new ArrayList<>();
        InputStreamReader is = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(is);
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                Log.e("xubaipei", "list:" + line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find() && !matcher.group().equals("")) {
                    list.add(matcher.group());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Long> sizeList = new ArrayList<>();
        ArrayList<String> avaliablePath = new ArrayList<>();
        for (String path : list) {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong() * blockSize;
            for (Long size : sizeList) {
                if (size == totalBlocks) {
                    sizeList.remove(totalBlocks);
                    avaliablePath.remove(path);
                }
            }
            avaliablePath.add(path);
            sizeList.add(totalBlocks);
        }
        sizeList.clear();
        for (String path : avaliablePath) {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getAvailableBlocksLong() * blockSize;
            for (Long size : sizeList) {
                if (size == totalBlocks) {
                    sizeList.remove(totalBlocks);
                }
            }
            sizeList.add(totalBlocks);
        }

        long totalSize = 0;
        for (Long size : sizeList) {
            Log.e("xubaipei", "list:" + size);
            totalSize += size;
        }
        return totalSize;
    }

//	public static long getSystemAvaialbeSize(){
//		
//	}

    /**
     * 获取指定路径所在空间的剩余可用容量字节数，单位byte
     *
     * @param filePath
     */
    public static long getFreeBytes(String filePath) throws Exception {
        // 如果是sd卡的下的路径，则获取sd卡可用容量
        StatFs stat = new StatFs(filePath);
        long availableBlocks = (long) stat.getAvailableBlocks();
        availableBlocks = stat.getBlockSize() * availableBlocks;
        return availableBlocks;
    }
    @SuppressLint("NewApi")
    public static long getTotalBytes(String path) throws Exception {
        StatFs stat = new StatFs(path);
        long size = stat.getTotalBytes();
        return size;
    }

    /**
     * 判定U盘是否可以写入数据；
     *
     * @return true 内存充足
     */
    public static boolean isSDLowMemory(String usbPath) {
        if (usbPath != null) {
            try {
                if (getFreeBytes(usbPath) > 1024 * 1024) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 获取系统存储路径
     *
     * @return
     */
    public static String getRootDirectoryPath() {
        return Environment.getRootDirectory().getAbsolutePath();
    }


    /**
     * 申请su权限
     *
     * @return
     * @throws IOException
     */
    public static Process createSuProcess() throws IOException {
        File rootUser = new File("/system/xbin/ru");
        if (rootUser.exists()) {
            return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
        } else {
            return Runtime.getRuntime().exec("su");
        }
    }

    /**
     * 格式化SD卡
     *
     * @return
     */
    public static boolean wipingSdcard() {
        File deleteMatchingFile = new File(getSDCardPath());
        try {
            File[] filenames = deleteMatchingFile.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                deleteMatchingFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 格式化文件夹
     *
     * @param name
     */
    private static void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();
        try {
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());

                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                directoryFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取外置SD卡路径
     *
     * @return map<path,canWrite>
     */
    public static List<Map<String, Object>> getHandMachineCanWriteExtSDCardPath() {
        List<Map<String, Object>> lResult = new ArrayList<Map<String, Object>>();
        Process proc = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec("ls storage");
            is = proc.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                String path = "/storage/" + line;
                File file = new File(path);
                if (file.isDirectory()) {
                    LogUtils.v(AppConfig.MODULE_APP, "获取路径：" + path);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("path", path);
                    lResult.add(map);
                }
            }

//            String usbDeviceName = br.readLine();
//            String path = "/storage/" + usbDeviceName;
//            File file = new File(path);
//            if (file.isDirectory()) {
//                LogUtils.v(AppConfig.MODULE_APP, "获取路径：" + path);
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put("path", path);
//                lResult.add(map);
//            }
//            LogUtils.v(AppConfig.MODULE_APP, "获取路径：" + path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        processDestroy(proc);
        return lResult;
    }

    /**
     * 获取外置SD卡路径
     *
     * @return map<path,canWrite>
     */
    public static List<Map<String, Object>> getCanWriteExtSDCardPath() {
        List<Map<String, Object>> lResult = new ArrayList<Map<String, Object>>();
        Process proc = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec("mount");
            is = proc.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/mnt/usb_storage/")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        LogUtils.v(AppConfig.MODULE_APP, "获取路径：" + path);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("path", path);
                        String canWrite = arr[3];
                        map.put("rw", canWrite.startsWith("rw"));
                        lResult.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        processDestroy(proc);
        return lResult;
    }

    public static StorageManager getStorageManager(Context cxt) {
        StorageManager sm = (StorageManager)
                cxt.getSystemService(Context.STORAGE_SERVICE);
        return sm;
    }

    public static long getExtSDANDTFToTalSize() {
        String[] paths = new String[]{
                "/mnt/external_sd",
                "/mnt/usb_storage/USB_DISK0/udisk0"};
        List<Map<String, Object>> list = getCanWriteExtSDCardPath();
        ArrayList<String> pathsList = new ArrayList<>();
        long size = 0;
        for (String path : paths) {
            try {
                size += getTotalBytes(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public static long getExtSDANDTFAvaliableSize() {
        String[] paths = new String[]{
                "/mnt/external_sd",
                "/mnt/usb_storage/USB_DISK0/udisk0"};
        long size = 0;
        for (String path : paths) {
            try {
                size += getFreeBytes(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public static String[] getVolumePaths(Context cxt) {
        if (!isSupportApi()) {
            return null;
        }
        StorageManager sm = getStorageManager(cxt);
        if (sm == null) {
            return null;
        }
        try {
            Class<?>[] argTypes = new Class[0];
            Method method_getVolumeList =
                    StorageManager.class.getMethod("getVolumePaths", argTypes);
            Object[] args = new Object[0];
            Object array = method_getVolumeList.invoke(sm, args);
            int arrLength = Array.getLength(array);
            String[] paths = new
                    String[arrLength];
            for (int i = 0; i < arrLength; i++) {
                String path = (String) Array.get(array, i);
                paths[i] = path;
            }
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * see if SDK version of current device is greater
     * than 14 (IceCreamSandwich, 4.0).
     */
    private static boolean isSupportApi() {
        int osVersion = android.os.Build.VERSION.SDK_INT;
        boolean avail = osVersion >= 14;
        return avail;
    }


    public static List<String> getExtSDCardPaths() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            paths.add(extFile.getAbsolutePath());
        }
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 获取进程的ID
     *
     * @param process 进程
     * @return
     */
    private static int getProcessId(Process process) {
        String str = process.toString();
        try {
            int i = str.indexOf("=") + 1;
            int j = str.indexOf("]");
            str = str.substring(i, j);
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 通过Android底层实现进程关闭
     *
     * @param process 进程
     */
    private static void killProcess(Process process) {
        int pid = getProcessId(process);
        if (pid != 0) {
            try {
                // android kill process
                android.os.Process.killProcess(pid);
            } catch (Exception e) {
                try {
                    process.destroy();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * 销毁进程
     *
     * @param process 进程
     */
    @SuppressLint("NewApi")
    private static void processDestroy(Process process) {
        if (process != null) {
            process.destroy();
        }
//		if (process != null) {
//			try {
//				// 判断是否正常退出
//				process.waitFor();
//				if (process.exitValue() != 0) {
//					killProcess(process);
//				}
//			} catch (IllegalThreadStateException e) {
//        killProcess(process);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
    }
}
