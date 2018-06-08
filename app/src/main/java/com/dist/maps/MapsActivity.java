package com.dist.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.People;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static String TAG = MapsActivity.class.getSimpleName();
    View view;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    Button btnCalc;
    TextView txtDistance, txtDuration;
    String strSource, strDestin;
    RequestQueue requestQueue;
    LatLng originLatLng, desgnLatLng;
    private double currentLatitude;
    private double currentLongitude;
    private GoogleMap mMap;
    private Gson gson;

    String strDistance, strDuration;

    MapsDatabase mapsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.go_googleMap);
        mapFragment.getMapAsync(this);
        txtDistance = (TextView) findViewById(R.id.txtTDistance);
        txtDuration = (TextView) findViewById(R.id.txtTDuration);
        requestQueue = Volley.newRequestQueue(this);

        mapsDatabase = MapsDatabase.getDatabase(getApplicationContext());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        btnCalc = (Button) findViewById(R.id.btnCalcDistance);
        btnCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateDistance();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }

    }

    protected synchronized void buildGoogleApiClient() {
        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @SuppressLint({"MissingPermission", "RestrictedApi"})
    @Override
    public void onConnected(Bundle bundle) {
             Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
             mLocationRequest = new LocationRequest().create()
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onConnected: ");}
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            originLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "Lat Lon Value" + originLatLng);
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                strSource = addresses.get(0).getSubLocality();
                Log.d("My Current Locality", "" + strSource);
                Toast.makeText(this, "My Sub Locality " + strSource, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Your Here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(21));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
       // Toast.makeText(this, "Location Changed: " + latLng, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            buildGoogleApiClient();
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /*private void calculateDistance() {
        EditText locationSearch = (EditText) findViewById(R.id.edtDesignation);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        if (!TextUtils.isEmpty(location)) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            if (address != null) {
                desgnLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                strDestin = address.getLocality();
                mMap.addMarker(new MarkerOptions().position(desgnLatLng).title("Your Designation"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(desgnLatLng));
                Toast.makeText(this, "My destination Locality " + strDestin, Toast.LENGTH_LONG).show();
            }
            final String jsonURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + strSource + "&destination=" + strDestin;// + "&key=";
            Log.d(TAG, "JSON URL: " + jsonURL);

            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, jsonURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override

                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                try {
                                    JSONArray ja = response.getJSONArray("routes");
                                    if (ja != null) {
                                        for (int i = 0; i < ja.length(); i++) {
                                            JSONObject jsonObject = ja.getJSONObject(i);
                                            if (jsonObject != null) {
                                                JSONArray legsArray = jsonObject.getJSONArray("legs");
                                                if (legsArray != null) {
                                                    JSONObject legs = legsArray.getJSONObject(0);
                                                    if (legs != null) {
                                                        JSONObject distanceObj = legs.getJSONObject("distance");
                                                        if (distanceObj != null) {
                                                            String strDistance = distanceObj.getString("text");
                                                            txtDistance.setText("Distance: " + strDistance);

                                                            JSONObject durationObj = legs.getJSONObject("duration");
                                                            if (durationObj != null) {
                                                                String strDuration = durationObj.getString("text");
                                                                txtDuration.setText("Duration: " + strDuration);
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley", "Error");
                        }
                    }
            );
            requestQueue.add(jor);
        }
    }*/
    @Override
    protected void onResume() {
        super.onResume();
        buildGoogleApiClient();
    }

    private void calculateDistance() {

        EditText locationSearch = (EditText) findViewById(R.id.edtDesignation);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        if (!TextUtils.isEmpty(location)) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            if (address != null) {
                desgnLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                strDestin = address.getLocality();
                mMap.addMarker(new MarkerOptions().position(desgnLatLng).title("Your Designation"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(desgnLatLng));
                Toast.makeText(this, "My destination Locality " + strDestin, Toast.LENGTH_SHORT).show();
            }
            final String jsonURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + strSource + "&destination=" + strDestin;// + "&key=";
            Log.d(TAG, "JSON URL: " + jsonURL);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, jsonURL, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    MapsPOJO mapsPOJO = gson.fromJson(response.toString(), MapsPOJO.class);
                    Log.d(TAG, "mapsPOJO" + mapsPOJO);

                    strDistance = mapsPOJO.getRoutes().get(0).getLegs().get(0).getDistance().getText();
                    strDuration = mapsPOJO.getRoutes().get(0).getLegs().get(0).getDuration().getText();

                    txtDistance.setText("Distance : " + strDistance);
                    txtDuration.setText("Duration : " + strDuration);


                    final Maps maps = new Maps();
                    maps.setmSource(strSource);
                    maps.setmDestination(strDestin);
                    maps.setmDistance(strDistance);
                    maps.setmDuration(strDuration);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mapsDatabase.mapsDao().addMaps(maps);
                            return null;
                        }
                    }.execute();

                    Toast.makeText(getApplicationContext(), "Added into MapsDB successfully", Toast.LENGTH_SHORT).show();

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            List<Maps> listMaps = mapsDatabase.mapsDao().getMapsList();
                            String mapsInfo ="";
                            for(Maps map : listMaps){
                                String source = map.getmSource();
                                String destination = map.getmDestination();
                                String distance = map.getmDistance();
                                String duration = map.getmDuration();
                                mapsInfo = mapsInfo +source+ " - " + destination+ " - "+distance+ " - " +duration+"\n \n";
                                Log.d(TAG, "Read_DB " + mapsInfo);
                            }
                            return null;
                        }
                    }.execute();
                    /*List<Maps> listMaps = mapsDatabase.mapsDao().getMapsList();
                    String mapsInfo ="";
                    for(Maps map : listMaps){
                        String source = map.getmSource();
                        String destination = map.getmDestination();
                        String distance = map.getmDistance();
                        String duration = map.getmDuration();
                        mapsInfo = mapsInfo +source+ " - " + destination+ " - "+distance+ " - " +duration+"\n \n";
                        Log.d(TAG, "Read_DB " + mapsInfo);
                    }*/

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley", "Error");
                }
            });

            requestQueue.add(request);
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mGoogleApiClient != null&&mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
}