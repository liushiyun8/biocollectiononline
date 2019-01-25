package com.emptech.biocollectiononline.common;

import android.content.res.Resources;

import com.emptech.biocollectiononline.dao.DatabaseOpenHelper;
import com.emptech.biocollectiononline.manager.PreferencesManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private App application;

    public AppModule(App application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public App provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    @Singleton
    public MusicPlayer providerMusicPlayer() {
        return MusicPlayer.get(application);
    }

//	@Provides
//	@Singleton
//	public CustomTypeFace providerCustomTypeFace() {
//		return CustomTypeFace.get(application);
//	}

    @Provides
    @Singleton
    public DatabaseOpenHelper providerDatabaseOpenHelper() {
        return DatabaseOpenHelper.get(application);
    }

    @Provides
    @Singleton
    public Resources providerResourceHandler() {
        return application.getResources();
    }

    @Provides
    @Singleton
    public PreferencesManager providerPreferencesManager() {
        return PreferencesManager.getIns(application);
    }

}
