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
import com.puuga.opennote.model.Message;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMyLocationChangeListener {

    private OnFragmentReadyListener mOnFragmentReady;

    GoogleMap mGoogleMap;
    boolean isCameraMoving;

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

        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

        mOnFragmentReady.OnFragmentReady();
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    public void moveCameraToMyLocation(Location location, float zoom) {
        if (!isCameraMoving) {
            isCameraMoving = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    public void moveCameraToMyLocation(Location location) {
        if (!isCameraMoving) {
            isCameraMoving = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mGoogleMap.animateCamera(cameraUpdate);
        }

    }

    void makeMarkers(Message[] messages) {
        mGoogleMap.clear();
        for (Message message : messages) {
            LatLng latLng = new LatLng(message.getLat(), message.getLng());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon96v2))
                    .title(message.getMessage()));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d("GoogleMap", cameraPosition.toString());
        isCameraMoving = false;
    }

    @Override
    public void onMyLocationChange(Location location) {
        moveCameraToMyLocation(location);
    }

    public interface OnFragmentReadyListener {
        public void OnFragmentReady();
    }

}
