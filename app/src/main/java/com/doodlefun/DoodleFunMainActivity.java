package com.doodlefun;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.doodlefun.fragment.DoodleFunMainFragment;
import com.doodlefun.utils.AppConst;
import com.doodlefun.utils.FileUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DoodleFunMainActivity extends AppCompatActivity
        implements DoodleFunMainFragment.OnNewProjectButtonClickedListener,
                    DoodleFunMainFragment.OnOldProjectButtonClickedListener {

    private DoodleFunMainFragment mDoodleFunMainFragment;
    public static Fragment sCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Solution from stackoverflow for app launch from widget. brings the previously
        //running activity to the front
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        setContentView(R.layout.doodle_fun_main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFragments();
        loadAd();
        new CreatePrivateFolderTask().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNewProjectButtonClicked() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(AppConst.IS_NEW_PROJECT, true);

        Intent intent = new Intent(this, DoodleFunDrawingActivity.class);
        intent.putExtra(AppConst.DRAWING_BUNDLE_KEY,bundle);
        startActivity(intent);
    }

    @Override
    public void onOldProjectButtonClicked(String projectFolder) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.OLD_PROJECT_FOLDER_KEY, projectFolder);
        bundle.putBoolean(AppConst.IS_SAVED_ON_SDCARD_KEY, projectFolder.contains(FileUtils.getExternalProjectFolder(this)));
        bundle.putBoolean(AppConst.IS_NEW_PROJECT, false);

        Intent intent = new Intent(this, DoodleFunDrawingActivity.class);
        intent.putExtra(AppConst.DRAWING_BUNDLE_KEY,bundle);
        startActivity(intent);
    }

    /*
    Methods
     */

    private void initFragments() {
        if (sCurrentFragment != null) {
            Log.d("TAG", " initFragments () " + sCurrentFragment.getTag());
        }
        Fragment f = getSupportFragmentManager().findFragmentByTag(getString(R.string.main_fragment_tag));
        if (f == null) {
            mDoodleFunMainFragment = DoodleFunMainFragment.getInstance();
            try {
                if (!isFinishing()) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_container, mDoodleFunMainFragment, getString(R.string.main_fragment_tag))
                            .commit();
                }
            } catch (IllegalStateException ignored) {
                ignored.printStackTrace();
            }
            sCurrentFragment = DoodleFunMainFragment.getInstance();
        }
    }

    private void loadAd(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.
                Builder().addTestDevice(AppConst.ADVIEW_TEST_DEVICE_CODE).
                build();
        mAdView.loadAd(adRequest);
    }

    /**
     * Create private folders on app startup
     */
    private class CreatePrivateFolderTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return FileUtils.createPrivateDirs(getApplicationContext());
        }
    }
}
