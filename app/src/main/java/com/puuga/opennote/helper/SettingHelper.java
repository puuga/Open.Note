package com.puuga.opennote.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by siwaweswongcharoen on 10/4/2015 AD.
 */
public class SettingHelper {
    static final String TAG = "setting";
    static final String PREFERENCE_FILE_KEY = "open.note_settings_file";

    // SharedPreferences
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private SettingHelper(Context context) {
        sharedPref = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public static SettingHelper createSettingHelper(Context context) {
        return new SettingHelper(context);
    }

    public boolean isFacebookLogin() {
        return sharedPref.getBoolean(Constant.FACEBOOK_IS_LOGIN, false);
    }

    public void setFacebookLogin(boolean val) {
        editor.putBoolean(Constant.FACEBOOK_IS_LOGIN,val);
        editor.commit();
        Log.d(TAG, "setFacebookLogin:" + val);
    }

    public String getFacebookToken() {
        return sharedPref.getString(Constant.FACEBOOK_TOKEN, "");
    }

    public void setFacebookToken(String val) {
        editor.putString(Constant.FACEBOOK_TOKEN, val);
        editor.commit();
        Log.d(TAG, "setFacebookToken:" + val);
    }

    public String getFacebookId() {
        return sharedPref.getString(Constant.FACEBOOK_ID, "");
    }

    public void setFacebookId(String val) {
        editor.putString(Constant.FACEBOOK_ID, val);
        editor.commit();
        Log.d(TAG, "setFacebookId:" + val);
    }

    public String getFacebookName() {
        return sharedPref.getString(Constant.FACEBOOK_NAME, "");
    }

    public void setFacebookName(String val) {
        editor.putString(Constant.FACEBOOK_NAME, val);
        editor.commit();
        Log.d(TAG, "setFacebookName:" + val);
    }

    public String getAppId() {
        return sharedPref.getString(Constant.APP_ID, "");
    }

    public void setAppId(String val) {
        editor.putString(Constant.APP_ID, val);
        editor.commit();
        Log.d(TAG, "setAppId:" + val);
    }
}
