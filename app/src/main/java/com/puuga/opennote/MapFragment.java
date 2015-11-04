package com.puuga.opennote;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.puuga.opennote.helper.Constant;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMyLocationChangeListener {

    private OnFragmentReadyListener mOnFragmentReady;

    boolean isRestore = false;
    float zoom;
    double lat;
    double lng;

    GoogleMap mGoogleMap;
    CameraPosition mCameraPosition;
    boolean isCameraMoving;
    boolean isCameraMovingFirstTime;

    Location myLocation;

    // Mixpanel
    MixpanelAPI mixpanelAPI;

    // SharedPreferences
    SettingHelper settingHelper;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isCameraMoving = false;

        initSharedPreferences();
        initMixpanelAPI();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inflate the layout for this fragment
        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fg_map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnFragmentReady = (OnFragmentReadyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnFragmentReady = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("GoogleMap", "Ready");

        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnCameraChangeListener(this);
        mGoogleMap.setOnMyLocationChangeListener(this);

//        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

        if (isRestore) {
            LatLng latLng = new LatLng(lat, lng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mGoogleMap.moveCamera(cameraUpdate);
        }

        mOnFragmentReady.OnFragmentReady();
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    public void moveCameraToMyLocation(Location location, float zoom, boolean isCameraMovingFirstTime) {
        myLocation = location;
        if (!isCameraMoving) {
            isCameraMoving = true;
            this.isCameraMovingFirstTime = isCameraMovingFirstTime;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    public void moveCameraToMyLocation(Location location) {
        myLocation = location;
        if (!isCameraMoving && !isCameraMovingFirstTime) {
            isCameraMoving = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mGoogleMap.animateCamera(cameraUpdate);
        }

    }

    void makeMarkers(Message[] messages) {
        mGoogleMap.clear();
        for (Message message : messages) {
            LatLng latLng = new LatLng(message.lat, message.lng);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon96v2))
                    .title("@" + message.user.name)
                    .snippet(message.message));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d("GoogleMap", cameraPosition.toString());
        mCameraPosition = cameraPosition;

        isCameraMoving = false;
        if (cameraPosition.zoom == 15) {
            isCameraMovingFirstTime = false;
        }

        if (cameraPosition.zoom < Constant.MAP_MIN_ZOOM) {
            LatLng latLng = cameraPosition.target;
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(latLng, Constant.MAP_MIN_ZOOM);
            mGoogleMap.animateCamera(cameraUpdate);
        }

        try {
            JSONObject props = new JSONObject();
            props.put("user", settingHelper.getFacebookName());
            props.put("zoom", cameraPosition.zoom);
            props.put("latLng", cameraPosition.target.toString());
            mixpanelAPI.track("MapFragment - onCameraChange called", props);
        } catch (JSONException e) {
            Log.e(Constant.APP_NAME(getActivity()), "Unable to add properties to JSONObject", e);
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        // moveCameraToMyLocation(location);
        myLocation = location;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        zoom = savedInstanceState.getFloat(Constant.CAMERA_ZOOM);
        lat = savedInstanceState.getDouble(Constant.CAMERA_LAT);
        lng = savedInstanceState.getDouble(Constant.CAMERA_LNG);
        isRestore = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putFloat(Constant.CAMERA_ZOOM, mCameraPosition.zoom);
        outState.putDouble(Constant.CAMERA_LAT, mCameraPosition.target.latitude);
        outState.putDouble(Constant.CAMERA_LNG, mCameraPosition.target.longitude);
    }

    public interface OnFragmentReadyListener {
        public void OnFragmentReady();
    }

    private void initMixpanelAPI() {
        mixpanelAPI = MixpanelAPI.getInstance(getActivity(), getString(R.string.mixpanel_token));
    }

    private void initSharedPreferences() {
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        settingHelper = application.getSettingHelper();
    }

}
