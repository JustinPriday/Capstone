package com.justinpriday.nanodegree.capstone.Models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;

/**
 * Created by justinpriday on 2016/09/27.
 */

public class CourseLocationData implements Parcelable {

    private static final String LOG_TAG = CourseLocationData.class.getSimpleName();

    public long id;
    public int locationCourseID;
    public LatLng locationLocation;
    public int locationOrder;
    public double locationDistance;
    public CourseKeyPointData keyPointData;

    public CourseLocationData() {

    }



    protected CourseLocationData(Parcel in) {
        id = in.readLong();
        locationCourseID = in.readInt();
        locationLocation = new LatLng(in.readDouble(),in.readDouble());
        locationOrder = in.readInt();
        locationDistance = in.readDouble();
        keyPointData = in.readParcelable(CourseKeyPointData.class.getClassLoader());
    }

    public CourseLocationData(ContentValues inVals) {
        id = inVals.getAsLong(CourseContract.LocationEntry._ID);
        locationCourseID = inVals.getAsInteger(CourseContract.LocationEntry.COLUMN_LOCATION_COURSE);
        locationLocation = new LatLng(inVals.getAsFloat(CourseContract.LocationEntry.COLUMN_LOCATION_LATITUDE)
                ,inVals.getAsFloat(CourseContract.LocationEntry.COLUMN_LOCATION_LONGITUDE));
        locationOrder = inVals.getAsInteger(CourseContract.LocationEntry.COLUMN_LOCATION_ORDER);
        locationDistance = inVals.getAsInteger(CourseContract.LocationEntry.COLUMN_LOCATION_DISTANCE);
    }

    public CourseLocationData(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(CourseContract.LocationEntry._ID));
        locationCourseID = cursor.getInt(cursor.getColumnIndex(CourseContract.LocationEntry.COLUMN_LOCATION_COURSE));
        locationLocation = new LatLng(
                cursor.getFloat(cursor.getColumnIndex(CourseContract.LocationEntry.COLUMN_LOCATION_LATITUDE)),
                cursor.getFloat(cursor.getColumnIndex(CourseContract.LocationEntry.COLUMN_LOCATION_LONGITUDE)));
        locationOrder = cursor.getInt(cursor.getColumnIndex(CourseContract.LocationEntry.COLUMN_LOCATION_ORDER));
        locationDistance = cursor.getInt(cursor.getColumnIndex(CourseContract.LocationEntry.COLUMN_LOCATION_DISTANCE));
    }

    public static final Creator<CourseLocationData> CREATOR = new Creator<CourseLocationData>() {
        @Override
        public CourseLocationData createFromParcel(Parcel in) {
            return new CourseLocationData(in);
        }

        @Override
        public CourseLocationData[] newArray(int size) {
            return new CourseLocationData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(locationCourseID);
        dest.writeDouble(locationLocation.latitude);
        dest.writeDouble(locationLocation.longitude);
        dest.writeInt(locationOrder);
        dest.writeDouble(locationDistance);
        dest.writeParcelable(keyPointData,flags);
    }

    public ContentValues getContentValues() {
        ContentValues rVal = new ContentValues();
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_COURSE,locationCourseID);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_LATITUDE,locationLocation.latitude);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_LONGITUDE,locationLocation.longitude);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_ORDER,locationOrder);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_DISTANCE,locationDistance);
        return rVal;
    }

    public ContentValues getContentValues(int parentID) {
        ContentValues rVal = new ContentValues();
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_COURSE,parentID);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_LATITUDE,locationLocation.latitude);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_LONGITUDE,locationLocation.longitude);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_ORDER,locationOrder);
        rVal.put(CourseContract.LocationEntry.COLUMN_LOCATION_DISTANCE,locationDistance);
        return rVal;
    }
}
