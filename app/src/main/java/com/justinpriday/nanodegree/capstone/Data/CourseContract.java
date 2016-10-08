package com.justinpriday.nanodegree.capstone.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by justinpriday on 2016/09/27.
 */
public class CourseContract {
    public static final String CONTENT_AUTHORITY = "com.justinpriday.nanodegree.capstone.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_COURSE = "course";
    public static final String PATH_LOCATION = "location";
    public static final String PATH_KEY_POINT = "keyPoint";

    public static final class CourseEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

        public static final String TABLE_NAME = "course_table";

        public static final String COLUMN_COURSE_NAME = "course_name";
        public static final String COLUMN_COURSE_DATE = "course_date";
        public static final String COLUMN_COURSE_DESCRIPTION = "course_description";
        public static final String COLUMN_COURSE_IMAGE = "course_image";
        public static final String COLUMN_COURSE_DISTANCE = "course_distance";
        public static final String COLUMN_COURSE_IDEAL_TIME = "course_ideal_time";
        public static final String COLUMN_COURSE_LOCATION_COUNT = "course_location_count";
        public static final String COLUMN_COURSE_KEY_POINT_COUNT = "course_key_point_count";
        public static final String COLUMN_COURSE_FLAGGED_COUNT = "course_flagged_count";

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class LocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location_table";

        public static final String COLUMN_LOCATION_COURSE = "course_id";
        public static final String COLUMN_LOCATION_LATITUDE = "location_latitude";
        public static final String COLUMN_LOCATION_LONGITUDE = "location_longitude";
        public static final String COLUMN_LOCATION_ORDER = "location_order";
        public static final String COLUMN_LOCATION_DISTANCE = "location_distance";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

    public static final class KeyPointEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_KEY_POINT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_KEY_POINT;

        public static final String TABLE_NAME = "key_point_table";

        public static final String COLUMN_KEY_LOCATION = "key_location_id";
        public static final String COLUMN_KEY_TITLE = "key_title";
        public static final String COLUMN_KEY_DESCRIPTION = "key_description";
        public static final String COLUMN_KEY_FLAGGED = "key_flagged";
        public static final String COLUMN_KEY_PHOTO = "key_photo";

        public static Uri buildKeyPointUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

}
