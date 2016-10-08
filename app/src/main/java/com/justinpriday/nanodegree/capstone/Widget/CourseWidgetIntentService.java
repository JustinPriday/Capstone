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

        }

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.course_widget;

            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);
            remoteViews.setTextViewText(R.id.widget_header, mCurrentCourse.courseName);

            remoteViews.setImageViewBitmap(R.id.widget_course_image,mCurrentCourse.courseImage);

            remoteViews.setOnClickPendingIntent(R.id.widget_icon_button, getPendingIntent(this, null));
            remoteViews.setOnClickPendingIntent(R.id.course_widget_button_1, getPendingIntent(this, LastCourseWidget.REVIEW_CLICKED));
            remoteViews.setOnClickPendingIntent(R.id.course_widget_button_2, getPendingIntent(this, LastCourseWidget.COMPETE_CLICKED));




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
