package com.blz.internetsppedtester;

import android.os.Build;

public class DeviceInfoManager {
    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String API_LEVEL = String.valueOf(android.os.Build.VERSION.SDK_INT);
    public static final String DEVICE = android.os.Build.DEVICE;
    public static final String MODEL = android.os.Build.MODEL;
    public static final String PRODUCT =android.os.Build.PRODUCT;
    public static final String RELEASE = android.os.Build.VERSION.RELEASE;
    public static final String BRAND = android.os.Build.BRAND;
    public static final String DISPLAY = android.os.Build.DISPLAY;
    public static final String[] SUPPORTED_ABIS = android.os.Build.SUPPORTED_ABIS;
    public static final String UNKNOWN = android.os.Build.UNKNOWN;
    public static final String HARDWARE = android.os.Build.HARDWARE;
    public static final String Build = android.os.Build.ID;
    public static final String MANUFACTURER = android.os.Build.MANUFACTURER;
    public static final String SERIAL = android.os.Build.SERIAL;
    public static final String USER= android.os.Build.USER;
    public static final String HOST= android.os.Build.HOST;

}
