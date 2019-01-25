package com.emptech.biocollectiononline.utils;

import android.util.Log;


import com.emptech.biocollectiononline.AppConfig;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class FileUtil {
    private final static String TAG = AppConfig.MODULE_APP + "_FILE";

    /**
     * 判断目录是否存在，不存在则创建
     */
    public static boolean isDirExist(String dir) {
        File file = new File(dir);
        boolean mk = true;
        if (!file.exists()) {
            mk = file.mkdirs();
        }
        return mk;
    }

    /**
     * 获取某个文件的size
     *
     * @param file 文件
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) {// 取得文件大小
        long s = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                s = fis.available();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return s;
    }

    /**
     * 获取文件夹的size
     *
     * @param dirFile 文件夹
     * @return
     * @throws Exception
     */
    public static long getDirSize(File dirFile) throws Exception {
        long size = 0;
        File flist[] = dirFile.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getDirSize(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 覆盖文件
     */
    public static boolean CoverFile(String srcFileName, String destFileName) {
        File destFile = new File(destFileName);
        if (destFile.exists()) {
            LogUtils.v(TAG, "删除文件" + destFileName);
            destFile.delete();
        }
        return copyFile(srcFileName, destFileName);

    }

    /**
     * 复制文件
     */
    public static boolean copyFile(File srcFile, File destFile) {
        boolean ret = false;
        if (!srcFile.exists()) {
            LogUtils.v(AppConfig.MODULE_APP, "文件不存在");
            return ret;
        }
        if (!srcFile.isFile()) {
            LogUtils.v(AppConfig.MODULE_APP, "文件不是file文件");
            return ret;
        }
        if (!srcFile.canRead()) {
            LogUtils.v(AppConfig.MODULE_APP, "文件不能读取");
            return ret;
        }

        isDirExist(destFile.getParentFile().getAbsolutePath());
        /*
         * if(!destFile.getParentFile().exists()){ destFile.mkdirs(); }
         */
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            if (destFile.exists()) {
                destFile.createNewFile();
            }
            if (srcFile.exists()) {
                inStream = new FileInputStream(srcFile);
                outStream = new FileOutputStream(destFile);
                LogUtils.v(AppConfig.MODULE_APP, "文件大小：" + inStream.available());
                byte[] buf = new byte[1024 * 100];
                int byteRead = 0;
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                    outStream.flush();
                }
                ret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(AppConfig.MODULE_APP, "copy file failture：" + e.getMessage());
        } finally {
            try {
                long time = System.currentTimeMillis();
                outStream.getFD().sync();
                LogUtils.v(AppConfig.MODULE_APP,
                        "执行sync文件时间：" + (System.currentTimeMillis() - time));
                outStream.close();
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return ret;
    }

    /**
     * 复制文件
     */
    public static boolean copyFile(String srcFileName, String destFileName) {
        File srcFile = new File(srcFileName);
        File destFile = new File(destFileName);
        return copyFile(srcFile, destFile);
    }


    /**
     * 备份文件
     *
     * @param srcFilePath  需要备份的源路径
     * @param destFilePath 目标路径
     */
    public static boolean backUpDirsAndFiles(String srcFilePath,
                                             String destFilePath) {
        File srcFile = new File(srcFilePath);
        File files[] = srcFile.listFiles();
        boolean ret = false;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    LogUtils.v(
                            AppConfig.MODULE_APP,
                            "正在导出：" + files[i].getPath() + "；导入："
                                    + destFilePath + File.separator
                                    + files[i].getName());
                    ret = CoverFile(files[i].getPath(), destFilePath
                            + File.separator + files[i].getName());
                    if (!ret) {
                        return false;
                    }
                }
                if (files[i].isDirectory()) {
                    // 递归函数
                    backUpDirsAndFiles(files[i].toString(), destFilePath
                            + File.separator + files[i].getName());
                }
            }
        }
        return ret;
    }

    /**
     * 循环删除文件及目录
     */
    public static boolean deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.exists()) {
            if (file.isFile()) {
                return file.delete();
            }
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    return file.delete();
                }

                for (File childFile : childFiles) {
                    deleteDir(childFile.getPath());
                }

                return file.delete();
            }
        }

        return false;
    }

    public static boolean deleteFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);

            return file.exists() && file.isFile() && file.delete();
        }

        return false;
    }

    public static File createFile(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "createFile: file path is null");
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    try {
                        if (!file.createNewFile()) {
                            return null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    return null;
                }
            }
        }

        return file;
    }

    /**
     * 获取当前文件夹下的所有文件，不包含子文件
     *
     * @param path
     * @return
     */
    public static ArrayList<File> getCurrentFilesArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    listFile.add(files[i]);
                }
            }
        }
        return listFile;
    }

    /**
     * 获取当前文件夹下的所有文件夹名称，带ALL，用于导出日志定制的方法
     *
     * @param path
     * @return
     */
    public static ArrayList<String> getCurrentALLDirsNameArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<String> listFile = new ArrayList<String>();
        listFile.add("ALL");
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    listFile.add(files[i].getName());
                }

            }
        }
        return listFile;
    }

    /**
     * 获取当前文件夹下的所有文件夹，不包含子文件
     *
     * @param path
     * @return
     */
    public static ArrayList<String> getCurrentDirsNameArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<String> listFile = new ArrayList<String>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    listFile.add(files[i].getName());
                }
            }
        }
        return listFile;
    }

    /**
     * 获取当前文件夹下的所有文件夹，不包含子文件
     *
     * @param path
     * @return
     */
    public static ArrayList<File> getCurrentDirsArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    listFile.add(files[i]);
                }
            }
        }
        return listFile;
    }

    /**
     * 获取文件夹下的Log文件
     *
     * @param path
     * @return
     */
    public static ArrayList<File> getFilesLogArray(String path, long startTime,
                                                   long endTime) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    long time = files[i].lastModified();
                    if (time >= startTime && time <= endTime) {
                        String name = files[i].getName();
                        if (name != null && name.endsWith(".log")) {
                            listFile.add(files[i]);
                        }
                    }
                }
                if (files[i].isDirectory()) {
                    listFile.addAll(getFilesLogArray(files[i].toString(),
                            startTime, endTime));
                }
            }
        }
        return listFile;
    }

    /**
     * 获取文件夹下所有文件
     *
     * @param path
     * @return
     */
    public static ArrayList<File> getFilesArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    listFile.add(files[i]);
                }
                if (files[i].isDirectory()) {
                    listFile.addAll(getFilesArray(files[i].toString()));
                }
            }
        }
        return listFile;
    }

    /**
     * 获取文件bufferedReader
     *
     * @param filepath
     * @return
     */
    public static BufferedReader getBufferedReader(String filepath) {
        File file = new File(filepath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        return reader;

    }

    /**
     * byte[] 转 文件
     *
     * @param buf
     * @param filePath
     * @param fileName
     * @throws IOException
     */
    public static File byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
            bos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.getFD().sync();
                bos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getFileBytes(File file) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return buffer;
    }

    public static byte[] getBytes(String filePath) {
        File file = new File(filePath);
        return getFileBytes(file);
    }

    public static void removeOldlogs() {
        //获取每个需要处理的文件夹
        File[] folders = new File(AppConfig.WORK_PATH_LOG).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return true;
                return false;
            }
        });

        File[] crashfolders = new File[]{new File(AppConfig.WORK_PATH_CRASH)};

        //保留前一个月的日志
        long time = CommUtils.getMonth(-1);

        //检测当前Sdcard的可用磁盘大小.
        long freeSpace = new File("/sdcard").getFreeSpace();
        if (freeSpace < 1024 * 1024)
            time = CommUtils.getDay(-1);

        //清理日志文件
        if (folders != null) {
            for (File f : folders) {
                CommUtils.removeFilesByTime(f.getAbsolutePath(), time);
            }
        }
        //根据时间清理crash文件
        //保留前6个月的crash日志
        long time2 = CommUtils.getMonth(-6);
        //检测当前Sdcard的可用磁盘大小.
        if(freeSpace<1024*1024)
            time2=CommUtils.getMonth(-1);
        //清理crash日志
            for (File f : crashfolders) {
                CommUtils.removeFilesByTime(f.getAbsolutePath(), time2);
            }
    }
}

