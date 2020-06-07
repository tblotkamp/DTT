package com.example.dtt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private final int OK_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void pokreniPregled(View view) {

        Intent intent = new Intent(this, ParkingActivity.class);
        startActivityForResult(intent, OK_CODE);
    }

    public void pokreniMarker(View view) {
        Intent intent = new Intent(this, MarkerActivity.class);
        startActivityForResult(intent, OK_CODE);
    }

    public void pokreniZastoj(View view){
        Intent intent = new Intent(this, ZastojActivity.class);
        startActivityForResult(intent, OK_CODE);
    }

    public void pokreniRadar(View view){
        Intent intent = new Intent(this, RadarActivity.class);
        startActivityForResult(intent, OK_CODE);
    }

    public void pokreniPatrole(View view){
        Intent intent = new Intent(this, PatrolActivity.class);
        startActivityForResult(intent, OK_CODE);
    }
}
