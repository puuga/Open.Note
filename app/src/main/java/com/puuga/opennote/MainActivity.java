package com.puuga.opennote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.assist.AssistContent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.puuga.opennote.helper.Constant;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.manager.APIService;
import com.puuga.opennote.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        MapFragment.OnFragmentReadyListener,
        MessageFragment.OnMessageFragmentRefresh,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // SharedPreferences
    SettingHelper settingHelper;

    // Google Analytic
    Tracker mTracker;

    // Google API
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Location mLastLocation;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    // REQUEST_LOCATION code
    private static final int REQUEST_LOCATION = 2;

    // Retrofit
    APIService service;

    // Mixpanel
    MixpanelAPI mixpanelAPI;

    // widget
    FloatingActionButton fab;

    Dialog denyLocationPermissionDialog;
    Dialog requestLocationPermissionDialog;
    Dialog submitMessageDialog;

    boolean isMessagesLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        initSharedPreferences();

        initGoogleAnalytic();

        initInstances();

        initRetrofit();

        initMixpanelAPI();

        createLocationRequest();
        buildGoogleApiClient();

        initPager();

        isMessagesLoaded = false;
    }

    private void initMixpanelAPI() {
        mixpanelAPI = MixpanelAPI.getInstance(this, getString(R.string.mixpanel_token));
    }

    private void initPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fab);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void initRetrofit() {
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        service = application.getAPIService();
    }

    private void initInstances() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setLogoDescription(R.string.app_name);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if (submitMessageDialog == null) {
                    submitMessageDialog = makeSubmitMessageDialog();
                }
                submitMessageDialog.show();
            }
        });
    }

    void submitMessage(String message, final View view) {
        Log.d("submit", message);
        Log.d("location", mCurrentLocation.toString());

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Submit message")
                .build());

        String lat = String.valueOf(mCurrentLocation.getLatitude());
        String lng = String.valueOf(mCurrentLocation.getLongitude());

        Call<Message> call = service.submitMessage(settingHelper.getAppId(), message, lat, lng);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                Message message = response.body();
                Snackbar.make(fab, "Submitted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d("submitted", message.toString());
                loadMessage();

                EditText edtMessage = ((EditText) view.findViewById(R.id.edt_message));
                edtMessage.setText("");
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.d("MainActivity", "submitMessage - onFailure", t);
            }
        });
    }

    void loadMessage() {
        if (mCurrentLocation == null) {
            return;
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Load message")
                .build());

        try {
            JSONObject props = new JSONObject();
            props.put("user", settingHelper.getFacebookName());
            if (mCurrentLocation != null) {
                props.put("location", mCurrentLocation.toString());
            }
            mixpanelAPI.track("MainActivity - loadMessage called", props);
        } catch (JSONException e) {
            Log.e(Constant.APP_NAME(this), "Unable to add properties to JSONObject", e);
        }

        String lat = String.valueOf(mCurrentLocation.getLatitude());
        String lng = String.valueOf(mCurrentLocation.getLongitude());
        Log.d("location", mCurrentLocation.toString());
        Call<Message[]> call = service.loadMessages(lat, lng);
        call.enqueue(new Callback<Message[]>() {
            @Override
            public void onResponse(Call<Message[]> call, Response<Message[]> response) {
                Message[] messages = response.body();
//                Toast.makeText(getApplicationContext(), "response", Toast.LENGTH_SHORT).show();
//                Snackbar.make(fab, "Messages loaded", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Log.d("response", "messages count:" + String.valueOf(messages.length));
                for (Message message : messages) {
                    Log.d("response", "messages :" + message.toString());
                }
                makeMarker(messages);
                drawBuffer(mCurrentLocation);
                setAdapter(messages);
                setSwipeLayoutStop();
            }

            @Override
            public void onFailure(Call<Message[]> call, Throwable t) {
                Log.d("MainActivity", "loadMessages - onFailure", t);
            }
        });
    }

    void makeMarker(Message[] messages) {
        if (messages.length == 0) {
            notiFirstPerson();
        }
        mSectionsPagerAdapter.mapFragment.makeMarkers(messages);
    }

    private void notiFirstPerson() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.yelp_first_person)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    void drawBuffer(Location location) {
        mSectionsPagerAdapter.mapFragment.drawBuffer(location);
    }

    void setAdapter(Message[] messages) {
        List<Message> messageList = new ArrayList<>(Arrays.asList(messages));
        mSectionsPagerAdapter.messageFragment.setAdapter(messageList);
    }

    void setSwipeLayoutStop() {
        mSectionsPagerAdapter.messageFragment.swipeLayout.setRefreshing(false);
    }

    private Dialog makeDenyLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.message_deny_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private Dialog makeRequestLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.message_request_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION);
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private Dialog makeSubmitMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams")
        final View v = inflater.inflate(R.layout.dialog_submit_message, null);
        builder.setView(v)
                .setPositiveButton(R.string.publish, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText edtMessage = ((EditText) v.findViewById(R.id.edt_message));
                        String message = edtMessage.getText().toString();
                        submitMessage(message, v);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        mTracker.setScreenName("Main Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // check login
        if (!settingHelper.isFacebookLogin()) {
            Intent i = new Intent(this, FacebookLoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);

        stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permission was denied or request was cancelled
                if (denyLocationPermissionDialog == null) {
                    denyLocationPermissionDialog = makeDenyLocationPermissionDialog();
                }
                denyLocationPermissionDialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
//            case R.id.action_settings:
//                return true;
            case R.id.action_logout:
                Intent iFacebookLoginActivity = new Intent(this, FacebookLoginActivity.class);
                startActivity(iFacebookLoginActivity);
                return true;
            case R.id.action_about:
                Intent iAboutActivity = new Intent(this, AboutActivity.class);
                startActivity(iAboutActivity);
                return true;
            case R.id.action_profile:
                Intent iProfileActivity = new Intent(this, ProfileActivity.class);
                startActivity(iProfileActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
                if (requestLocationPermissionDialog == null) {
                    requestLocationPermissionDialog = makeRequestLocationPermissionDialog();
                }
                requestLocationPermissionDialog.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        } else {
            // permission has been granted, continue as usual
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
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
    public void OnFragmentReady() {
        loadMessage();
    }

    @Override
    public void onMessageFragmentRefresh() {
        loadMessage();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            // showErrorDialog(result.getErrorCode());
            Log.d("GoogleAPI", "code: " + connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation == null) {
            mLastLocation = location;
            mSectionsPagerAdapter.mapFragment.moveCameraToMyLocation(location, 15, true);
        }


        mCurrentLocation = location;

        if (!isMessagesLoaded && mCurrentLocation.getAccuracy() < 100) {
            isMessagesLoaded = true;
            loadMessage();
        }

        if (mCurrentLocation.distanceTo(mLastLocation) > 200) {
            // change position
            mLastLocation = location;
            // load new message
            loadMessage();
        }
        Log.d("location", location.toString());
    }

    @Override
    public void onProvideAssistData(Bundle data) {
        String q = String.valueOf(mCurrentLocation.getLatitude()) + "," + String.valueOf(mCurrentLocation.getLongitude());
        data.putString(SearchManager.QUERY, q);
        super.onProvideAssistData(data);
    }

    @Override
    public void onProvideAssistContent(AssistContent outContent) {
        if (mCurrentLocation.hasAccuracy() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                JSONObject geo = new JSONObject()
                        .put("@type", "GeoCoordinates")
                        .put("latitude", String.valueOf(mCurrentLocation.getLatitude()))
                        .put("longitude", String.valueOf(mCurrentLocation.getLongitude()));
                JSONObject structuredJson = new JSONObject()
                        .put("@type", "Place")
                        .put("geo", geo);
                Log.d("json_provider", geo.toString());
                outContent.setStructuredData(structuredJson.toString());

            } catch (JSONException e) {
                Log.d("json_error", e.getMessage());
            }
        }

        super.onProvideAssistContent(outContent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        FloatingActionButton fab;

        MapFragment mapFragment;
        MessageFragment messageFragment;

        public SectionsPagerAdapter(FragmentManager fm, FloatingActionButton fab) {
            super(fm);
            this.fab = fab;

            mapFragment = MapFragment.newInstance();
            messageFragment = MessageFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    fab.hide();
                    mapFragment = MapFragment.newInstance();

                    return mapFragment;
                case 1:
                    fab.show();
                    messageFragment = MessageFragment.newInstance();

                    return messageFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MAP";
                case 1:
                    return "MESSAGES";
            }
            return null;
        }


    }
}
