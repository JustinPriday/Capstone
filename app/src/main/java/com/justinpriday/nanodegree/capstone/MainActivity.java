package com.justinpriday.nanodegree.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Widget.LastCourseWidget;

public class MainActivity extends AppCompatActivity implements CourseListFragment.OnFragmentInteractionListener,CourseListFragment.CallBack {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String action = getIntent().getAction();

        if (action != null) {
            Log.d(LOG_TAG,"Got Action " + action);
            if ((action.equals(LastCourseWidget.REVIEW_CLICKED)) ||
                    (action.equals(LastCourseWidget.COMPETE_CLICKED))) {
                Cursor cursor = getContentResolver().query(CourseContract.CourseEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        CourseContract.CourseEntry.COLUMN_COURSE_DATE + " DESC " + "LIMIT 1"
                );

                if (cursor.moveToFirst()) {
                    Intent intent = new Intent(this, CourseOverviewActivity.class);
                    intent.putExtra(CourseData.COURSE_DATA_ID_KEY, cursor.getLong(cursor.getColumnIndex(CourseContract.CourseEntry._ID)));
                    intent.setAction(action);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onNewCourseSelected() {
        Intent intent = new Intent(this,CourseCreateActivity.class);
        startActivity(intent);
    }



    @Override
    public void onCourseSelected(long courseID) {
        Intent intent = new Intent(this,CourseOverviewActivity.class);
        intent.putExtra(CourseData.COURSE_DATA_ID_KEY,courseID);
        startActivity(intent);
    }
}
