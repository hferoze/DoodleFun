package com.doodlefun.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.doodlefun.R;


public class ColorPaletteAdapter extends BaseAdapter {

    private Context mContext;

    public String[] thumbTags;

    public ColorPaletteAdapter(Context c, int cols) {
        mContext = c;
        thumbTags = mContext.getResources().getStringArray(R.array.colorsmore);
    }

    @Override
    public int getCount() {
        return thumbTags.length;
    }

    @Override
    public Object getItem(int arg0) {
        return thumbTags[arg0];
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View grid;

        if(convertView==null){
            grid = new View(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            grid=inflater.inflate(R.layout.color_palette_item, parent, false);
        }else{
            grid = (View)convertView;
        }

        final ImageView palleteBtn = (ImageView)grid.findViewById(R.id.grid_palette_image);
        palleteBtn.setImageDrawable(getCircle(Paint.Style.FILL_AND_STROKE, Color.parseColor(thumbTags[position]), -1));
        palleteBtn.setTag(Color.parseColor(thumbTags[position]));


        final ImageView borderBtn = (ImageView)grid.findViewById(R.id.grid_palette_background_image);
        borderBtn.setImageDrawable(
                getCircle(Paint.Style.STROKE,
                mContext.getResources().getColor(R.color.black_translucent),
                (int)mContext.getResources().getDimension(R.dimen.color_palette_color_circle_stroke_width)));

        return grid;
    }

    private ShapeDrawable getCircle(Paint.Style style, int color, int strokeWidth){
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setStyle(style);
        if(style == Paint.Style.STROKE)drawable.getPaint().setStrokeWidth(strokeWidth);
        drawable.getPaint().setColor(color);
        drawable.getPaint().setAntiAlias(true);
        drawable.setIntrinsicHeight((int)mContext.getResources().getDimension(R.dimen.color_palette_color_circle_width_height));
        drawable.setIntrinsicWidth((int)mContext.getResources().getDimension(R.dimen.color_palette_color_circle_width_height));
        return drawable;
    }

    OnImageSelectedListener mCallback;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        mCallback = listener;
    }

    public interface OnImageSelectedListener
    {
        void onImageSelected(int color);
    }

}


