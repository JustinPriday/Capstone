package com.justinpriday.nanodegree.capstone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.justinpriday.nanodegree.capstone.Loaders.CourseCollectorTask;
import com.justinpriday.nanodegree.capstone.Models.CourseData;
import com.justinpriday.nanodegree.capstone.Models.CourseLocationData;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CompeteFragment extends Fragment implements CourseCollectorTask.CourseCallBack,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, android.location.GpsStatus.Listener {

    private static final String LOG_TAG = CompeteFragment.class.getSimpleName();

    private static final int GPS_UPDATE_PERIOD = 1000;
    private static final int START_THRESHOLD = 20;
    private static final int FINISH_THRESHOLD = 5;

    private Context mContext;
    private CourseData mCurrentCourse = null;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;

    private boolean mCourseRunning = false;
    private Thread updateUIThread;


    private Location mStartCoursePosition = null;
    private int mCurrentCoursePositionID = 0;
    private Location mPreviousCoursePosition = null;
    private Location mNextCoursePosition = null;
    private Location mFollowingCoursePosition = null;

    private Location mLastLocation = null;
    private float mLastNextDistance = 0;
    private float mLastFollowingDistance;
    private float mStartTime;
    private float mLastTime;
    private float mcurrentProgress;

    private long lastUpdate = 0;
    private long updateTotal;
    private int updateCount;

    private int mSatellites = 0;
    private int mSatellitesInFix = 0;

    private static final String CURRENT_COURSE_KEY = "current_course";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.compete_text_1)
    TextView competeText1;

    @Bind(R.id.compete_text_2)
    TextView competeText2;

    @Bind(R.id.compete_text_3)
    TextView competeText3;

    @Bind(R.id.compete_text_4)
    TextView competeText4;

    @Bind(R.id.compete_text_5)
    TextView competeText5;

    @Bind(R.id.compete_start_button)
    Button startButton;

    MenuItem gpsMenuItem = null;

    public CompeteFragment() {
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

        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.addGpsStatusListener(this);

        if (savedInstanceState != null) {
            mCurrentCourse = savedInstanceState.getParcelable(CURRENT_COURSE_KEY);
        } else {
            if (getActivity().getIntent().getExtras() != null) {
                long courseID = getActivity().getIntent().getExtras().getLong(CourseData.COURSE_DATA_ID_KEY);
                if (courseID > 0) {
                    CourseCollectorTask courseCollector = new CourseCollectorTask(mContext,this);
                    Long[] params = new Long[1];
                    params[0] = (Long)courseID;
                    courseCollector.setNoPhotos();
                    courseCollector.execute(params);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(CURRENT_COURSE_KEY,mCurrentCourse);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.gps_status_menu, menu);
        gpsMenuItem = menu.findItem(R.id.gps_status_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compete, container, false);
        ButterKnife.bind(this,rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar tBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (tBar != null) {
            tBar.setDisplayHomeAsUpEnabled(true);
            tBar.setDisplayShowHomeEnabled(true);
        }
        setHasOptionsMenu(true);


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (updateUIThread != null) {
            updateUIThread.interrupt();
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

    @OnClick(R.id.compete_start_button)
    public void startPressed() {

        mCourseRunning = true;
        mCurrentCoursePositionID = 1;
        mPreviousCoursePosition = getLocationfromLatLng(mCurrentCourse.courseLocations.get(mCurrentCoursePositionID - 1).locationLocation);
        mNextCoursePosition = getLocationfromLatLng(mCurrentCourse.courseLocations.get(mCurrentCoursePositionID).locationLocation);
        mFollowingCoursePosition = getLocationfromLatLng(mCurrentCourse.courseLocations.get(mCurrentCoursePositionID + 1).locationLocation);
        mLastNextDistance = mLastLocation.distanceTo(mNextCoursePosition);
        mLastFollowingDistance = mLastLocation.distanceTo(mFollowingCoursePosition);
        startUIUpdate();
    }

    private void startUIUpdate() {
        updateUIThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateOutput();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        updateUIThread.start();
    }

    private Location getLocationfromLatLng(LatLng latLng) {
        Location location = new Location("");
        if (latLng != null) {
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            return location;
        } else {
            return null;
        }
    }

    private int calculateDistance() {
        CourseLocationData previousCourseLocation = mCurrentCourse.courseLocations.get(mCurrentCoursePositionID - 1);
        double startDistance = mCurrentCourse.courseLocations.get(mCurrentCoursePositionID - 1).locationDistance;
        double diffDistance = mCurrentCourse.courseLocations.get(mCurrentCoursePositionID).locationDistance - startDistance;

        double diffPrevious = mLastLocation.distanceTo(mPreviousCoursePosition);
        double diffNext = mLastLocation.distanceTo(mNextCoursePosition);

        return (int)(startDistance + (diffDistance / (diffPrevious + diffNext) * diffPrevious));
    }

    private void updateOutput() {
        if (mCourseRunning) {
            int currentDistance = calculateDistance();
            if (mCurrentCourse.courseDistance > 0) {
                mcurrentProgress = (float) currentDistance / (float)mCurrentCourse.courseDistance;
                competeText1.setText("Current Distance " + currentDistance + " (" + (int) (mcurrentProgress * 100) + ")");
            }
            competeText2.setText("Moving towards "+mCurrentCoursePositionID);
        } else {
            if ((mLastLocation != null) && (mStartCoursePosition != null)) {
                competeText1.setText("Distance to start " + mLastLocation.distanceTo(mStartCoursePosition));
            }
        }
        if (mLastLocation != null) {
            competeText3.setText("Accuracy "+mLastLocation.getAccuracy()+"m");
        }

        competeText4.setText(mSatellites+" satellites ("+mSatellitesInFix+" in fix)");

        competeText5.setText("Average Update Time = "+(int)((float)updateTotal / (float)updateCount));
    }

    @Override
    public void CourseCollectorGotCourse(CourseData courseData) {
        Log.d(LOG_TAG,"Course Collected");
        mCurrentCourse = courseData;
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
        Log.d(LOG_TAG,"Google connected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Best Available
//        mLocationRequest.setInterval(GPS_UPDATE_PERIOD);
        mLocationRequest.setInterval(0);

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(LOG_TAG,"No Permission for location");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"Got New Location "+location.getLatitude()+","+
                location.getLongitude()+" accurate to "+location.getAccuracy()+"m");


//        private Date lastUpdate = null;
//        private double updateTotal;
//        private int updateCount;

        if (lastUpdate == 0) {
            lastUpdate = System.currentTimeMillis();
            updateTotal = 0;
            updateCount = 0;
        } else {
            long nextDate = System.currentTimeMillis();
            updateTotal += (nextDate - lastUpdate);
            updateCount++;
            lastUpdate = nextDate;
        }

        if (mCurrentCourse != null) {
            mLastLocation = location;

            if (mCourseRunning) {
                if (mFollowingCoursePosition != null) {
                    float nextDistance = location.distanceTo(mNextCoursePosition);
                    float followingDistance = location.distanceTo(mFollowingCoursePosition);

                    float dNext = nextDistance - mLastNextDistance;
                    float dFollowing = followingDistance - mLastFollowingDistance;

                    if ((dNext > 0) && (dFollowing < 0)) {
                        //detected a position changeover.
                        mCurrentCoursePositionID++;
                        mPreviousCoursePosition = mNextCoursePosition;
                        mNextCoursePosition = mFollowingCoursePosition;
                        if (mCurrentCoursePositionID < (mCurrentCourse.courseLocations.size() - 2)) {
                            mFollowingCoursePosition = getLocationfromLatLng(mCurrentCourse.courseLocations.get(mCurrentCoursePositionID + 1).locationLocation);
                        } else {
                            mFollowingCoursePosition = null; //moving towards finish
                        }
                    } else {
                        mLastNextDistance = nextDistance;
                        mLastFollowingDistance = followingDistance;
                    }
                } else {
                    //Moving towards finish
                    if (mNextCoursePosition.distanceTo(location) < FINISH_THRESHOLD) {
                        Toast.makeText(getContext(),"Finish Found",Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (mStartCoursePosition == null) {
                    mStartCoursePosition = getLocationfromLatLng(mCurrentCourse.courseLocations.get(0).locationLocation);
                }
//                startButton.setEnabled(true);
                startButton.setEnabled(location.distanceTo(mStartCoursePosition) < START_THRESHOLD);
                updateOutput();
            }
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        int satellites = 0;
        int satellitesInFix = 0;
        if (getContext() != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            int timetofix = mLocationManager.getGpsStatus(null).getTimeToFirstFix();
            Log.i(LOG_TAG, "Time to first fix = " + timetofix);
            for (GpsSatellite sat : mLocationManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }
            mSatellites = satellites;
            mSatellitesInFix = satellitesInFix;
            if (mSatellites > 0) {
                if (mSatellitesInFix < 5) {
                    gpsMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.gps_status_none, null));
                } else if (mSatellitesInFix < 9) {
                    gpsMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.gps_status_part, null));
                } else {
                    gpsMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.gps_status_full, null));
                }
            }
        }
    }
}
