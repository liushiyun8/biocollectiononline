package com.emptech.biocollectiononline.manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PreferencesManager {

	public static final String PREF_XID = "pref_xid";
	public static final String PREF_DID = "pref_did";
	public static final String PREF_IS_LOGINED = "pref_is_logined";
	public static final String PREF_IS_LOAD_SETTING = "pref_already_load_config";
	private static PreferencesManager sInstance;
	private final SharedPreferences mPref;

	private PreferencesManager(Context context) {
		mPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}

	public static synchronized PreferencesManager getIns(Context context) {
		if (sInstance == null) {
			sInstance = new PreferencesManager(context);
		}
		return sInstance;
	}

	public boolean isLogined() {
		return mPref.getBoolean(PREF_IS_LOGINED, false);
	}

	public void setLogined(boolean value) {
		mPref.edit().putBoolean(PREF_IS_LOGINED, value).commit();
	}

	public void logout() {
		SharedPreferences.Editor editor = mPref.edit();
		editor.putBoolean(PREF_IS_LOGINED, false);
		editor.putString(PREF_XID, null);
		editor.putString(PREF_IS_LOAD_SETTING, null);
		editor.commit();
	}

	public void setXid(String value) {
		mPref.edit().putString(PREF_XID, value).commit();
	}

	public String getXid() {
		return mPref.getString(PREF_XID, "");
	}

	public void setDid(String value) {
		mPref.edit().putString(PREF_DID, value).commit();
	}

	public String getDid() {
		return mPref.getString(PREF_DID, "");
	}

	public void remove(String key) {
		mPref.edit().remove(key).commit();
	}

	public boolean clear() {
		return mPref.edit().clear().commit();
	}

	public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		mPref.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		mPref.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public void setBooleanPref(String key, boolean value) {
		mPref.edit().putBoolean(key, value).commit();
	}

	public boolean getBooleanPref(String key, boolean defaultValue) {
		return mPref.getBoolean(key, defaultValue);
	}

	public void setStringPref(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}

	public String getStringPref(String key) {
		return mPref.getString(key, "");
	}

	public String getStringPref(String key,String def) {
		return mPref.getString(key, def);
	}

	public void setIntegerPref(String key, Integer value) {
		mPref.edit().putInt(key, value).commit();
	}

	public int getIntegerPref(String key) {
		return mPref.getInt(key, 0);
	}

	public int getIntegerPref(String key,int defValue) {
		return mPref.getInt(key, defValue);
	}

	public void setStringSetPref(String key, String value) {
		Set<String> set = mPref.getStringSet(key, new HashSet<String>());
		set.add(value);
		mPref.edit().putStringSet(key, set).commit();
	}

	public void setStringSetsPref(String key, Set<String> set) {
		mPref.edit().putStringSet(key, set).commit();
	}

	public Set<String> getStringSetPref(String key) {
		return mPref.getStringSet(key, new HashSet<String>());
	}
}