package com.example.photoremark;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photoremark.poi.ToHomeActivity;

import org.supermap.fm.license.AppLicense;
import org.supermap.fm.license.AppLicenseUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private ImageView image;
    private Button takephoto;
    private String tempFile = "";
    public static int PHOTOSUCESS = 1;
    public static int STORESUCESS = 2;
    public static int STOREFIELD = 3;
    public static int RESULT_CAPTURE_IMAGE = 4;// 拍照获取
    public static int INITOVER = 5;
    private String deviceIdFile = "";
    private SystemInfo systemInfo;
    private ProgressDialog processDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        systemInfo = (SystemInfo) getApplication();
        image = (ImageView) this.findViewById(R.id.image);
        takephoto = (Button) this.findViewById(R.id.takephoto);
        takephoto.setOnClickListener(this);
        takephoto.setClickable(false);
        findViewById(R.id.create_word_home).setOnClickListener(this);
        findViewById(R.id.create_word_village).setOnClickListener(this);
        //暂时屏蔽许可证验证
        //initresouceFile();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PHOTOSUCESS) {
                String path = (String) msg.obj;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap bm = null;
                try {
                    bm = scalePicture(path, options, systemInfo.screenWidth, systemInfo.screenWidth);
                } catch (OutOfMemoryError err) {

                }
                if (bm != null) {
                    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    image.setImageBitmap(bm);
                }
                if (processDialog != null) {
                    processDialog.cancel();
                }
            } else if (msg.what == STORESUCESS) {
                if (processDialog != null) {
                    processDialog.cancel();
                }
                Log.d("hzm", "照片保存成功！");
                Toast.makeText(getApplication(), "照片保存成功！", Toast.LENGTH_SHORT)
                        .show();

            } else if (msg.what == STOREFIELD) {
                if (processDialog != null) {
                    processDialog.cancel();
                }
                Log.d("hzm", "照片保存失败！");
                Toast.makeText(getApplication(), "照片保存失败！", Toast.LENGTH_SHORT)
                        .show();
            } else if (msg.what == INITOVER) {
                AppLicense applicense = matchingDeviceID();
                if (!applicense.isLicenseValid) {
                    takephoto.setClickable(false);
                    // 设备许可不正确
                    showLicenseDialog(applicense.status);
                } else {
                    Toast.makeText(MainActivity.this, "许可配置成功",
                            Toast.LENGTH_SHORT).show();
                    takephoto.setClickable(true);
                }
            }

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takephoto:
                // 先验证手机是否有sdcard
                String status = Environment.getExternalStorageState();
                if (status.equals(Environment.MEDIA_MOUNTED)) {
                    try {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        tempFile = String.valueOf(System.currentTimeMillis()) + ".jpg";
                        File f = new File(systemInfo.PATH_TEMP, tempFile);
                        Uri u = Uri.fromFile(f);
                        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                        startActivityForResult(intent, RESULT_CAPTURE_IMAGE);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "没有找到储存目录",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "没有储存卡", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.create_word_home:
                startActivity(new Intent(this, ToHomeActivity.class));
                break;
            case R.id.create_word_village:
                startActivity(new Intent(this, ToHomeActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CAPTURE_IMAGE
                && (resultCode == Activity.RESULT_OK)) {

            processDialog = new ProgressDialog(MainActivity.this);
            processDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // processDialog.setTitle("");
            processDialog.setMessage("正在处理图片...");
            // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
            processDialog.setIndeterminate(false);
            // 设置ProgressDialog 是否可以按退回键取消
            processDialog.setCancelable(false);
            processDialog.show();
            watermarkphoto();
        }
    }

    /**
     * 匹配设备id
     */
    private AppLicense matchingDeviceID() {
        long lasttime = System.currentTimeMillis();
        AppLicense applicense = new AppLicense();
        File licenseFile = new File(deviceIdFile);
        if (licenseFile.exists()) {
            String deviceId = FileUtil.readFile(deviceIdFile).toString();
            applicense = AppLicenseUtil.readFMLicense(systemInfo.PATH_LICENSE
                    + "SuperMap FieldMapper Trial.slm", lasttime, deviceId);

        } else {
            applicense.status = "APP设备ID文件不存在";
        }
        return applicense;
    }

    private void initresouceFile() {
        Thread tr = new Thread() {
            @Override
            public void run() {
                super.run();
                // 写入设备deviceId，每次进来都更新文件
                deviceIdFile = systemInfo.PATH_LICENSE + "device.txt";
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = tm.getDeviceId().trim();// 获取设备id 去除空格
                FileUtil.writeFile(deviceIdFile, deviceId);// 写文件,不管文件是否存在每次都要写
                handler.sendEmptyMessage(INITOVER);
            }
        };
        tr.start();

    }

    private void watermarkphoto() {
        Thread tr = new Thread() {
            @Override
            public void run() {
                super.run();
                File f = new File(systemInfo.PATH_TEMP + tempFile);
                Uri uri = null;
                try {
                    uri = Uri.parse(android.provider.MediaStore.Images.Media
                            .insertImage(getContentResolver(),
                                    f.getAbsolutePath(), null, null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            MainActivity.this.getContentResolver(), uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                String str = formatter.format(System.currentTimeMillis())
                        .replace(" ", "T");

                PhotoWatermark photoWatermark = new PhotoWatermark();

                String[] longstr = changeDu(systemInfo.naviGps.dLongitude);
                String[] latstr = changeDu(systemInfo.naviGps.dLatitude);
                String lon = longstr[0] + "°" + longstr[1] + "′" + longstr[2]
                        + "″ ";
                String lat = latstr[0] + "°" + latstr[1] + "′" + latstr[2]
                        + "″ ";

                String alt = roundDouble(systemInfo.naviGps.dAltitude, 2) + "";
                String name = String.valueOf(System.currentTimeMillis());
                String path = systemInfo.PATH_CAMERA + name + ".jpg";
                photoWatermark.addWordToBitmap(bitmap, str, lon, lat, alt,
                        handler, path);

            }
        };
        tr.start();

    }

    private void showLicenseDialog(String content) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("许可错误");
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setPositiveButton("我知道了",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process
                                .myPid());
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                onExit();
            }
        }
        return true;
    }

    private void onExit() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setTitle("退出");
        builder.setMessage("您确定要退出系统吗？");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public boolean isGpsDataValid(double dx, double dy) {
        boolean bResult = false;
        if (dx >= 0.00000001 || dx <= -0.00000001 || dy >= 0.00000001
                || dy <= -0.00000001) {
            bResult = true;
        }
        return bResult;
    }

    /**
     * 度转度分秒
     */
    public String[] changeDu(double dx) {
        int d = (int) dx;// 度
        double f = (dx - d) * 60;
        double tm = (f - (int) f) * 60;
        double m = Math.round(tm * 100) / 100.0;
        String[] str = new String[3];
        str[0] = d + "";
        str[1] = (int) f + "";
        str[2] = m + "";
        return str;
    }

    /**
     * 旋转图片浏览时
     *
     * @param filepath 图片地址
     * @param options  BitmapFactory.Options
     * @return
     */
    public Bitmap scalePicture(String filepath, BitmapFactory.Options options,
                               int screenWidth, int scaleHeight) {
        // 获取照片的选装角度
        int rotate = getPictureDegree(filepath);
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeFile(filepath, options);
        } catch (OutOfMemoryError err) {

        }
        // 获取这个图片的宽和高
        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            // 计算缩放率，新尺寸除原始尺寸
            float scaleWidth = ((float) screenWidth) / width;
            Matrix matrix = new Matrix();
            // 缩放图片动作
            matrix.postScale(scaleWidth, scaleWidth);
            // 旋转图片 动作
            matrix.postRotate(rotate);
            bm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        }
        return bm;
    }

    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度
     */
    public int getPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processDialog != null) {
            processDialog.cancel();
        }
    }

    public double roundDouble(double value, int weishu) {
        BigDecimal bg = new BigDecimal(value);
        double newValue = bg.setScale(weishu, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return newValue;
    }
}
