package com.justinpriday.nanodegree.capstone;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.justinpriday.nanodegree.capstone.Adapters.CourseListAdapter;
import com.justinpriday.nanodegree.capstone.Data.CourseContentProvider;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Loaders.CourseCollectorTask;
import com.justinpriday.nanodegree.capstone.Loaders.CoursesLoader;
import com.justinpriday.nanodegree.capstone.Models.CourseData;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CourseListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CourseData>>
        , CourseListAdapter.CallBack {

    private static final String LOG_TAG = CourseListFragment.class.getSimpleName();

//    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.recyclerView) RecyclerView courseList;

    @Bind(R.id.no_course_indicator)
    TextView noCourseIndicator;

    private Context mContext = null;

    private static final int COURSE_LOADER = 0;

    private ArrayList<CourseData> mCourseList = null;
    private CourseListAdapter mCourseListAdaptor;

    public CourseListFragment() {
        // Required empty public constructor
    }


    public static CourseListFragment newInstance(String param1, String param2) {
        CourseListFragment fragment = new CourseListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void gotNewCourses(ArrayList<CourseData> newCourses) {
        if (newCourses != null) {
            mCourseList = newCourses;

            courseList.setAdapter(new CourseListAdapter(mContext,mCourseList,this));
            courseList.invalidate();

            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Course List Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void refreshCourseList() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(COURSE_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_course_list, container, false);

        ButterKnife.bind(this, rootView);

//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        ActionBar tBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        if (tBar != null) {
//            tBar.setDisplayHomeAsUpEnabled(false);
//            tBar.setDisplayShowHomeEnabled(false);
//        }

        setHasOptionsMenu(false);

        gotNewCourses(new ArrayList<CourseData>());

        int tH = getActivity().getResources().getConfiguration().screenHeightDp;
        int tW = getActivity().getResources().getConfiguration().screenWidthDp;
        int smallerDim = (tH < tW)?tH:tW;
        boolean isTab = (smallerDim >= 600);
        boolean isPort = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if(isPort && isTab){
            courseList.setLayoutManager(new GridLayoutManager(mContext, 2));
        } else {
            courseList.setLayoutManager(new LinearLayoutManager(mContext));
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    //Loader Overrides

    @Override
    public Loader<List<CourseData>> onCreateLoader(int id, Bundle args) {
        return new CoursesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<CourseData>> loader, List<CourseData> data) {
        if (data != null) {
            gotNewCourses((ArrayList<CourseData>) data);
            noCourseIndicator.setVisibility((((ArrayList<CourseData>) data).size() > 0) ? View.GONE : View.VISIBLE);
        } else {
            gotNewCourses(new ArrayList<CourseData>());
            noCourseIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<CourseData>> loader) {

    }

    @Override
    public void onCourseSelectedWithId(long id) {
        Log.d(LOG_TAG,"Got Course with ID "+id);
        ((CallBack) getActivity()).onCourseSelected(id);
//        CourseCollectorTask courseCollector = new CourseCollectorTask(mContext,this);
//        Long[] params = new Long[1];
//        params[0] = (Long)id;
//        courseCollector.execute(params);
    }

//    @Override
//    public void CourseCollectorGotCourse(CourseData courseData) {
//        Log.d(LOG_TAG,"Got course with ID "+courseData.id+", Name: "+courseData.courseName);
//        ((CallBack) getActivity()).onCourseSelected(courseData);
//    }
//
//    @Override
//    public void CourseCollectorFailed() {
//        Log.d(LOG_TAG,"Failed to get course");
//    }


    public interface CallBack {
        void onNewCourseSelected();
        void onCourseSelected(long courseID);
    }

//    @OnClick(R.id.add_fab)
//    public void addFabClicked() {
//        ((CallBack) getActivity()).onNewCourseSelected();
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
