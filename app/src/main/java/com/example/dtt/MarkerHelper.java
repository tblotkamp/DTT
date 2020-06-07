package com.example.dtt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MarkerHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "THEDBHELPER";
    private static final String DATABASE_NAME = "marker.db";
    private static final int DATABASE_VERSION = 6;
    private SQLiteDatabase db;


    private static final String TABLE_PARKINGS = "parkings";
    private static final String COLUMN_PARKID = "_idpark";
    private static final String COLUMN_parklati = "parklatitude";
    private static final String COLUMN_parklongi = "parklongitude";
    private static final String COLUMN_parktime = "parktime";

    private static final String TABLE_JAMS = "jams";
    private static final String COLUMN_JAMID = "_idjam";
    private static final String COLUMN_jamlati = "jamlatitude";
    private static final String COLUMN_jamlongi = "jamlongitude";
    private static final String COLUMN_jamtime = "jamtime";

    private static final String TABLE_RADARS = "radars";
    private static final String COLUMN_RADARID = "_idradar";
    private static final String COLUMN_radarlati = "radarlatitude";
    private static final String COLUMN_radarlongi = "radarlongitude";
    private static final String COLUMN_radartime = "radartime";

    private static final String TABLE_PATROLS = "patrols";
    private static final String COLUMN_PATROLID = "_idpatrol";
    private static final String COLUMN_patrollati = "patrollatitude";
    private static final String COLUMN_patrollongi = "patrollongitude";
    private static final String COLUMN_patroltime = "patroltime";

    private static final String TABLE_CREATE_PARKINGS = "CREATE TABLE " + TABLE_PARKINGS + " ("
            + COLUMN_PARKID + " INTEGER PRIMARY KEY AUTOINCREMENT, "  + COLUMN_parklati
            + " TEXT, " + COLUMN_parklongi + " TEXT, " + COLUMN_parktime + " TEXT)";

    private static final String TABLE_CREATE_JAMS = "CREATE TABLE " + TABLE_JAMS + " ("
            + COLUMN_JAMID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_jamlati
            + " TEXT, " + COLUMN_jamlongi + " TEXT, " + COLUMN_jamtime + " TEXT)";

    private static final String TABLE_CREATE_RADARS = "CREATE TABLE " + TABLE_RADARS + " ("
            + COLUMN_RADARID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_radarlati
            + " TEXT, " + COLUMN_radarlongi + " TEXT, " + COLUMN_radartime + " TEXT)";

    private static final String TABLE_CREATE_PATROLS = "CREATE TABLE " + TABLE_PATROLS + " ("
            + COLUMN_PATROLID + " INTEGER PRIMARY KEY AUTOINCREMENT, "  + COLUMN_patrollati
            + " TEXT, " + COLUMN_patrollongi + " TEXT, " + COLUMN_patroltime + " TEXT)";




    public MarkerHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PARKINGS);
        db.execSQL(TABLE_CREATE_JAMS);
        db.execSQL(TABLE_CREATE_RADARS);
        db.execSQL(TABLE_CREATE_PATROLS);
        Log.i(LOGTAG, "TABLE HAS BEEN CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JAMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RADARS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATROLS);
        onCreate(db);
    }
}
