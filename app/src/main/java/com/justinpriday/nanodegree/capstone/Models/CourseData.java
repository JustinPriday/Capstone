package com.justinpriday.nanodegree.capstone.Models;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.justinpriday.nanodegree.capstone.Data.CourseContentProvider;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Utility.ImageUtility;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by justinpriday on 2016/09/27.
 */

public class CourseData implements Parcelable{

    private static final String LOG_TAG = CourseData.class.getSimpleName();

    public static final String COURSE_DATA_KEY = "course_item_extra";
    public static final String COURSE_DATA_ID_KEY = "course_item_id_extra";

    public long id;
    public String courseName;
    public Date courseDate;
    public String courseDescription;
    public Bitmap courseImage;
    public int courseDistance;
    public int courseIdealTime;
    public int courseLocationsCount;
    public ArrayList<CourseLocationData> courseLocations;
    public int courseKeyPointCount;
    public int courseFlaggedCount;

    public CourseData() {
        courseName = "";
        courseDescription = "";
        courseDate = new Date();
        courseLocations = new ArrayList<CourseLocationData>();
    }

    public CourseData(Parcel in) {
        id = in.readLong();
        courseName = in.readString();
        courseDate = new Date(in.readLong());
        courseDescription = in.readString();

        byte[] byteArray = new byte[in.readInt()];
        in.readByteArray(byteArray);
        if (byteArray != null) {
            courseImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            courseImage = null;
        }

        courseDistance = in.readInt();
        courseIdealTime = in.readInt();
        courseLocationsCount = in.readInt();
        courseKeyPointCount = in.readInt();
        courseFlaggedCount = in.readInt();
        in.readTypedList(courseLocations, CourseLocationData.CREATOR);
    }

    public CourseData(ContentValues inVals) {
        id = inVals.getAsLong(CourseContract.CourseEntry._ID);
        courseName = inVals.getAsString(CourseContract.CourseEntry.COLUMN_COURSE_NAME);
        courseDate = new Date(inVals.getAsLong(CourseContract.CourseEntry.COLUMN_COURSE_DATE));
        courseDescription = inVals.getAsString(CourseContract.CourseEntry.COLUMN_COURSE_DESCRIPTION);

        byte[] byteArray = inVals.getAsByteArray(CourseContract.CourseEntry.COLUMN_COURSE_IMAGE);
        if (byteArray != null) {
            courseImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            courseImage = null;
        }
        courseDistance = inVals.getAsInteger(CourseContract.CourseEntry.COLUMN_COURSE_DISTANCE);
        courseIdealTime = inVals.getAsInteger(CourseContract.CourseEntry.COLUMN_COURSE_IDEAL_TIME);
        courseLocationsCount = inVals.getAsInteger(CourseContract.CourseEntry.COLUMN_COURSE_LOCATION_COUNT);
        courseKeyPointCount = inVals.getAsInteger(CourseContract.CourseEntry.COLUMN_COURSE_KEY_POINT_COUNT);
        courseFlaggedCount = inVals.getAsInteger(CourseContract.CourseEntry.COLUMN_COURSE_FLAGGED_COUNT);
    }

    public CourseData(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry._ID));
        courseName = cursor.getString(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_NAME));
        courseDate = new Date (cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_DATE)));
        courseDescription = cursor.getString(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_DESCRIPTION));
        if (cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_IMAGE) > -1) {
            byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_IMAGE));
            if (byteArray != null) {
                courseImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            } else {
                courseImage = null;
            }
        }
        courseDistance = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_DISTANCE));
        courseIdealTime = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_IDEAL_TIME));
        courseLocationsCount = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_LOCATION_COUNT));
        courseKeyPointCount = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_KEY_POINT_COUNT));
        courseFlaggedCount = cursor.getInt(cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_FLAGGED_COUNT));
    }

    public static final Creator<CourseData> CREATOR = new Creator<CourseData>() {
        @Override
        public CourseData createFromParcel(Parcel in) {
            return new CourseData(in);
        }

        @Override
        public CourseData[] newArray(int size) {
            return new CourseData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(courseName);
        dest.writeLong(courseDate.getTime());
        dest.writeString(courseDescription);
        ImageUtility imageUtility = new ImageUtility();
        byte[] byteArray = imageUtility.getBytesFromBitmap(courseImage);
        dest.writeInt(byteArray.length);
        dest.writeByteArray(byteArray);
        dest.writeInt(courseDistance);
        dest.writeInt(courseIdealTime);
        dest.writeInt(courseLocationsCount);
        dest.writeInt(courseKeyPointCount);
        dest.writeInt(courseFlaggedCount);
        dest.writeTypedList(courseLocations);
        Log.d(LOG_TAG,"Got Parcel with size "+dest.dataSize());
    }

    public ContentValues getContentValues() {
        ContentValues rVal = new ContentValues();
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_NAME,this.courseName);
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_DATE,this.courseDate.getTime());
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_DESCRIPTION,this.courseDescription);
        ImageUtility imageUtility = new ImageUtility();
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_IMAGE, imageUtility.getBytesFromBitmap(courseImage));
        imageUtility = null;
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_DISTANCE,this.courseDistance);
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_IDEAL_TIME,this.courseIdealTime);
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_LOCATION_COUNT,this.courseLocationsCount);
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_KEY_POINT_COUNT,this.courseKeyPointCount);
        rVal.put(CourseContract.CourseEntry.COLUMN_COURSE_FLAGGED_COUNT,this.courseFlaggedCount);
        return rVal;
    }
}
