package com.justinpriday.nanodegree.capstone.Data;

import android.appwidget.AppWidgetManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.justinpriday.nanodegree.capstone.R;
import com.justinpriday.nanodegree.capstone.Widget.LastCourseWidget;

/**
 * Created by justin on 2016/09/30.
 */

public class CourseContentProvider extends ContentProvider {

    public static final String ACTION_DATA_UPDATED = "com.justinpriday.nanodegree.capstone.ACTION_DATA_UPDATED";

    private SQLiteDatabase db;
    private CourseDBHelper dbHelper;

    private final static UriMatcher uriMatcher = buildUriMatcher();

    private final static int COURSES = 100;
    private final static int COURSE_WITH_ID = 101;
    private final static int LOCATIONS = 200;
    private final static int KEY_POINTS = 300;

    @Override
    public boolean onCreate() {
        dbHelper = new CourseDBHelper(getContext());
        return true;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CourseContract.CONTENT_AUTHORITY, CourseContract.PATH_COURSE, COURSES);
        matcher.addURI(CourseContract.CONTENT_AUTHORITY, CourseContract.PATH_LOCATION, LOCATIONS);
        matcher.addURI(CourseContract.CONTENT_AUTHORITY, CourseContract.PATH_KEY_POINT, KEY_POINTS);

        return matcher;
    }

    private void updateWidget() {
        Context context = getContext();
        if (context != null) {
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
            context.sendBroadcast(dataUpdatedIntent);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        Cursor rCursor = null;
        db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case COURSES:
                rCursor = db.query(CourseContract.CourseEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case LOCATIONS:
                rCursor = db.query(CourseContract.LocationEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case KEY_POINTS:
                rCursor = db.query(CourseContract.KeyPointEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

        }

        rCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return rCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case COURSES:
                long _courseId = db.insert(CourseContract.CourseEntry.TABLE_NAME, null, values);
                if (_courseId > 0) {
                    returnUri = CourseContract.CourseEntry.buildCourseUri(_courseId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case LOCATIONS:
                long _locationId = db.insert(CourseContract.LocationEntry.TABLE_NAME, null, values);
                if (_locationId > 0) {
                    returnUri = CourseContract.LocationEntry.buildLocationUri(_locationId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case KEY_POINTS:
                long _kpId = db.insert(CourseContract.KeyPointEntry.TABLE_NAME, null, values);
                if (_kpId > 0) {
                    returnUri = CourseContract.KeyPointEntry.buildKeyPointUri(_kpId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
        }
        db.close();
        getContext().getContentResolver().notifyChange(uri, null);
        updateWidget();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted = 0;
        if (selection == null) selection = "1";

        switch (match) {
            case COURSES:
                rowsDeleted = db.delete(CourseContract.CourseEntry.TABLE_NAME,selection,selectionArgs);
                break;
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            updateWidget();
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
