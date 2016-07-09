package com.doodlefun.fragment;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.doodlefun.Drawingboard;
import com.doodlefun.R;
import com.doodlefun.adapter.ColorPaletteAdapter;
import com.doodlefun.data.LruCacheHelper;
import com.doodlefun.data.SaveProjectService;
import com.doodlefun.data.db.UpdaterService;
import com.doodlefun.utils.AppConst;
import com.doodlefun.utils.CheckPermissions;
import com.doodlefun.utils.FileUtils;
import com.doodlefun.utils.IPenOptionsChangedListener;
import com.doodlefun.utils.LocationService;
import com.doodlefun.utils.LocationService.OnLocationFoundListener;
import com.doodlefun.utils.PenColorGridView;
import com.doodlefun.utils.PenDemoView;
import com.doodlefun.utils.PenTypes;
import com.doodlefun.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import io.codetail.animation.SupportAnimator;

public class DoodleFunDrawingFragment extends Fragment
        implements OnLocationFoundListener, CheckPermissions.OnPermissionAllowedListener {

    private static final String TAG = getInstance().getClass().getSimpleName();

    private static final int OPTIONS_MODE_DRAWING = 0;
    private static final int OPTIONS_MODE_PEN = 1;
    private static final int OPTIONS_MODE_PREVIEW = 2;

    private static final int PEN_OPTIONS_REVEAL_DURATION = 350;

    /*
    Permission request Storage
     */
    private static final int PERMISSION_LOCATION = 2;
    private static final int PERMISSION_STORAGE = 3;

    private static final String LOCATION_FRAME_LAYOUT_TAG = "location_frame_layout";

    private static final float THUMB_SCALE = 0.4f;

    //mode
    private int mCurrentOptionsMode = OPTIONS_MODE_DRAWING;

    private Bitmap mBackgroundBitmap = null;

    private String mProjectName = "";
    private String mProjectFolder = "";

    private boolean isNewProject = true;
    private boolean mIsOptionsBarHidden = false;
    private boolean mIsProjectSavedInSDCard;
    private boolean mIsScreenExapanded = false;

    //this view
    public View _View;

    //Drawing board
    public static Drawingboard sDrawingBoard;

    //this Fragment
    private static DoodleFunDrawingFragment mDoodleFunDrawingFragment;

    //Shared preferences
    private SharedPreferences.Editor mSharedPrefEditor;
    private SharedPreferences mSharedPref;

    private Context mContext;

    private LruCacheHelper mLruCache;

    private ImageView mExpandScreenBtn;
    private ImageView mPenOptionsBtn;
    private ImageView mPenOptionBackground;
    private ImageView mRemoveLocationFilter;
    private ArrayList<ImageView> mPenToolOptionBackgrounds;

    //pen options
    private ArrayList<IPenOptionsChangedListener> mPenOptionChangedListeners;
    private SeekBar mPenSizeSeekBar;
    private int mSeekBarMin;
    private int mSeekBarMax;

    //location
    private AssetManager mAsset;
    private Typeface mLocationFontTypeFace;
    private FrameLayout mLocationFrameLayout;

    public static DoodleFunDrawingFragment getInstance() {
        if (mDoodleFunDrawingFragment == null)
            mDoodleFunDrawingFragment = new DoodleFunDrawingFragment();
        return mDoodleFunDrawingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getContext();
        mLruCache = LruCacheHelper.getInstance();

        mSharedPref = mContext.getSharedPreferences(getString(R.string.permission_preferences), Context.MODE_PRIVATE);
        mSharedPrefEditor = mSharedPref.edit();
        mSharedPrefEditor.putBoolean(getString(R.string.storage_permissions_requested_pref), false);
        mSharedPrefEditor.putBoolean(getString(R.string.location_permissions_requested_pref), false);
        mSharedPrefEditor.putInt(getString(R.string.permissions_request_count_pref), 0);
        mSharedPrefEditor.apply();

        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSharedPref.getBoolean(getString(R.string.storage_permissions_requested_pref), false)
                && mSharedPref.getInt(getString(R.string.permissions_request_count_pref), 0) < 2) {
            CheckPermissions.getInstance().checkPermission(mContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    PERMISSION_STORAGE,
                    this);
        } else if (mSharedPref.getBoolean(getString(R.string.storage_permissions_requested_pref), false)) {
            Toast.makeText(mContext,
                    getString(R.string.permission_storage_failure_toast), Toast.LENGTH_SHORT).show();
        }
        if (mSharedPref.getBoolean(getString(R.string.location_permissions_requested_pref), false)
                && mSharedPref.getInt(getString(R.string.permissions_request_count_pref), 0) < 2) {
            CheckPermissions.getInstance().checkPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    PERMISSION_LOCATION,
                    this);
        } else if (mSharedPref.getBoolean(getString(R.string.location_permissions_requested_pref), false)) {
            Toast.makeText(mContext,
                    getString(R.string.permission_location_failure_toast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPenToolOptionBackgrounds.clear();
        mPenToolOptionBackgrounds=null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.doodle_fun_drawing_fragment, container, false);
        _View = rootView;
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int DB_UPDATE_DELAY = 1500;

        sDrawingBoard = (Drawingboard) view.findViewById(R.id.drawingboard_view);
        mExpandScreenBtn = (ImageView) view.findViewById(R.id.expand_screen_btn);
        mExpandScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsScreenExapanded) {
                    setScreenExpanded(true);
                    mExpandScreenBtn.setImageResource(R.drawable.full_screen_close);
                } else {
                    setScreenExpanded(false);
                    mExpandScreenBtn.setImageResource(R.drawable.full_screen);
                }
            }
        });

        mRemoveLocationFilter = (ImageView) view.findViewById(R.id.remove_location_filter_btn);
        mRemoveLocationFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup parent = (ViewGroup) _View.getParent();
                if(parent.findViewWithTag(LOCATION_FRAME_LAYOUT_TAG) != null){
                    parent.removeView(mLocationFrameLayout);
                    mRemoveLocationFilter.setVisibility(View.GONE);
                }
            }
        });
        mLocationFrameLayout = new FrameLayout(mContext);

        initiPenOptions(view);

        if (isNewProject) {
            //create new project and add entry to db
            mIsProjectSavedInSDCard = false;
            mProjectName = UUID.randomUUID().toString();
            Utils.log(TAG, "new project " + mProjectName);

            if (mLruCache.getCache() != null && mLruCache.getCache().size() > 0) {

                //set Background from cache
                mBackgroundBitmap = mLruCache.getBitmapFromMemCache(getString(R.string.lrucache_background_bitmap_key));
                setBackground(view, mBackgroundBitmap);
                //set canvas to null
                sDrawingBoard.setCanvasBackground(null);

            }
        } else {
            //open old project
            try {
                mProjectName = new File(mProjectFolder).getName();
                Utils.log(TAG, "open old project " + mProjectName);

                //set Background from file
                File backgroundFile = new File(mProjectFolder + "/" + AppConst.BACKGROUND_FILE_NAME);
                mBackgroundBitmap = BitmapFactory.decodeFile(backgroundFile.getAbsolutePath());
                setBackground(view, mBackgroundBitmap);

                //set drawing canvas from previously saved file
                File drawingFile = new File(mProjectFolder + "/" + AppConst.DRAWING_FILE_NAME);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inMutable = true;
                Bitmap drawingBitmap = BitmapFactory.decodeFile(drawingFile.getAbsolutePath(), bmOptions);
                sDrawingBoard.setCanvasBackground(drawingBitmap);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        //update db
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isVisible())
                    getActivity().startService(new Intent(mContext, UpdaterService.class));
            }
        }, DB_UPDATE_DELAY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        Utils.log(TAG, "onActivityCreated");
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    //in preview mode, go back to drawing mode
                    if (keyCode == KeyEvent.KEYCODE_BACK && getCurrentOptionsMode() == OPTIONS_MODE_PREVIEW) {
                        setOptionsMode(OPTIONS_MODE_DRAWING);
                        return true;
                        //ask user to if they want to save project ONLY if changes were made
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && getCurrentOptionsMode() == OPTIONS_MODE_DRAWING && sDrawingBoard.getChangesMade()) {
                        querySaveChanges();
                        return true;
                        //in pen mode, go back to drawing mode
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && getCurrentOptionsMode() == OPTIONS_MODE_PEN) {
                        setOptionsMode(OPTIONS_MODE_DRAWING);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void OnLocationFound(final String location) {
        final String LOCATION_SEARCH_FAILED_KEY = "Failed";
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Utils.log(TAG, "location found: " + location);
                if (!TextUtils.equals(location, LOCATION_SEARCH_FAILED_KEY) && location.length()>0) {
                    Bitmap locationBitmap = Bitmap.createBitmap((int) sDrawingBoard.getWidth(),
                            sDrawingBoard.getHeight() / 4, Bitmap.Config.ARGB_8888);
                    Paint paint = createLocationTextPaint(mLocationFontTypeFace, true, locationBitmap.getWidth()/location.length());
                    Canvas canvas = new Canvas(locationBitmap);
                    canvas.drawText(location,
                            sDrawingBoard.getWidth() / 2 - paint.measureText(location) / 2,
                            locationBitmap.getHeight() >> 1, paint);
                    ViewGroup parent = (ViewGroup) _View.getParent();
                    //mLocationFrameLayout = new FrameLayout(mContext);
                    mLocationFrameLayout.setTag(LOCATION_FRAME_LAYOUT_TAG);
                    if(parent.findViewWithTag(LOCATION_FRAME_LAYOUT_TAG) != null){
                        Utils.log(TAG, "remove previous location frame");
                        parent.removeViewAt(parent.getChildCount()-1);
                    }

                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) sDrawingBoard.getWidth(),
                            sDrawingBoard.getHeight());
                    lp.topMargin = (int)(locationBitmap.getHeight());

                    ImageView locationImageView = new ImageView(mContext);
                    locationImageView.setImageBitmap(locationBitmap);
                    mLocationFrameLayout.addView(locationImageView, lp);

                    lp = new FrameLayout.LayoutParams((int) sDrawingBoard.getWidth(),
                            sDrawingBoard.getHeight());
                    lp.topMargin = sDrawingBoard.getTop();
                    lp.leftMargin = sDrawingBoard.getLeft();

                    parent.addView(mLocationFrameLayout, lp);
                    mRemoveLocationFilter.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(mContext, getString(R.string.location_not_found_toast), Toast.LENGTH_SHORT).show();
                }
                _View.findViewById(R.id.location_filter_option_main).setEnabled(true);
            }
        });
    }


    private void init() {
        mIsScreenExapanded = false;
        mBackgroundBitmap = null;
        mProjectFolder = null;
        isNewProject = true;
        mIsProjectSavedInSDCard = false;
        mProjectName = "";

        mAsset = getActivity().getAssets();
        mLocationFontTypeFace = Typeface.createFromAsset(mAsset, getResources().getString(R.string.hafertoon_font));

        if (getArguments() != null) {
            if (getArguments().containsKey(AppConst.IS_NEW_PROJECT)) {
                isNewProject = getArguments().getBoolean(AppConst.IS_NEW_PROJECT);
                if (!isNewProject) {
                    if (getArguments().containsKey(AppConst.OLD_PROJECT_FOLDER_KEY)) {
                        mProjectFolder = getArguments().getString(AppConst.OLD_PROJECT_FOLDER_KEY);
                    }
                    if (getArguments().containsKey(AppConst.IS_SAVED_ON_SDCARD_KEY)) {
                        mIsProjectSavedInSDCard = getArguments().getBoolean(AppConst.IS_SAVED_ON_SDCARD_KEY);
                    }
                }
            }
        }
    }

    private void initiPenOptions(View view) {
        mPenOptionsBtn = (ImageView) view.findViewById(R.id.pen_option_main);

        mPenOptionBackground = (ImageView) view.findViewById(R.id.pen_option_main_background);
        mPenOptionBackground.setColorFilter(Color.parseColor("#" + PenTypes.getInstance(mContext).getColor()));

        mPenToolOptionBackgrounds = new ArrayList<>();
        mPenToolOptionBackgrounds.add(((ImageView) _View.findViewById(R.id.eraser_option_background)));
        mPenToolOptionBackgrounds.add(((ImageView) _View.findViewById(R.id.pen_0_option_background)));
        mPenToolOptionBackgrounds.add(((ImageView) _View.findViewById(R.id.pen_1_option_background)));
        mPenToolOptionBackgrounds.add(((ImageView) _View.findViewById(R.id.pen_2_option_background)));
        mPenToolOptionBackgrounds.add(((ImageView) _View.findViewById(R.id.pen_3_option_background)));

        setPenOptionButon(PenTypes.getInstance(mContext).
                getSharedPrefs().getInt(mContext.getString(R.string.last_selected_pen_key), 1));
    }

    private void setPenOptionButon(int key) {

        for (ImageView im : mPenToolOptionBackgrounds)
            im.setImageResource(android.R.color.transparent);

        mPenToolOptionBackgrounds.get(key).setImageResource(R.drawable.pen_option_background);
        mPenToolOptionBackgrounds.get(key).setColorFilter(getResources().getColor(R.color.black_translucent));

        switch (key) {
            case 0:
                mPenOptionsBtn.setImageResource(R.drawable.eraser);
                break;
            case 1:
                mPenOptionsBtn.setImageResource(R.drawable.pointer);
                break;
            case 2:
                mPenOptionsBtn.setImageResource(R.drawable.pencil);
                break;
            case 3:
                mPenOptionsBtn.setImageResource(R.drawable.brush);
                break;
            case 4:
                mPenOptionsBtn.setImageResource(R.drawable.highlighter);
                break;
        }
    }

    private void setBackground(final View view, final Bitmap bitmap) {
        final ImageView backgroundView = (ImageView) view.findViewById(R.id.drawingboard_background_image_view);
        final Drawable backgroundDrawable = new BitmapDrawable(getResources(), bitmap);
        backgroundView.setImageDrawable(backgroundDrawable);
        backgroundView.setAdjustViewBounds(true);

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) sDrawingBoard.getLayoutParams();
                lp.width = backgroundView.getWidth();
                lp.height = backgroundView.getHeight();
                lp.gravity = Gravity.CENTER;
                sDrawingBoard.setLayoutParams(lp);
                sDrawingBoard.requestLayout();
            }
        }, 15);
    }


    public void optionMenuItemClicked(int id) {
        switch (id) {
            case R.id.pen_option_main:
                if (getCurrentOptionsMode() != OPTIONS_MODE_PEN)
                    setOptionsMode(OPTIONS_MODE_PEN);
                break;
            case R.id.location_filter_option_main:
                Toast.makeText(mContext, getString(R.string.location_finding_toast), Toast.LENGTH_SHORT).show();
                locationFilerClicked();
                break;
            case R.id.emoji_option_main:
                addEmojis();
                break;
            case R.id.undo_option_main:
                sDrawingBoard.undoDoodle();
                break;
            case R.id.redo_option_main:
                sDrawingBoard.redoDoodle();
                break;
            case R.id.done_option_main:
                doneDrawingClicked();
                break;
        }
    }

    public void previewMenuItemClicked(int id) {
        switch (id) {
            case R.id.save_option:
                saveProjectClicked();
                break;
            case R.id.share_option:
                shareProjectClicked();
                break;
            case R.id.discard_option:
                getActivity().finish();
                break;
        }
    }

    public void optionPenSelected(int id) {
        Paint paint = new Paint();
        boolean eraser = false;
        switch (id) {
            case R.id.eraser_option_pen:
                paint = PenTypes.getInstance(mContext).getPenType0();
                setPenOptionButon(0);
                eraser = true;
                break;
            case R.id.pen_0_option_pen:
                paint = PenTypes.getInstance(mContext).getPenType1();
                setPenOptionButon(1);
                break;
            case R.id.pen_1_option_pen:
                paint = PenTypes.getInstance(mContext).getPenType2();
                setPenOptionButon(2);
                break;
            case R.id.pen_2_option_pen:
                paint = PenTypes.getInstance(mContext).getPenType3();
                setPenOptionButon(3);
                break;
            case R.id.pen_3_option_pen:
                paint = PenTypes.getInstance(mContext).getPenType4();
                setPenOptionButon(4);
                break;
        }
        mSeekBarMax = PenTypes.getInstance(mContext).getCurrentPenMaxStrokeWidth();
        mSeekBarMin = PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth();
        int stroke = PenTypes.getInstance(mContext).getStrokeWidth();
        mPenSizeSeekBar.setMax(mSeekBarMax);
        mPenSizeSeekBar.setProgress(stroke);

        for (IPenOptionsChangedListener l : mPenOptionChangedListeners) {
            l.onPenTypeChanged(paint, eraser);
        }
    }

    private void displayPenOptions(final boolean display, int x, int y) {
        final RelativeLayout relativeLayout = (RelativeLayout) _View.findViewById(R.id.pen_tools_relative_layout);
        int radius = relativeLayout.getWidth();
        int startRadius = 0, endRadius = 0;

        if (display && relativeLayout.getVisibility() == View.INVISIBLE) {
            startRadius = 0;
            endRadius = radius;
            ((ImageView) _View.findViewById(R.id.done_option_main)).setImageResource(R.drawable.close_pen_menu);
        } else if (!display && relativeLayout.getVisibility() == View.VISIBLE) {
            startRadius = radius;
            endRadius = 0;
            ((ImageView) _View.findViewById(R.id.done_option_main)).setImageResource(R.drawable.done);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            SupportAnimator animator =
                    io.codetail.animation.ViewAnimationUtils.createCircularReveal(relativeLayout, x, y, startRadius, endRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(PEN_OPTIONS_REVEAL_DURATION);
            if (display) {
                relativeLayout.setVisibility(View.VISIBLE);
                relativeLayout.setAlpha(1);
            }
            animator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {
                    displayPenOptions(display, relativeLayout);
                    if (!display) {
                        relativeLayout.setVisibility(View.INVISIBLE);
                        relativeLayout.setAlpha(0);
                    }
                }

                @Override
                public void onAnimationCancel() {

                }

                @Override
                public void onAnimationRepeat() {

                }
            });
            animator.start();
        } else {
            Animator animator =
                    ViewAnimationUtils.createCircularReveal(relativeLayout, x, y, startRadius, endRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(PEN_OPTIONS_REVEAL_DURATION);
            if (display) {
                relativeLayout.setVisibility(View.VISIBLE);
                relativeLayout.setAlpha(1);
            }
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    displayPenOptions(display, relativeLayout);
                    if (!display) {
                        relativeLayout.setVisibility(View.INVISIBLE);
                        relativeLayout.setAlpha(0);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    private void displayPenOptions(boolean display, RelativeLayout frameLayout) {

        if (display) {
            final PenDemoView penDemoView = (PenDemoView) frameLayout.findViewById(R.id.pen_width_view);
            mPenOptionChangedListeners = new ArrayList<>();
            //PenTypes should be the first listener, otherwise color doesn't update correctly
            mPenOptionChangedListeners.add(PenTypes.getInstance(mContext));
            mPenOptionChangedListeners.add(penDemoView);
            mPenOptionChangedListeners.add(sDrawingBoard);

            //stroke width change
            mPenSizeSeekBar = (SeekBar) frameLayout.findViewById(R.id.pen_size_seek_bar);

            PenTypes.getInstance(mContext).
                    getPenType(PenTypes.getInstance(mContext).
                            getSharedPrefs().getInt(getString(R.string.last_selected_pen_key), 1));

            mSeekBarMax = PenTypes.getInstance(mContext).getCurrentPenMaxStrokeWidth();
            mSeekBarMin = PenTypes.getInstance(mContext).getCurrentPenMinStrokeWidth();
            mPenSizeSeekBar.setMax(mSeekBarMax);
            int progress = PenTypes.getInstance(mContext).getStrokeWidth();
            mPenSizeSeekBar.setProgress(progress);
            mPenSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    for (IPenOptionsChangedListener l : mPenOptionChangedListeners) {
                        l.onPenStrokeWidthChanged(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            //color change
            final PenColorGridView penColorGridView = (PenColorGridView) frameLayout.findViewById(R.id.pen_color_paletter_grid_view);
            penColorGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ColorPaletteAdapter colorPaletteAdapter = (ColorPaletteAdapter) penColorGridView.getAdapter();
                    for (IPenOptionsChangedListener l : mPenOptionChangedListeners) {
                        String color = colorPaletteAdapter.thumbTags[position];
                        mPenOptionBackground.setColorFilter(Color.parseColor(color));
                        Utils.log(TAG, "Color " + color);
                        l.onPenColorChanged(color.substring(1, color.length()));
                    }
                }
            });

        } else {
            Utils.log(TAG, "Close options ");
            frameLayout.setVisibility(View.GONE);
            Utils.log(TAG, "Last selected pen " + PenTypes.getInstance(mContext).getCurrentPenId());
            PenTypes.getInstance(mContext).
                    getSharedPrefs().
                    edit().
                    putInt(getString(R.string.last_selected_pen_key), PenTypes.getInstance(mContext).getCurrentPenId()).
                    apply();
        }
    }

    private void locationFilerClicked(){
        CheckPermissions.getInstance().checkPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSION_LOCATION,
                this);
    }

    private Paint createLocationTextPaint(Typeface typeface, boolean shadow, int size) {
        String[] colors = mContext.getResources().getStringArray(R.array.colorslist);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        double nextColor = Math.random() * (colors.length);
        paint.setColor(Color.parseColor(colors[(int)nextColor]));
        paint.setTextSize(size);
        if (typeface != null)
            paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        if (shadow)
            paint.setShadowLayer(4.0f, 2.0f, 2.0f, getResources().getColor(R.color.black_translucent));
        return paint;
    }


    private void addEmojis() {
        Toast.makeText(mContext, "Coming soon...", Toast.LENGTH_SHORT).show();
    }

    private void doneDrawingClicked() {
        if (mCurrentOptionsMode == OPTIONS_MODE_PEN) {
            setOptionsMode(OPTIONS_MODE_DRAWING);
        } else {
            setOptionsMode(OPTIONS_MODE_PREVIEW);
        }
    }

    private void shareProjectClicked() {

        final int SHARE_PROJ_DELAY = 300;

        sDrawingBoard.setDrawingCacheEnabled(true);
        final Bitmap screenContent = Bitmap.createBitmap(sDrawingBoard.getMeasuredWidth(), sDrawingBoard.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenContent);
        canvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
        sDrawingBoard.draw(canvas);
        sDrawingBoard.setDrawingCacheEnabled(false);

        if (mLocationFrameLayout != null) {
            Utils.log(TAG, "Adding location to share");
            mLocationFrameLayout.setDrawingCacheEnabled(true);
            mLocationFrameLayout.draw(canvas);
            mLocationFrameLayout.setDrawingCacheEnabled(false);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.saveTempFile(mContext, screenContent) == 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            Log.d(TAG, " sharing ... ");
                            sharingIntent.setType("image/jpeg");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out my Doodle I made with Doodle Fun!!");
                            sharingIntent.putExtra(Intent.EXTRA_STREAM, FileUtils.getFileProviderImage(mContext));
                            startActivityForResult(Intent.createChooser(sharingIntent, "Let's Share on..."), AppConst.SHARE_IMG_REQ);
                        }
                    }, SHARE_PROJ_DELAY);
                }
            }
        });
    }


    private void saveProjectClicked() {

        //if version >= M
        //if permission not granted, notify user that we need storage permission to save project
        if (Utils.getCurrentOSVersion() >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                CheckPermissions.getInstance().checkPermission(mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        PERMISSION_STORAGE,
                        this);
            } else {
                new CreateProjectDirTask(false).execute();
            }
        } else {
            new CreateProjectDirTask(false).execute();
        }
    }

    private void saveCurrentTempProject(final boolean privateFiles) {

        final int DB_UPDATE_DELAY = 200;
        final int SAVE_SERVICE_DELAY = 250;

        //create drawing bitmap
        sDrawingBoard.setDrawingCacheEnabled(true);
        Bitmap drawing = Bitmap.createBitmap(sDrawingBoard.getMeasuredWidth(), sDrawingBoard.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas drawingCanvas = new Canvas(drawing);
        sDrawingBoard.draw(drawingCanvas);


        if (mLocationFrameLayout != null) {
            mLocationFrameLayout.setDrawingCacheEnabled(true);
            mLocationFrameLayout.draw(drawingCanvas);
            mLocationFrameLayout.setDrawingCacheEnabled(false);
        }

        //create thumbnail bitmap
        Rect dest = new Rect(0, 0, (int) (sDrawingBoard.getMeasuredWidth()), (int) (sDrawingBoard.getMeasuredHeight()));
        Bitmap thumb = Bitmap.createBitmap(
                (int) (sDrawingBoard.getMeasuredWidth()), (int) (sDrawingBoard.getMeasuredHeight()), Bitmap.Config.ARGB_8888);
        Canvas thumbCanvas = new Canvas(thumb);
        thumbCanvas.drawColor(Color.WHITE);
        if (mBackgroundBitmap != null) {
            ImageView view = (ImageView) _View.findViewById(R.id.drawingboard_background_image_view);
            Bitmap b = ((BitmapDrawable) view.getDrawable()).getBitmap();
            int x = (sDrawingBoard.getMeasuredWidth() - b.getWidth()) >> 1;
            int y = (sDrawingBoard.getMeasuredHeight() - b.getHeight()) >> 1;
            thumbCanvas.drawBitmap(b, x, y, null);
        }

        thumbCanvas.drawBitmap(Bitmap.createBitmap(sDrawingBoard.getDrawingCache()), null, dest, null);

        final Bitmap thumbScaled =
                Bitmap.createScaledBitmap(thumb,
                        (int) (sDrawingBoard.getMeasuredWidth() * THUMB_SCALE), (int) (sDrawingBoard.getMeasuredHeight() * THUMB_SCALE), false);

        //first save thumb nail
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileUtils.saveFile(mContext, thumbScaled, mProjectName, "thumb", privateFiles);
            }
        }, 30);

        //save project files
        mLruCache.clearCache();
        mLruCache.addBitmapToMemoryCache(getString(R.string.lrucache_background_bitmap_key), mBackgroundBitmap);
        mLruCache.addBitmapToMemoryCache(getString(R.string.lrucache_drawing_bitmap_key), drawing);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                IntentFilter filter = new IntentFilter(SaveProjectService.BROADCAST_ACTION_STATE_CHANGE);
                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(saveProjectListener, filter);

                Intent intent = new Intent(mContext, SaveProjectService.class);
                Bundle bundle = new Bundle();
                bundle.putString(AppConst.PROJ_NAME_EXTRA, mProjectName);
                bundle.putBoolean(AppConst.PROJ_IS_PRIVATE_EXTRA, privateFiles);
                intent.putExtra(AppConst.PROJ_BUNDLE_EXTRA, bundle);
                getActivity().startService(intent);
                if (getActivity() != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().startService(new Intent(mContext, UpdaterService.class));
                        }
                    }, DB_UPDATE_DELAY);
                }
            }
        }, SAVE_SERVICE_DELAY);

        sDrawingBoard.setDrawingCacheEnabled(false);
    }


    /**
     * save project listener
     */
    BroadcastReceiver saveProjectListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra(SaveProjectService.EXTRA_SAVING, false)) {
                    Utils.log(TAG, "onReceive saving now");
                    getActivity().finish();
                } else if (!intent.getBooleanExtra(SaveProjectService.EXTRA_SAVING, false)) {
                    Utils.log(TAG, "onReceive saving done");

                    if (intent.getBooleanExtra(SaveProjectService.EXTRA_SAVE_SUCCESSFULL, false)) {
                        Utils.log(TAG, "onReceive saving successful!");
                        sDrawingBoard.setChangesMade(false);
                    } else {
                        Utils.log(TAG, "onReceive saving failed...");
                    }
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                }
            }
        }
    };

    public void setOptionsMode(int mode) {
        int[] options_btn_screen_loc = new int[2];
        mCurrentOptionsMode = mode;
        _View.findViewById(R.id.pen_option_main).getLocationOnScreen(options_btn_screen_loc);
        switch (mode) {
            case OPTIONS_MODE_DRAWING:
                _View.findViewById(R.id.options_bar_layout).setBackgroundColor(getResources().getColor(R.color.colorPrimary_translucent));
                _View.findViewById(R.id.preview_options).setVisibility(View.GONE);
                _View.findViewById(R.id.drawing_options_main).setVisibility(View.VISIBLE);
                zoomInDrawingBoard();
                displayPenOptions(false, options_btn_screen_loc[0], options_btn_screen_loc[1]);
                break;
            case OPTIONS_MODE_PEN:
                _View.findViewById(R.id.pen_option_main).getLocationOnScreen(options_btn_screen_loc);
                displayPenOptions(true, options_btn_screen_loc[0], options_btn_screen_loc[1]);
                break;
            case OPTIONS_MODE_PREVIEW:
                _View.findViewById(R.id.options_bar_layout).setBackgroundColor(Color.TRANSPARENT);
                _View.findViewById(R.id.preview_options).setVisibility(View.VISIBLE);
                _View.findViewById(R.id.drawing_options_main).setVisibility(View.GONE);
                displayPenOptions(false, options_btn_screen_loc[0], options_btn_screen_loc[1]);
                zoomOutDrawingBoard();
                break;
        }
    }

    public int getCurrentOptionsMode() {
        return mCurrentOptionsMode;
    }

    private void querySaveChanges() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(getString(R.string.query_save_changes_title));
        alertDialogBuilder.setMessage(getString(R.string.query_save_changes_msg));

        alertDialogBuilder.setNegativeButton(getString(R.string.query_save_changes_discard_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });

        alertDialogBuilder.setPositiveButton(getString(R.string.query_save_changes_save_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (!mIsProjectSavedInSDCard) {
                    new CreateProjectDirTask(true).execute();
                } else {
                    CheckPermissions.getInstance().checkPermission(mContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            PERMISSION_STORAGE,
                            DoodleFunDrawingFragment.this);
                }
            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * OPTIONS_MODE_DRAWING <-> OPTIONS_MODE_PREVIEW animations
     */
    private void zoomOutDrawingBoard() {

        final float ZOOM_OUT_SCALE = 0.8f;
        final float Y_TRANSLATION = getResources().getDimension(R.dimen.app_bar_height) / 2;

        _View.findViewById(R.id.drawing_board_frame_layout).
                animate().
                scaleX(ZOOM_OUT_SCALE).
                scaleY(ZOOM_OUT_SCALE).
                translationY(Y_TRANSLATION);

        if (mLocationFrameLayout!=null)mLocationFrameLayout.
                animate().
                scaleX(ZOOM_OUT_SCALE).
                scaleY(ZOOM_OUT_SCALE).
                translationY(Y_TRANSLATION);
    }

    private void zoomInDrawingBoard() {
        final float NORMAL_SCALE = 1.0f;

        _View.findViewById(R.id.drawing_board_frame_layout).
                animate().
                scaleX(NORMAL_SCALE).
                scaleY(NORMAL_SCALE).
                translationY(0);

        if (mLocationFrameLayout!=null) mLocationFrameLayout.
                animate().
                scaleX(NORMAL_SCALE).
                scaleY(NORMAL_SCALE).
                translationY(0);

    }

    public void hideOptionsBar(boolean hide) {
        final float REMOVE_LOC_FILTER_HIDE_X_TRANS = mRemoveLocationFilter.getWidth()+200;

        FrameLayout optionsBarFrameLayout = (FrameLayout) _View.findViewById(R.id.options_bar_layout);
        if (hide && !mIsOptionsBarHidden) {
            optionsBarFrameLayout.animate().translationY(-getResources().getDimension(R.dimen.app_bar_height));
            mRemoveLocationFilter.animate().translationX(REMOVE_LOC_FILTER_HIDE_X_TRANS);
            mIsOptionsBarHidden = true;
        } else {
            if (mIsOptionsBarHidden) {
                optionsBarFrameLayout.animate().translationY(0);
                mRemoveLocationFilter.animate().translationX(0);
                mIsOptionsBarHidden = false;
            }
        }
    }

    public void setScreenExpanded(boolean expanded) {
        mIsScreenExapanded = expanded;
        hideOptionsBar(expanded);
    }

    public boolean getScreenExpanded() {
        return mIsScreenExapanded;
    }

    /**
     * Permission sequence
     * 1 - ask for permission
     * 2 - if user clicks ALLOW, create external folders and save project
     * 3 - if user clicks DENY, pop up to show user reason for permission, increment request count
     * 4 - if user clicks Deny - tell user the project wasn't saved, keep project in internal memory
     * 5 - if user clicks OK - ask for permission again
     * 6 - if user clicks ALLOW, create external folders and save project
     * 7 - if user clicks DENY, pop up to show user reason for permission, increment request count
     * 8 - Repeat 3-7 until request count equals 2, then pop-up Settings Activity for user
     * 9 - if user allows permission, create external folders and save project
     * 10 - if user didn't allow permission, tell user the project wasn't saved, keep project in internal memory
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],@NonNull int[] grantResults) {
        Utils.log(TAG, "requestCode: " + requestCode + " grantResults.length " + grantResults.length);

        switch (requestCode) {

            case PERMISSION_STORAGE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Utils.log(TAG, "grantResults[" + i + "] " + grantResults[i] +
                                "permissions[" + i + "] " + permissions[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            initAfterPermission(permissions[i]);
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            boolean showRationale = shouldShowRequestPermissionRationale(permissions[i]);
                            if (showRationale) {
                                mSharedPrefEditor.putBoolean(getString(R.string.storage_permissions_requested_pref), true);
                                mSharedPrefEditor.apply();
                            } else {
                                Toast.makeText(mContext,
                                        getString(R.string.permission_storage_failure_toast), Toast.LENGTH_SHORT).show();
                                mSharedPrefEditor.putBoolean(getString(R.string.storage_permissions_requested_pref), false);
                                mSharedPrefEditor.putInt(getString(R.string.permissions_request_count_pref), 0);
                                mSharedPrefEditor.apply();
                            }
                        }
                    }

                }
                return;
            }
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Utils.log(TAG, "grantResults[" + i + "] " + grantResults[i] +
                                "permissions[" + i + "] " + permissions[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            initAfterPermission(permissions[i]);
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            boolean showRationale = shouldShowRequestPermissionRationale(permissions[i]);
                            if (showRationale) {
                                mSharedPrefEditor.putBoolean(getString(R.string.location_permissions_requested_pref), true);
                                mSharedPrefEditor.apply();
                            } else {
                                Toast.makeText(mContext,
                                        getString(R.string.permission_location_failure_toast), Toast.LENGTH_SHORT).show();
                                mSharedPrefEditor.putBoolean(getString(R.string.location_permissions_requested_pref), false);
                                mSharedPrefEditor.putInt(getString(R.string.permissions_request_count_pref), 0);
                                mSharedPrefEditor.apply();
                            }
                        }
                    }

                }
            }
        }
    }

    private void initAfterPermission(String permissionType) {
        switch (permissionType) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                Utils.log(TAG, "permission granted init storage ");
                mSharedPrefEditor.putBoolean(getString(R.string.storage_permissions_requested_pref), false);
                mSharedPrefEditor.apply();
                new CreateProjectDirTask(false).execute();
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                Utils.log(TAG, "permission granted init location ");
                mSharedPrefEditor.putBoolean(getString(R.string.location_permissions_requested_pref), false);
                mSharedPrefEditor.apply();
                _View.findViewById(R.id.location_filter_option_main).setEnabled(false);
                LocationService.getInstance().getLocation(mContext, this);
                break;
        }
    }

    @Override
    public void OnPermissionAllowed(String permissionType) {
        initAfterPermission(permissionType);
    }


    /**
     * Create project directories for this project
     */

    private class CreateProjectDirTask extends AsyncTask<Void, Void, Boolean> {

        private boolean mPrivateFiles;

        public CreateProjectDirTask(boolean privateFiles) {
            mPrivateFiles = privateFiles;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mPrivateFiles ? FileUtils.createInternalProjectDirs(mContext, mProjectName) : FileUtils.createExternalDirs(mContext);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Utils.log(TAG, "Project created successfully");
                saveCurrentTempProject(mPrivateFiles);
            } else {
                Utils.log(TAG, "Project creation unsuccessful");
            }
        }
    }
}
