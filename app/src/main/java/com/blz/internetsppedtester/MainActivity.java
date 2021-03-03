package com.blz.internetsppedtester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blz.internetsppedtester.databinding.ActivityMainBinding;
import com.blz.internetsppedtester.databinding.InfoBottomsheetBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private SpeedTestSocket speedTestSocket = new SpeedTestSocket();
    //private InfoBottomsheetBinding bottomSheetBinding;
    //private BottomSheetBehavior behavior;
    //Map<String,String> info_map = new HashMap<>();
    private List<Info> infoList = new ArrayList<>();

    private InfoAdapter adapter;

    private boolean doubleBackToExitPressedOnce = false;

    /** For Location **/

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /** For adresses;
     *  Geocoder geocoder = new Geocoder(this, Locale.getDefault());
     */
    private Geocoder geocoder;

    /**For Location**/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        //bottomSheetBinding = binding.bottomSheet;
        setContentView(view);
        initialSetup();
        setupListeners();
        setupClicks();
        initializeRecyclerView();

    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speedTestSocket = null;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void initialSetup() {

        //checkInternetValidity();
        //behavior = BottomSheetBehavior.from(binding.bottomSheet.getRoot());
        mRequestingLocationUpdates = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        //initial Kick Off
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        getDeviceInfo();

        if (ConnectionManager.isOnline(getApplicationContext())){
            getPublicIP();
        }
        else {
            Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
        }

        if (checkPermissions()) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }


    }

    private void initializeRecyclerView() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvDeviceInfo.setHasFixedSize(true);
        binding.rvDeviceInfo.setLayoutManager(layoutManager);
        adapter = new InfoAdapter(getApplicationContext(),infoList, binding.rvDeviceInfo);
        binding.rvDeviceInfo.setAdapter(adapter);
    }

    private void setupListeners() {
        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is complete
                Log.d("speed_debug", "[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                Log.d("speed_debug", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                //double bps = Double.valueOf(report.getTransferRateBit());
                //Log.d("speed_debug", "[COMPLETED] rate in bit/s  Double : " + );
                //convertToMbps( report.getTransferRateBit());
                convertToMbps(report.getTransferRateBit().doubleValue());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.checkSpeedBtn.setVisibility(View.VISIBLE);
                        binding.speedProgressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
                Log.d("speed_debug", "onError: ");
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // called to notify download/upload progress
                Log.d("speed_debug", "[PROGRESS] progress : " + percent + "%");
                Log.d("speed_debug", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                Log.d("speed_debug", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                convertToMbps(report.getTransferRateBit().doubleValue());
            }
        });
    }

    private void setupClicks() {


       binding.checkSpeedBtn.setOnClickListener(v -> {



           if (ConnectionManager.isOnline(getApplicationContext())){
               new SpeedTestAsyncTask().execute();
               binding.checkSpeedBtn.setVisibility(View.GONE);
               binding.speedProgressBar.setVisibility(View.VISIBLE);
           }
           else {
               Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
           }

        });

       binding.infoBtn.setOnClickListener(v -> {
           //infoList.clear();
           /** Comment out to get Private Network Information
            * Like Device is connected with Wi-FI or Mobile Data
            * private IP**/
           /**checkInternetValidity();**/

           //adapter.setInfoList(infoList);
           //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
           Log.d("location_debug", "setupClicks: lattitude: " + mCurrentLocation.getLatitude()+ " Longitude: " + mCurrentLocation.getLongitude());
       });

       //bottomSheetBinding.cancelBtn.setOnClickListener(v -> behavior.setState(BottomSheetBehavior.STATE_COLLAPSED));


    }

    private void checkInternetValidity() {

       String network_state = ConnectionManager.isInternetAvailable(MainActivity.this);

       switch (network_state){
           case ConnectionManager.MOBILE_NETWORK:
               Log.d("ip_debug", "Ip Address: " + getMobileIPAddress(true));
               infoList.add(new Info("Ip Address" , getMobileIPAddress(true)));
               //new SpeedTestAsyncTask().execute();
               break;
           case ConnectionManager.WIFI_NETWORK:
               Log.d("ip_debug", "Ip Address: " + getWifiIPAddress());
               infoList.add(new Info("Ip Address" , getWifiIPAddress()));
               //new SpeedTestAsyncTask().execute();
               break;
           case ConnectionManager.NETWORK_NOT_AVAILABLE:
               Log.d("ip_debug", "Failed: Internet Not Available");
               Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
       }
    }


    class SpeedTestAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso");
            //speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000);
            return null;
        }
    }

    private void convertToMbps(double bits){
        String hrSize;
        double b = bits;
        double k = bits/1024.0;
        double m = ((bits/1024.0)/1024.0);
        double g = (((bits/1024.0)/1024.0)/1024.0);
        double t = ((((bits/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" ");

        } else if ( g>1 ) {
            hrSize = dec.format(g);
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" mb/s");
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   //binding.speedometer.speedTo((float) m,10);
                   //binding.speedometer.setUnit(" mbps");
                   binding.tvDownloadSpeed.setText(hrSize);
               }
           });
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" kb/s");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //binding.speedometer.speedTo((float) m,10);
                    //binding.speedometer.setUnit(" kbps");
                    binding.tvDownloadSpeed.setText(hrSize);
                }
            });
        } else {
            hrSize = dec.format(b);
        }

        Log.d("speed_debug", "convertToMbps: final result: " + hrSize);
    }

    private void getDeviceInfo(){

        /** Comment out to show other device Info*/

        Log.d("device_info_debug", "OS_VERSION: " + DeviceInfoManager.OS_VERSION);
        infoList.add(new Info("Os Version" , DeviceInfoManager.OS_VERSION));
        /*Log.d("device_info_debug", "API_LEVEL: " + DeviceInfoManager.API_LEVEL);*/
        infoList.add(new Info("Api Level" , DeviceInfoManager.API_LEVEL));
        /*Log.d("device_info_debug", "DEVICE: " + DeviceInfoManager.DEVICE);*/
        //infoList.add(new Info("Device" , DeviceInfoManager.DEVICE));
        /*Log.d("device_info_debug", "MODEL: " + DeviceInfoManager.MODEL);*/
        infoList.add(new Info("Model" , DeviceInfoManager.MODEL));
       /* Log.d("device_info_debug", "PRODUCT: " + DeviceInfoManager.PRODUCT);*/
        //infoList.add(new Info("Product" , DeviceInfoManager.PRODUCT));
        /*Log.d("device_info_debug", "RELEASE: " + DeviceInfoManager.RELEASE);*/
        infoList.add(new Info("Android Version" , DeviceInfoManager.RELEASE));
        /*Log.d("device_info_debug", "BRAND: " + DeviceInfoManager.BRAND);*/
        infoList.add(new Info("Brand" , DeviceInfoManager.BRAND));
        /*Log.d("device_info_debug", "DISPLAY: " + DeviceInfoManager.DISPLAY);*/
        //infoList.add(new Info("Display" , DeviceInfoManager.DISPLAY));
        /*Log.d("device_info_debug", "SUPPORTED_ABIS: " + Arrays.toString(DeviceInfoManager.SUPPORTED_ABIS));
        Log.d("device_info_debug", "UNKNOWN: " + DeviceInfoManager.UNKNOWN);*/
       /* Log.d("device_info_debug", "HARDWARE: " + DeviceInfoManager.HARDWARE);*/
        //infoList.add(new Info("Hardware" , DeviceInfoManager.HARDWARE));
       /* Log.d("device_info_debug", "Build: " + DeviceInfoManager.Build);*/
        //infoList.add(new Info("Build" , DeviceInfoManager.Build));
        /*Log.d("device_info_debug", "MANUFACTURER: " + DeviceInfoManager.MANUFACTURER);*/
        //infoList.add(new Info("Manufacturer" , DeviceInfoManager.MANUFACTURER));
        /*Log.d("device_info_debug", "SERIAL: " + DeviceInfoManager.SERIAL);*/
        //infoList.add(new Info("Serial" , DeviceInfoManager.SERIAL));
        /*Log.d("device_info_debug", "USER: " + DeviceInfoManager.USER);*/
        //infoList.add(new Info("User" , DeviceInfoManager.USER));
        /*Log.d("device_info_debug", "HOST: " + DeviceInfoManager.HOST);*/
        //infoList.add(new Info("Host" , DeviceInfoManager.HOST));

    }



    private String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        byte[] myIPAddress = BigInteger.valueOf(wifiInfo.getIpAddress()).toByteArray();
        reverse(myIPAddress);
        // you must reverse the byte array before conversion. Use Apache's commons library
        InetAddress myInetIP = null;
        try {
            myInetIP = InetAddress.getByAddress(myIPAddress);
            return myInetIP.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getMobileIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private void getPublicIP(){


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.ipify.org/";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //extView.setText("Response is: "+ response.substring(0,500));
                        Log.d("public_ip_debug", "onResponse: " + response);
                        infoList.add(new Info("Public Ip" , response));
                        adapter.notifyDataSetChanged();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                Log.d("public_ip_debug", "Error fetching public Ip: " + error);
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    /** Location Section**/
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                getAddress();
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                //updateLocationUI();
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("location_debug", "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("location_debug", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        //updateUI();
                        break;
                }
                break;
        }
    }


    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("location_debug", "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i("location_debug", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i("location_debug", "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("location_debug", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i("location_debug", "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d("location_debug", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        Log.d("location_debug", "startLocationUpdates: called");
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("location_debug", "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("location_debug", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("location_debug", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("location_debug", errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                    }
                });
    }

    private String getCurrentAddress(){
        String errorMessage = "";
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e("location_debug", errorMessage, ioException);
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e("location_debug", errorMessage + ". " +
                    "Latitude = " + mCurrentLocation.getLatitude() +
                    ", Longitude = " + mCurrentLocation.getLongitude(), illegalArgumentException);
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e("location_debug", errorMessage);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i("location_debug", getString(R.string.address_found));

            return TextUtils.join(System.getProperty("line.separator"), addressFragments);

        }
        return "errorMessage";
    }
    private void getAddress(){

        infoList.add(new Info("Current Address" , getCurrentAddress()));
        adapter.notifyDataSetChanged();
        stopLocationUpdates();


    }


}