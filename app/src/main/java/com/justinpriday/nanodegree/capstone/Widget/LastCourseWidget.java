package com.justinpriday.nanodegree.capstone.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.justinpriday.nanodegree.capstone.Data.CourseContentProvider;


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
