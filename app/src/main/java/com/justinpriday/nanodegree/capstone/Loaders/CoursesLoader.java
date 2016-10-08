package com.justinpriday.nanodegree.capstone.Loaders;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.content.AsyncTaskLoader;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;

/**
 * Created by justin on 2016/09/30.
 */

public class CoursesLoader extends AsyncTaskLoader<List<CourseData>> {

    private List<CourseData> mResults;

    private static final String LOG_TAG = CoursesLoader.class.getSimpleName();

    public CoursesLoader(Context context) {
        super(context);
    }

    @Override
    public List<CourseData> loadInBackground() {
        return null;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected List<CourseData> onLoadInBackground() {
        ArrayList<CourseData> rList = new ArrayList<CourseData>();

        Cursor courseCursor = getContext().getContentResolver().query(
                CourseContract.CourseEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        ContentValues courseValues;
        try {
            while (courseCursor.moveToNext()) {
//                courseValues = new ContentValues();
//                DatabaseUtils.cursorRowToContentValues(courseCursor,courseValues);
//                rList.add(new CourseData(courseValues));
                rList.add(new CourseData(courseCursor));
            }
        } finally {
            courseCursor.close();
        }
        return rList;
    }
}
