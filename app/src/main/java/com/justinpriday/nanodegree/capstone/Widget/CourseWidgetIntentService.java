package com.justinpriday.nanodegree.capstone.Widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.MainActivity;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.R;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Created by justin on 2016/10/07.
 */

public class CourseWidgetIntentService extends IntentService {

    private static final String LOG_TAG = CourseWidgetIntentService.class.getSimpleName();

    public CourseWidgetIntentService() {
        super("CourseWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                LastCourseWidget.class));

        CourseData mCurrentCourse = null;

        Cursor cursor = getContentResolver().query(CourseContract.CourseEntry.CONTENT_URI,
                null,
                null,
                null,
                CourseContract.CourseEntry.COLUMN_COURSE_DATE + " DESC " + "LIMIT 1"
        );

        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, "Got New Data");
            mCurrentCourse = new CourseData(cursor);
            cursor.close();
        }


        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.course_widget;

            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);

            remoteViews.setImageViewBitmap(R.id.widget_course_image,mCurrentCourse.courseImage);
            remoteViews.setTextViewText(R.id.widget_course_name, mCurrentCourse.courseName);
            DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            remoteViews.setTextViewText(R.id.widget_course_date, f.format(mCurrentCourse.courseDate));
            int courseMinutes = (int)(mCurrentCourse.courseIdealTime / 60);
            int courseSeconds = (int)(mCurrentCourse.courseIdealTime - (courseMinutes * 60));
            remoteViews.setTextViewText(R.id.widget_course_distance, mCurrentCourse.courseDistance+"m ("+courseMinutes+":"+courseSeconds+")");
            remoteViews.setTextViewText(R.id.widget_course_flagged, mCurrentCourse.courseKeyPointCount+" points ("+mCurrentCourse.courseFlaggedCount+" flagged)");
            remoteViews.setTextViewText(R.id.widget_course_description, mCurrentCourse.courseDescription);

            remoteViews.setOnClickPendingIntent(R.id.widget_icon_button, getPendingIntent(this, null));
            remoteViews.setOnClickPendingIntent(R.id.course_widget_review_button, getPendingIntent(this, LastCourseWidget.REVIEW_CLICKED));
            remoteViews.setOnClickPendingIntent(R.id.course_widget_compete_button, getPendingIntent(this, LastCourseWidget.COMPETE_CLICKED));




            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    protected PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, MainActivity.class);
        if (action != null)
            intent.setAction(action);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }
}
