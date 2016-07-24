package com.puuga.opennote;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
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

    // REQUEST_LOCATION code
    private static final int REQUEST_LOCATION = 2;

    Dialog denyLocationPermissionDialog;
    Dialog requestLocationPermissionDialog;

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

        if (checkLocationPermission()) {
            setMap();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
                if (requestLocationPermissionDialog == null) {
                    requestLocationPermissionDialog = makeRequestLocationPermissionDialog();
                }
                requestLocationPermissionDialog.show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMap();
                } else {
                    // Permission was denied or request was cancelled
                    if (denyLocationPermissionDialog == null) {
                        denyLocationPermissionDialog = makeDenyLocationPermissionDialog();
                    }
                    denyLocationPermissionDialog.show();
                }
            }
        }

//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    boolean checkLocationPermission() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private Dialog makeRequestLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_request_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION);
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private Dialog makeDenyLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_deny_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    void setMap() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
        if (!isCameraMoving && mGoogleMap != null) {
            isCameraMoving = true;
            this.isCameraMovingFirstTime = isCameraMovingFirstTime;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    public void moveCameraToMyLocation(Location location) {
        myLocation = location;
        if (!isCameraMoving && !isCameraMovingFirstTime && mGoogleMap != null) {
            isCameraMoving = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mGoogleMap.animateCamera(cameraUpdate);
        }

    }

    void makeMarkers(Message[] messages) {
        if (mGoogleMap == null) {
            return;
        }
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

    void drawBuffer(Location location) {
        drawCircleBuffer(location);
    }

    void drawRectangleBuffer(Location location) {
        // Instantiates a new Polygon object and adds points to define a rectangle
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(lat - 0.5, lng + 0.5),
                        new LatLng(lat + 0.5, lng + 0.5),
                        new LatLng(lat + 0.5, lng - 0.5),
                        new LatLng(lat - 0.5, lng - 0.5),
                        new LatLng(lat - 0.5, lng + 0.5))
                .strokeColor(Color.parseColor("#ff4040"));

        // Get back the mutable Polygon
        Polygon polygon = mGoogleMap.addPolygon(rectOptions);
    }

    void drawCircleBuffer(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(50000) // In meters
                .strokeColor(Color.parseColor("#ff4040"));

        // Get back the mutable Circle
        Circle circle = mGoogleMap.addCircle(circleOptions);
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

        try {
            outState.putFloat(Constant.CAMERA_ZOOM, mCameraPosition.zoom);
            outState.putDouble(Constant.CAMERA_LAT, mCameraPosition.target.latitude);
            outState.putDouble(Constant.CAMERA_LNG, mCameraPosition.target.longitude);
        } catch (Exception ignored) {

        }
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
