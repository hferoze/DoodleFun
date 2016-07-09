package com.doodlefun.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.doodlefun.R;
import com.doodlefun.fragment.DoodleFunMainFragment;
import com.doodlefun.utils.AppConst;
import com.doodlefun.utils.FileUtils;
import com.doodlefun.utils.Utils;

public class SaveProjectService extends IntentService {

    private static final String TAG = "SaveProjectService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.doodlefun.SAVE_PRJ_STATE_CHANGE";
    public static final String EXTRA_SAVING
            = "com.doodlefun.SAVING";
    public static final String EXTRA_SAVE_SUCCESSFULL
            = "com.doodlefun_SUCCESSFULL";
    public static final String EXTRA_ERR
            = "com.doodlefun.ERROR";

    private Context mContext;

    public SaveProjectService() {
        super(TAG);
        mContext = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String projectName="";
        boolean isProjPrivate=true;
        Bundle bundle = intent.getBundleExtra(AppConst.PROJ_BUNDLE_EXTRA);
        if (bundle!=null){
            projectName = bundle.getString(AppConst.PROJ_NAME_EXTRA);
            isProjPrivate = bundle.getBoolean(AppConst.PROJ_IS_PRIVATE_EXTRA);
        }

        sendLocalBroadcast(true,null,null);
        save(projectName, isProjPrivate);
    }

    private void save(String projName, boolean isProjPrivate){
        new SaveProjectFiles(projName, isProjPrivate).execute();
    }

    private class SaveProjectFiles extends AsyncTask<Void, Void, Integer> {

        private boolean mPrivateFiles = true;
        private String mProjectName = "";

        public SaveProjectFiles(String projectName, boolean privateFiles){
            mPrivateFiles = privateFiles;
            mProjectName = projectName;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int ret = -1;
            Bitmap background = LruCacheHelper.getInstance().getBitmapFromMemCache(getString(R.string.lrucache_background_bitmap_key));
            Bitmap drawing = LruCacheHelper.getInstance().getBitmapFromMemCache(getString(R.string.lrucache_drawing_bitmap_key));

            if (!mPrivateFiles){
                boolean success = FileUtils.deleteInternalProjectFolder(mContext, mProjectName);
                if (success)ret = 1;
                else ret = -1;
            }

            ret = FileUtils.saveFile(mContext, drawing, mProjectName, "drawing", mPrivateFiles );
            if (background!=null)ret = FileUtils.saveFile(mContext, background, mProjectName, "background", mPrivateFiles );

            return ret;
        }
        @Override
        protected void onPostExecute(Integer ret) {
            super.onPostExecute(ret);
            if (ret == 1){
                Toast.makeText(mContext, getString(R.string.project_saved_successfully), Toast.LENGTH_SHORT).show();
                Utils.log(TAG, "Project saved successfully");
                sendLocalBroadcast(false, true, null);
            }else{
                Toast.makeText(mContext, getString(R.string.project_save_failed), Toast.LENGTH_SHORT).show();
                Utils.log(TAG, "Failed to save project");
                sendLocalBroadcast(false, false, "Failed to save project");
            }
            DoodleFunMainFragment.getInstance().refresh();
        }
    }
    private void sendLocalBroadcast(Boolean save_state, Boolean save_result, String err){
        Intent intent = new Intent(BROADCAST_ACTION_STATE_CHANGE);
        if(save_state!=null)intent.putExtra(EXTRA_SAVING, save_state);
        if(save_result!=null)intent.putExtra(EXTRA_SAVE_SUCCESSFULL, save_result);
        if(err!=null)intent.putExtra(EXTRA_ERR, err);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
