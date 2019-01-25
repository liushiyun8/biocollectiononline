package com.emptech.biocollectiononline.utils;

import android.content.Context;
import android.media.AudioManager;

public class VolumeControlUtil {

	private static AudioManager getAudioManager(Context context) {
		return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 调整所有服务的音量（音乐，通话，系统，铃声，提示音）
	 */
	public static int adjustVolume(Context context, int curVolume, boolean hasSound) {
		adjustMusicVolume(context, curVolume,hasSound);
		adjustRingVolume(context, curVolume,hasSound);
		adjustSysVolume(context, curVolume,hasSound);
		adjustCallVolume(context, curVolume,hasSound);
		adjustNotificationVolume(context, curVolume,hasSound);
		return getCurVolume(context, AudioManager.STREAM_MUSIC);
	}

	/**
	 * 调整铃声音量
	 */
	public static void adjustRingVolume(Context context, int curVolume, boolean hasSound) {
		adjustTypeVolume(context, AudioManager.STREAM_RING, curVolume,hasSound);
	}

	/**
	 * 调整音乐音量
	 */
	public static void adjustMusicVolume(Context context, int curVolume, boolean hasSound) {
		adjustTypeVolume(context, AudioManager.STREAM_MUSIC, curVolume,hasSound);
	}

	/**
	 * 调整系统音量
	 */
	public static void adjustSysVolume(Context context, int curVolume, boolean hasSound) {
		adjustTypeVolume(context, AudioManager.STREAM_SYSTEM, curVolume,hasSound);
	}

	/**
	 * 调整通话音量
	 */
	public static void adjustCallVolume(Context context, int curVolume, boolean hasSound) {
		adjustTypeVolume(context, AudioManager.STREAM_VOICE_CALL, curVolume,hasSound);
	}

	/**
	 * 调整通知音量
	 */
	public static void adjustNotificationVolume(Context context, int curVolume, boolean hasSound) {
		adjustTypeVolume(context, AudioManager.STREAM_NOTIFICATION, curVolume,hasSound);
	}


	/**
	 * 调整音乐音量
	 */
	public static void adjustTypeVolume(Context context, int Type,
                                        int curVolume, boolean hasSound) {
		AudioManager audioMgr = getAudioManager(context);
		// 获取最大音乐音量
		int maxVolume = audioMgr.getStreamMaxVolume(Type);
		int sound = 0;
		if (hasSound) {
			sound = AudioManager.FLAG_PLAY_SOUND;
		}
		if (curVolume >= maxVolume) {
			audioMgr.setStreamVolume(Type, maxVolume, sound);
		} else if (curVolume <= 0) {
			audioMgr.setStreamVolume(Type, 0, sound);
		} else {
			audioMgr.setStreamVolume(Type, curVolume, sound);
		}
	}

	/**
	 * 调整音量
	 */
	public static int getMaxVolume(Context context, int Type) {
		AudioManager audioMgr = getAudioManager(context);
		// 获取最大音乐音量
		return audioMgr.getStreamMaxVolume(Type);
	}

	public static int getCurVolume(Context context, int Type) {
		AudioManager audioMgr = getAudioManager(context);
		return audioMgr.getStreamVolume(Type);
	}

}
