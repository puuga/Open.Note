package com.puuga.opennote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.puuga.opennote.helper.SettingHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class FacebookLoginActivity extends AppCompatActivity {

    TextView tvFacebookInfo;

    // facebook sdk
    LoginButton btnFacebookLogin;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;

    // SharedPreferences
    SettingHelper settingHelper;

    // Google Analytic
    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initFacebookSdk();
        setContentView(R.layout.activity_facebook_login);

        printHashKey();

        initGoogleAnalytic();

        initSharedPreferences();

        initFacebookLogin();

        initInstance();
    }

    private void initInstance() {
        tvFacebookInfo = (TextView) findViewById(R.id.tv_facebook_info);
        if (settingHelper.isFacebookLogin()) {
            tvFacebookInfo.setText(getString(R.string.login_as, settingHelper.getFacebookName()));
        }
    }

    private void initGoogleAnalytic() {
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    private void initSharedPreferences() {
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        settingHelper = application.getSettingHelper();
    }

    private void initFacebookLogin() {
        btnFacebookLogin = (LoginButton) findViewById(R.id.btn_facebook_login);
        btnFacebookLogin.setReadPermissions(Arrays.asList("public_profile", "email"));

        // Callback registration
        btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Facebook Login Success")
                        .build());

                accessTokenTracker.startTracking();

                getFacebookUserInfo(loginResult);
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("FBonC", "onCancel");

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Facebook Login Cancel")
                        .build());
            }

            @Override
            public void onError(FacebookException exception) {
                // App code

                try {
                    Log.d("FBonE", exception.getCause().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("FBonE", "Exception");
                }

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Facebook Login Error")
                        .build());
            }
        });
    }

    private void initFacebookSdk() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                if (currentAccessToken == null) {
                    //write your code here what to do when user logout
                    Log.d("fb_logout", "fb_logout");
                    tvFacebookInfo.setText("");

                    setFacebookInfo(false, "", "", "");
                }
            }
        };
    }

    private void getFacebookUserInfo(final LoginResult loginResult) {
        Log.d("FBonS", "user id:" + loginResult.getAccessToken().getUserId());
        Log.d("FBonS", "user token:" + loginResult.getAccessToken().getToken());

        final String facebookToken = loginResult.getAccessToken().getToken();

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            Log.d("fb_info", "json: " + object.toString());
                            String newBirthDay;
                            String birthDay;
                            String[] temp;
                            try {
                                birthDay = object.getString("birthday");
                                temp = birthDay.split("/");
                                newBirthDay = temp[2] + "-" + temp[0] + "-" + temp[1];
                            } catch (JSONException | NullPointerException e) {
                                newBirthDay = "0000-00-00";
                            }
                            Log.d("fb_info", "firstname: " + object.getString("first_name"));
                            Log.d("fb_info", "lastname: " + object.getString("last_name"));
                            Log.d("fb_info", "email: " + object.getString("email"));
                            Log.d("fb_info", "facebook_id: " + object.getString("id"));
                            Log.d("fb_info", "birthday: " + newBirthDay);
                            Log.d("fb_info", "gender: " + object.getString("gender"));
                            Log.d("fb_info", "facebook_token: " + facebookToken);

                            setFacebookInfo(true, facebookToken, object.getString("id"), object.getString("name"));

                            tvFacebookInfo.setText(getString(R.string.login_as, settingHelper.getFacebookName()));

//                            loginCity(LoginManager.getParamsByFacebookGraph(object, facebookToken));

                            Intent i = new Intent(FacebookLoginActivity.this, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,first_name,last_name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        mTracker.setScreenName("First Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setFacebookInfo(boolean isLogin, String facebookToken, String id, String name) {
        settingHelper.setFacebookLogin(isLogin);
        settingHelper.setFacebookToken(facebookToken);
        settingHelper.setFacebookId(id);
        settingHelper.setFacebookName(name);
    }

    private void printHashKey() {
        // Add code to print out the key hash
        try {
            @SuppressLint
                    ("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.puuga.opennote",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
