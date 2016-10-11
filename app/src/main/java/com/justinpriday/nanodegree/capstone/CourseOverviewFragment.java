package com.justinpriday.nanodegree.capstone;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Loaders.CourseCollectorTask;
import com.justinpriday.nanodegree.capstone.Models.CourseData;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CourseOverviewFragment extends Fragment implements CourseCollectorTask.CourseCallBack {
    private static final String LOG_TAG = CourseOverviewFragment.class.getSimpleName();

    private CourseData mCurrentCourse = null;
    private Context mContext = null;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.overview_course_image)
    ImageView courseImage;

    @Bind(R.id.overview_course_name)
    TextView courseName;

    @Bind(R.id.overview_date_text)
    TextView courseDate;

    @Bind(R.id.overview_distance_time)
    TextView courseDistanceTime;

    @Bind(R.id.overview_keypoints_flagged)
    TextView courseKeyPoints;

    @Bind(R.id.overview_description)
    TextView courseDescription;

    private  static final String CURRENT_COURSE_KEY = "current_course";


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(CURRENT_COURSE_KEY,mCurrentCourse);

        super.onSaveInstanceState(outState);
    }

    public CourseOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            mCurrentCourse = savedInstanceState.getParcelable(CURRENT_COURSE_KEY);
        } else {
            if (getActivity().getIntent().getExtras() != null) {
                long movieID = getActivity().getIntent().getExtras().getLong(CourseData.COURSE_DATA_ID_KEY);
                if (movieID > 0) {
                    CourseCollectorTask courseCollector = new CourseCollectorTask(mContext,this);
                    Long[] params = new Long[1];
                    params[0] = (Long)movieID;
                    courseCollector.setNoLocations();
                    courseCollector.execute(params);
                }
            }
        }

    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course_overview, container, false);
        ButterKnife.bind(this,rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar tBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (tBar != null) {
            tBar.setDisplayHomeAsUpEnabled(true);
            tBar.setDisplayShowHomeEnabled(true);
        }
        setHasOptionsMenu(false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mCurrentCourse != null) {
            updateCourseDisplay();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void updateCourseDisplay() {
        courseImage.setImageBitmap(mCurrentCourse.courseImage);
        courseName.setText(mCurrentCourse.courseName);
        DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        courseDate.setText(f.format(mCurrentCourse.courseDate));
        int courseMinutes = (int)(mCurrentCourse.courseIdealTime / 60);
        int courseSeconds = (int)(mCurrentCourse.courseIdealTime - (courseMinutes * 60));
        courseDistanceTime.setText(mCurrentCourse.courseDistance+"m ("+courseMinutes+":"+courseSeconds+")");
        courseKeyPoints.setText(mCurrentCourse.courseKeyPointCount+" points ("+mCurrentCourse.courseFlaggedCount+" flagged)");
        courseDescription.setText(mCurrentCourse.courseDescription);
    }

    @Override
    public void CourseCollectorGotCourse(CourseData courseData) {
        mCurrentCourse = courseData;
        updateCourseDisplay();
    }

    @Override
    public void CourseCollectorFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Unable to load selected Course")
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().onBackPressed();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @OnClick(R.id.overview_delete_button)
    public void deleteCoursePressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to delete this Course? This course's information will be lost.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int deletedCount = mContext.getContentResolver().delete(
                                CourseContract.CourseEntry.CONTENT_URI,
                                CourseContract.CourseEntry._ID + " = ?",
                                new String[]{String.format("%d", mCurrentCourse.id)}
                        );

                        if (deletedCount == 1){
                            Toast.makeText(getContext(), "Course Deleted", Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getContext(), "Unable to Delete Course", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public interface CallBack {
        void onReviewCourseSelected(long courseID);
        void onCompeteCourseSelected(long courseID);
    }

    @OnClick(R.id.overview_review_button)
    public void reviewCourseButtonPressed() {
        if (mCurrentCourse != null) {
            ((CallBack)getActivity()).onReviewCourseSelected(mCurrentCourse.id);
        } else {
            Log.d(LOG_TAG,"null course??");
        }

    }

    @OnClick(R.id.overview_compete_button)
    public void competeCourseButtonPressed() {
        if (mCurrentCourse != null) {
            ((CallBack)getActivity()).onCompeteCourseSelected(mCurrentCourse.id);
        } else {
            Log.d(LOG_TAG,"null course??");
        }
    }

}
