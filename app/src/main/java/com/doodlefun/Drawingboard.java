package com.doodlefun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.doodlefun.fragment.DoodleFunDrawingFragment;
import com.doodlefun.utils.IPenOptionsChangedListener;
import com.doodlefun.utils.PaintInfo;
import com.doodlefun.utils.PenTypes;
import com.doodlefun.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by locker on 6/21/2016.
 */
public class Drawingboard extends ImageView implements View.OnTouchListener, IPenOptionsChangedListener{

    private static final String TAG = "Drawingboard";

    private static final int OPTIONS_MODE_DRAWING = 0;
    private static final int OPTIONS_MODE_PEN = 1;
    private static final int OPTIONS_MODE_PREVIEW = 2;

    private static final float TOUCH_TOLERANCE = 2;

    //Doodle
    private Bitmap mBackgroundBitmap;
    private Bitmap mDrawBitmap;
    private Paint mPaint;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private Path mPath;
    private ArrayList<Path> mUndoneDoodlePaths = new ArrayList<Path>();
    public ArrayList<Path> doodlePaths = new ArrayList<Path>();
    private HashMap<Path, PaintInfo> mPaintInfoMap;
    private Paint.Cap mStrokeCap;
    private BitmapShader mPatternShader;
    private PorterDuffXfermode mXferMode;
    private MaskFilter mMaskFilter;
    public int mPaintColor;
    private int mStrokeWidth;
    private int mWidth;
    private int mHeight;
    private boolean mEraserEnabled;
    private boolean mChangesMade;
    private float mX, mY;

    private Context mContext;

    public Drawingboard(Context context) {
        super(context);
        init(context);
    }

