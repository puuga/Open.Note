package com.puuga.opennote;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.puuga.opennote.helper.Constant;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.manager.APIService;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by siwaweswongcharoen on 10/4/2015 AD.
 */
public class AnalyticsApplication extends Application implements
        Application.OnProvideAssistDataListener {
    private Tracker mTracker;

    // SharedPreferences
    private SettingHelper settingHelper;

    // Retrofit
    private APIService service;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
//            AnalyticsTrackers.initialize(this);
//            AnalyticsTrackers analytics = AnalyticsTrackers.getInstance();
//            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//            mTracker = analytics.get(AnalyticsTrackers.Target.APP);

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.app_tracker);
        }
        return mTracker;
    }

    synchronized public SettingHelper getSettingHelper() {
        if (settingHelper == null) {
            settingHelper = SettingHelper.createSettingHelper(this);
        }
        return settingHelper;
    }

    synchronized public APIService getAPIService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constant.API_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(APIService.class);
        }
        return service;
    }

    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.registerOnProvideAssistDataListener(callback);
    }

    @Override
    public void onProvideAssistData(Activity activity, Bundle bundle) {

    }
}
