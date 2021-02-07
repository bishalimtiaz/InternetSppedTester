package com.blz.internetsppedtester;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.blz.internetsppedtester.databinding.ActivityMainBinding;
import com.blz.internetsppedtester.databinding.InfoBottomsheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private SpeedTestSocket speedTestSocket = new SpeedTestSocket();
    private InfoBottomsheetBinding bottomSheetBinding;
    //Map<String,String> info_map = new HashMap<>();
    private List<Info> infoList = new ArrayList<>();
    private BottomSheetBehavior behavior;
    private InfoAdapter adapter;

    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        bottomSheetBinding = binding.bottomSheet;
        setContentView(view);
        initialSetup();
        setupListeners();
        setupClicks();
        initializeRecyclerView();

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
        behavior = BottomSheetBehavior.from(binding.bottomSheet.getRoot());

    }

    private void initializeRecyclerView() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        bottomSheetBinding.rvInfo.setHasFixedSize(true);
        bottomSheetBinding.rvInfo.setLayoutManager(layoutManager);
        adapter = new InfoAdapter(getApplicationContext(),infoList);
        bottomSheetBinding.rvInfo.setAdapter(adapter);
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
           }
           else {
               Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
           }

        });

       binding.infoBtn.setOnClickListener(v -> {
           infoList.clear();
           getDeviceInfo();
           checkInternetValidity();
           adapter.setInfoList(infoList);
           behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
       });

       bottomSheetBinding.cancelBtn.setOnClickListener(v -> behavior.setState(BottomSheetBehavior.STATE_COLLAPSED));


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
                   binding.speedometer.speedTo((float) m,10);
                   binding.speedometer.setUnit(" mbps");
               }
           });
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" kb/s");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.speedometer.speedTo((float) m,10);
                    binding.speedometer.setUnit(" kbps");
                }
            });
        } else {
            hrSize = dec.format(b);
        }

        Log.d("speed_debug", "convertToMbps: final result: " + hrSize);
    }

    private void getDeviceInfo(){

        Log.d("device_info_debug", "OS_VERSION: " + DeviceInfoManager.OS_VERSION);
        infoList.add(new Info("Os Version" , DeviceInfoManager.OS_VERSION));
        /*Log.d("device_info_debug", "API_LEVEL: " + DeviceInfoManager.API_LEVEL);*/
        infoList.add(new Info("Api Level" , DeviceInfoManager.API_LEVEL));
        /*Log.d("device_info_debug", "DEVICE: " + DeviceInfoManager.DEVICE);*/
        infoList.add(new Info("Device" , DeviceInfoManager.DEVICE));
        /*Log.d("device_info_debug", "MODEL: " + DeviceInfoManager.MODEL);*/
        infoList.add(new Info("Model" , DeviceInfoManager.MODEL));
       /* Log.d("device_info_debug", "PRODUCT: " + DeviceInfoManager.PRODUCT);*/
        infoList.add(new Info("Product" , DeviceInfoManager.PRODUCT));
        /*Log.d("device_info_debug", "RELEASE: " + DeviceInfoManager.RELEASE);*/
        infoList.add(new Info("Release" , DeviceInfoManager.RELEASE));
        /*Log.d("device_info_debug", "BRAND: " + DeviceInfoManager.BRAND);*/
        infoList.add(new Info("Brand" , DeviceInfoManager.BRAND));
        /*Log.d("device_info_debug", "DISPLAY: " + DeviceInfoManager.DISPLAY);*/
        infoList.add(new Info("Display" , DeviceInfoManager.DISPLAY));
        /*Log.d("device_info_debug", "SUPPORTED_ABIS: " + Arrays.toString(DeviceInfoManager.SUPPORTED_ABIS));
        Log.d("device_info_debug", "UNKNOWN: " + DeviceInfoManager.UNKNOWN);*/
       /* Log.d("device_info_debug", "HARDWARE: " + DeviceInfoManager.HARDWARE);*/
        infoList.add(new Info("Hardware" , DeviceInfoManager.HARDWARE));
       /* Log.d("device_info_debug", "Build: " + DeviceInfoManager.Build);*/
        infoList.add(new Info("Build" , DeviceInfoManager.Build));
        /*Log.d("device_info_debug", "MANUFACTURER: " + DeviceInfoManager.MANUFACTURER);*/
        infoList.add(new Info("Manufacturer" , DeviceInfoManager.MANUFACTURER));
        /*Log.d("device_info_debug", "SERIAL: " + DeviceInfoManager.SERIAL);*/
        infoList.add(new Info("Serial" , DeviceInfoManager.SERIAL));
        /*Log.d("device_info_debug", "USER: " + DeviceInfoManager.USER);*/
        infoList.add(new Info("User" , DeviceInfoManager.USER));
        /*Log.d("device_info_debug", "HOST: " + DeviceInfoManager.HOST);*/
        infoList.add(new Info("Host" , DeviceInfoManager.HOST));

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


}