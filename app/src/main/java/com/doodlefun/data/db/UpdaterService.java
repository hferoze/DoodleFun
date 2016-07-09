package com.doodlefun.data.db;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.doodlefun.utils.FileUtils;
import com.doodlefun.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.hferoze.android.snapdroll.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.hferoze.android.snapdroll.REFRESHING";

    public UpdaterService() {

        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = DoodleFunContract.Items.buildDirUri();

        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {

            File internalProjectFolder = new File(FileUtils.getInternalProjectFolder(this));
            File externalProjectFolder = new File(FileUtils.getExternalProjectFolder(this));

            Utils.log(TAG, " internal folder is " + internalProjectFolder);
            ArrayList<DoodleFunProjectData> doodlefunProjectDataArray = GetProjectsInfo.getAllProjectInfo(internalProjectFolder, externalProjectFolder);
            if (doodlefunProjectDataArray == null) {
                throw new ArrayIndexOutOfBoundsException("Invalid parsed item array" );
            }

            for (int i = 0; i < doodlefunProjectDataArray.size(); i++) {
                ContentValues values = new ContentValues();
                DoodleFunProjectData snapFilesData = doodlefunProjectDataArray.get(i);

                values.put(DoodleFunContract.Items.PROJECT_NAME, snapFilesData.getProjectName());
                values.put(DoodleFunContract.Items.DATE_CREATED, snapFilesData.getDateCreated());
                values.put(DoodleFunContract.Items.DATE_MODIFIED, snapFilesData.getDateModified());
                values.put(DoodleFunContract.Items.HAS_BACKGROUND_IMAGE, snapFilesData.getHasBackgroundImage());
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(DoodleFunContract.CONTENT_AUTHORITY, cpo);

        } catch (RemoteException | OperationApplicationException | NullPointerException | IndexOutOfBoundsException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }
}
