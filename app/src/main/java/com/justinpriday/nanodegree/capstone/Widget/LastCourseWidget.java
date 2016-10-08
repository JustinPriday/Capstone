package com.justinpriday.nanodegree.capstone.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.justinpriday.nanodegree.capstone.Data.CourseContentProvider;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.MainActivity;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by justin on 2016/10/07.
 */

public class LastCourseWidget extends AppWidgetProvider {
    public static final String REVIEW_CLICKED    = "widgetReviewMostRecentClicked";
    public static final String COMPETE_CLICKED    = "widgetCompeteMostRecentClicked";

    private static final String LOG_TAG = LastCourseWidget.class.getSimpleName();


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, CourseWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, CourseWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (CourseContentProvider.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, CourseWidgetIntentService.class));
        }
    }


}
