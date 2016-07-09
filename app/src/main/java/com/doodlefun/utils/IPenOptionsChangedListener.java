package com.doodlefun.utils;

import android.graphics.Paint;

/**
 * Created by locker on 6/27/2016.
 */
public interface IPenOptionsChangedListener {
    public void onPenColorChanged(String color);
    public void onPenStrokeWidthChanged(int strokeWidth);
    public void onPenTypeChanged(Paint penType, boolean eraser);
}