    public Drawingboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Drawingboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){

        mContext = context;

        setChangesMade(false);

        setEraserEnabled(false);

        setDrawingCacheEnabled(true);

        setDoodleBitmap();

        setOnTouchListener(this);
    }

    private void setDoodleBitmap(){

        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setAdjustViewBounds(true);

        mPaint = new Paint();
        mPaintInfoMap = new HashMap<>();
        mPath = new Path();

        Utils.log(TAG,"setting pen to " +PenTypes.getInstance(mContext).
                getSharedPrefs().getInt(mContext.getString(R.string.last_selected_pen_key), 1) );
        PenTypes.getInstance(mContext).
                getPenType(PenTypes.getInstance(mContext).
                        getSharedPrefs().getInt(mContext.getString(R.string.last_selected_pen_key), 1));

        setPenConfig(PenTypes.getInstance(mContext).getCurrentPenType());

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setPenConfig(Paint paint){
        mPaint  = new Paint(paint.getFlags());
        mPaint.setStyle(paint.getStyle());
        mPaint.setStrokeWidth(paint.getStrokeWidth()+PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth());
        mPaint.setColor(paint.getColor());
        mPaint.setStrokeJoin(paint.getStrokeJoin());//
        mPaint.setStrokeCap(paint.getStrokeCap());
        mPaint.setStrokeMiter(paint.getStrokeMiter());
        mPaint.setShader(paint.getShader());
        mPaint.setXfermode(paint.getXfermode());
        mPaint.setMaskFilter(paint.getMaskFilter());

        mPaintColor = mPaint.getColor();
        mStrokeWidth = (int)mPaint.getStrokeWidth();
        mStrokeCap = mPaint.getStrokeCap();
        mPatternShader = (BitmapShader) mPaint.getShader();
        mXferMode = (PorterDuffXfermode) mPaint.getXfermode();
        mMaskFilter = mPaint.getMaskFilter();

        mPaintInfoMap.put(mPath, new PaintInfo(mPaintColor,
                mStrokeWidth, mMaskFilter, mStrokeCap, mPatternShader, mXferMode));

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setCanvasBackground(Bitmap bitmap){
        if (bitmap!=null) {
            mBackgroundBitmap = bitmap;
        }else{
            setBackground(null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Utils.log(TAG, "onSizeChanged w " + w + " h " + h );
        if (w >0 && h>0) {

            mDrawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mDrawBitmap);

            if(mBackgroundBitmap!=null){
                mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
            }

            mWidth = w;
            mHeight = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mDrawBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (DoodleFunDrawingFragment.getInstance().getCurrentOptionsMode() == OPTIONS_MODE_DRAWING) {
            if (event.getPointerCount() == 1) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_start(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touch_move(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up();
                        break;
                }
                invalidate();
                return true;
            }
        } else if (DoodleFunDrawingFragment.getInstance().getCurrentOptionsMode() == OPTIONS_MODE_PREVIEW) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    DoodleFunDrawingFragment.getInstance().setOptionsMode(OPTIONS_MODE_DRAWING);
                    break;
            }
            return true;
        } else if (DoodleFunDrawingFragment.getInstance().getCurrentOptionsMode() == OPTIONS_MODE_PEN) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    DoodleFunDrawingFragment.getInstance().setOptionsMode(OPTIONS_MODE_DRAWING);
                    break;
            }
            return true;
        }
        return false;
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mUndoneDoodlePaths.clear();
        mX = x;
        mY = y;
        if (!DoodleFunDrawingFragment.getInstance().getScreenExpanded())
            DoodleFunDrawingFragment.getInstance().hideOptionsBar(true);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);

        mCanvas.drawPath(mPath, mPaint);

        mPaintInfoMap.put(mPath, new PaintInfo(mPaintColor,
                mStrokeWidth, mMaskFilter, mStrokeCap, mPatternShader, mXferMode));

        doodlePaths.add(mPath);
        mPath = new Path();
        mPath.reset();
        if (!DoodleFunDrawingFragment.getInstance().getScreenExpanded())
            DoodleFunDrawingFragment.getInstance().hideOptionsBar(false);

        if(doodlePaths.size()>0) {
            setChangesMade(true);
            ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.undo_option_main)).setImageResource(R.drawable.undo_enabled);
            ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.redo_option_main)).setImageResource(R.drawable.redo);
        }
    }

    public void undoDoodle(){
        if(doodlePaths.size()>0) {
            mUndoneDoodlePaths.add(doodlePaths.remove(doodlePaths.size() - 1));
            mDrawBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mDrawBitmap);
            if(mBackgroundBitmap!=null){
                mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
            }

            for (Path p : doodlePaths) {
                mPaint.setColor(mPaintInfoMap.get(p).getColor());
                mPaint.setStrokeWidth(mPaintInfoMap.get(p).getWidth());
                mPaint.setStrokeCap(mPaintInfoMap.get(p).getStrokeCap());
                mPaint.setShader(mPaintInfoMap.get(p).getBitmapShader());
                mPaint.setMaskFilter(mPaintInfoMap.get(p).getFilter());
                mPaint.setXfermode(mPaintInfoMap.get(p).getXferMode());

                mCanvas.drawPath(p, mPaint);
            }

            if (doodlePaths.size()==0){
                ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.undo_option_main)).setImageResource(R.drawable.undo);
                setChangesMade(false);
            }
            ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.redo_option_main)).setImageResource(R.drawable.redo_enabled);

            setPaintToCurrent();
            invalidate();
        }
    }

    public void redoDoodle(){
        if (mUndoneDoodlePaths.size()>0) {
            doodlePaths.add(mUndoneDoodlePaths.remove(mUndoneDoodlePaths.size() - 1));
            mDrawBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mDrawBitmap);
            if(mBackgroundBitmap!=null){
                mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
            }

            for (Path p : doodlePaths) {
                mPaint.setColor(mPaintInfoMap.get(p).getColor());
                mPaint.setStrokeWidth(mPaintInfoMap.get(p).getWidth());
                mPaint.setStrokeCap(mPaintInfoMap.get(p).getStrokeCap());
                mPaint.setShader(mPaintInfoMap.get(p).getBitmapShader());
                mPaint.setMaskFilter(mPaintInfoMap.get(p).getFilter());
                mPaint.setXfermode(mPaintInfoMap.get(p).getXferMode());

                mCanvas.drawPath(p, mPaint);
            }

            if(mUndoneDoodlePaths.size()==0){
                ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.redo_option_main)).setImageResource(R.drawable.redo);
            }
            if(doodlePaths.size()>0) {
                ((ImageView)DoodleFunDrawingFragment.getInstance()._View.findViewById(R.id.undo_option_main)).setImageResource(R.drawable.undo_enabled);
            }

            setPaintToCurrent();
            invalidate();
        }
    }

    private void setPaintToCurrent(){
        mPaint.setColor(mPaintColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(mStrokeCap);
        mPaint.setShader(mPatternShader);
        mPaint.setMaskFilter(mMaskFilter);
        mPaint.setXfermode(mXferMode);
    }


    private void setEraserEnabled(boolean eraserEnabled){
        mEraserEnabled=eraserEnabled;
    }

    private boolean getEraserEnabled(){
        return mEraserEnabled;
    }

    public void setChangesMade(boolean changesMade){
        mChangesMade = changesMade;
    }

    public boolean getChangesMade(){
        return mChangesMade;
    }

    @Override
    public void onPenColorChanged(String color) {

        mPaintColor = Color.parseColor("#"+PenTypes.getInstance(mContext).getCurrentPenAlpha()+color);

        if (mPaint.getShader()!=null){
            mPatternShader = PenTypes.getInstance(mContext).getShader();
            mPaint.setShader(mPatternShader);
        }
        mPaint.setColor(mPaintColor);
    }

    @Override
    public void onPenStrokeWidthChanged(int strokeWidth) {
        mStrokeWidth = strokeWidth+PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth();
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    public void onPenTypeChanged(Paint penType, boolean eraser) {
        setPenConfig(penType);
        setEraserEnabled(eraser);
    }
}
