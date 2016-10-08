package com.justinpriday.nanodegree.capstone;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.justinpriday.nanodegree.capstone.Data.CourseContract;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Models.CourseKeyPointData;
import com.justinpriday.nanodegree.capstone.Models.CourseLocationData;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

public class CourseCreateFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback, Animation.AnimationListener {

    private OnFragmentInteractionListener mListener;

    private static final int MINIMUM_GPS = 20;
    private static final int GPS_UPDATE_DISTANCE = 5;
    private static final int GPS_UPDATE_PERIOD = 1000;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final String LOG_TAG = CourseCreateFragment.class.getSimpleName();
    private Context mContext;

    public static final String LAST_SAVED_LOCATION_KEY = "last_saved_location";
    public static final String LOCATION_COUNT_KEY = "location_count";
    public static final String LOCATION_DISTANCE_KEY = "location_distance";
    public static final String CURRENT_COURSE_KEY = "current_course";
    public static final String COURSE_RECORDING_KEY = "course_recording";
    public static final String COURSE_COMPLETED_KEY = "course_completed";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private GoogleMap mMap;
    MapView mapView;
    private boolean mapReady = false;

    Circle mLocationCircle;
    private Polyline coursePoly = null;

    private Location mLastLocation;
    private Location mLastSavedLocation;
    private int mLocationCount;
    private float mLocationDistance;
    private LocationManager mLocationManager;
    private boolean courseRecording = false;
    private boolean courseCompleted = false;
    private boolean courseRecordingPaused = false;
    private ArrayList<CourseKeyPointData> courseItems = null;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private CourseData mCurrentCourse = null;
    private CourseKeyPointData mEditingPoint = null;

    private boolean endingOptimumTimeAnimation = false;

    private Boolean mRequestingPhotoForCourse = false;
    private CourseKeyPointData mPhotoRequestKeyPoint = null;

