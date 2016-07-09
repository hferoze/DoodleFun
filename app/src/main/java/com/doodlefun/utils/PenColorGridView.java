package com.doodlefun.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.GridView;

import com.doodlefun.adapter.ColorPaletteAdapter;

/**
 * Created by locker on 6/25/2016.
 */
public class PenColorGridView extends GridView{

    private static final int NUM_OF_COLUMNS = 7;

    private ColorPaletteAdapter mColorPaletteAdapter;

    public PenColorGridView(Context context) {
        super(context);
        init(context);
    }

    public PenColorGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PenColorGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context){

        mColorPaletteAdapter = new ColorPaletteAdapter(context, NUM_OF_COLUMNS);
        setAdapter(mColorPaletteAdapter);
        requestFocusFromTouch();
        setVerticalSpacing(6);
        setHorizontalSpacing(0);
        setNumColumns(NUM_OF_COLUMNS);
        setGravity(Gravity.CENTER);
    }

    //Trick found in a stackoverflow discussion that let's gridview show all its items
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec;

        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {

            heightSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        }
        else {
            heightSpec = heightMeasureSpec;
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
