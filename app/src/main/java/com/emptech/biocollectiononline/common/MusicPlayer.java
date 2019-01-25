package com.emptech.biocollectiononline.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MusicPlayer {
    Context mContext;

    static MusicPlayer inst;
    SoundPool mSoundPool;
    int mSoundCount;
    private int currentStreamId;
    static long lastTime;
    private static WeakReference<Context> weakReference;

    class SoundInfo {
        public int soundId;
        public int playCount;
        public boolean loadedOK;

        public SoundInfo(int soundId, int playCount) {
            super();
            this.soundId = soundId;
            this.playCount = playCount;
            this.loadedOK = false;
        }
    }

    ConcurrentMap<Integer, SoundInfo> mSoundIdMap = new ConcurrentHashMap<Integer, SoundInfo>();
    @SuppressLint("NewApi")
    private MusicPlayer(Context context) {
        mContext = context;
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
        mSoundPool = new SoundPool.Builder().setMaxStreams(255).setAudioAttributes(audioAttributes).build();
        mSoundPool.setOnLoadCompleteListener(onLoadCompleteListener);
    }

    OnLoadCompleteListener onLoadCompleteListener = new OnLoadCompleteListener() {

        @Override
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            if (status != 0) {
                LogUtils.e(AppConfig.MODULE_APP, "MusicPlayer mSoundPool onLoadComplete failed sampleId=" + sampleId);
            }
        }
    };

    public static MusicPlayer get(Context context) {
        weakReference = new WeakReference<>(context);
        synchronized (MusicPlayer.class) {
            if (inst == null)
                inst = new MusicPlayer(weakReference.get().getApplicationContext());
        }
        return inst;
    }

    public void loadRes(int resId) {

        int soundId = mSoundPool.load(mContext, resId, 1);
        SoundInfo soundInfo = new SoundInfo(soundId, 0);
        mSoundIdMap.put(resId, soundInfo);
    }

    public int playNoRepeat(int resId, boolean loop) {
        SoundInfo soundInfo = mSoundIdMap.get(resId);
        if (soundInfo == null) {
            loadRes(resId);
            soundInfo = mSoundIdMap.get(resId);
        }
        if(System.currentTimeMillis()-lastTime<3000)
            return 0;
        if(currentStreamId!=0){
            mSoundPool.stop(currentStreamId);
        }
        int streamId = 0;
        if (soundInfo != null) {
            streamId = mSoundPool.play(soundInfo.soundId, 1, 1, 0, (loop ? -1 : 0), 1);
            soundInfo.playCount++;
            currentStreamId=streamId;
            lastTime=System.currentTimeMillis();
            return streamId;
        } else {
            LogUtils.e(AppConfig.MODULE_APP, "MusicPlayer play failed");
        }
        return streamId;
    }

    public int play(int resId, boolean loop) {
        SoundInfo soundInfo = mSoundIdMap.get(resId);
        if (soundInfo == null) {
            loadRes(resId);
            soundInfo = mSoundIdMap.get(resId);
        }
        if(currentStreamId!=0){
            mSoundPool.stop(currentStreamId);
        }
        int streamId = 0;
        if (soundInfo != null) {
            streamId = mSoundPool.play(soundInfo.soundId, 1, 1, 0, (loop ? -1 : 0), 1);
            soundInfo.playCount++;
            currentStreamId=streamId;
            lastTime=System.currentTimeMillis();
            return streamId;
        } else {
            LogUtils.e(AppConfig.MODULE_APP, "MusicPlayer play failed");
        }
        return streamId;
    }

    public void stop(int streamId) {
        if (streamId != 0) {
            mSoundPool.stop(streamId);
        }
    }

}