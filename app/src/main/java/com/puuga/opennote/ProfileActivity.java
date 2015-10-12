package com.puuga.opennote;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.model.User;

import io.fabric.sdk.android.Fabric;

public class ProfileActivity extends AppCompatActivity {

    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView ivUserPictureHeader;

    // SharedPreferences
    SettingHelper settingHelper;

    // Google Analytic
    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_profile);

        setupToolbar();

        initInstances();

        initSharedPreferences();

        initGoogleAnalytic();

    }

    private void initInstances() {
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        ivUserPictureHeader = (ImageView) findViewById(R.id.iv_user_picture_header);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.d("profile", e.getMessage());
        }
    }

    private void initSharedPreferences() {
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        settingHelper = application.getSettingHelper();
    }

    private void initGoogleAnalytic() {
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("Profile Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    void bindUserToWidget(User me) {
        collapsingToolbarLayout.setTitle(me.name);

        Glide.with(this)
                .load(me.getUserPictureUrl())
                .centerCrop()
                .into(ivUserPictureHeader);
    }

}
