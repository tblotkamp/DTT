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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


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
     * Upravlja kartom kad je spremna za korištenje.
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
        //Postavlja oznake parkinga
        Cursor pr = db.query("parkings", new String[] {"parklatitude",
                        "parklongitude", "parktime"}, null, null,
                null, null, null);
        if ((pr != null) && (pr.getCount() > 0)){
            pr.moveToFirst();
            while (pr.moveToNext()) {
                double lati = Double.valueOf(pr.getString(0));
                double longi = Double.valueOf(pr.getString(1));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lati, longi))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
        }
        pr.close();
        //Postavlja oznake zastoja
        Cursor za = db.query("jams", new String[] {"jamlatitude",
                        "jamlongitude"}, null, null,
                null, null, null);
        if ((za != null) && (za.getCount() > 0)){
            za.moveToFirst();
            while (za.moveToNext()) {
                double lati = Double.valueOf(za.getString(0));
                double longi = Double.valueOf(za.getString(1));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lati, longi))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
        za.close();
        Cursor ra = db.query("radars", new String[] {"radarlatitude",
                        "radarlongitude"}, null, null,
                null, null, null);
        if ((ra != null) && (ra.getCount() > 0)){
            ra.moveToFirst();
            while (ra.moveToNext()) {
                double lati = Double.valueOf(ra.getString(0));
                double longi = Double.valueOf(ra.getString(1));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lati, longi))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
        ra.close();
        Cursor pa = db.query("patrols", new String[] {"patrollatitude",
                        "patrollongitude"}, null, null,
                null, null, null);
        if ((pa != null) && (pa.getCount() > 0)){
            pa.moveToFirst();
            while (pa.moveToNext()) {
                double lati = Double.valueOf(pa.getString(0));
                double longi = Double.valueOf(pa.getString(1));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lati, longi))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }
        pa.close();

        db.close();

    }


    /**
     * Kroz dijalog provjerava odluku korisnika o stvaranju markera te ga stvara ako je odluka
     * potvrdna.
     */
    @Override
    public void onMapClick(final LatLng latLng) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MarkerActivity.this);
        alertDialog.setTitle("Odaberite oznaku");
        String[] items = {"Parking","Zastoj","Radar","Patrola"};
        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MarkerHelper markerHelper = new MarkerHelper(con);
                SQLiteDatabase db = markerHelper.getWritableDatabase();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                String formattedDate = df.format(c.getTime());
                switch (which) {
                    case 0:
                        // U slučaju parkinga
                        dialog.dismiss();
                        Toast.makeText(MarkerActivity.this, "Oznaka dodana.", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        // Spremanje lokacije markera u bazu
                        ContentValues koordParking = new ContentValues();
                        koordParking.put("parklatitude", latLng.latitude);
                        koordParking.put("parklongitude", latLng.longitude);
                        koordParking.put("parktime", formattedDate);
                        db.insert("parkings", null, koordParking);

                        break;
                    case 1:
                        // U slučaju zastoja
                        dialog.dismiss();
                        Toast.makeText(MarkerActivity.this, "Oznaka dodana.", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        // Spremanje lokacije markera u bazu
                        ContentValues zastojValues = new ContentValues();
                        zastojValues.put("jamlatitude", latLng.latitude);
                        zastojValues.put("jamlongitude", latLng.longitude);
                        zastojValues.put("jamtime", formattedDate);
                        db.insert("jams", null, zastojValues);
                        break;
                    case 2:
                        // U slučaju radara
                        dialog.dismiss();
                        Toast.makeText(MarkerActivity.this, "Oznaka dodana.", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        // Spremanje lokacije markera u bazu
                        ContentValues radarValues = new ContentValues();
                        radarValues.put("radarlatitude", latLng.latitude);
                        radarValues.put("radarlongitude", latLng.longitude);
                        radarValues.put("radartime", formattedDate);
                        db.insert("radars", null, radarValues);
                        break;
                    case 3:
                        // U slučaju patrole
                        dialog.dismiss();
                        Toast.makeText(MarkerActivity.this, "Oznaka dodana.", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        // Spremanje lokacije markera u bazu
                        ContentValues patrolaValues = new ContentValues();
                        patrolaValues.put("patrollatitude", latLng.latitude);
                        patrolaValues.put("patrollongitude", latLng.longitude);
                        patrolaValues.put("patroltime", formattedDate);
                        db.insert("patrols", null, patrolaValues);
                        break;
                }
                db.close();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
        /**new AlertDialog.Builder(this)

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
                        koordValues.put("parklatitude", latLng.latitude);
                        koordValues.put("parklongitude", latLng.longitude);
                        db.insert("parkings", null, koordValues);
                        db.close();


                    }})
                .setNegativeButton(android.R.string.no, null).show();
*/


    /**
     * Kroz dijalog provjerava odluku korisnika o brisanju i potom briše marker ako je odluka
     * potvrdna.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        new AlertDialog.Builder(this)
                .setMessage("Jeste li sigurni da želite izbrisati oznaku?")
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

                        Toast.makeText(MarkerActivity.this, "Oznaka izbrisana.", Toast.LENGTH_SHORT).show();
                        marker.remove();

                        db.delete("parkings", "parklatitude = ? ", new String[] {lati});
                        db.delete("jams", "jamlatitude = ? ", new String[] {lati});
                        db.delete("radars", "radarlatitude = ? ", new String[] {lati});
                        db.delete("patrols", "patrollatitude = ? ", new String[] {lati});
                        db.close();

                    }})
                .setNegativeButton(android.R.string.no, null).show();
        return true;
    }

}
