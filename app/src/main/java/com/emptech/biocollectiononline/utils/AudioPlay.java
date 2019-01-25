package com.emptech.biocollectiononline.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.util.Log;

import java.io.IOException;


public class AudioPlay {
	//ToneGenerator.MAX_VOLUME = 100
	private static final String TAG = "AudioPlay";
    private static ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
    private static MediaPlayer   mp  =   new MediaPlayer();
    private static Context mContex;
    private Object mLock = new Object();//监视器对象锁
	private static AudioPlay AudioPlay;

	private AudioPlay(){

	}

	public static AudioPlay getInstance() {
		if(AudioPlay ==null)
			AudioPlay =new AudioPlay();
		return AudioPlay;
	}

	public static AudioPlay getInstance(Context context) {
		mContex=context;
		if(AudioPlay ==null){
			AudioPlay =new AudioPlay();
		}
		return AudioPlay;
	}

	//播放系统声音 (ToneGenerator.TONE_PROP_BEEP2, 100)
    public int PlayTone(int toneType, int durationMs){

		synchronized(mLock) {
			if (mToneGenerator == null) {
			Log.e(TAG, "playTone: mToneGenerator == null");
			return	0;
			}
			
			if (mp == null) {
			Log.e(TAG, "mp == null");
			return	0;
			}
			
		}

    		if (mToneGenerator != null) {
			//(Tone, TONE_LENGTH_MS)	ToneGenerator.TONE_PROP_BEEP, ToneGenerator.TONE_PROP_PROMPT
			mToneGenerator.startTone(toneType, durationMs);
			
		}
		else
		{
			Log.e(TAG, "mToneGenerator Invalid !");
			return	-1;
		}
	    return	0;
	}

    //播放文件系统的 mp3 声音
	public int PlayFile(String filewithpath) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
//	    String  path   =  "/sdcard/1.mp3";
		if (mp != null){

            mp.reset();	// 重置 MediaPlayer
			mp.setDataSource(filewithpath);
			mp.prepare();
			mp.start();
		}
		else
		{
			Log.e(TAG, "MediaPlayer Invalid !");
			return	-1;
		}
		return	0;
	}

	public int PlayAssetFile(String filewithpath,AssetManager am){
//	    String  path   =  "/sdcard/1.mp3";
		if (mp != null){
			try {
				if(mp.isPlaying())
					mp.stop();
				mp.reset();	// 重置 MediaPlayer
				AssetFileDescriptor afd =  am.openFd(filewithpath);
				mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),
						afd.getLength());
				mp.prepare();
				mp.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		else
		{
			Log.e(TAG, "MediaPlayer Invalid !");
			return	-1;
		}
		return	0;
	}

	public int PlayAssetFile1(String filewithpath,AssetManager am){
//	    String  path   =  "/sdcard/1.mp3";
		if (mp != null){
			if(mp.isPlaying()){
				return 0;
			}
            mp.reset();	// 重置 MediaPlayer
			try {
				AssetFileDescriptor afd =  am.openFd(filewithpath);
				mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),
						afd.getLength());
				mp.prepare();
				mp.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		else
		{
			Log.e(TAG, "MediaPlayer Invalid !");
			return	-1;
		}
		return	0;
	}

	public int playRaw(int resId){
//	    String  path   =  "/sdcard/1.mp3";
		if (mp != null){
			if(mp.isPlaying()){
				return 0;
			}
            mp.reset();	// 重置 MediaPlayer
			try {
				mp=MediaPlayer.create(mContex,resId);
				mp.prepare();
				mp.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		else
		{
			Log.e(TAG, "MediaPlayer Invalid !");
			return	-1;
		}
		return	0;
	}
	
	
	public void PlayAsset(int key , AssetManager am) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
//	    String  path   =  "/sdcard/1.mp3";
		
//		if (AppConfig.AUTOTEST)
//			return;
		
		
		switch (key) {
		
		case 1:	//PLAY_SOUND_PUCODE_ERROR
			
			Log.v("crjlog","1");
			if (mp != null){
				AssetFileDescriptor afd =  am.openFd("finger_scan_fail.wav");
	            mp.reset();	// 重置 MediaPlayer
	            mp.setDataSource(afd.getFileDescriptor(),
	            		afd.getStartOffset(),
	            		afd.getLength());
				mp.prepare();
				mp.start();
			}
			else
			{
				Log.e(TAG, "MediaPlayer Invalid !"); 
			}
			
			break;
			
		case 2:
			
			Log.v("crjlog","2");
			if (mp != null){
				AssetFileDescriptor afd =  am.openFd("put_finger.wav");
	            mp.reset();	// 重置 MediaPlayer
	            mp.setDataSource(afd.getFileDescriptor(),
	            		afd.getStartOffset(),
	            		afd.getLength());
				mp.prepare();
				mp.start();
			}
			else
			{
				Log.e(TAG, "MediaPlayer Invalid !");
			}
			
			break;
			
		case 3:
			
			break;
			
		case 4:	//fail
			
			if (mp != null){
//				AssetFileDescriptor afd =  am.openFd("finger_authentication_fail.mp3");
				AssetFileDescriptor afd =  am.openFd("verification_fail.wav");
	            mp.reset();	// 重置 MediaPlayer
	            mp.setDataSource(afd.getFileDescriptor(),
	            		afd.getStartOffset(),
	            		afd.getLength());
				mp.prepare();
				mp.start();
			}
			else
			{
				Log.e(TAG, "MediaPlayer Invalid !");
			}
			
			
			break;
			
		case 5:	//success
			
			if (mp != null){
//				AssetFileDescriptor afd =  am.openFd("finger_verification.mp3");
				AssetFileDescriptor afd =  am.openFd("verification_passed.wav");
	            mp.reset();	// 重置 MediaPlayer
	            mp.setDataSource(afd.getFileDescriptor(),
	            		afd.getStartOffset(),
	            		afd.getLength());
				mp.prepare();
				mp.start();
			}
			else
			{
				Log.e(TAG, "MediaPlayer Invalid !");
			}
			
			break;

		}
		
		

	}

	//释放 播放器资源
	public int PlayRelease(){
		
		if (mToneGenerator != null){
			//yjh 2014.03.13  屏蔽原因: esc键退出应用以后,再次进入读卡播放系统 Tone 应用程序崩溃退出, 错误提示: mToneGenerator 调用已释放接口
//			mToneGenerator.release();
		}

        if (mp != null) {
        	//yjh 2014.03.13  add stop()
        	mp.stop();
            mp.reset();
            //yjh 2014.03.13  屏蔽原因: 反复进出 person 界面读卡后应用程序崩溃退出, 错误提示:Caused by: java.lang.IllegalStateException
//            mp.release();
        }
		return	0;
	}

/*	protected void onResume(){
		super.onResume();
		
		synchronized(mLock) {
			if (mToneGenerator == null) {
				try {
					mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUM);
				} catch (RuntimeException e) {
					Log.e(TAG, "Exception caught while creating local tone generator: " + e);
					mToneGenerator = null;
				}
			}
	
		}
	}*/
}
