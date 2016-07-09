package com.doodlefun.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

public class PenDemoView extends View implements IPenOptionsChangedListener{

    private int w, h;

    private PointF mPoint1;
    private PointF mPoint2;
    private PointF mPoint3;
    private Path mPath;
    private Paint mPaint;
    private Paint mPaintBackground;

    private boolean mEraser;
    private Xfermode mEraserXferMode;

    private Context mContext;

    public PenDemoView(Context context) {
        super(context);
        init(context);
    }

    public PenDemoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PenDemoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){

        mContext = context;
        mPath = new Path();

        mEraser = false;
        mEraserXferMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mPaintBackground = new Paint();
        mPaintBackground.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintBackground.setColor(Color.WHITE);

        setPenConfig(PenTypes.getInstance(mContext).getCurrentPenType(), PenTypes.getInstance(mContext).getCurrentPenId()==0);
    }

    public void setPenConfig(Paint paint, boolean eraser){
        mPaint  = new Paint(paint.getFlags());
        mPaint.setStyle(paint.getStyle());
        mPaint.setStrokeWidth(paint.getStrokeWidth()+PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth());
        mPaint.setColor(paint.getColor());
        mPaint.setStrokeJoin(paint.getStrokeJoin());
        mPaint.setStrokeCap(paint.getStrokeCap());
        mPaint.setStrokeMiter(paint.getStrokeMiter());
        mPaint.setShader(paint.getShader());
        mPaint.setColorFilter(paint.getColorFilter());
        mPaint.setXfermode(paint.getXfermode());
        mPaint.setMaskFilter(paint.getMaskFilter());
        setEraser(eraser);

        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        mPoint1 = new PointF(0.65f*w, h/1.2F);
        mPoint2 = new PointF(0.45f*w, h/12F);
        mPoint3 = new PointF(0.15f*w, h/1.2F);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getEraser())canvas.drawRect(0,0,w,h,mPaintBackground);
        mPath = drawCurve(mPoint1, mPoint2, mPoint3);
        canvas.drawPath(mPath, mPaint);
    }

    private Path drawCurve(PointF mPointa, PointF mPointb, PointF mPointc) {

        Path path = new Path();
        path.moveTo(0.85f*w, h/4);
        path.cubicTo(mPointa.x, mPointa.y, mPointb.x, mPointb.y, mPointc.x, mPointc.y);
        return path;
    }

    @Override
    public void onPenColorChanged(String color) {
        String col = "#"+PenTypes.getInstance(mContext).getCurrentPenAlpha()+color;
        if (mPaint.getShader()!=null){
            mPaint.setShader(PenTypes.getInstance(mContext).getShader());
        }
        mPaint.setColor(Color.parseColor(col));

        invalidate();
    }

    private void setEraser(boolean eraser){
        mEraser = eraser;
    }

    private boolean getEraser(){
        return mEraser;
    }

    @Override
    public void onPenStrokeWidthChanged(int strokeWidth) {

        int stroke = strokeWidth+PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth();
        mPaint.setStrokeWidth(stroke);
        invalidate();
    }

    @Override
    public void onPenTypeChanged(Paint penType, boolean eraser) {
        setPenConfig(penType, eraser);
    }
}
