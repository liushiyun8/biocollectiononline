package com.emptech.biocollectiononline.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by linxiaohui on 2017/11/28.
 */

public class BitmapUtil {

    /**
     * drawable转Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            String s = new String(b);
            Bitmap mBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            return mBitmap;
        } else {
            LogUtils.e(AppConfig.MODULE_SERVER, "the picture data：" + Converter.BytesToHexString(b, b.length));
            return null;
        }
    }

    /**
     * 旋转图片
     *
     * @param bitmap 图片
     * @param degree 偏移角度
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degree);
        int width = bitmap.getHeight();
        int height = bitmap.getWidth();
        return Bitmap.createBitmap(bitmap, 0, 0, height, width, matrix, false);
    }

    public static Bitmap Bytes2Bimap(byte[] b, int scaleRate) {
        if (b.length != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scaleRate;
            return BitmapFactory.decodeByteArray(b, 0, b.length, options);
        } else {
            return null;
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 90, baos);
        return baos.toByteArray();
    }

    public static byte[] Bitmap2JpegBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String Bitmap2HexString(Bitmap bm) {
        byte[] bmHexString = Bitmap2Bytes(bm);
        return Converter.BytesToHexString(bmHexString, bmHexString.length);
    }

    public static Bitmap hexString2Bitmap(String bmHexString) {
        byte[] bmByte = Converter.string2Hex(bmHexString);
        return Bytes2Bimap(bmByte);
    }

    // 将bmp格式图片转换成png格式，同时保存到sdcard/Sign这个文件夹里面

    public static File saveBitmapToPngFile(Bitmap b, String filePath, String fileName) {
        String currentPath = filePath;
        FileOutputStream fos = null;
        File file = null;
        try {
            File sddir = new File(currentPath);
            if (!sddir.exists()) {
                sddir.mkdirs();
            }
            file = new File(currentPath + "/" + fileName + ".png");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            if (fos != null) {
                b.compress(Bitmap.CompressFormat.PNG, 50, fos);
                fos.close();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 二次压缩，先按照像素压缩再按照质量压缩
     *
     * @param imgUrl   图片路径
     * @param reqWidth 期望宽度 可以根据市面上的常用分辨率来设置
     * @param size     期望图片的大小，单位为kb
     * @param quality  图片压缩的质量，取值1-100，越小表示压缩的越厉害，如输入30，表示压缩70%
     * @return Bitmap 压缩后得到的图片
     */
    public static Bitmap compressBitmap(String imgUrl, int reqWidth, int size, int quality) {
        // 创建bitMap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgUrl, options);
        int height = options.outHeight;
        int width = options.outWidth;
        int reqHeight;
        reqHeight = (reqWidth * height) / width;
        // 在内存中创建bitmap对象，这个对象按照缩放比例创建的
        options.inSampleSize = calculateInSampleSize(
                options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(
                imgUrl, options);
        Bitmap mBitmap = compressImage(Bitmap.createScaledBitmap(
                bm, 480, reqHeight, false), size, quality);
        return mBitmap;
    }

    public static Bitmap compressBitmap(byte[] imgByte, int reqWidth, int size, int quality) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length, options);
        int height = options.outHeight;
        int width = options.outWidth;
        int reqHeight;
        reqHeight = (reqWidth * height) / width;
        options.inSampleSize = calculateInSampleSize(
                options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeByteArray(
                imgByte, 0, imgByte.length, options);
        return compressImage(bm, size, quality);
    }

    /**
     * 根据目标长宽和内存，压缩image数据
     *
     * @param imgByte  图片数据
     * @param reqWidth 要求宽度；
     * @param maxSize  最大内存
     * @param quality  图片质量；
     * @return
     */
    public static byte[] compressBitmapByte(byte[] imgByte, int reqWidth, int maxSize, int quality) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length, options);
        int height = options.outHeight;
        int width = options.outWidth;
        int reqHeight;
        reqHeight = (reqWidth * height) / width;
        options.inSampleSize = calculateInSampleSize(
                options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeByteArray(
                imgByte, 0, imgByte.length, options);
        return compressImageToByte(bm, maxSize, quality);
    }


    /**
     * 质量压缩图片，图片占用内存减小，像素数不变，常用于上传
     *
     * @param image
     * @param size    期望图片的大小，单位为kb
     * @param options 图片压缩的质量，取值1-100，越小表示压缩的越厉害,如输入30，表示压缩70%
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int size, int options) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        int compressCount = 5;
        while (baos.toByteArray().length / 1024 > size || compressCount > 0) {
            compressCount--;
            options -= 1;// 每次都减少10
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if (options <= 0) {
                break;
            }
            Log.v(AppConfig.MODULE_APP, "压缩图片中...");
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static byte[] compressImageToByte(Bitmap image, int size, int options) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        long t1=System.currentTimeMillis();
        image.compress(Bitmap.CompressFormat.PNG, options, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        int compressCount = 3;
        while (baos.toByteArray().length / 1024 > size && compressCount > 0) {
            compressCount--;
            options -= 1;// 每次都减少10
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.PNG, options, baos);
            if (options <= 0) {
                break;
            }
            Log.v(AppConfig.MODULE_APP, "压缩图片中...");
        }
        Log.e(AppConfig.MODULE_APP, "压缩用时："+(System.currentTimeMillis()-t1));
        return baos.toByteArray();
    }

    /**
     * 计算像素压缩的缩放比例
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    // 更换图片尺寸

    public static Bitmap scaleToNewBitmap(Bitmap origin, float scaleRate) {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleRate, scaleRate);
        Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, origin.getWidth(),
                origin.getHeight(), matrix, true);
        return newBitmap;
    }

    /**
     * 根据 路径 得到 file 得到 bitmap
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Bitmap decodeFile(String filePath){
        Bitmap b = null;
        int IMAGE_MAX_SIZE = 600;

        File f = new File(filePath);
        if (f == null) {
            return null;
        }
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }
}
