package br.com.mobi.redemicro.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Map;

public class LocationThread implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = this.getClass().getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context context;

    private GoogleApiClient googleApiClient;

    private FutureCallback callback;

    private Location bestLocation;

    public LocationThread(final Context context) {
        this.context = context;

        /*
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(R.string.dialog_config_location_title);
            alertDialogBuilder.setMessage(R.string.dialog_config_location_msg);
            alertDialogBuilder.setPositiveButton(R.string.dialog_config_location_positive,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);
                        }
                    });
            alertDialogBuilder.setNegativeButton(R.string.dialog_config_location_negative,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }
        */
    }

    public void requestLocation(FutureCallback callback) {
        this.callback = callback;
        this.googleConnect();
    }

    public void requestLocation() {
        this.callback = null;
        this.googleConnect();
    }

    private void googleConnect() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(0);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(1000 * 1);
            locationRequest.setNumUpdates(1);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    public void onConnectionFailed(ConnectionResult cr) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    public void onLocationChanged(Location location) {
        try {
            if (location != null) {
                if (isBetterLocation(location, bestLocation)) {
                    SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putFloat(Constants.LAT, (float) location.getLatitude());
                    editor.putFloat(Constants.LNG, (float) location.getLongitude());
                    editor.putFloat(Constants.ACC, location.getAccuracy());
                    editor.putLong(Constants.DATE, location.getTime());
                    editor.apply();

                    DecodeLatLngAsyncTask task = new DecodeLatLngAsyncTask();
                    task.execute(null, null, null);
                    bestLocation = location;
                }

                if (callback != null) {
                    callback.onLocationUpdate(bestLocation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private class DecodeLatLngAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

                double latitude = preferences.getFloat(Constants.LAT, 0);
                double longitude = preferences.getFloat(Constants.LNG, 0);
                GeoLocation geoLocation = new GeoLocation(context);
                geoLocation.setLatLng(latitude, longitude);
                Map<GeoLocation.TypeAddress, String> map = geoLocation.translate();
                if (map != null && !TextUtils.isEmpty(map.get(GeoLocation.TypeAddress.COMPLETE))) {
                    SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit();
                    editor.putString(Constants.ADDRESS, map.get(GeoLocation.TypeAddress.ADDRESS));
                    editor.putString(Constants.NUMBER, map.get(GeoLocation.TypeAddress.NUMBER));
                    editor.putString(Constants.PAIS, map.get(GeoLocation.TypeAddress.COUNTRY));
                    editor.putString(Constants.NEIGHBORHOOD, map.get(GeoLocation.TypeAddress.NEIGHBOR));
                    editor.putString(Constants.CITY, map.get(GeoLocation.TypeAddress.CITY));
                    editor.putString(Constants.POSTAL_CODE, map.get(GeoLocation.TypeAddress.CEP));
                    editor.putString(Constants.STATE, map.get(GeoLocation.TypeAddress.STATE));
                    editor.putString(Constants.FULL_ADDRESS, map.get(GeoLocation.TypeAddress.COMPLETE));
                    editor.apply();
                }

                if (callback != null) {
                    callback.onLocationAddressDecode();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface FutureCallback {

        void onLocationUpdate(Location location);

        void onLocationAddressDecode();

    }

}