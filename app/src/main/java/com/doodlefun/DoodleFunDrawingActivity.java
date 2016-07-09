package com.doodlefun;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.doodlefun.fragment.DoodleFunDrawingFragment;
import com.doodlefun.utils.AppConst;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DoodleFunDrawingActivity extends AppCompatActivity {

    private DoodleFunDrawingFragment mDoodleFunDrawingFragment;

    private Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doodle_fun_drawing_activity);

        mBundle = getIntent().getBundleExtra("drawing_bundle");
        initFragments();

        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAd();
        setupUi();
    }

    /*
    Methods
     */
    private void initFragments() {

        Fragment f = getSupportFragmentManager().findFragmentByTag(getString(R.string.drawing_fragment_tag));
        if (f == null) {
            mDoodleFunDrawingFragment = DoodleFunDrawingFragment.getInstance();
            if(mBundle!=null)mDoodleFunDrawingFragment.setArguments(mBundle);
            try {
                if (!isFinishing()) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.drawing_container, mDoodleFunDrawingFragment, getString(R.string.drawing_fragment_tag))
                            .commit();
                }
            } catch (IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private void setupUi(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility((View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
    }

    private void loadAd(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.
                Builder().addTestDevice(AppConst.ADVIEW_TEST_DEVICE_CODE).
                build();
        mAdView.loadAd(adRequest);
    }

    /**
     * Options
     */
    public void previewMenuItemClicked(View view){
        int id = view.getId();
        DoodleFunDrawingFragment.getInstance().previewMenuItemClicked(id);
    }

    public void optionMenuItemClicked(View view){
        int id = view.getId();
        DoodleFunDrawingFragment.getInstance().optionMenuItemClicked(id);
    }

    public void penSelected(View view){
        DoodleFunDrawingFragment.getInstance().optionPenSelected(view.getId());
    }
}
