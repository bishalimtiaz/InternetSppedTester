package com.blz.internetsppedtester;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionManager {

    public static final String NETWORK_NOT_AVAILABLE = "network_not_available";
    public static final String WIFI_NETWORK = "wifi_network";
    public static final String MOBILE_NETWORK = "mobile_network";

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String isInternetAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi != null && wifi.isConnected() && wifi.isAvailable()){
                return WIFI_NETWORK;
            }
            else if (mobile != null && mobile.isConnected() && mobile.isAvailable()){
                return MOBILE_NETWORK;
            }
        }
        else{
            return NETWORK_NOT_AVAILABLE;
        }
        return "";
    }


}
