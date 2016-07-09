package com.doodlefun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;

import com.doodlefun.R;

public class PenTypes implements IPenOptionsChangedListener{

    private int mStrokeWidth, mCurrentPenMinStrokeWidth, mCurrentPenMaxStrokeWidth, mCurrentPenTypeId;
    private String  mColor,mCurrentPenAlpha, mCurrentPenName;
    private Paint mCurrentPenType;

    private static PenTypes mPenTypes = null;

    private static Context mContext;

    public static PenTypes getInstance(Context context){
        if (mPenTypes == null) {
            mPenTypes = new PenTypes();
            mContext = context;
        }
        return mPenTypes;
    }

    public Paint getPenType(int id){
        switch(id){
            case 0:
                return getPenType0();
            case 1:
                return getPenType1();
            case 2:
                return getPenType2();
            case 3:
                return getPenType3();
            case 4:
                return getPenType4();
        }
        return null;
    }

    //eraser
    public Paint getPenType0(){

        mCurrentPenTypeId = 0;
        mCurrentPenName = mContext.getString(R.string.eraser_stroke_width_key);

        int min = (int)mContext.getResources().getDimension(R.dimen.eraser_min_stroke_width);
        int max = (int)mContext.getResources().getDimension(R.dimen.eraser_max_stroke_width);
        float current = min + (min+max) / 2;
        setCurrentPenMinStrokeWidth(min);
        setCurrentPenMaxStrokeWidth(max);
        setCurrentStrokeWidth(getSharedPrefs().getInt(mCurrentPenName, (int)current));
        setCurrentPenAlpha(mContext.getString(R.string.eraser_stroke_width_key));
        setCurrentPenColor(getColor());

        String color = "#"+getColor();

        //flag, stroke, color, style, join, cap, blur, xfermode, shader,
        Paint paint = getPaint(Paint.ANTI_ALIAS_FLAG,
                getStrokeWidth(),
                Color.parseColor(color),
                Paint.Style.STROKE,
                Paint.Join.ROUND,
                Paint.Cap.ROUND,
                null,
                new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
                null);


        setCurrentPenType(paint, true);
        return paint;
    }

    //pointer
    public Paint getPenType1(){

        mCurrentPenTypeId = 1;
        mCurrentPenName = mContext.getString(R.string.pointer_stroke_width_key);

        int min = (int)mContext.getResources().getDimension(R.dimen.pointer_min_stroke_width);
        int max = (int)mContext.getResources().getDimension(R.dimen.pointer_max_stroke_width);
        float current = min+(min+max) / 2;
        setCurrentPenMinStrokeWidth(min);
        setCurrentPenMaxStrokeWidth(max);
        setCurrentStrokeWidth(getSharedPrefs().getInt(mCurrentPenName, (int)current));
        setCurrentPenAlpha(mContext.getString(R.string.pointer_alpha));
        setCurrentPenColor(getColor());

        String color = "#"+getCurrentPenAlpha()+getColor();

        //flag, stroke, color, style, join, cap, blur, xfermode, shader,
        Paint paint = getPaint(Paint.ANTI_ALIAS_FLAG,
                getStrokeWidth(),
                Color.parseColor(color),
                Paint.Style.STROKE,
                Paint.Join.ROUND,
                Paint.Cap.ROUND,
                null,
                null,
                null);

        setCurrentPenType(paint, false);

        return paint;
    }

    //pencil
    public Paint getPenType2(){

        mCurrentPenTypeId = 2;
        mCurrentPenName = mContext.getString(R.string.pencil_stroke_width_key);

        int min = (int)mContext.getResources().getDimension(R.dimen.pencil_min_stroke_width);
        int max = (int)mContext.getResources().getDimension(R.dimen.pencil_max_stroke_width);
        float current = min+(min+max) / 2;
        setCurrentPenMinStrokeWidth(min);
        setCurrentPenMaxStrokeWidth(max);
        setCurrentStrokeWidth(getSharedPrefs().getInt(mCurrentPenName, (int)current));
        setCurrentPenAlpha(mContext.getString(R.string.pencil_alpha));
        setCurrentPenColor(getColor());

        String color = "#"+getCurrentPenAlpha()+getColor();

        //flag, stroke, color, style, join, cap, blur, xfermode, shader,
        Paint paint = getPaint(Paint.DITHER_FLAG,
                getStrokeWidth(),
                Color.parseColor(color),
                Paint.Style.STROKE,
                Paint.Join.ROUND,
                Paint.Cap.ROUND,
                null,
                null,
                getShader());

        setCurrentPenType(paint, false);

        return paint;
    }

    //brush
    public Paint getPenType3(){

        mCurrentPenTypeId = 3;
        mCurrentPenName = mContext.getString(R.string.brush_stroke_width_key);

        int min = (int)mContext.getResources().getDimension(R.dimen.brush_min_stroke_width);
        int max = (int)mContext.getResources().getDimension(R.dimen.brush_max_stroke_width);
        float current = min+(min+max) / 2;
        setCurrentPenMinStrokeWidth(min);
        setCurrentPenMaxStrokeWidth(max);
        setCurrentStrokeWidth(getSharedPrefs().getInt(mCurrentPenName, (int)current));
        setCurrentPenAlpha(mContext.getString(R.string.brush_alpha));
        setCurrentPenColor(getColor());

        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(16, BlurMaskFilter.Blur.NORMAL);
        String color = "#"+getCurrentPenAlpha()+getColor();

        //flag, stroke, color, style, join, cap, blur, xfermode, shader,
        Paint paint = getPaint(Paint.ANTI_ALIAS_FLAG,
                getStrokeWidth(),
                Color.parseColor(color),
                Paint.Style.STROKE,
                Paint.Join.MITER,
                Paint.Cap.ROUND,
                blurMaskFilter,
                null,
                null);

        setCurrentPenType(paint, false);

        return paint;
    }


