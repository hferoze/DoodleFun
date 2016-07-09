package com.doodlefun.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.doodlefun.R;
import com.doodlefun.data.db.DoodleFunContract;
import com.doodlefun.data.db.DoodleFunLoader;
import com.doodlefun.utils.AppConst;
import com.doodlefun.utils.PicassoHelper;

public class UserProjectsAdapter extends RecyclerView.Adapter<UserProjectsAdapter.ViewHolder> {

    private OnProjectItemClickedListener mListener;
    private Context mContext;

    private Cursor mCursor;

    private PicassoHelper mPicassoHelper;


    public UserProjectsAdapter(Context context, OnProjectItemClickedListener listener) {
        mContext = context;
        mListener = listener;
        mPicassoHelper = PicassoHelper.getInstance(mContext);
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor!=null && !mCursor.isClosed()) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(DoodleFunLoader.Query._ID);
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_items, null);
        final ViewHolder viewHolder = new ViewHolder(layoutView);

        viewHolder.setIsRecyclable(false);

        layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                DoodleFunContract.Items.buildItemUri(getItemId(position)).toString();
                int width = viewHolder.projectThumbImageView.getWidth();
                int height = viewHolder.projectThumbImageView.getHeight();
                float x = v.getX()+width/2;
                float y = v.getY()+height/2;

                mListener.OnProjectItemClicked(mCursor.getString(DoodleFunLoader.Query.PROJECT_NAME), (int)x,  (int)y);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor.getString(DoodleFunLoader.Query.PROJECT_NAME)!=null) {
            String jpgPath = AppConst.FILE_URI_EXT + mCursor.getString(DoodleFunLoader.Query.PROJECT_NAME) + "/" + AppConst.THUMB_FILE_NAME;
            mPicassoHelper.setImage(jpgPath, holder.projectThumbImageView);
        }
    }


    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor!=null ) {
            count =  mCursor.getCount();
        }
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView projectThumbImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            projectThumbImageView = (ImageView) itemView.findViewById(R.id.project_thumb_image_view);
        }
    }

    public interface OnProjectItemClickedListener{
        public void OnProjectItemClicked(String projectFolder, int x, int y);
    }
}
