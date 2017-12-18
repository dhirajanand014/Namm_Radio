package aegismatrix.com.namm_radio;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dhiraj on 06-05-2017.
 */


public class NammRadioSplashScreen extends Activity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private boolean connection = false;
    private LocationManager locationManager;
    private boolean mLocationUpdate = false;
    private Location mLocation;
    private AddressResultReceiver mResultReceiver;
    private String mCountryCode;
    private Bitmap[] mImages;
    private ImageLoader imageLoader;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private AppCompatTextView mainTextSplashScreen;
    private ImageView nammRadioConnectivityChange;
    private List<ProgramItemSchedulerModel> programItemSchedulerModelList = new ArrayList<>();
    private JSONArray mJsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateSavedInstance(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_splash_screen);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkChangeReceiver, filter);
    }

    private void updateSavedInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains("LocUpdate")) {
                mLocationUpdate = savedInstanceState.getBoolean("LocUpdate");
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            if (savedInstanceState.keySet().contains("CurrentLoc")) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mLocation = savedInstanceState.getParcelable("CurrentLoc");
            }
            if (savedInstanceState.keySet().contains("LastCountry")) {
                mCountryCode = savedInstanceState.getString("LastCountry");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (connection) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 11);
            return;
        }
        if (mLocationUpdate) {
            mLocationUpdate = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        performImageAnimation();
    }

    private void performImageAnimation() {
        ImageView nammRadioSplashImage = (ImageView) findViewById(R.id.nammRadioLandingImage);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.namm_radio_bounce_startup);
        myAnim.setInterpolator(new Interpolator() {
            double mAmplitude = 0.2;
            double mFrequency = 20;

            @Override
            public float getInterpolation(float time) {
                return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                        Math.cos(mFrequency * time) + 1);
            }
        });
        nammRadioSplashImage.startAnimation(myAnim);
        nammRadioConnectivityChange = (ImageView) findViewById(R.id.connectivityChange);
        ((AnimationDrawable) nammRadioConnectivityChange.getBackground()).start();
    }

    private void locateDevice() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GlobalConstants.PERMISION_REQUEAT_CODE);
        } else {
            if (mainTextSplashScreen != null) {
                mainTextSplashScreen.setText(R.string.fetchLocation);
            }
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    getLocationAddress();
                }
            });
            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(NammRadioSplashScreen.this, 11);
                            } catch (IntentSender.SendIntentException sendEx) {
                                Log.d(sendEx.getMessage(), "Error on loading settings");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case 11:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocationAddress();
                        break;
                    case Activity.RESULT_CANCELED:
                        locateDevice();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    private void getLocationAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 11);
            return;
        }
        //PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GlobalConstants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(GlobalConstants.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(GlobalConstants.DISPLACEMENT);
    }

    private void buildGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode, GlobalConstants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case GlobalConstants.PLAY_SERVICES_RESOLUTION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleAPIClient();
                    }
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("LocUpdate", mLocationUpdate);
        outState.putParcelable("CurrentLoc", mLocation);
        outState.putString("LastCountry", mCountryCode);
        super.onSaveInstanceState(outState);
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            NetworkInfo info = intent.getExtras().getParcelable("networkInfo");
            NetworkInfo.State state = info.getState();

            mainTextSplashScreen = (AppCompatTextView) findViewById(R.id.changingText);
            mainTextSplashScreen.setText(R.string.connectingInternet);
            mainTextSplashScreen.postInvalidate();

            NammRadioSplashScreen nammRadioSpashScreen = (NammRadioSplashScreen) context;
            LinearLayout linearLayout = (LinearLayout) nammRadioSpashScreen.findViewById(R.id.noConnectionLayout);
            AppCompatTextView appCompatTextView = (AppCompatTextView) linearLayout.getChildAt(0);

            if (state == NetworkInfo.State.CONNECTED) {
                appCompatTextView.setText("");
                connection = true;
                if (checkPlayServices()) {
                    buildGoogleAPIClient();
                    createLocationRequest();
                    nammRadioSpashScreen.onStart();
                }
            } else {
                appCompatTextView.setText(R.string.noConnctionText);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.no_connection_layout_slide);
                linearLayout.startAnimation(animation);
            }
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 11);
            return;
        }
        if (location != null) {
            mLocation = location;
            startIntentService();
        }
    }

    private void startIntentService() {
        Intent intent = new Intent(this, NammRadioCountryLocationFetcher.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(GlobalConstants.RECEIVER, mResultReceiver);
        intent.putExtra(GlobalConstants.LOCATION_DATA_EXTRA, mLocation);
        startService(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locateDevice();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class AddressResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            // Display the address string
            // or an error message sent from the intent service.
            mCountryCode = resultData.getString(GlobalConstants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (!mLocationUpdate && resultCode == GlobalConstants.SUCCESS_RESULT && mCountryCode != null) {
                mLocationUpdate = true;
                prepareDataTobeSentToMainActivity();
                if (((AnimationDrawable) nammRadioConnectivityChange.getBackground()).isRunning())
                    ((AnimationDrawable) nammRadioConnectivityChange.getBackground()).stop();
                loadPlayer();
                mainTextSplashScreen.setText("");
                mainTextSplashScreen.postInvalidate();
            }
        }

    }

    private void loadPlayer() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Title", mCountryCode);
        intent.putExtra("jsonData", mJsonData.toString());
        intent.putParcelableArrayListExtra("PS", (ArrayList<ProgramItemSchedulerModel>) programItemSchedulerModelList);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.splash_slide_down, R.anim.nothing);
    }

    private void prepareDataTobeSentToMainActivity() {
        if (mainTextSplashScreen != null) {
            mainTextSplashScreen.setText(R.string.preparingPlayer);
        }
        readJSONFromURL();
    }

    private void readJSONFromURL() {
        initDisplayOptionsImageLoader();
        JSONDataParser jsonDataParser = new JSONDataParser();
        try {
            String jsonObjectString = jsonDataParser.execute("http://temp2.aegismatrix.com/ps.php").get();
            if (null != jsonObjectString) {
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                for (int i = 0; i < jsonObject.length(); i++) {
                    String key = jsonObject.names().getString(i);
                    if (key.equalsIgnoreCase(GlobalConstants.PROGRAM_SCHEDULES_JSON_OBJECT)) {
                        JSONArray jsonArray = (JSONArray) jsonObject.opt(GlobalConstants.PROGRAM_SCHEDULES_JSON_OBJECT);
                        mJsonData = jsonArray;
                        for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.length(); jsonArrayIndex++) {
                            addProgramSchedule(jsonArrayIndex, jsonArray);
                        }
                    } else if (key.equalsIgnoreCase(GlobalConstants.STATION_LINKS_JSON_OBJECT)) {
                        processStationLinks(jsonObject);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.d("JSON String read interruption", e.getMessage());
        }
    }

    private void processStationLinks(JSONObject jsonObject) throws JSONException {
        JSONObject stationLinks = (JSONObject) jsonObject.opt(GlobalConstants.STATION_LINKS_JSON_OBJECT);
        GlobalConstants.STATIONLINKS = null;
        HashMap<String, String> map = new HashMap<String, String>();
        for (int stationKey = 0; stationKey < stationLinks.length(); stationKey++) {
            String stationKeyy = stationLinks.names().getString(stationKey);
            map.put(stationKeyy, stationLinks.optString(stationKeyy));
        }
        GlobalConstants.STATIONLINKS = Collections.unmodifiableMap(map);
    }

    private void initDisplayOptionsImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void addProgramSchedule(int jsonArrayIndex, JSONArray jsonArray) {
        ProgramItemSchedulerModel programItemSchedulerModel = new ProgramItemSchedulerModel();
        JSONObject jsonData = ((JSONObject) jsonArray.opt(jsonArrayIndex));
        programItemSchedulerModel.setProgramName((String) jsonData.opt(GlobalConstants.PROGRAM_SCHEDULES_JSON_PROGRAM_NAME));
        programItemSchedulerModel.setHostName((String) jsonData.opt(GlobalConstants.PROGRAM_SCHEDULES_JSON_HOST_NAME));
        programItemSchedulerModel.setShowTimings((String) jsonData.opt(GlobalConstants.PROGRAM_SCHEDULES_JSON_PROGRAM_TIMINGS));
        if (jsonArrayIndex == 0)
            programItemSchedulerModel.setEquilizer(new BitmapDrawable(getResources(), getImageToShow(GlobalConstants.PROGRAM_SCHEDULE_CURENT_PROGRAM_EQUILISER)));
        programItemSchedulerModelList.add(programItemSchedulerModel);
    }

    public Bitmap getImageToShow(final String imageURL) {
        Bitmap imageToShow = null;
        try {
            imageToShow = new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                            .cacheOnDisk(true).resetViewBeforeLoading(true)
/*                           .showImageOnFail(fallback)
                            .showImageOnLoading(fallback)*/.build();
                    return imageLoader.loadImageSync(imageURL, options);
                }
            }.execute(imageURL).get();
        } catch (Exception e) {
            Log.e("Image Not Available", e.getMessage());
        }
        return imageToShow;
    }

}

