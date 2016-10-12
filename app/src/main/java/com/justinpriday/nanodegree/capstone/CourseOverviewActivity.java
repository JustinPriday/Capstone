package com.justinpriday.nanodegree.capstone;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Widget.LastCourseWidget;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CourseOverviewActivity extends AppCompatActivity implements CourseOverviewFragment.CallBack {

    private static final String LOG_TAG = CourseOverviewActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_overview);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar tBar = getSupportActionBar();
        if (tBar != null) {
            tBar.setDisplayHomeAsUpEnabled(true);
            tBar.setDisplayShowHomeEnabled(true);
        }
        String action = getIntent().getAction();
        Log.d(LOG_TAG,"Got Action " + action);

        if (action != null) {
            if ((action.equals(LastCourseWidget.REVIEW_CLICKED)) ||
                    (action.equals(LastCourseWidget.COMPETE_CLICKED))) {

                long courseID = getIntent().getLongExtra(CourseData.COURSE_DATA_ID_KEY, 0);
                if (courseID > 0) {
                    if (action.equals(LastCourseWidget.REVIEW_CLICKED)) {
                        onReviewCourseSelected(courseID);
                    } else if (action.equals(LastCourseWidget.COMPETE_CLICKED)) {
                        onCompeteCourseSelected(courseID);
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReviewCourseSelected(long courseID) {
        Intent intent = new Intent(this,CourseReviewActivity.class);
        intent.putExtra(CourseData.COURSE_DATA_ID_KEY,courseID);
        startActivity(intent);

    }

    @Override
    public void onCompeteCourseSelected(long courseID) {
        Intent intent = new Intent(this,CompeteActivity.class);
        intent.putExtra(CourseData.COURSE_DATA_ID_KEY,courseID);
        startActivity(intent);

    }

    @Override
    public void courseDeleted() {
    }
}
