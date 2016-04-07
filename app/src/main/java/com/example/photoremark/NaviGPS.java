package com.example.photoremark;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class NaviGPS {
    public final static int LOCATION_CHANGE = 198505;

    public double dLatitude; // !< 纬度，单位是度。
    public double dLongitude; // !< 经度，单位是度。
    public double dBearing; // !< 方位角，表示行进的方向，单位是度。正北为0，顺时针方向，值域为0-360。
    public double dSpeed; // !< 行进速度，单位是米/秒。
    public double dAltitude; // !< 高程，用高于平均海平面即海拔表示。单位是米。
    public long nTime; // !<时间。

    public int nFixMode; // !< 定位模式标识。0表示GPS未定位；1表示定位
    public int nSatellites; // !< 卫星数目。

//	public static class Satellite {
//		public int nSatelliteID;
//		public float nElevation;
//		public float nAzimuth;
//		public float nSignal;
//		public boolean bFix;
//	}

//	// 卫星信息
//	public List<Satellite> listStatellites = null;
//
//	NaviGPS() {
//		// 初始化，避免每次new对象
//		listStatellites = new ArrayList<Satellite>();
//		for (int i = 0; i < 14; i++) {
//			listStatellites.add(new Satellite());
//		}
//	}

    boolean OpenGpsDevice(LocationManager loc) {
        locMgr = loc;

        gpsListener = new GPSNaviListener();
        if (!locMgr.addGpsStatusListener(gpsListener)) {
            return false;
        }
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.0f, gpsListener);

        int version = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (version < 11 || version > 13) {// 判断android的系统版本号（如果是高于2.3且小于4.0的版本则不注册network监听）
            networkListner = new GPSNaviListener();
            locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0.0f, networkListner);
        }

        return true;
    }

    void CloeGpsDevice() {

        if (locMgr == null) {
            locMgr.removeGpsStatusListener(gpsListener);

            locMgr.removeUpdates(gpsListener);
        }

    }

    /*--------------------GPS Listener-----------------------------------*/
    private class GPSNaviListener implements LocationListener,
            GpsStatus.Listener {

        public GPSNaviListener() {

        }

        public void onLocationChanged(Location location) {

            dAltitude = location.getAltitude();
            dLongitude = location.getLongitude();
            dLatitude = location.getLatitude();
            dBearing = location.getBearing();
            dSpeed = location.getSpeed();
            nTime = location.getTime();
        }

        @Override
        public void onGpsStatusChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    }

    private GPSNaviListener gpsListener = null;
    private GPSNaviListener networkListner = null;
    private LocationManager locMgr = null;

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */

    public static final boolean GPSisOPen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
