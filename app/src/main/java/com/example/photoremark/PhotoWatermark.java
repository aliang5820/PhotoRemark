package com.example.photoremark;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PhotoWatermark {

	public PhotoWatermark() {

	}

	public void addWordToBitmap(final Bitmap photo, final String str,
			final String longitude, final String Latitude, final String height,
			final Handler handler, final String path) {

		new Thread() {
			@Override
			public void run() {

				int width = photo.getWidth(), hight = photo.getHeight();
				Log.d("hzm", "宽" + width + "高" + hight);

				Paint photoPaint = new Paint(); // 建立画笔
				photoPaint.setDither(true); // 获取跟清晰的图像采样
				photoPaint.setFilterBitmap(true);// 过滤一些

				Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
						| Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
				Float size = Float.valueOf(width / 30);
				Log.d("hzm", "size = " + size);
				textPaint.setTextSize(size);// 字体大小
				textPaint.setTypeface(Typeface.DEFAULT_BOLD);// 采用默认的宽度
				textPaint.setColor(Color.RED);// 采用的颜色

				Bitmap icon = convertToMutable(photo);

				Canvas canvas = new Canvas(icon); 

				canvas.drawText("经度：" + longitude + "E", size / 2, size,
						textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制
				canvas.drawText("纬度：" + Latitude + "N", size / 2, 2 * size,
						textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制
				canvas.drawText("高度：" + height + "米", size / 2, 3 * size,
						textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制
				canvas.drawText("时间：" + str, size / 2, 4 * size, textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制

				canvas.save(Canvas.ALL_SAVE_FLAG);
				canvas.restore();

				Looper.prepare();

				try {
					saveBitmapToFile(icon, path, handler);
				} catch (IOException e) {
					e.printStackTrace();
				}

				Message msg = new Message();
				msg.what = MainActivity.PHOTOSUCESS;
				msg.obj = path;
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * Converts a immutable bitmap to a mutable bitmap. This operation doesn't
	 * allocates more memory that there is already allocated.
	 * 
	 * @param imgIn
	 *            - Source image. It will be released, and should not be used
	 *            more
	 * @return a copy of imgIn, but muttable.
	 */
	public Bitmap convertToMutable(Bitmap imgIn) {
		try {
			// this is the file going to use temporally to save the bytes.
			// This file will not be a image, it will store the raw image data.
			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "temp.tmp");

			// Open an RandomAccessFile
			// Make sure you have added uses-permission
			// android:name="android.permission.WRITE_EXTERNAL_STORAGE"
			// into AndroidManifest.xml file
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			// get the width and height of the source bitmap.
			int width = imgIn.getWidth();
			int height = imgIn.getHeight();
			Config type = imgIn.getConfig();
			// Config type = Config.ARGB_8888;
			// Copy the byte to the file
			// Assume source bitmap loaded using options.inPreferredConfig =
			// Config.ARGB_8888;
			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0,
					imgIn.getRowBytes() * height);
			imgIn.copyPixelsToBuffer(map);
			// recycle the source bitmap, this will be no longer used.
			imgIn.recycle();
			System.gc();// try to force the bytes from the imgIn to be released

			// Create a new bitmap to load the bitmap again. Probably the memory
			// will be available.

			imgIn = Bitmap.createBitmap(width, height, type);
			map.position(0);
			// load it back from temporary
			imgIn.copyPixelsFromBuffer(map);
			// close the temporary file and channel , then delete that also
			channel.close();
			randomAccessFile.close();

			// delete the temp file
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imgIn;
	}


	/**
	 * Save Bitmap to a file.保存图片到SD卡。
	 * 
	 * @param bitmap
	 * @param file
	 * @return error message if the saving is failed. null if the saving is
	 *         successful.
	 * @throws IOException
	 */
	public void saveBitmapToFile(Bitmap bitmap, String _file, Handler handler)
			throws IOException {
		BufferedOutputStream os = null;
		try {
			File file = new File(_file);
			// String _filePath_file.replace(File.separatorChar +
			// file.getName(), "");
			int end = _file.lastIndexOf(File.separator);
			String _filePath = _file.substring(0, end);
			File filePath = new File(_filePath);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			file.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os);

			Message msg = new Message();
			msg.what = MainActivity.STORESUCESS;
			handler.sendMessage(msg);

		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					Log.e("hzm", e.getMessage(), e);
					Message msg = new Message();
					msg.what = MainActivity.STOREFIELD;
					handler.sendMessage(msg);
				}
			}
		}
	}
}
