package com.example.dtt;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



public class MarkerActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MarkerActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Context con;

    // Ulazna točka za Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Trenutna lokacija uređaja, tj. zadnja poznata lokacija preuzeta od Fused Location Providera.
    private Location mLastKnownLocation;

    // Početna lokacija i zoom postavljen kada nije dano dopuštenje pristupa lokaciji uređaja.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    // Povratak lokacije i pozicije kamere nakon nastavljanja aktivnosti.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con=this;

        // Povratak lokacije i pozicije kamere nakon nastavljanja aktivnosti.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        setContentView(R.layout.activity_map);

        // Stvaramo Fused Location Provider klijent.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Gradimo kartu.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Čuva stanje karte kada je aplikacija pauzirana.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    private void getDeviceLocation() {
        /*
         * Dohvaća najbolju i najkasniju lokaciju uređaja.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Postavlja kameru mape na trenutnu lokaciju uređaja.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Šalje upit korisniku za dopuštenje korištenja lokacije.
     */
    private void getLocationPermission() {
        /*
         * Tražimo dopuštenje pristupa lokaciji uređaja.
         * Rezultat prihvaća callback metoda
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Ažurira UI mape ovisno o tome je li korisnik prihvatio zahtjev za lokacijom.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Obrađiva rezultat zahtjeva za lokacijom.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Upravlja mapom kad je spremna za korištenje.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);


        // Šalje korisniku zahtjev za pristup lokaciji uređaja.
        getLocationPermission();

        // Pali sloj lokacije i povezane kontrole nad kartom.
        updateLocationUI();

        // Dohvaća lokaciju uređaja.
        getDeviceLocation();

        //Pristup bazi za pregled markera
        MarkerHelper markerHelper = new MarkerHelper(con);
        SQLiteDatabase db = markerHelper.getReadableDatabase();
        Cursor c = db.query("markers", new String[] {"latitude",
                        "longitude"}, null, null,
                null, null, null);
        if ((c != null) && (c.getCount() > 0)){
            c.moveToFirst();
            while (c.moveToNext()) {
                double lati = Double.valueOf(c.getString(0));
                double longi = Double.valueOf(c.getString(1));
                Marker MarkerName = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lati, longi))
                        .title("Parking"));
            }
        }
        c.close();
        db.close();

    }


    /**
     * Kroz dijalog provjerava odluku korisnika o stvaranju markera te ga stvara ako je odluka
     * potvrdna.
     */
    @Override
    public void onMapClick(final LatLng latLng) {
        new AlertDialog.Builder(this)
                .setMessage("Jeste li sigurni da želite dodati parking?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        Toast.makeText(MarkerActivity.this, "Hvala.", Toast.LENGTH_SHORT).show();
                        Marker MarkerName = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Parking"));
                        // Spremanje lokacije markera u bazu
                        MarkerHelper markerHelper = new MarkerHelper(con);
                        SQLiteDatabase db = markerHelper.getWritableDatabase();
                        ContentValues koordValues = new ContentValues();
                        koordValues.put("latitude", latLng.latitude);
                        koordValues.put("longitude", latLng.longitude);
                        db.insert("markers", null, koordValues);
                        db.close();


                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    /**
     * Kroz dijalog provjerava odluku korisnika o brisanju i potom briše marker ako je odluka
     * potvrdna.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        new AlertDialog.Builder(this)
                .setMessage("Jeste li sigurni da želite izbrisati parking?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        //Pristup bazi za brisanje markera
                        MarkerHelper markerHelper = new MarkerHelper(con);
                        SQLiteDatabase db = markerHelper.getWritableDatabase();
                        double lat = (marker.getPosition()).latitude;
                        double longit = (marker.getPosition()).longitude;
                        String lati = lat+"";
                        String longi = longit+"";

                        Toast.makeText(MarkerActivity.this, "Hvala.", Toast.LENGTH_SHORT).show();
                        marker.remove();

                        db.delete("markers", "latitude = ? ", new String[] {lati});
                        db.close();

                    }})
                .setNegativeButton(android.R.string.no, null).show();
        return true;
    }

}
