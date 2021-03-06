package com.example.photoremark;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.supermap.mobileapp.log.LogCrash;

import java.io.File;

public class SystemInfo extends Application {

    public NaviGPS naviGps = null;
    private static SystemInfo mInstance;// SystemInfo的单例
    // 屏幕宽度
    public int screenWidth = 480;
    // 屏幕高度
    public int screenHeight = 800;
    public String PATH_CAMERA = "";
    public String PATH_LOG = "";
    public String PATH_LICENSE = "";
    public String PATH_TEMP = "";
    public String PATH_WORD_DOC = "";
    public String PATH_HTML_VALUE = "";

    @Override
    public void onCreate() {
        super.onCreate();
        // 启动GPS
        try {
            naviGps = new NaviGPS();
            // 启动GPS
            LocationManager gpslm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            naviGps.OpenGpsDevice(gpslm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取屏幕的宽高
        WindowManager winMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        winMgr.getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;

        PATH_CAMERA = Environment.getExternalStorageDirectory() + "/Watermark/camera/";
        PATH_LICENSE = Environment.getExternalStorageDirectory() + "/Watermark/license/";
        PATH_LOG = Environment.getExternalStorageDirectory() + "/Watermark/log/";
        PATH_TEMP = Environment.getExternalStorageDirectory() + "/Watermark/temp/";
        PATH_WORD_DOC = Environment.getExternalStorageDirectory() + "/Watermark/word/";
        PATH_HTML_VALUE = Environment.getExternalStorageDirectory() + "/Watermark/html/";

        // 先验证手机是否有sdcard
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            File dir1 = new File(PATH_CAMERA);
            File dir2 = new File(PATH_LICENSE);
            File dir3 = new File(PATH_LOG);
            File dir4 = new File(PATH_TEMP);
            File dir5 = new File(PATH_WORD_DOC);
            File dir6 = new File(PATH_HTML_VALUE);
            if (!dir1.exists()) {
                dir1.mkdirs();
            }
            if (!dir2.exists()) {
                dir2.mkdirs();
            }
            if (!dir3.exists()) {
                dir3.mkdirs();
            }
            if (!dir4.exists()) {
                dir4.mkdirs();
            }
            if (!dir5.exists()) {
                dir5.mkdirs();
            }
            if (!dir6.exists()) {
                dir6.mkdirs();
            }
        }
        LogCrash logCrash = LogCrash.getInstance();
        logCrash.init(getApplicationContext(), PATH_LOG);
    }

    // 单例模式中获取唯一的SystemInfo实例
    public static SystemInfo getInstance() {
        if (mInstance == null) {
            mInstance = new SystemInfo();
        }
        return mInstance;
    }
}
