package com.justinpriday.nanodegree.capstone.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.justinpriday.nanodegree.capstone.Data.CourseContract.CourseEntry;
import com.justinpriday.nanodegree.capstone.Data.CourseContract.LocationEntry;
import com.justinpriday.nanodegree.capstone.Data.CourseContract.KeyPointEntry;


/**
 * Created by justinpriday on 2016/09/27.
 */
public class CourseDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "courses.db";

    public CourseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_COURSE_TABLE = "CREATE TABLE " + CourseEntry.TABLE_NAME + " (" +
                CourseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CourseEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL, " +
                CourseEntry.COLUMN_COURSE_DATE + " INTEGER NOT NULL, " +
                CourseEntry.COLUMN_COURSE_DESCRIPTION + " TEXT NOT NULL, " +
                CourseEntry.COLUMN_COURSE_IMAGE + " BLOB, " +
                CourseEntry.COLUMN_COURSE_DISTANCE + " INTEGER NOT NULL, " +
                CourseEntry.COLUMN_COURSE_IDEAL_TIME + " INTEGER NOT NULL, " +
                CourseEntry.COLUMN_COURSE_LOCATION_COUNT + " INTEGER NOT NULL, " +
                CourseEntry.COLUMN_COURSE_KEY_POINT_COUNT + " INTEGER NOT NULL, " +
                CourseEntry.COLUMN_COURSE_FLAGGED_COUNT + " INTEGER NOT NULL" +
                ");";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                LocationEntry.COLUMN_LOCATION_COURSE + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_ORDER + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_DISTANCE + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + LocationEntry.COLUMN_LOCATION_COURSE + ") REFERENCES " +
                CourseEntry.TABLE_NAME + " (" + CourseEntry._ID + "));";

        final String SQL_CREATE_KEY_POINT_TABLE = "CREATE TABLE " + KeyPointEntry.TABLE_NAME + " (" +
                KeyPointEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                KeyPointEntry.COLUMN_KEY_LOCATION + " INTEGER NOT NULL, " +
                KeyPointEntry.COLUMN_KEY_TITLE + " TEXT NOT NULL, " +
                KeyPointEntry.COLUMN_KEY_DESCRIPTION + " TEXT NOT NULL, " +
                KeyPointEntry.COLUMN_KEY_PHOTO + " BLOB, " +
                KeyPointEntry.COLUMN_KEY_FLAGGED + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + KeyPointEntry.COLUMN_KEY_LOCATION + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "));";

        db.execSQL(SQL_CREATE_COURSE_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_KEY_POINT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + KeyPointEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CourseEntry.TABLE_NAME);
        onCreate(db);
    }
}
