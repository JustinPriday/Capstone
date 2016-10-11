package com.justinpriday.nanodegree.capstone.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.justinpriday.nanodegree.capstone.CourseListFragment;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by justinpriday on 2016/10/04.
 */

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {
    public static final String LOG_TAG = CourseListAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public long courseID;
        @Bind(R.id.item_course_image) ImageView courseImage;
        @Bind(R.id.item_course_name) TextView courseName;
        @Bind(R.id.item_course_date) TextView courseDate;
        @Bind(R.id.item_course_description) TextView courseDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public interface CallBack {
        void onCourseSelectedWithId(long id);
    }

    private List<CourseData> mCourses;
    private Context mContext;
    private CourseListFragment mCourseList;


    public CourseListAdapter(Context context, List<CourseData> courses) {
        mCourses = courses;
        mContext = context;
    }

    public CourseListAdapter(Context context, List<CourseData> courses, CourseListFragment courseList) {
        mCourses = courses;
        mContext = context;
        mCourseList = courseList;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View courseView = inflater.inflate(R.layout.item_course, parent, false);
        return new ViewHolder(courseView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CourseData course = mCourses.get(position);
        holder.courseID = course.id;
        holder.courseImage.setImageBitmap(course.courseImage);
        holder.courseName.setText(course.courseName);
        DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
        holder.courseDate.setText(f.format(course.courseDate));
        holder.courseDescription.setText(course.courseDescription);
        holder.itemView.setTag(holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder)v.getTag();
                ((CourseListAdapter.CallBack) mCourseList).onCourseSelectedWithId(holder.courseID);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCourses != null)
            return mCourses.size();
        return 0;
    }


}
