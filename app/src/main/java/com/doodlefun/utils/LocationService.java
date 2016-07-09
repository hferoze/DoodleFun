package com.doodlefun.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService
        implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{


    private static final String TAG = LocationService.getInstance().getClass().getSimpleName();

    private static final int LOCATION_REQUEST_INTERVAL = 10*1000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 1*1000;

    private LocationRequest mLocationRequest = null;
    private GoogleApiClient mGoogleApiClient = null;
    private static LocationService mLocationService = null;

    private OnLocationFoundListener mOnLocationFoundListener;

    private Context mContext;

    public interface OnLocationFoundListener{
        public void OnLocationFound(String location);
    }

    public static LocationService getInstance(){

        if (mLocationService==null){
            mLocationService = new LocationService();
        }

        return mLocationService;
    }

    public void getLocation(Context context, OnLocationFoundListener listener){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mLocationRequest == null){
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_REQUEST_INTERVAL)
                    .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        }

        Utils.log(TAG,"Connect Google Services");
        mContext = context;
        mOnLocationFoundListener = listener;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Utils.log(TAG, "Google Services connected!");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (lastLocation != null) {
                locationFound(lastLocation);
            }else{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO: let the user know reason for connection failure
    }

    @Override
    public void onLocationChanged(Location location) {
        locationFound(location);
    }

    private void locationFound(Location location){
        Utils.log(TAG,"location " + location);
        if (Utils.isDataAvaialable(mContext)) {
            new FindCityName().execute(location.getLatitude(), location.getLongitude());
        }else{
            Toast.makeText(mContext,"Data Connection Unavailable", Toast.LENGTH_SHORT).show();
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    private class FindCityName extends AsyncTask<Double, Void, String>{

        @Override
        protected String doInBackground(Double... params) {
            String result = "";
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(params[0], params[1], 1);
                Utils.log(TAG, "address" + addresses);
                if (addresses.size() > 0) result = addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String location) {
            super.onPostExecute(location);
            final String LOCATION_SEARCH_FAILED_KEY = "Failed";
            if (!TextUtils.isEmpty(location)) {
                mOnLocationFoundListener.OnLocationFound(location);
            }else{
                mOnLocationFoundListener.OnLocationFound(LOCATION_SEARCH_FAILED_KEY);
            }
        }
    }
}
