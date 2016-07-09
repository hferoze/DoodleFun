package com.doodlefun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static boolean isDataAvaialable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isDataConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isDataConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE
                    || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static int getCurrentOSVersion(){
        return android.os.Build.VERSION.SDK_INT;
    }

    public static float getScreenWidth(Context mContext){
        Point size = new Point();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(size);
        return size.x;
    }

    public static float getScreenHeight(Context mContext){
        Point size = new Point();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(size);
        return size.y;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, float reqWidth, float reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(Context context, Uri file) {

        final int MAX_BITMAP_HEIGHT = 480; //px
        final int MAX_BITMAP_WIDTH = 512; //px

        try {
        InputStream inputStream = context.getContentResolver().openInputStream(file);

            if (inputStream!=null) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);

                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;

                if (imageHeight > imageWidth){
                    if (imageHeight <= MAX_BITMAP_HEIGHT) return BitmapFactory.decodeStream(inputStream);

                    options.inSampleSize = calculateInSampleSize(options, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT);
                    options.inJustDecodeBounds = false;
                    inputStream.close();

                }else if (imageHeight < imageWidth ){
                    if (imageWidth <= MAX_BITMAP_WIDTH) return BitmapFactory.decodeStream(inputStream);

                    options.inSampleSize = calculateInSampleSize(options, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT/2);
                    options.inJustDecodeBounds = false;
                    inputStream.close();
                }else{
                    if (imageWidth <= MAX_BITMAP_WIDTH) return BitmapFactory.decodeStream(inputStream);

                    options.inSampleSize = calculateInSampleSize(options, MAX_BITMAP_WIDTH, MAX_BITMAP_WIDTH);
                    options.inJustDecodeBounds = false;
                    inputStream.close();
                }
                inputStream = context.getContentResolver().openInputStream(file);
                return BitmapFactory.decodeStream(inputStream, null, options);
            }
        }catch (FileNotFoundException | NullPointerException e ){
            e.printStackTrace();
            return null;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void log(String TAG, String log){
        if (AppConst.DEBUG){
            Log.d(TAG, log);
        }
    }
}
