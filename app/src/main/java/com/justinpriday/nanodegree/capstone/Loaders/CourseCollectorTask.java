package com.justinpriday.nanodegree.capstone.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.util.Log;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Models.CourseKeyPointData;
import com.justinpriday.nanodegree.capstone.Models.CourseLocationData;

import java.util.ArrayList;

/**
 * Created by justin on 2016/10/05.
 */

public class CourseCollectorTask extends AsyncTask<Long, Void, CourseData> {

    private static final String LOG_TAG = CourseCollectorTask.class.getSimpleName();

    private Context mContext = null;
    private CourseCallBack mCallback = null;

    private CourseData collectedCourse = null;

    private boolean mNoLocations = false;
    private boolean mNoPhotoCollect = false;

    public CourseCollectorTask(Context context, CourseCallBack callBack) {
        mContext = context;
        mCallback = callBack;
    }

    public void setNoPhotos() {
        mNoPhotoCollect = true;
    }

    public void setNoLocations() {
        mNoLocations = true;
    }

    public interface CourseCallBack {
        void CourseCollectorGotCourse(CourseData courseData);
        void CourseCollectorFailed();
    }

    @Override
    protected void onPostExecute(CourseData courseData) {
        super.onPostExecute(courseData);
        Log.d(LOG_TAG,"Course Task on post");
        if (collectedCourse != null) {
            mCallback.CourseCollectorGotCourse(collectedCourse);
        } else {
            mCallback.CourseCollectorFailed();
        }
    }

    @Override
    protected CourseData doInBackground(Long... params) {
        Log.d(LOG_TAG,"Course Task do in background");
        String[] selectArgs = new String[1];
        selectArgs[0] = params[0].toString();
        String[] projection = null;
        if (mNoPhotoCollect) {
            projection = new String[] {
                CourseContract.CourseEntry._ID,
                CourseContract.CourseEntry.COLUMN_COURSE_NAME,
                CourseContract.CourseEntry.COLUMN_COURSE_DATE,
                CourseContract.CourseEntry.COLUMN_COURSE_DESCRIPTION,
                CourseContract.CourseEntry.COLUMN_COURSE_DISTANCE,
                CourseContract.CourseEntry.COLUMN_COURSE_IDEAL_TIME,
                CourseContract.CourseEntry.COLUMN_COURSE_LOCATION_COUNT,
                CourseContract.CourseEntry.COLUMN_COURSE_KEY_POINT_COUNT,
                CourseContract.CourseEntry.COLUMN_COURSE_FLAGGED_COUNT
            };
        }
        Cursor courseCursor = mContext.getContentResolver().query(
                CourseContract.CourseEntry.CONTENT_URI,
                projection,
                CourseContract.CourseEntry._ID+"=?",
                selectArgs,
                null
        );

        try {
            if (courseCursor.moveToFirst())
                collectedCourse = new CourseData(courseCursor);
        } finally {
            courseCursor.close();
        }

        if ((collectedCourse != null) && (!mNoLocations)) {
            //have a course, collect locations, course ID still in selectArgs
            collectedCourse.courseLocations = new ArrayList<CourseLocationData>();
            projection = null;
            if (mNoPhotoCollect) {
                projection = new String[]{
                    CourseContract.KeyPointEntry._ID,
                    CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION,
                    CourseContract.KeyPointEntry.COLUMN_KEY_TITLE,
                    CourseContract.KeyPointEntry.COLUMN_KEY_DESCRIPTION,
                    CourseContract.KeyPointEntry.COLUMN_KEY_FLAGGED
                };
            }
            Cursor locationCursor = mContext.getContentResolver().query(
                CourseContract.LocationEntry.CONTENT_URI,
                    null,
                    CourseContract.LocationEntry.COLUMN_LOCATION_COURSE+"=?",
                    selectArgs,
                    CourseContract.LocationEntry.COLUMN_LOCATION_ORDER
            );
            try {
                int pointCount = 1;
                while (locationCursor.moveToNext()) {
                    CourseLocationData tLoc = new CourseLocationData(locationCursor);
                    if (tLoc.id > 0) {
                        selectArgs[0] = String.valueOf(tLoc.id);
                        Cursor kpCursor = mContext.getContentResolver().query(
                                CourseContract.KeyPointEntry.CONTENT_URI,
                                projection,
                                CourseContract.KeyPointEntry.COLUMN_KEY_LOCATION + "=?",
                                selectArgs,
                                null
                        );
                        try {
                            if (kpCursor.moveToFirst()) {
                                tLoc.keyPointData = new CourseKeyPointData(kpCursor);
                                tLoc.keyPointData.keyNumber = pointCount++;
                            }
                        } finally {
                            kpCursor.close();
                        }
                    }
                    collectedCourse.courseLocations.add(tLoc);
                }
            } finally {
                locationCursor.close();
            }
        }
        return collectedCourse;
    }
}