    //highlighter
    public Paint getPenType4(){

        mCurrentPenTypeId = 4;
        mCurrentPenName = mContext.getString(R.string.highlighter_stroke_width_key);

        int min = (int)mContext.getResources().getDimension(R.dimen.highlighter_min_stroke_width);
        int max = (int)mContext.getResources().getDimension(R.dimen.highlighter_max_stroke_width);
        float current = min+(min+max) / 2;
        setCurrentPenMinStrokeWidth(min);
        setCurrentPenMaxStrokeWidth(max);
        setCurrentStrokeWidth(getSharedPrefs().getInt(mCurrentPenName, (int)current));
        setCurrentPenAlpha(mContext.getString(R.string.highlighter_alpha));
        setCurrentPenColor(getColor());

        String color = "#"+getCurrentPenAlpha()+getColor();

        //flag, stroke, color, style, join, cap, blur, xfermode, shader,
        Paint paint = getPaint(Paint.ANTI_ALIAS_FLAG,
                getStrokeWidth(),
                Color.parseColor(color),
                Paint.Style.STROKE,
                Paint.Join.BEVEL,
                Paint.Cap.SQUARE,
                null,
                null,
                null);

        setCurrentPenType(paint, false);

        return paint;
    }

    public SharedPreferences getSharedPrefs(){
        return mContext.getSharedPreferences(mContext.getResources().getString(R.string.pen_config_preferences), Context.MODE_PRIVATE);
    }

    private Paint getPaint(int flag, float strokeWidth, int color, Paint.Style style, Paint.Join join, Paint.Cap cap, BlurMaskFilter blur, Xfermode xfermode, BitmapShader shader){
        Paint paint = new Paint();

        paint.setFlags(flag);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setStrokeJoin(join);
        paint.setStrokeCap(cap);
        paint.setMaskFilter(blur);
        paint.setXfermode(xfermode);
        paint.setShader(shader);

        return paint;
    }

    public BitmapShader getShader(){

        final String PENCIL_SHADER_RES = "pencil_path1";
        final String RES_FOLDER = "drawable";

        int patternID = mContext.getResources().getIdentifier(PENCIL_SHADER_RES, RES_FOLDER, mContext.getPackageName());

        Bitmap patternBMP = BitmapFactory.decodeResource(mContext.getResources(), patternID);
        Bitmap resultBitmap = Bitmap.createBitmap(patternBMP, 0, 0,
                patternBMP.getWidth() - 1, patternBMP.getHeight() - 1);


        String color = "#"+getCurrentPenAlpha()+getColor();

        Paint p = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(color), PorterDuff.Mode.OVERLAY);
        p.setColorFilter(filter);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);

        BitmapShader patternShader = new BitmapShader(resultBitmap,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        return patternShader;
    }

    public Paint getCurrentPenType(){
        return this.mCurrentPenType;
    }

    private void setCurrentPenMinStrokeWidth(int minStrokeWidth){
        mCurrentPenMinStrokeWidth = minStrokeWidth;
    }

    private void setCurrentPenMaxStrokeWidth(int maxStrokeWidth){
        mCurrentPenMaxStrokeWidth = maxStrokeWidth;
    }

    public void setCurrentStrokeWidth(int strokeWidth){

        mStrokeWidth = strokeWidth;
        getSharedPrefs().edit().putInt(mCurrentPenName, mStrokeWidth).apply();
    }

    public void setCurrentPenType(Paint penType, boolean eraser){
        mCurrentPenType = penType;
    }

    public void setCurrentPenColor(String color){
        mColor = color;
    }

    private void setCurrentPenAlpha(String alpha){
        mCurrentPenAlpha = alpha;
    }

    public int getCurrentPenMinStrokeWidth(){
        return mCurrentPenMinStrokeWidth;
    }

    public int getCurrentPenMaxStrokeWidth(){
        return mCurrentPenMaxStrokeWidth;
    }

    public int getStrokeWidth(){
        return getSharedPrefs().getInt(mCurrentPenName, -1);
    }

    public String getCurrentPenAlpha(){
        return mCurrentPenAlpha;
    }

    public String getCurrentPenName(){
        return mCurrentPenName;
    }

    public int getCurrentPenId(){
        return mCurrentPenTypeId;
    }

    public String getColor(){
        return getSharedPrefs().
                getString(mContext.getResources().getString(R.string.pen_color_key),
                        mContext.getResources().getString(R.string.pen_color_default));
    }


    @Override
    public void onPenColorChanged(String color) {
        mColor = color;
        getSharedPrefs().
                edit().
                putString(mContext.getResources().getString(R.string.pen_color_key),mColor).
                apply();
    }

    @Override
    public void onPenStrokeWidthChanged(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        getSharedPrefs().edit().putInt(mCurrentPenName, mStrokeWidth).apply();
    }

    @Override
    public void onPenTypeChanged(Paint penType, boolean eraser) {
        mCurrentPenType = penType;
    }
}
