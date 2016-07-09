package com.doodlefun.utils;

import android.graphics.BitmapShader;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;

public class PaintInfo {

    private int mColor;
    private float mWidth;
    private Paint.Cap mStrokeCap;
    private BitmapShader mBitmapShader;

    private MaskFilter mFilter;

    private PorterDuffXfermode mXferMode;

    public PaintInfo(int color, float width, MaskFilter filter, Paint.Cap strokeCap, BitmapShader bitmapShader, PorterDuffXfermode xferMode){
        this.mColor = color;
        this.mWidth = width;
        this.mFilter = filter;
        this.mStrokeCap = strokeCap;
        this.mBitmapShader = bitmapShader;
        this.mXferMode = xferMode;
    }

    public int getColor(){
        return this.mColor;
    }

    public float getWidth(){
        return this.mWidth;
    }

    public MaskFilter getFilter(){
        return this.mFilter;
    }

    public Paint.Cap getStrokeCap(){
        return this.mStrokeCap;
    }

    public BitmapShader getBitmapShader(){
        return this.mBitmapShader;
    }

    public PorterDuffXfermode getXferMode(){
        return this.mXferMode;
    }
}