    //region butterknife binding

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.map_container)
    CardView mapContainerCard;

    @Bind(R.id.course_info_card)
    CardView courseInfoCard;

    @Bind(R.id.keypoint_info_card)
    CardView keypointInfoCard;

    @Bind(R.id.optimum_time_card)
    CardView optimumTimeCard;

    @Bind(R.id.course_name_edit)
    EditText courseNameEdit;

    @Bind (R.id.course_description_edit)
    EditText courseDescriptionEdit;

    @Bind(R.id.course_create_action_button)
    Button actionButton;

    @Bind(R.id.course_create_cancel_button)
    Button cancelCourseButton;

    @Bind(R.id.add_key_fab)
    FloatingActionButton addObstacleButton;

    @Bind(R.id.course_image_view)
    ImageView courseImageView;

    @Bind(R.id.course_info_distance)
    TextView courseInfoDistance;

    @Bind(R.id.course_info_obstacles)
    TextView courseInfoObstacles;

    @Bind(R.id.keypoint_image_view)
    ImageView keypointImageView;

    @Bind(R.id.keypoint_title_edit)
    EditText keypointTitleEdit;

    @Bind(R.id.keypoint_description_edit)
    EditText keypointDescriptionEdit;

    @Bind(R.id.keypoint_flagged_checkbox)
    CheckBox keypointFlaggedCheckbox;

    @Bind(R.id.keypoint_info)
    TextView keypointInfoText;

    @Bind(R.id.optimum_time_min_text)
    TextView optimumTimeMinuteText;

    @Bind(R.id.optimum_time_sech_text)
    TextView optimumTimeSecHText;

    @Bind(R.id.optimum_time_secl_text)
    TextView optimumTimeSecLText;

    //endregion

    //region animation listeners

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (mEditingPoint == null) {
            keypointInfoCard.setVisibility(View.GONE);
        }
        if (endingOptimumTimeAnimation) {
            optimumTimeCard.setVisibility(View.GONE);
            endingOptimumTimeAnimation = false;
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //endregion

    public interface CallBack {
        public void requestPhoto();
    }

    public void receivePhoto(Bitmap inPhoto) {
        if (inPhoto != null) {
            Log.d(LOG_TAG,"got photo");
        } else {
            Log.d(LOG_TAG,"got null photo");
        }
        if ((mRequestingPhotoForCourse) && (mCurrentCourse != null)) {
            mCurrentCourse.courseImage = inPhoto;
            courseImageView.setImageBitmap(inPhoto);
            mRequestingPhotoForCourse = false;
        } else if (mPhotoRequestKeyPoint != null) {
            mPhotoRequestKeyPoint.keyPhoto = inPhoto;
            keypointImageView.setImageBitmap(inPhoto);
            if (mPhotoRequestKeyPoint.keyMarker != null) {
                updateMapPoly();
            }

            mPhotoRequestKeyPoint = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        final Bundle mapViewSaveState = new Bundle(outState);
        mapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle("mapViewSaveState", mapViewSaveState);

        outState.putParcelable(LAST_SAVED_LOCATION_KEY,mLastSavedLocation);
        outState.putInt(LOCATION_COUNT_KEY,mLocationCount);
        outState.putFloat(LOCATION_DISTANCE_KEY,mLocationDistance);
        outState.putParcelable(CURRENT_COURSE_KEY,mCurrentCourse);
        outState.putBoolean(COURSE_RECORDING_KEY,courseRecording);
        outState.putBoolean(COURSE_COMPLETED_KEY,courseCompleted);

        super.onSaveInstanceState(outState);
    }

    public CourseCreateFragment() {
        // Required empty public constructor
    }

    public static CourseCreateFragment newInstance(String param1, String param2) {
        CourseCreateFragment fragment = new CourseCreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationManager = (LocationManager) getContext().getSystemService( Context.LOCATION_SERVICE );
        if (savedInstanceState != null) {
            mLastSavedLocation = savedInstanceState.getParcelable(LAST_SAVED_LOCATION_KEY);
            mLocationCount = savedInstanceState.getInt(LOCATION_COUNT_KEY,0);
            mLocationDistance = savedInstanceState.getFloat(LOCATION_DISTANCE_KEY,0);
            mCurrentCourse = savedInstanceState.getParcelable(CURRENT_COURSE_KEY);
            courseRecording = savedInstanceState.getBoolean(COURSE_RECORDING_KEY);
            courseCompleted = savedInstanceState.getBoolean(COURSE_COMPLETED_KEY);
            courseItems = new ArrayList<CourseKeyPointData>();
        } else {
            mLastLocation = null;
            mLastSavedLocation = null;
            mLocationDistance = 0;
            mCurrentCourse = new CourseData();
            courseItems = new ArrayList<CourseKeyPointData>();
            courseRecording = false;
            courseCompleted = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_course_create, container, false);

        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar tBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (tBar != null) {
            tBar.setDisplayHomeAsUpEnabled(true);
            tBar.setDisplayShowHomeEnabled(true);
        }

        if (mCurrentCourse != null) {
            updateCourseDisplay();
        }
        setHasOptionsMenu(false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;
        mapView.onCreate(mapViewSavedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        if ((mCurrentCourse != null) && courseRecording) {
            Log.d(LOG_TAG,"Got Current Course");
//            startButton.setText("Finish");
//            obstacleButton.setVisibility(View.VISIBLE);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Google API Connection connected");

        //Set up location collector.

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Best Available
        mLocationRequest.setInterval(GPS_UPDATE_PERIOD);

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"Google API Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG,"Google API Connection failed: "+connectionResult.getErrorCode());
    }

    private void updateCourseDisplay() {
        updateCourseSpecs(mCurrentCourse);
        courseNameEdit.setText(mCurrentCourse.courseName);
        courseDescriptionEdit.setText(mCurrentCourse.courseDescription);
        courseImageView.setImageBitmap(mCurrentCourse.courseImage);

        if (courseRecording || courseCompleted) {
            //show course info labels
            String accuracyString = (mLastLocation != null)?(" (acc:" + mLastLocation.getAccuracy()+"m)"):("");
            courseInfoDistance.setText(mCurrentCourse.courseDistance+"m"+accuracyString);
            courseInfoObstacles.setText(mCurrentCourse.courseKeyPointCount+" obstacles ("+mCurrentCourse.courseFlaggedCount+" flagged)");
        }

        if (mEditingPoint != null) {
            String tText = mEditingPoint.keyFlaggedDistance+"m";
            if (mEditingPoint.keyFlagged)
                tText = tText + ", flagged "+mEditingPoint.keyFlaggedNumber;
            keypointInfoText.setText(tText);
        }

        if (courseRecording) {
            actionButton.setText(getResources().getString(R.string.course_create_finish_button));
            addObstacleButton.setVisibility(View.VISIBLE);
            cancelCourseButton.setVisibility(View.GONE);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            if (courseCompleted) {
                actionButton.setText(getResources().getString(R.string.course_create_save_button));
                addObstacleButton.setVisibility(View.GONE);
                cancelCourseButton.setVisibility(View.VISIBLE);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                actionButton.setText(getResources().getString(R.string.course_create_start_button));
                addObstacleButton.setVisibility(View.GONE);
                cancelCourseButton.setVisibility(View.GONE);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void updateKeypointDisplay() {
        keypointImageView.setImageBitmap(mEditingPoint.keyPhoto);
        keypointTitleEdit.setText(mEditingPoint.keyTitle);
        keypointFlaggedCheckbox.setChecked(mEditingPoint.keyFlagged);
        keypointDescriptionEdit.setText(mEditingPoint.keyDescription);
    }

    private void startEditingKeyPoint(CourseKeyPointData inKeyPoint,boolean inAnimate) {
        boolean toAnimate = inAnimate;
        if (mEditingPoint != null)
            toAnimate = false;

        mEditingPoint = inKeyPoint;
        updateKeypointDisplay();

        if (toAnimate) {
            int[] courseLocation = new int[2];
            courseInfoCard.getLocationOnScreen(courseLocation);
            int[] mapLocation = new int[2];
            mapContainerCard.getLocationOnScreen(mapLocation);

            int startX = mapLocation[0] - courseLocation[0];
            int startY = mapLocation[1] - courseLocation[1];

            keypointInfoCard.setVisibility(View.VISIBLE);

            TranslateAnimation transAnimation = new TranslateAnimation(
                    startX, 0, startY, 0);
            Log.d(LOG_TAG, "Animating from " + startX + "," + startY);
            transAnimation.setDuration(700);
            keypointInfoCard.startAnimation(transAnimation);
        }
    }

    private void updateCourseSpecs(CourseData specCourse) {
        specCourse.courseKeyPointCount = 0;
        specCourse.courseFlaggedCount = 0;
        specCourse.courseLocationsCount = specCourse.courseLocations.size();
        int currentDistance = 0;
        Location lastLocation = null;
        int locationOrder = 1;
        for (CourseLocationData tLoc : specCourse.courseLocations) {
            tLoc.locationOrder = locationOrder++;
            Location cLocation = new Location("");
            cLocation.setLatitude(tLoc.locationLocation.latitude);
            cLocation.setLongitude(tLoc.locationLocation.longitude);
            if (lastLocation != null) {
                currentDistance += cLocation.distanceTo(lastLocation);
                tLoc.locationDistance = currentDistance;
            }
            lastLocation = cLocation;
            if (tLoc.keyPointData != null) {
                specCourse.courseKeyPointCount++;
                if (tLoc.keyPointData.keyFlagged) {
                    mCurrentCourse.courseFlaggedCount++;
                    tLoc.keyPointData.keyFlaggedNumber = mCurrentCourse.courseFlaggedCount;
                }
                tLoc.keyPointData.keyFlaggedDistance = currentDistance;
            }
        }
        specCourse.courseDistance = currentDistance;
    }

    private boolean saveCourse(CourseData saveCourse) {
        //setup completed course
        updateCourseSpecs(saveCourse);

        //save course

        Uri insertedUri = getContext().getContentResolver().insert(
                CourseContract.CourseEntry.CONTENT_URI,
                saveCourse.getContentValues()
        );
        Boolean writeSuccess = true;
        int insertedID = (int)ContentUris.parseId(insertedUri);
        if (insertedID > 0) {
            for (CourseLocationData tLoc : saveCourse.courseLocations) {
                Uri insertedLocationUri = getContext().getContentResolver().insert(
                    CourseContract.LocationEntry.CONTENT_URI,
                    tLoc.getContentValues(insertedID)
                );
                int locationID = (int)ContentUris.parseId(insertedLocationUri);
                Log.d(LOG_TAG,"Inserted Location "+locationID);
                if ((locationID > 0) && (tLoc.keyPointData != null)) {
                    Uri insertedKeyUri = getContext().getContentResolver().insert(
                        CourseContract.KeyPointEntry.CONTENT_URI,
                        tLoc.keyPointData.getContentValues(locationID)
                    );
                    int keyID = (int)ContentUris.parseId(insertedKeyUri);
                    Log.d(LOG_TAG,"Inserted Key Data "+keyID);
                }
            }
        } else {
            writeSuccess = false;
        }

        Log.d(LOG_TAG,"Inserted "+insertedID);

        return writeSuccess;
    }

    public boolean hasUnsavedData() {
        return (courseRecording || courseCompleted);
    }

    //region course description handlers

    @OnClick(R.id.course_create_action_button)
    public void courseActionButtonClicked() {
        if (mapReady && (mLastLocation != null)) {
            if (!courseRecording) {
                if (!courseCompleted) {
                    //Start Recording
                    courseRecording = true;
                    courseItems = new ArrayList<CourseKeyPointData>();
                    mLastSavedLocation = mLastLocation;
                    mLocationCount = 1;
                    mCurrentCourse.courseDate = new Date();
                    CourseLocationData tLocation = new CourseLocationData();
                    tLocation.locationLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    tLocation.locationDistance = mLocationDistance;
                    mCurrentCourse.courseLocations.add(tLocation);

                    actionButton.setText(getResources().getString(R.string.course_create_finish_button));
                    addObstacleButton.setVisibility(View.VISIBLE);
                    cancelCourseButton.setVisibility(View.GONE);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    updateMapPoly();
                } else {
                    //Save Course
                    actionButton.setText(getResources().getString(R.string.course_create_start_button));
                    cancelCourseButton.setVisibility(View.GONE);
                    saveCourse(mCurrentCourse);
                    courseCompleted = false;
                    mCurrentCourse = new CourseData();
                    updateCourseDisplay();
                    updateMapPoly();
                    getActivity().onBackPressed();
                    Toast.makeText(getContext(), "Course Saved", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Finish Recording
                courseRecording = false;
                courseCompleted = true;
                actionButton.setText(getResources().getString(R.string.course_create_save_button));
                addObstacleButton.setVisibility(View.INVISIBLE);
                cancelCourseButton.setVisibility(View.VISIBLE);

                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                CourseLocationData tLocation = new CourseLocationData();
                tLocation.locationLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                tLocation.locationDistance = mLocationDistance;
                mCurrentCourse.courseLocations.add(tLocation);


                endMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("Finish")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_finish))
                );
            }
        }
    }

    @OnClick(R.id.course_create_cancel_button)
    public void cancelCourse() {
        if ((!courseRecording) && (courseCompleted)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Are you sure you want to cancel this Course? This course's information will be lost.")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mCurrentCourse = null;
                            getActivity().onBackPressed();
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
    }

    @OnClick(R.id.add_key_fab)
    public void addObstableClicked() {
        mLocationDistance += mLastLocation.distanceTo(mLastSavedLocation);
        mLastSavedLocation = mLastLocation;
        mLocationCount++;
        mCurrentCourse.courseDate = new Date();
        CourseLocationData tLocation = new CourseLocationData();
        tLocation.locationLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        tLocation.locationDistance = mLocationDistance;
        tLocation.keyPointData = new CourseKeyPointData();
        mCurrentCourse.courseLocations.add(tLocation);
        startEditingKeyPoint(tLocation.keyPointData,true);
        updateMapPoly();
        updateCourseDisplay();
    }

    @OnClick(R.id.take_course_picture_button)
    public void takeCoursePicture() {
        mRequestingPhotoForCourse = true;
        mPhotoRequestKeyPoint = null;
        ((CourseCreateFragment.CallBack) getActivity()).requestPhoto();
    }

    @OnTextChanged(R.id.course_name_edit)
    public void courseNameChanged() {
        mCurrentCourse.courseName = courseNameEdit.getText().toString();
    }

    @OnEditorAction(R.id.course_name_edit)
    public boolean courseNameEditAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(courseNameEdit.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.course_description_edit)
    public void courseDescriptionChanged() {
        mCurrentCourse.courseDescription = courseDescriptionEdit.getText().toString();
    }

    @OnEditorAction(R.id.course_description_edit)
    public boolean courseDescriptionEditAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(courseNameEdit.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void editOptimumTime(int adjust) {
        if ((adjust > 0) || (Math.abs(adjust) < mCurrentCourse.courseIdealTime)) {
            mCurrentCourse.courseIdealTime += adjust;
        } else {
            mCurrentCourse.courseIdealTime = 0;
        }
        int cIdealTime = mCurrentCourse.courseIdealTime;
        int mins = cIdealTime / 60;
        cIdealTime = cIdealTime - (mins * 60);
        int secH = cIdealTime / 10;
        int secL = cIdealTime - (secH * 10);
        optimumTimeMinuteText.setText(String.valueOf(mins));
        optimumTimeSecHText.setText(String.valueOf(secH));
        optimumTimeSecLText.setText(String.valueOf(secL));
    }

    @OnClick(R.id.optimum_time_edit_min_plus)
    public void optimumTimeMinuteAdd() {
        editOptimumTime(60);
    }

    @OnClick(R.id.optimum_time_edit_min_minus)
    public void optimumTimeMinuteSubtract() {
        editOptimumTime(-60);
    }

    @OnClick(R.id.optimum_time_edit_sech_plus)
    public void optimumTimeSecHAdd() {
        editOptimumTime(10);
    }

    @OnClick(R.id.optimum_time_edit_sech_minus)
    public void optimumTimeSecHSubtract() {
        editOptimumTime(-10);
    }

    @OnClick(R.id.optimum_time_edit_secl_plus)
    public void optimumTimeSecLAdd() {
        editOptimumTime(1);
    }

    @OnClick(R.id.optimum_time_edit_secl_minus)
    public void optimumTimeSecLSubtract() {
        editOptimumTime(-1);
    }

    @OnClick(R.id.optimum_time_button)
    public void optimeTimePressed() {
        int[] courseLocation = new int[2];
        courseInfoCard.getLocationOnScreen(courseLocation);
        int[] mapLocation = new int[2];
        mapContainerCard.getLocationOnScreen(mapLocation);

        int startX = mapLocation[0] - courseLocation[0];
        int startY = mapLocation[1] - courseLocation[1];

        optimumTimeCard.setVisibility(View.VISIBLE);

        TranslateAnimation transAnimation = new TranslateAnimation(
                startX, 0, startY, 0);
        Log.d(LOG_TAG, "Animating from " + startX + "," + startY);
        transAnimation.setDuration(700);
        transAnimation.setAnimationListener(this);
        editOptimumTime(0);
        optimumTimeCard.startAnimation(transAnimation);
    }

    //endregion

    //region Keypoint handlers

    @OnClick(R.id.take_keypoint_picture_button)
    public void takeKeyPointPicture() {
        mRequestingPhotoForCourse = false;
        mPhotoRequestKeyPoint = mEditingPoint;
        ((CourseCreateFragment.CallBack) getActivity()).requestPhoto();
    }

    @OnClick(R.id.keypoint_flagged_checkbox)
    public void flaggedClicked() {
        mEditingPoint.keyFlagged = keypointFlaggedCheckbox.isChecked();
        updateMapPoly();
        updateCourseDisplay();
    }

    @OnTextChanged(R.id.keypoint_title_edit)
    public void keypointTitleChanged() {
        if (mEditingPoint != null)
            mEditingPoint.keyTitle = keypointTitleEdit.getText().toString();
    }

    @OnEditorAction(R.id.keypoint_title_edit)
    public boolean keypointTitleEditAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(keypointTitleEdit.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.keypoint_description_edit)
    public void keypointDescriptionChanged() {
        if (mEditingPoint != null)
            mEditingPoint.keyDescription = keypointDescriptionEdit.getText().toString();
    }

    @OnEditorAction(R.id.keypoint_description_edit)
    public boolean keypointDescriptionEditAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(keypointDescriptionEdit.getWindowToken(), 0);
            return true;
        }
        return false;
    }


    @OnClick(R.id.keypoint_action_button)
    public void keypointActionPressed() {
        mEditingPoint = null;
        int[] courseLocation = new int[2];
        courseInfoCard.getLocationOnScreen(courseLocation);
        int[] mapLocation = new int[2];
        mapContainerCard.getLocationOnScreen(mapLocation);

        int endX = mapLocation[0] - courseLocation[0];
        int endY = mapLocation[1] - courseLocation[1];

        TranslateAnimation transAnimation = new TranslateAnimation(
                0, endX, 0, endY);
        transAnimation.setDuration(700);
        transAnimation.setAnimationListener(this);
        keypointInfoCard.startAnimation(transAnimation);

    }

    //endregion

    @OnClick(R.id.optimum_time_done_button)
    public void optimumDonePressed() {
        int[] courseLocation = new int[2];
        courseInfoCard.getLocationOnScreen(courseLocation);
        int[] mapLocation = new int[2];
        mapContainerCard.getLocationOnScreen(mapLocation);

        int endX = mapLocation[0] - courseLocation[0];
        int endY = mapLocation[1] - courseLocation[1];

        optimumTimeCard.setVisibility(View.VISIBLE);

        TranslateAnimation transAnimation = new TranslateAnimation(
                0, endX, 0, endY);
        transAnimation.setDuration(700);
        transAnimation.setAnimationListener(this);
        endingOptimumTimeAnimation = true;
        optimumTimeCard.startAnimation(transAnimation);

    }

    //region optimum time handlers

    //endregion

    private void updateMapPoly() {
        if (mMap != null) {
            if (coursePoly != null) {
                coursePoly.remove();
            }
            if (startMarker != null)
                startMarker.remove();
            if (endMarker != null)
                endMarker.remove();

            for (CourseKeyPointData tKey : courseItems) {
                tKey.keyMarker.remove();
            }

            courseItems.clear();


            if ((courseRecording) || (courseCompleted)) {
                PolylineOptions newPoly = new PolylineOptions();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (CourseLocationData tLoc : mCurrentCourse.courseLocations) {
                    newPoly.add(tLoc.locationLocation);
                    builder.include(tLoc.locationLocation);
                    if (mCurrentCourse.courseLocations.indexOf(tLoc) == 0) {
                        startMarker = mMap.addMarker(new MarkerOptions()
                                .position(tLoc.locationLocation)
                                .title("Start")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_start))
                        );
                    }

                    if ((mCurrentCourse.courseLocations.indexOf(tLoc) == (mCurrentCourse.courseLocations.size() - 1)) && (courseCompleted)) {
                        endMarker = mMap.addMarker(new MarkerOptions()
                                .position(tLoc.locationLocation)
                                .title("Finish")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_finish))
                        );
                    }

                    if (tLoc.keyPointData != null) {
                        tLoc.keyPointData.keyMarker = mMap.addMarker(new MarkerOptions()
                                .position(tLoc.locationLocation)
                                .title("Obstacle")
                                .icon(BitmapDescriptorFactory.fromBitmap(tLoc.keyPointData.getMarkerForKey(getContext())))
                        );
                        courseItems.add(tLoc.keyPointData);
                    }
                }
                LatLngBounds bounds = builder.build();
                newPoly
                        .width(3)
                        .color(Color.argb(220, 0, 255, 0));
                coursePoly = mMap.addPolyline(newPoly);
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu, 1000, null);
            } else {
                if (mLastLocation != null) {
                    LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraPosition target = CameraPosition.builder().target(loc).zoom(15).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 2000, null);
                }
            }
        }
    }

    private void editKeyPoint(CourseKeyPointData keyPoint) {
        mEditingPoint = keyPoint;
    }

    @Override
    public void onLocationChanged(Location location) {
        if ((mLastLocation == null) && (mapReady)) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition target = CameraPosition.builder().target(loc).zoom(15).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        }
        mLastLocation = location;
        if ((courseRecording) || (courseCompleted)) {
            courseInfoDistance.setText(mCurrentCourse.courseDistance+"m (acc:" + (int)mLastLocation.getAccuracy()+"m)");
        } else {
            courseInfoDistance.setText("GPS accuracy: " + (int)mLastLocation.getAccuracy()+"m");
        }
        if (courseRecording) {
            if ((location.distanceTo(mLastSavedLocation) > GPS_UPDATE_DISTANCE) &&
                    (location.distanceTo(mLastSavedLocation) > location.getAccuracy())){
                mLocationDistance += location.distanceTo(mLastSavedLocation);
                mLocationCount++;
                mLastSavedLocation = location;
                updateMapPoly();
                CourseLocationData tLocation = new CourseLocationData();
                tLocation.locationLocation = new LatLng(location.getLatitude(),location.getLongitude());
                tLocation.locationDistance = mLocationDistance;
                mCurrentCourse.courseLocations.add(tLocation);
                updateCourseDisplay();
            }
        }

        if (mapReady) {
            if (mLocationCircle != null) {
                mLocationCircle.remove();
            }
            mLocationCircle = mMap.addCircle(
                    new CircleOptions()
                            .center(new LatLng(location.getLatitude(),location.getLongitude()))
                            .radius(location.getAccuracy())
                            .fillColor(Color.argb(80,255,0,0))
                            .strokeColor(Color.argb(255,255,0,0))
                            .strokeWidth(1)
            );
        }

        if (location.getAccuracy() < MINIMUM_GPS) {
//            locationTextView5.setText("Approved Location");
        } else {
//            locationTextView5.setText("Insufficient Location");
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        mMap = map;
        if (mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraPosition target = CameraPosition.builder().target(loc).zoom(15).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        }
        if (mCurrentCourse != null) {
            updateMapPoly();
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(LOG_TAG,"got Marker "+marker.getId());
                CourseKeyPointData foundItem = null;
                for (CourseKeyPointData tKey : courseItems) {
                    Log.d(LOG_TAG,"checking "+tKey.keyMarker.getId());
                    if (tKey.keyMarker.getId().equals(marker.getId())) {
                        foundItem = tKey;
                        startEditingKeyPoint(foundItem,true);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
