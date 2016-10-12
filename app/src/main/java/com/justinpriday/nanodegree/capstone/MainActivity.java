package com.justinpriday.nanodegree.capstone;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Widget.LastCourseWidget;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements CourseListFragment.CallBack,CourseOverviewFragment.CallBack {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String OVERVIEWFRAGMENT_TAG = "OVERVIEWTAG";
    private static final String REVIEWFRAGMENT_TAG = "REVIEWTAG";

    boolean mTwoPane = false;
    private MediaPlayer mp;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Nullable @Bind(R.id.course_overview_container)
    FrameLayout courseContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwoPane = (findViewById(R.id.course_overview_container) != null);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar tBar = getSupportActionBar();
        if (tBar != null) {
            tBar.setDisplayHomeAsUpEnabled(false);
            tBar.setDisplayShowHomeEnabled(false);
        }

        mp = new MediaPlayer();

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
                    cursor.close();
                    startActivity(intent);
                }
            }
        }
    }

    @OnClick(R.id.add_fab)
    public void addFabClicked() {
        onNewCourseSelected();
    }

    public void onNewCourseSelected() {
        Intent intent = new Intent(this,CourseCreateActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCourseSelected(long courseID) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            if (courseID > 0) {
                CourseOverviewFragment fragment = new CourseOverviewFragment();
                args.putLong(CourseData.COURSE_DATA_ID_KEY,courseID);
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.course_overview_container, fragment, OVERVIEWFRAGMENT_TAG)
                        .commit();
            }
        } else {
            Intent intent = new Intent(this, CourseOverviewActivity.class);
            intent.putExtra(CourseData.COURSE_DATA_ID_KEY, courseID);
            startActivity(intent);
        }
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
        //only called in two pane mode.
        courseContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        View inflatedLayout= inflater.inflate(R.layout.select_course, null, false);
        courseContainer.addView(inflatedLayout);
        Fragment fragment =  getSupportFragmentManager().findFragmentById(R.id.course_list_fragment);
        if (fragment instanceof CourseListFragment) {
            ((CourseListFragment) fragment).refreshCourseList();
        }
    }
}
