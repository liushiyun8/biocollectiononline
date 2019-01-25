package com.emptech.biocollectiononline.manager;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.FileUtil;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.apache.commons.io.HexDump;

import java.io.File;

import cn.xiongdi.jni.UserDevices;
import utils.SerialPortFinder;
import utils.Uart;

public class SignatureManager {
	private static final String TAG = "SignatureManager";
	private int devicefd = -1;
	private boolean isReading = false;
	private boolean isForceStop = false;// 强制停止读取;
	private byte[] data;

	public boolean openDevice() {
		String devname=null;
		SerialPortFinder serialPortFinder = new SerialPortFinder();
		String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
		for (int i = 0; i < allDevicesPath.length; i++) {
//			LogUtils.e(TAG,i+":"+allDevicesPath[i]);
			if(allDevicesPath[i].contains("ttyACM")){
				devname=allDevicesPath[i];
                if(!devname.contains("ttyACM0"))
                    break;
			}
		}
		if(devname==null){
			LogUtils.e(TAG, "device can't find");
			return false;
		}
		Log.v(TAG,"devname:"+devname+",length:"+devname.length());
		devicefd = Uart.open(devname, Uart.O_RDWR);
		if (devicefd > 0) {
			Uart.set_port(devicefd, 9600, 8, (byte) 'N', 1);
			Log.d(TAG, "devicefd opened ok");
			return true;
		} else {
			Log.e(TAG, "signature devicefd opened failed,fd:"+devicefd);
			return false;
		}
	}

	public String getVersion() {
		String message = "version";
		byte[] buf = message.getBytes();
		Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		int ret = Uart.write(devicefd, buf, 0, buf.length);
		byte[] verb=new byte[50];
		int count = Uart.poll_read(devicefd, verb, 0, verb.length, 300);
		Uart.close(devicefd);
		devicefd = -1;
		if(count>1){
			return new String(verb, 0, count-1);
		}
		return null;
	}

	//升级
	public boolean update(String path){
		int ret=-1;
		int trycount=100;
		byte[] down = "down".getBytes();
		Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		ret= Uart.write(devicefd, down,0 , down.length);
		if(ret<0){
			Log.v(TAG,"write down:"+ret);
			return false;
		}
//		byte[] msg=new byte[50];
//		int count=Uart.poll_read(devicefd,msg , 0,msg.length , 300);
//		String res = new String(msg, 0, count - 1);
//		Log.v(TAG,res+"" );
//		if(count<0){
//			Log.v(TAG, "get down fail");
//			return false;
//		}
		int closeret = Uart.close(devicefd);
		Log.v(TAG,"Uart.close1:"+ closeret);
		UserDevices.uart_close();
		SystemClock.sleep(8000);
		ret=UserDevices.uart_open();
		if(ret!=0){
			Log.v(TAG, "uart_open fail");
			return false;
		}
		int opencount=3;
		boolean open;
		while (!(open=openDevice())){
			SystemClock.sleep(1000);
			if(opencount--<0)
				break;
		}
		if(!open){
			Log.v(TAG,"openDevice:"+open);
			return false;
		}
		Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		byte[] start="start".getBytes();
		ret=Uart.write(devicefd, start, 0, start.length);
		if(ret<0){
			Log.v(TAG,"send start error" );
			return false;
		}
		byte[] re_start=new byte[50];
		int count;
		int offset=0;
		while (true){
			 count = Uart.poll_read(devicefd, re_start, offset, re_start.length, 500);
			if(count<0){
				Log.v(TAG, "read start fail");
				return false;
			}else if(count>0){
			    offset+=count;
				String s = new String(re_start, 0, offset);
				Log.e(TAG,"start read:"+s );
				if(s.contains("ease 200kB ok")){
					break;
				}
			}else {
				trycount--;
				if(trycount<0){
					return false;
				}
			}
		}
		byte[] bytes = FileUtil.getFileBytes(new File(path));
		long startTime = System.currentTimeMillis();
		if(bytes==null||bytes.length<=0){
			return false;
		}
		Log.v(TAG,"bytes:"+bytes.length+",startTiem:"+ startTime);
		    count = Uart.write(devicefd, bytes, 0, bytes.length);
		    if(count<0){
		    	Log.v(TAG, "send file fail");
		    	return false;
			}
		 Log.v(TAG,"发送文件用时:"+(System.currentTimeMillis()-startTime));
		 SystemClock.sleep(1000);
		byte[] endb="end".getBytes();
		count=Uart.write(devicefd,endb ,0 , endb.length);
		if(count<0){
			Log.v(TAG,"send end fail" );
			return false;
		}
		Log.v(TAG,"send end success" );
		closeret = Uart.close(devicefd);
		Log.v(TAG,"Uart.close2:"+ closeret);
		UserDevices.uart_close();
		SystemClock.sleep(1000);
		UserDevices.uart_open();
		return true;
	}

	/**
	 * 发送开始采集信号
	 * 
	 * @return
	 */
	public boolean StartCapture() {
		if (isReading) {
			return false;
		}
        Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		String message = "open";
		String lang = App.get().getmPreferencesManager().getStringPref(AppConfig.LANGUAGE_KEY, "ru");
		switch (lang){
			case "ru":
				message="openp";
				break;
			case "cn":
				message="openc";
				break;
		}
		byte[] buf = message.getBytes();
		int ret = Uart.write(devicefd, buf, 0, buf.length);
		if (ret > 0) {
			return true;
		}
		LogUtils.e(TAG,"signature open fail，ret:"+ret);
		return false;
	}

