package com.justinpriday.nanodegree.capstone.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.R;
import com.justinpriday.nanodegree.capstone.Utility.ImageUtility;

/**
 * Created by justinpriday on 2016/09/27.
 */

public class CourseKeyPointData implements Parcelable {

    private static final String LOG_TAG = CourseKeyPointData.class.getSimpleName();

    public long id;
    public long keyLocationID;
    public String keyTitle;
    public String keyDescription;
    public boolean keyFlagged;
    public int keyFlaggedNumber;
    public int keyFlaggedDistance;
    public Marker keyMarker;

    public Bitmap keyPhoto;

    public CourseKeyPointData() {
        keyFlaggedNumber = -1;
        keyFlaggedDistance = 0;
        keyTitle = "";
        keyDescription = "";
    }


    protected CourseKeyPointData(Parcel in) {
        id = in.readLong();
        keyLocationID = in.readLong();
        keyTitle = in.readString();
        keyDescription = in.readString();
        keyFlagged = in.readByte() != 0;
        keyFlaggedNumber = in.readInt();
        byte[] byteArray = new byte[in.readInt()];
        in.readByteArray(byteArray);
        if (byteArray != null) {
            keyPhoto = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            keyPhoto = null;
        }
    }

    public CourseKeyPointData(ContentValues inVals) {
        id = inVals.getAsLong(CourseContract.KeyPointEntry._ID);
        keyLocationID = inVals.getAsInteger(CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION);
        keyTitle = inVals.getAsString(CourseContract.KeyPointEntry.COLUMN_KEY_TITLE);
        keyDescription = inVals.getAsString(CourseContract.KeyPointEntry.COLUMN_KEY_DESCRIPTION);
        keyFlaggedNumber = inVals.getAsInteger(CourseContract.KeyPointEntry.COLUMN_KEY_FLAGGED);
        keyFlagged = (keyFlaggedNumber > -1);

        byte[] byteArray = inVals.getAsByteArray(CourseContract.KeyPointEntry.COLUMN_KEY_PHOTO);
        keyPhoto = BitmapFactory.decodeByteArray(byteArray, 0 ,byteArray.length);
    }

    public CourseKeyPointData(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndex(CourseContract.KeyPointEntry._ID));
        keyLocationID = cursor.getInt(cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION));
        keyTitle = cursor.getString(cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_TITLE));
        keyDescription = cursor.getString(cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_DESCRIPTION));
        keyFlaggedNumber = cursor.getInt(cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_FLAGGED));
        keyFlagged = (keyFlaggedNumber > -1);
        if (cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_PHOTO) > -1) {
            byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(CourseContract.KeyPointEntry.COLUMN_KEY_PHOTO));
            if (byteArray != null) {
                keyPhoto = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            } else {
                keyPhoto = null;
            }
        }
    }


    public static final Creator<CourseKeyPointData> CREATOR = new Creator<CourseKeyPointData>() {
        @Override
        public CourseKeyPointData createFromParcel(Parcel in) {
            return new CourseKeyPointData(in);
        }

        @Override
        public CourseKeyPointData[] newArray(int size) {
            return new CourseKeyPointData[size];
        }
    };

    public Bitmap getMarkerForKey(Context context) {
        Bitmap jumpPic = null;
        Bitmap bgPic = null;
        if (keyPhoto != null) {
            jumpPic = keyPhoto;
        } else {
            jumpPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.jump_default);
        }

        if (keyFlagged) {
            bgPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_flagged);
        } else {
            bgPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_unflagged);
        }
        if ((bgPic != null) && (jumpPic != null)) {
            Bitmap combinedImage = Bitmap.createBitmap(bgPic.getWidth(),bgPic.getHeight(), bgPic.getConfig());
            int width = bgPic.getWidth();
            int height = bgPic.getHeight();
            Matrix matrix = new Matrix();
            float bgWidth = (float)bgPic.getWidth();
            float translate = ((2.0f * ((float)bgPic.getWidth())) / 36.0f);
            float sx = ((float)bgPic.getWidth()/(float)jumpPic.getWidth()) * (32.0f/36.0f);
            float sy = ((float)bgPic.getHeight()/(float)jumpPic.getHeight()) * (24.0f/36.0f);
            matrix.setScale(sx,sy);
            matrix.postTranslate(translate,translate);
            Canvas canvas = new Canvas(combinedImage);
            canvas.drawBitmap(jumpPic,matrix,null);
            canvas.drawBitmap(bgPic, new Matrix(), null);
            return combinedImage;
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(keyLocationID);
        dest.writeString(keyTitle);
        dest.writeString(keyDescription);
        dest.writeByte((byte) (keyFlagged ? 1 : 0));
        dest.writeInt(keyFlaggedNumber);
        ImageUtility imageUtility = new ImageUtility();
        byte[] byteArray = imageUtility.getBytesFromBitmap(keyPhoto);
        dest.writeInt(byteArray.length);
        dest.writeByteArray(byteArray);
    }

    public ContentValues getContentValues() {
        ContentValues rVal = new ContentValues();
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION,keyLocationID);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_TITLE,keyTitle);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_DESCRIPTION,keyDescription);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_FLAGGED,keyFlaggedNumber);
        ImageUtility imageUtility = new ImageUtility();
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_PHOTO, imageUtility.getBytesFromBitmap(keyPhoto));
        imageUtility = null;
        return rVal;
    }

    public ContentValues getContentValues(int locationID) {
        ContentValues rVal = new ContentValues();
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION,locationID);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_TITLE,keyTitle);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_DESCRIPTION,keyDescription);
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_FLAGGED,keyFlaggedNumber);
        ImageUtility imageUtility = new ImageUtility();
        rVal.put(CourseContract.KeyPointEntry.COLUMN_KEY_PHOTO, imageUtility.getBytesFromBitmap(keyPhoto));
        imageUtility = null;
        return rVal;
    }

}
