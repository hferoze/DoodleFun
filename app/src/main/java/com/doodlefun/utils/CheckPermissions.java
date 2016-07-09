package com.doodlefun.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.doodlefun.R;
import com.doodlefun.fragment.DoodleFunDrawingFragment;

import java.util.ArrayList;
import java.util.List;

public class CheckPermissions {

    private static final String TAG = CheckPermissions.getInstance().getClass().getSimpleName();

    /*
    Permission request Storage
     */
    private static final int PERMISSION_LOCATION = 2;
    private static final int PERMISSION_STORAGE = 3;

    private static CheckPermissions mCheckPermissions = null;
    private DoodleFunDrawingFragment mDoodleFunDrawingFragment = null;

    private String mCurrentlyRequestPermission;
    private int mCurrentlyRequestPermissionCode;

    //Shared preferences
    private SharedPreferences.Editor mSharedPrefEditor;
    private SharedPreferences mSharedPref;

    private OnPermissionAllowedListener mListener;

    private Dialog mAlert;

    private Context mContext;

    public interface OnPermissionAllowedListener{
        public void OnPermissionAllowed(String permissionType);
    }

    public static CheckPermissions getInstance() {
        if (mCheckPermissions == null) {
            mCheckPermissions = new CheckPermissions();
        }
        return mCheckPermissions;
    }

    public void checkPermission(Context context, String permission, int permissionCode, OnPermissionAllowedListener onPermissionAllowedListener) {
        mContext = context;
        mCurrentlyRequestPermission = permission;
        mCurrentlyRequestPermissionCode = permissionCode;
        mDoodleFunDrawingFragment = DoodleFunDrawingFragment.getInstance();
        mSharedPref = mContext.getSharedPreferences(mContext.getString(R.string.permission_preferences), Context.MODE_PRIVATE);
        mSharedPrefEditor = mSharedPref.edit();

        mListener = onPermissionAllowedListener;

        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(mContext, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(permission);
        }

        if (!permissionList.isEmpty()) {

            switch (permissionCode) {

                case PERMISSION_STORAGE:
                    if (!mSharedPref.getBoolean(mContext.getString(R.string.storage_permissions_requested_pref), false)) {
                        mSharedPrefEditor.putBoolean(mContext.getString(R.string.storage_permissions_requested_pref), true);
                        mSharedPrefEditor.apply();
                        Utils.log(TAG,"requesting storage permission now");
                        mDoodleFunDrawingFragment.requestPermissions(permissionList.toArray(new String[permissionList.size()]), permissionCode);
                    } else {
                        int requestCount = mSharedPref.getInt(mContext.getString(R.string.permissions_request_count_pref), 0);
                        requestCount++;
                        Utils.log(TAG,"requesting storage permission no here " + requestCount);
                        mSharedPrefEditor.putInt(mContext.getString(R.string.permissions_request_count_pref), requestCount);
                        mSharedPrefEditor.apply();

                        createPermissionExplanationDialog(mContext.getString(R.string.permission_explanation),
                                mContext.getString(R.string.permission_explanation_storage_title),
                                mContext.getString(R.string.permission_explanation_storage_detail));
                    }
                    break;
                case PERMISSION_LOCATION:
                    if (!mSharedPref.getBoolean(mContext.getString(R.string.location_permissions_requested_pref), false)) {
                        mSharedPrefEditor.putBoolean(mContext.getString(R.string.location_permissions_requested_pref), true);
                        mSharedPrefEditor.apply();
                        Utils.log(TAG,"requesting location permission now");
                        mDoodleFunDrawingFragment.requestPermissions(permissionList.toArray(new String[permissionList.size()]), permissionCode);
                    } else {
                        int requestCount = mSharedPref.getInt(mContext.getString(R.string.permissions_request_count_pref), 0);
                        requestCount++;
                        Utils.log(TAG,"requesting location permission no here" + requestCount);
                        mSharedPrefEditor.putInt(mContext.getString(R.string.permissions_request_count_pref), requestCount);
                        mSharedPrefEditor.apply();
                        Utils.log(TAG,"requesting location permission no here >> " + mSharedPref.getInt(mContext.getString(R.string.permissions_request_count_pref), 0));

                        createPermissionExplanationDialog(mContext.getString(R.string.permission_explanation),
                                mContext.getString(R.string.permission_explanation_location_title),
                                mContext.getString(R.string.permission_explanation_location_detail));
                    }
                    break;
            }
        }else{
            mListener.OnPermissionAllowed(permission);
        }
    }

    private void createPermissionExplanationDialog(String exp, String title, String details) {

        mAlert = new Dialog(mContext);
        mAlert.setContentView(R.layout.dialog_layout);
        mAlert.setCancelable(false);

        ((TextView)mAlert.findViewById(R.id.dialog_title_textView)).setText(exp);
        ((TextView)mAlert.findViewById(R.id.dialog_msg2_textView_title)).setText(title);
        ((TextView)mAlert.findViewById(R.id.dialog_msg2_textView)).setText(details);

        Button okBtn = (Button) mAlert.findViewById(R.id.dialog_ok_btn);
        Button cancelBtn = (Button) mAlert.findViewById(R.id.dialog_cancel_btn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch(mCurrentlyRequestPermissionCode){
                    case PERMISSION_STORAGE:
                        mSharedPrefEditor.putBoolean(mContext.getString(R.string.storage_permissions_requested_pref), false);
                        mSharedPrefEditor.apply();
                        Utils.log(TAG,"requesting storage again");
                        break;
                    case PERMISSION_LOCATION:
                        Utils.log(TAG,"requesting location again " + mSharedPref.getBoolean(mContext.getString(R.string.location_permissions_requested_pref), false));
                        mSharedPrefEditor.putBoolean(mContext.getString(R.string.location_permissions_requested_pref), false);
                        mSharedPrefEditor.apply();
                        Utils.log(TAG,"requesting location again " + mSharedPref.getBoolean(mContext.getString(R.string.location_permissions_requested_pref), false));
                        break;
                }

                mAlert.dismiss();
                checkPermission(mContext, mCurrentlyRequestPermission, mCurrentlyRequestPermissionCode, mListener);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlert.dismiss();
                Toast.makeText(mContext,
                        "Please check permissions", Toast.LENGTH_SHORT).show();
                mSharedPrefEditor.putInt(mContext.getString(R.string.permissions_request_count_pref), 0);
                mSharedPrefEditor.apply();
                mSharedPrefEditor.putBoolean(mContext.getString(R.string.location_permissions_requested_pref), false);
                mSharedPrefEditor.apply();
                mSharedPrefEditor.putBoolean(mContext.getString(R.string.storage_permissions_requested_pref), false);
                mSharedPrefEditor.apply();
            }
        });

        mAlert.show();
    }
}
