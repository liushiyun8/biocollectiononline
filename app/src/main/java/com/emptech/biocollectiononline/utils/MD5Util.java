package com.emptech.biocollectiononline.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by moubiao on 2016/8/16.
 * MD5工具类
 */
public class MD5Util {
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    /** 
     *  
     * @param is 
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */  
    public static String getMD5(InputStream is) throws NoSuchAlgorithmException, IOException {  
        StringBuffer md5 = new StringBuffer();  
        MessageDigest md = MessageDigest.getInstance("MD5");  
        byte[] dataBytes = new byte[1024];  
          
        int nread = 0;   
        while ((nread = is.read(dataBytes)) != -1) {  
            md.update(dataBytes, 0, nread);  
        };  
        byte[] mdbytes = md.digest();  
          
        // convert the byte to hex format  
        for (int i = 0; i < mdbytes.length; i++) {  
            md5.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));  
        }  
        return md5.toString();  
    } 
    
    /** 
     *  
     * @param file 
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */  
    public static String getMD5(File file) throws NoSuchAlgorithmException, IOException {  
        FileInputStream fis = new FileInputStream(file);  
        return getMD5(fis);  
    }  
}
