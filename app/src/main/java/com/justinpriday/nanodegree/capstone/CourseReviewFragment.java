package com.justinpriday.nanodegree.capstone;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.justinpriday.nanodegree.capstone.Loaders.CourseCollectorTask;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Models.CourseKeyPointData;
import com.justinpriday.nanodegree.capstone.Models.CourseLocationData;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CourseReviewFragment extends Fragment implements CourseCollectorTask.CourseCallBack,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private static final String LOG_TAG = CourseReviewFragment.class.getSimpleName();
    private static final float ANIMATION_STEP_TIME = 1000.0f;


    Context mContext = null;
    CourseData mCurrentCourse = null;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    MapView mapView;
    private boolean mapReady = false;

    private Polyline coursePoly = null;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private ArrayList<CourseKeyPointData> courseItems = null;
    private CourseLocationData mCurrentLocation;
    private Marker mCurrentPositionMarker;
    private int mCurrentLocationID;
    private int mCurrentKeyPointID;
    private ArrayList<CourseLocationData> keypointItems = null;
    private boolean animatingPosition = false;
    private double mLastSoD;
    private boolean mAnimatingForward;

    public static final String CURRENT_COURSE_KEY = "current_course";
    public static final String CURRENT_LOCATION_KEY = "current_location";
    public static final String CURRENT_LOCATION_ID_KEY = "current_location_id";
    public static final String CURRENT_KEYPOIN_ID_KEY = "current_keypoint_id";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    public CourseReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        if (savedInstanceState != null) {
            mCurrentCourse = savedInstanceState.getParcelable(CURRENT_COURSE_KEY);
            mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY);
            mCurrentLocationID = savedInstanceState.getInt(CURRENT_LOCATION_ID_KEY);
            mCurrentKeyPointID = savedInstanceState.getInt(CURRENT_LOCATION_ID_KEY);
            courseItems = new ArrayList<CourseKeyPointData>();
        } else {
            mCurrentCourse = null;
            courseItems = new ArrayList<CourseKeyPointData>();
            mCurrentLocation = null;
            mCurrentLocationID = -1;
            mCurrentKeyPointID = -1;
            if (getActivity().getIntent().getExtras() != null) {
                long courseID = getActivity().getIntent().getExtras().getLong(CourseData.COURSE_DATA_ID_KEY);
                if (courseID > 0) {
                    CourseCollectorTask courseCollector = new CourseCollectorTask(mContext,this);
                    Long[] params = new Long[1];
                    params[0] = (Long)courseID;
                    courseCollector.execute(params);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course_review, container, false);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;
        mapView.onCreate(mapViewSavedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        final Bundle mapViewSaveState = new Bundle(outState);
        mapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle("mapViewSaveState", mapViewSaveState);

        outState.putParcelable(CURRENT_COURSE_KEY,mCurrentCourse);
        outState.putParcelable(CURRENT_LOCATION_KEY,mCurrentLocation);
        outState.putInt(CURRENT_LOCATION_ID_KEY,mCurrentLocationID);
        outState.putInt(CURRENT_KEYPOIN_ID_KEY,mCurrentKeyPointID);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

    private void gotCurrentCourse() {
        Toast.makeText(mContext,"Loaded Course",Toast.LENGTH_SHORT).show();

        keypointItems = new ArrayList<CourseLocationData>();
        keypointItems.add(mCurrentCourse.courseLocations.get(0)); //Start Point
        for (CourseLocationData tLoc : mCurrentCourse.courseLocations) {
            if (tLoc.keyPointData != null)
                keypointItems.add(tLoc);
        }
        keypointItems.add(mCurrentCourse.courseLocations.get(mCurrentCourse.courseLocations.size() - 1)); //Finish Point

        if (mapReady) {
            updateMapPoly();
            updateCurrentPosition();
            if (mCurrentLocation != null)
                moveToPoint(new CourseLocationData[]{mCurrentLocation},1000);
        }
    }

    private void updateCurrentPosition() {
        if (mCurrentPositionMarker == null) {

            mCurrentPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(mCurrentLocation.locationLocation)
                    .title("")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_current_position))
                    .anchor(0.5f,0.5f)
            );

        } else {
            mCurrentPositionMarker.setPosition(mCurrentLocation.locationLocation);
        }
    }

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


            PolylineOptions newPoly = new PolylineOptions();
            for (CourseLocationData tLoc : mCurrentCourse.courseLocations) {
                newPoly.add(tLoc.locationLocation);
                if (mCurrentCourse.courseLocations.indexOf(tLoc) == 0) {
                    startMarker = mMap.addMarker(new MarkerOptions()
                            .position(tLoc.locationLocation)
                            .title("Start")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_start))
                    );
                }

                if (mCurrentCourse.courseLocations.indexOf(tLoc) == (mCurrentCourse.courseLocations.size() - 1)) {
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
            newPoly
                    .width(3)
                    .color(Color.argb(220, 0, 255, 0));
            coursePoly = mMap.addPolyline(newPoly);
        }
    }

    private void moveToPoint(CourseLocationData[] locations, int inMs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (CourseLocationData tLoc : locations) {
            builder.include(tLoc.locationLocation);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        if (inMs > 0) {
            mMap.animateCamera(cu, inMs, null);
        } else {
            mMap.moveCamera(cu);
        }
    }

    private void moveToPoint(LatLng[] locations, int inMs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng tLoc : locations) {
            builder.include(tLoc);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        if (inMs > 0) {
            mMap.animateCamera(cu, inMs, null);
        } else {
            mMap.moveCamera(cu);
        }
    }

    private void animateLocations(final ArrayList<LatLng> locations, final int endLoc) {
        final int stepDelay = (int) (ANIMATION_STEP_TIME / locations.size());
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            int countPos = 0;

            @Override
            public void run() {
                LatLng drawPos;
                if (countPos == 0) {
                    drawPos = locations.get(1);
                    countPos++;
                    locations.toArray(new LatLng[locations.size()]);
                    moveToPoint(locations.toArray(new LatLng[locations.size()]),500);
                    handler.postDelayed(this,500);
                    animatingPosition = true;
                    Log.d(LOG_TAG,"Drawing Start Position "+drawPos.latitude+", "+drawPos.longitude);
                } else if (countPos == locations.size()) {
                    drawPos = locations.get(locations.size() - 1);
                    moveToPoint(new LatLng[]{drawPos},500);
                    animatingPosition = false;
                    Log.d(LOG_TAG,"Drawing End Position "+drawPos.latitude+", "+drawPos.longitude);
                    mCurrentLocationID = endLoc;
                    mCurrentLocation = mCurrentCourse.courseLocations.get(endLoc);
                    mCurrentKeyPointID = keypointItems.indexOf(mCurrentLocation);
                } else {
                    drawPos = locations.get(countPos);
                    countPos++;
                    handler.postDelayed(this,stepDelay);
                    Log.d(LOG_TAG,"Drawing Intermediate Position "+drawPos.latitude+", "+drawPos.longitude);
                }
                mCurrentPositionMarker.setPosition(drawPos);
            }
        });
    }

    @OnClick(R.id.review_previous_button)
    public void previousButtonPressed() {
    }

    @OnClick(R.id.review_next_button)
    public void nextButtonPressed() {
        if ((mCurrentKeyPointID < (keypointItems.size() - 1)) && (!animatingPosition)) {
            ArrayList<LatLng> animateList = new ArrayList<LatLng>();

            int lastPosition = mCurrentLocationID;
            do {
//                animateList.add(mCurrentCourse.courseLocations.get(countID).locationLocation);
                lastPosition++;
            } while ((mCurrentCourse.courseLocations.get(lastPosition).keyPointData == null) &&
                    (lastPosition < mCurrentCourse.courseLocations.size()));

            if (lastPosition == mCurrentCourse.courseLocations.size())
                lastPosition = mCurrentCourse.courseLocations.size() - 1;

            for (int count = mCurrentLocationID;count <= lastPosition;count++) {
                animateList.add(mCurrentCourse.courseLocations.get(count).locationLocation);
            }

            Log.d(LOG_TAG, "Got " + animateList.size() + " animation items");
            animateLocations(animateList,(lastPosition));
        }
    }

    @Override
    public void CourseCollectorGotCourse(CourseData courseData) {
        Log.d(LOG_TAG,"Course Collected");
        mCurrentCourse = courseData;
        mCurrentLocation = mCurrentCourse.courseLocations.get(0);
        mCurrentLocationID = 0;
        mCurrentKeyPointID = 0;
        gotCurrentCourse();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "Google API Connection connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"Google API Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG,"Google API Connection failed: "+connectionResult.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG,"Map Ready");
        mapReady = true;
        mMap = googleMap;
        if (mCurrentCourse != null) {
            updateMapPoly();
            updateCurrentPosition();
            if (mCurrentLocation != null)
                moveToPoint(new CourseLocationData[]{mCurrentLocation},1000);
        }

    }
}