	public boolean CloseCapture() {
		while (isReading) {
			isForceStop = true;
			SystemClock.sleep(10);
		}
		Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		//清除串口里面所有的缓存数据
		String message = "close";
		byte[] buf = message.getBytes();
		int ret = Uart.write(devicefd, buf, 0, buf.length);
		int closeret = Uart.close(devicefd);
		devicefd = -1;
		LogUtils.d(TAG, "signature close："+ret+",uart close："+closeret);
		if (ret > 0&&closeret==0) {
			return true;
		}
		LogUtils.e(TAG,"closeret:"+closeret +",error:"+Uart.strerror());
		return false;
	}

	/**
	 * 循环读取串口中的图片数据，耗时操作，需要线程中执行；
	 * 
	 * @return
	 */
	public byte[] getCaptureDataFromUart() {
		if (devicefd == -1) {
			return null;
		}
		int readBufOffset = 0;
		final byte suffix[] = { 0x5b, (byte) 0xb5 };
		boolean isTimeOut = false;
		int ret;
		long startTime = System.currentTimeMillis();// 开始时间
		String errorLog;// 错误提示；
		byte[] readBuf = new byte[1024 *40];
		isForceStop = false;
		Uart.tcflush(devicefd, Uart.TCIOFLUSH);
		Log.e(TAG,"isTimeOut:"+isTimeOut+"isForceStop:"+isForceStop );
		while (!isTimeOut && !isForceStop) {
			// isTimeOut = System.currentTimeMillis() - startTime >= 1000 * 60;
			isReading = true;
			ret = Uart.poll_read(devicefd, readBuf, readBufOffset, readBuf.length,10);
			Log.e(TAG,"ret:"+ret );
			if (ret == 0) {
//				SystemClock.sleep(10);
			} else {
				readBufOffset += ret;
				Log.e(TAG,"readBufOffset:"+readBufOffset);
//				LogUtils.e(TAG, "readBufOffset:"+readBufOffset+","+Converter.BytesToHexString(readBuf,readBufOffset ));
				if (readBufOffset >= suffix.length) {
					if (((readBuf[readBufOffset - 1] == suffix[suffix.length - 1]) && (readBuf[readBufOffset - 2] == suffix[suffix.length - 2]))
							||(readBufOffset >= 38574)) {
						readBufOffset -= suffix.length;
						data = new byte[readBufOffset];
						System.arraycopy(readBuf, 0, data, 0, readBufOffset);
						Log.e(TAG,"recieve data and return" );
						isReading = false;
						return data;
					}
				} else {
					LogUtils.e(TAG, "read ret:" + ret+",errorStr:"+Uart.strerror());
					return null;
				}
			}
		}
		isReading = false;
		LogUtils.e(TAG, "isForceStop:" + isForceStop);
		return null;
	}

	/**
	 * 从串口数据中提取真实的图片数据，剔除通讯协议的包头和CRC校验；
	 * 
	 * @param recvData
	 *            串口数据
	 * @return 图片数据
	 */
	public byte[] getPictureByte(byte[] recvData) {

		byte[] headPacket = { 0x5A, (byte) 0xA5 };
		int start=0;
		for (int i = 0; i < recvData.length; i++) {
			if((recvData[i]==headPacket[0]&&recvData[i+1]==headPacket[1])){
				start=i;
				break;
			}
		}
		if(start!=0){
			byte[] data=new byte[recvData.length-start];
			System.arraycopy(recvData, start,data ,0 ,data.length );
			recvData=data;
			String content = Converter.BytesToHexString(data,
					data.length);
			LogUtils.e(TAG, "read data:" + content);
		}
		// 60个字节为一包
		int packetSize = 60;
		int packetLen = recvData.length / packetSize;
		int picturePacketLen = 0;
		int lastPacketLen = recvData.length % packetSize;
		if (lastPacketLen != 0) {
			picturePacketLen = packetLen * (packetSize - 4)
					+ (lastPacketLen - 4);
			packetLen++;
		} else {
			picturePacketLen = packetLen * (packetSize - 4);
		}
		// int picturePacketLen = +recvData.length % packetSize;
		byte[] pictureByte = new byte[picturePacketLen];
		int pictureByteIndex = 0;
		for (int i = 0; i < packetLen; i++) {
			int index = i * packetSize;
			if (recvData[index] != headPacket[0]
					|| recvData[index + 1] != headPacket[1]) {
				// 包头不正确；
				return null;
			}
			int indexPacketLen = recvData[index + 2] & 0xff;
			System.arraycopy(recvData, index + 3, pictureByte,
					pictureByteIndex, indexPacketLen);
			pictureByteIndex += indexPacketLen;
		}

		return pictureByte;
	}

	/**
	 * 像素点 的扫描方式为从左到右从上到下，每一个像素点用一个字节的一位表示，1： 像素点为黑色；0：像素点为白色。 800*360;
	 * 
	 * @param PictureByte
	 * @return
	 */

	public Bitmap getBmpFormByte(byte[] PictureByte) {
		int width = 800;
		int height = 360;
		int[] recPicture = new int[width * height];
		if(PictureByte.length<800*360/8){
			String content = Converter.BytesToHexString(data,
					data.length);
			LogUtils.e(TAG, "read data:" + content);
			LogUtils.e(TAG,"PictureByte.length:"+PictureByte.length );
			return null;
		}
		int offset = 0;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width / 8; j++) {
				byte bit = PictureByte[i * width / 8 + j];   //to do 有问题的地方
				for (int index = 0; index < 8; index++) {
					if ((bit & (1 << (7 - index))) > 0) {
						recPicture[offset] = 0xFF000000;
					} else {
						recPicture[offset] = 0xFFFFFFFF;
					}
					offset++;
				}
			}
		}
		return Bitmap.createBitmap(recPicture, 0, width, width, height,
				Bitmap.Config.ARGB_8888);
	}

}
