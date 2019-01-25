package com.emptech.biocollectiononline.common;


import com.emptech.biocollectiononline.activity.WelcomeActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class})
public interface AppComponent {

	void inject(App app);

	void inject(AppActivity activity);

	void inject(WelcomeActivity activity);

}
