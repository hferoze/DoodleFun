package com.doodlefun.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

public class PicassoHelper {

    private static Picasso mPicasso;
    private static LruCache mPicassoLruCache;
    private static PicassoHelper mPicassoHelper = null;

    public static PicassoHelper getInstance(Context context){
        if (mPicassoHelper == null) {
            mPicassoHelper = new PicassoHelper();

            mPicassoLruCache = new LruCache(context);

            mPicasso = new Picasso.Builder(context)
                    .memoryCache(mPicassoLruCache)
                    .build();
        }
        return mPicassoHelper;
    }

    public void setImage(String imagePath, ImageView imageView){
        mPicasso.load(imagePath).into(imageView);
    }

    public void setImage(Uri imagePath, ImageView imageView){
        mPicasso.load(imagePath).into(imageView);
    }

    public void refresh(String string){
        mPicasso.invalidate(string);
    }



    public void clearCache(){
        mPicassoLruCache.evictAll();
    }

    public LruCache getCache(){
        return mPicassoLruCache;
    }
}
