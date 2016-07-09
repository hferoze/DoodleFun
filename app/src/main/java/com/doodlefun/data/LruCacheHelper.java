package com.doodlefun.data;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class LruCacheHelper {

    private static LruCacheHelper mLruCacheHelper = null;
    private static LruCache<String, Bitmap> mMemoryCache;

    public static LruCacheHelper getInstance(){
        if (mLruCacheHelper == null) {
            mLruCacheHelper = new LruCacheHelper();
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
        }

        return mLruCacheHelper;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void clearCache(){
        mMemoryCache.evictAll();
    }

    public LruCache getCache(){
        return mMemoryCache;
    }
}
