package com.emptech.biocollectiononline.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * 加密与校验解密文件
 * @author ZhangLibin
 *
 */
public class SecureUtils {
//	public static void main(String[] args) {
//		decryptFile();
//	}
	
	private static String filePath = "f://vote.png";
	private static String encryFile = "f://vote/vote_encry.png";
//	private static String key = "www.xiongdi.cn";
	
	public static boolean decryptFile(File file, String desDir, String desName){
//		File file = new File(encryFile);
		byte[] srcByte = file2Bytes(file);
		byte[] keyByte = new byte[32];
		if(srcByte.length < 32){
			System.out.println("检验失败");
			return false;
		}
		int width = srcByte.length/32;
		byte[] outputs = new byte[srcByte.length - 32];
		int j = 0, k = 0;
		for(int i = 0; i < srcByte.length; i++){
			if(i % width == 0 && j < 32){
				keyByte[j] = srcByte[i];
				j++;
			}else{
				outputs[k] = srcByte[i];
				k++;
			}
		}
		try {
			String srcMd5 = MD5Util.getMD5(new ByteArrayInputStream(outputs));
			if(srcMd5.equals(new String(keyByte))){
				System.out.println("校验成功");
				return saveByte2File(outputs, desDir, desName);
			}
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		System.out.println("校验失败");
		return false;
	}
	
	public static byte[] decryptFile(File file){
		byte[] srcByte = file2Bytes(file);
		byte[] keyByte = new byte[32];
		if(srcByte == null || srcByte.length < 32){
			System.out.println("检验失败");
			return null;
		}
		int width = srcByte.length/32;
		byte[] outputs = new byte[srcByte.length - 32];
		int j = 0, k = 0;
		for(int i = 0; i < srcByte.length; i++){
			if(i % width == 0 && j < 32){
				keyByte[j] = srcByte[i];
				j++;
			}else{
				outputs[k] = srcByte[i];
				k++;
			}
		}
		try {
			String srcMd5 = MD5Util.getMD5(new ByteArrayInputStream(outputs));
			if(srcMd5.equals(new String(keyByte))){
				System.out.println("校验成功");
				return outputs;
			}
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	public static void encryptFile(String filePath, String outDir, String outName){
		File file = new File(filePath);
		byte[] srcByte = file2Bytes(file);
		byte[] keyByte = null;
		try {
			keyByte = MD5Util.getMD5(file).getBytes();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(srcByte.length < 32){
			System.out.println("加密失败");
			return;
		}
		int width = srcByte.length/32;
		int j = 0, k = 0;
		byte[] outputs = new byte[srcByte.length + 32];
		for(int i = 0; i < outputs.length; i++){
			if(i % (width+1) == 0 && j < keyByte.length){//insert key
				outputs[i] = keyByte[j];
				j++;
			}else{
				outputs[i] = srcByte[k];
				k++;
			}
		}
		try {
			saveByte2File(outputs, outDir, outName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
    public static byte[] file2Bytes(File templateFile) {
        if (templateFile == null || !templateFile.exists()){
            return null;
        }
        byte[] templateContent = null;
        FileInputStream fs = null;
        try {
            long nFileSize = templateFile.length();
            fs = new FileInputStream(templateFile);

            byte[] fileContent = new byte[(int) nFileSize];
            fs.read(fileContent);
            fs.close();

            templateContent = fileContent;
        } catch (Exception e) {

        }
        return templateContent;
    }
    
    public static boolean saveByte2File(byte[] buf, String filePath, String fileName)
            throws IOException {
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
            return true;
        } catch(Exception e){
        	e.printStackTrace();
        }finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
