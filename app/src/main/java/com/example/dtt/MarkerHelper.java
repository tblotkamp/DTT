package com.example.dtt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MarkerHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "THEDBHELPER";
    private static final String DATABASE_NAME = "marker.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;


    private static final String TABLE_MARKERS = "markers";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_lati = "latitude";
    private static final String COLUMN_longi = "longitude";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_MARKERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +  " TEXT, " + COLUMN_lati
            + " DOUBLE, " + COLUMN_longi + " DOUBLE)";



    public MarkerHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        Log.i(LOGTAG, "TABLE HAS BEEN CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);
        onCreate(db);
    }
}
