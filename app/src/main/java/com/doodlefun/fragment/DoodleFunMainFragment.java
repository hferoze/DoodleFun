package com.doodlefun.fragment;

import android.animation.Animator;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.doodlefun.DoodleFunAppWidget;
import com.doodlefun.R;
import com.doodlefun.adapter.UserProjectsAdapter;
import com.doodlefun.data.LruCacheHelper;
import com.doodlefun.data.db.DoodleFunContract;
import com.doodlefun.data.db.DoodleFunLoader;
import com.doodlefun.data.db.UpdaterService;
import com.doodlefun.utils.AppConst;
import com.doodlefun.utils.FileUtils;
import com.doodlefun.utils.PicassoHelper;
import com.doodlefun.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.codetail.animation.SupportAnimator;

public class DoodleFunMainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        UserProjectsAdapter.OnProjectItemClickedListener {

    private static final String TAG = getInstance().getClass().getSimpleName();

    private static final int FAB_ANIM_DURATION = 250;

    private static final int REQUEST_IMAGE_SELECT = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int PROJECTVIEW_LOADER = 0;

    //FAB options
    private FloatingActionButton mMainOptionsFab;
    private FloatingActionButton mNewProjectFab;
    private FloatingActionButton mOpenCameraFab;
    private FloatingActionButton mOpenGalleryFab;

    private boolean mMainOptionsOpen = false;

    //RecyclerView
    private RecyclerView mRecyclerView;
    public UserProjectsAdapter mUserProjectsAdapter;
    private GridLayoutManager mGridLayoutManager;
    private Cursor mCursor = null;

    private static DoodleFunMainFragment mDoodleFunMainFragment = null;

    //project options
    private boolean isProjectDisplayVisible;
    private int mCurrentProjectX, mCurrentProjectY;

    //capture image rotation options
    private boolean isCapturedImageOptionVisible;
    private  Bitmap scaledImage;

    private View mView;

    private Context mContext;

    private LruCacheHelper mLruCache;

    public interface OnNewProjectButtonClickedListener {
        void onNewProjectButtonClicked();
    }

    public interface OnOldProjectButtonClickedListener {
        void onOldProjectButtonClicked(String projectFolder);
    }

    public static DoodleFunMainFragment getInstance() {
        if (mDoodleFunMainFragment == null) {
            mDoodleFunMainFragment = new DoodleFunMainFragment();
        }
        return mDoodleFunMainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mLruCache = LruCacheHelper.getInstance();

        isProjectDisplayVisible = false;
        isCapturedImageOptionVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCursor != null && mCursor.isClosed()) {
            getLoaderManager().restartLoader(PROJECTVIEW_LOADER, null, this);
        }
        refresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.doodle_fun_main_fragment, container, false);
        mView = rootView;
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeFABOptions(view);
        initRecyclerView(view);
    }

    /**
     * on back key press:
     * if fab openned then reverse back to normal state, else exit
     */

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getLoaderManager().getLoader(PROJECTVIEW_LOADER) == null) {
            getLoaderManager().initLoader(PROJECTVIEW_LOADER, null, this);
        }
        if (getActivity() != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().startService(new Intent(mContext, UpdaterService.class));
                }
            }, 200);
        }

        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mMainOptionsOpen) {
                        //close fab
                        mMainOptionsOpen = false;
                        expandOptions(false);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && isProjectDisplayVisible) {
                        displayProjectOptions(null, false, mCurrentProjectX, mCurrentProjectY);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && isCapturedImageOptionVisible) {
                        displayCapturePhotoOptions(false, null, 0, 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1) {
            //save captured image to cache to be set as drawingboard's background
            try {

                Uri file = FileUtils.getFileProviderImage(mContext);
                final Bitmap sampled = Utils.decodeSampledBitmapFromFile(mContext, file);

                if (sampled != null) {
                    //Let user rotate the image if it is not captured in correct orientation
                    displayCapturePhotoOptions(true, sampled, mView.getHeight(), mView.getWidth());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == -1) {
            //save selected image to cache to be set as drawingboard's background
            try {
                Bitmap sampled = Utils.decodeSampledBitmapFromFile(mContext, data.getData());
                if (sampled != null) {
                    mLruCache.addBitmapToMemoryCache(getString(R.string.lrucache_background_bitmap_key),
                            getScaledBitmap(sampled, mView.getHeight(), mView.getWidth()));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            OnNewProjectButtonClickedListener newProject = (OnNewProjectButtonClickedListener) getActivity();
            newProject.onNewProjectButtonClicked();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return DoodleFunLoader.newAllSnapsInstance(mContext);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.isClosed()) {

            mRecyclerView.setAdapter(mUserProjectsAdapter);
            mUserProjectsAdapter.setCursor(mCursor);
            mUserProjectsAdapter.notifyDataSetChanged();
        } else {
            getLoaderManager().restartLoader(PROJECTVIEW_LOADER, null, this);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void OnProjectItemClicked(String projectFolder, int x, int y) {

        mCurrentProjectX = x;
        mCurrentProjectY = y;

        displayProjectOptions(projectFolder, true, x, y);
    }

    private void displayCapturePhotoOptions(boolean show, Bitmap capturedImage, final float imageHeight, final float imageWidth) {
        final RelativeLayout relativeLayout = (RelativeLayout) mView.findViewById(R.id.capture_image_relative_layout);
        isCapturedImageOptionVisible = show;
        relativeLayout.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        relativeLayout.setAlpha(show?1:0);

        if (show) {
            scaledImage = Bitmap.createScaledBitmap(capturedImage, (int)imageWidth, (int)imageHeight, false);

            ((ImageView) relativeLayout.findViewById(R.id.captured_image_view)).setImageBitmap(scaledImage);
            ((ImageView) relativeLayout.findViewById(R.id.captured_image_rotate_left)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scaledImage = Utils.rotateImage(scaledImage, -90);
                    ((ImageView) relativeLayout.findViewById(R.id.captured_image_view)).setImageBitmap(scaledImage);
                }
            });
            ((ImageView) relativeLayout.findViewById(R.id.captured_image_rotate_right)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scaledImage = Utils.rotateImage(scaledImage, 90);
                    ((ImageView) relativeLayout.findViewById(R.id.captured_image_view)).setImageBitmap(scaledImage);
                }
            });

            ((ImageView) relativeLayout.findViewById(R.id.captured_image_rotate_done)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            relativeLayout.setVisibility(View.INVISIBLE);
                            relativeLayout.setAlpha(0);
                            isCapturedImageOptionVisible = false;
                        }
                    },500);

                    //pass the bitmap in cache
                    mLruCache.addBitmapToMemoryCache(getString(R.string.lrucache_background_bitmap_key), scaledImage);

                    OnNewProjectButtonClickedListener newProject = (OnNewProjectButtonClickedListener) getActivity();
                    newProject.onNewProjectButtonClicked();
                }
            });
        }
    }

    private Bitmap getScaledBitmap(Bitmap bitmap, float reqHeight, float reqWidth){
        Bitmap background = Bitmap.createBitmap((int)reqWidth, (int)reqHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);

        Matrix m = new Matrix();

        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float aspect_ratio = 1;
        float scale;

        if (height>width){
            aspect_ratio = height/width;
            scale = (reqHeight/height) * aspect_ratio;
        }else if(height<width){
            aspect_ratio = width/height;
            scale = (reqWidth/width) * aspect_ratio;
        }else{
            scale = (reqWidth/width) * aspect_ratio;
        }

        float xTranslation = (reqWidth - width * scale)/2.0f;
        float yTranslation = (reqHeight - height * scale)/2.0f;

        m.postTranslate(xTranslation, yTranslation);
        m.preScale(scale, scale);

        canvas.drawBitmap(bitmap, m, null);

        return background;
    }

    private void initializeFABOptions(final View view){
        if (view!=null) {
            mMainOptionsFab = (FloatingActionButton) view.findViewById(R.id.main_options_fab);
            mMainOptionsFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mMainOptionsOpen) {
                        mMainOptionsOpen = true;
                        expandOptions(true);
                    } else {
                        mMainOptionsOpen = false;
                        expandOptions(false);
                    }
                }
            });

            mNewProjectFab = (FloatingActionButton) view.findViewById(R.id.new_project_fab);
            mNewProjectFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getView() != null) {
                        mMainOptionsOpen = false;
                        expandOptions(false);
                    }

                    mRecyclerView.setClickable(false);

                    mLruCache.clearCache();
                    Bitmap background = Bitmap.createBitmap((int) Utils.getScreenWidth(mContext),
                            (int) (Utils.getScreenHeight(mContext)), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(background);
                    canvas.drawColor(Color.WHITE);
                    mLruCache.addBitmapToMemoryCache(getString(R.string.lrucache_background_bitmap_key), background);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OnNewProjectButtonClickedListener newProject = (OnNewProjectButtonClickedListener) getActivity();
                            newProject.onNewProjectButtonClicked();
                        }
                    }, FAB_ANIM_DURATION);
                }
            });

            mOpenCameraFab = (FloatingActionButton) view.findViewById(R.id.open_camera_fab);
            mOpenCameraFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getView() != null) {
                        mMainOptionsOpen = false;
                        expandOptions( false);
                    }
                    mLruCache.clearCache();
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtils.getFileProviderImage(mContext));
                        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });

            mOpenGalleryFab = (FloatingActionButton) view.findViewById(R.id.open_gallery_fab);
            mOpenGalleryFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getView() != null) {
                        mMainOptionsOpen = false;
                        expandOptions(false);
                    }
                    mLruCache.clearCache();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
                    }
                }
            });
        }
    }

    private void initRecyclerView(View view){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.projects_recycler_view);
        mGridLayoutManager = new GridLayoutManager(mContext, getResources().getInteger(R.integer.num_of_grid_columns));
        mGridLayoutManager.setReverseLayout(ViewCompat.getLayoutDirection(getView()) == ViewCompat.LAYOUT_DIRECTION_RTL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mUserProjectsAdapter = new UserProjectsAdapter(mContext, this);
        mUserProjectsAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mUserProjectsAdapter);

        if (mCursor != null && !mCursor.isClosed()) {
            mUserProjectsAdapter.setCursor(mCursor);
        }

    }

    /**
     * Refresh db and consequently the Recyclerview
     */
    public void refresh(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(getActivity()!=null) {
                    getActivity().startService(new Intent(mContext, UpdaterService.class));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PicassoHelper.getInstance(mContext).clearCache();
                            updateWidget();
                        }
                    },500);
                }
            }
        });
    }

    /**
     * Updates widget
     */

    private void updateWidget() {

        final String BITMAP_BYTE_ARRAY = "byteArray";
        if (mCursor != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent(getActivity(), DoodleFunAppWidget.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = {R.xml.doodle_fun_app_widget_info};
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    Cursor cursor = mContext.getContentResolver().query(DoodleFunContract.BASE_URI, null, null,
                            null, DoodleFunContract.Items.DEFAULT_SORT);
                    if (cursor!=null && cursor.getCount()>0 && !cursor.isClosed()){
                        try {
                            DoodleFunContract.Items.buildItemUri(getItemId(cursor, 0));
                            String prjName = cursor.getString(DoodleFunLoader.Query.PROJECT_NAME);

                            String currentPrj = prjName + "/" + AppConst.THUMB_FILE_NAME;

                            Bitmap b = BitmapFactory.decodeFile(currentPrj);
                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.JPEG, 50, bs);
                            intent.putExtra(BITMAP_BYTE_ARRAY, bs.toByteArray());
                        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                   }
                   getActivity().sendBroadcast(intent);
                }
            });
        }
    }

    public long getItemId(Cursor cursor, int position) {
        if (cursor!=null && !cursor.isClosed() && cursor.getCount()>0) {
            cursor.moveToPosition(position);
            return cursor.getLong(DoodleFunLoader.Query._ID);
        }
        return 0;
    }

    /**
     * Displays clicked project with circular reveal animation
     * @param projectFolder current project folder
     * @param display true or false. true will reveal the project, false will make it disappear
     * @param x x coordinate of circular reveal
     * @param y y coordinate of the circular reveal
     */
    private void displayProjectOptions(final String projectFolder, final boolean display, int x, int y){

        final RelativeLayout relativeLayout = (RelativeLayout)mView.findViewById(R.id.project_main_view);
        int radius = relativeLayout.getWidth();
        int startRadius=0, endRadius = radius;
        if(display && relativeLayout.getVisibility()==View.INVISIBLE){
            startRadius=0;
            endRadius=radius;
            isProjectDisplayVisible = true;
            File backgroundFile = new File(projectFolder + "/" + AppConst.BACKGROUND_FILE_NAME);
            final Bitmap background = BitmapFactory.decodeFile(backgroundFile.getAbsolutePath());
            ((ImageView)relativeLayout.findViewById(R.id.project_background_image_view)).setImageBitmap(background);
            File drawingFile = new File(projectFolder + "/" + AppConst.DRAWING_FILE_NAME);
            final Bitmap drawing = BitmapFactory.decodeFile(drawingFile.getAbsolutePath());
            ((ImageView)relativeLayout.findViewById(R.id.project_drawing_image_view)).setImageBitmap(drawing);

            ((ImageView)relativeLayout.findViewById(R.id.project_view_share)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareProjectClicked(background, drawing);
                }
            });
            ((ImageView)relativeLayout.findViewById(R.id.project_view_edit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnOldProjectButtonClickedListener listener = (OnOldProjectButtonClickedListener)getActivity();
                    listener.onOldProjectButtonClicked(projectFolder);
                    isProjectDisplayVisible = false;
                    ((ImageView)relativeLayout.findViewById(R.id.project_background_image_view)).setImageBitmap(null);
                    ((ImageView)relativeLayout.findViewById(R.id.project_drawing_image_view)).setImageBitmap(null);
                    relativeLayout.setVisibility( View.INVISIBLE);
                    relativeLayout.setAlpha(0);
                }
            });
            ((ImageView)relativeLayout.findViewById(R.id.project_view_delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDeleteDialog(projectFolder);
                }
            });

        }else if (!display && relativeLayout.getVisibility() == View.VISIBLE){
            startRadius=radius;
            endRadius=0;
            isProjectDisplayVisible = false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            SupportAnimator animator =
                    io.codetail.animation.ViewAnimationUtils.createCircularReveal(relativeLayout, x, y, startRadius, endRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(AppConst.PEN_OPTIONS_REVEAL_DURATION);
            if (display){
                relativeLayout.setVisibility(View.VISIBLE);
                relativeLayout.setAlpha(1);
            }

            animator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {
                    //displayProjectViewOptions(display, relativeLayout);
                    if(!display){
                        ((ImageView)relativeLayout.findViewById(R.id.project_background_image_view)).setImageBitmap(null);
                        ((ImageView)relativeLayout.findViewById(R.id.project_drawing_image_view)).setImageBitmap(null);
                        relativeLayout.setVisibility( View.INVISIBLE);
                        relativeLayout.setAlpha(0);
                        relativeLayout.destroyDrawingCache();
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
        }else{
            Animator animator =
                    ViewAnimationUtils.createCircularReveal(relativeLayout, x, y, startRadius, endRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(AppConst.PEN_OPTIONS_REVEAL_DURATION);
            if (display){
                relativeLayout.setVisibility(View.VISIBLE);
                relativeLayout.setAlpha(1);
            }

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(!display){
                        ((ImageView)relativeLayout.findViewById(R.id.project_background_image_view)).setImageBitmap(null);
                        ((ImageView)relativeLayout.findViewById(R.id.project_drawing_image_view)).setImageBitmap(null);
                        relativeLayout.setVisibility( View.INVISIBLE);
                        relativeLayout.setAlpha(0);
                        relativeLayout.destroyDrawingCache();
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

    /**
     * Asks user if they really want to delete this project
     * @param projectFolder current project folder
     */
    private void createDeleteDialog(final String projectFolder){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage(getString(R.string.query_delete_doodle_title));
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                getString(R.string.query_delete_doodle_yes_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        remove(projectFolder);
                    }
                });

        builder1.setNegativeButton(
                getString(R.string.query_delete_doodle_no_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Removes project under projectFolder if user chooses to
     * @param projectFolder current project folder
     */
    private void remove(final String projectFolder) {

        final int REFRESH_DELAY = 100; //ms
        final int DISAPPEAR_DELAY = 200; //ms

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteProjectFolder(projectFolder);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, REFRESH_DELAY);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayProjectOptions(null, false, mCurrentProjectX, mCurrentProjectY);
            }
        }, DISAPPEAR_DELAY);
    }

    /**
     * Share current project
     * @param background Background Bitmap
     * @param drawing Drawing Bitmap
     */
    private void shareProjectClicked(Bitmap background, Bitmap drawing){

        final int SHARE_DELAY = 300; //ms

        final Bitmap screenContent = Bitmap.createBitmap(mView.getMeasuredWidth(), mView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenContent);
        canvas.drawBitmap(background,0,0,null);
        canvas.drawBitmap(drawing,0,0,null);


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.saveTempFile(mContext, screenContent)==1){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            Utils.log(TAG, " sharing ... ");
                            sharingIntent.setType("image/jpeg");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                            sharingIntent.putExtra(Intent.EXTRA_STREAM,  FileUtils.getFileProviderImage(mContext));
                            startActivityForResult(Intent.createChooser(sharingIntent, getString(R.string.share_title)), AppConst.SHARE_IMG_REQ);
                        }
                    }, SHARE_DELAY);
                }
            }
        });
    }

    /**
     * Expands FAB options
     * @param expand true or false
     */
    private void expandOptions(boolean expand){
        int dir = ViewCompat.getLayoutDirection(getView()) == ViewCompat.LAYOUT_DIRECTION_RTL?1:-1;
        if (expand){
            mMainOptionsFab.animate().rotation(315).setDuration(FAB_ANIM_DURATION);
            mNewProjectFab.animate().
                    translationX(dir*getResources().getDimension(R.dimen.fab_translation)).setDuration(FAB_ANIM_DURATION);
            mOpenCameraFab.animate().
                    translationY(-1*getResources().getDimension(R.dimen.fab_translation)).setDuration(FAB_ANIM_DURATION);
            mOpenGalleryFab.animate().
                    translationX(dir*getResources().getDimension(R.dimen.fab_translation)).setDuration(FAB_ANIM_DURATION).
                    translationY(-1*getResources().getDimension(R.dimen.fab_translation)).setDuration(FAB_ANIM_DURATION);

        }else{
            mMainOptionsFab.animate().rotation(0).setDuration(FAB_ANIM_DURATION);
            mNewProjectFab.animate().translationX(0).setDuration(FAB_ANIM_DURATION);
            mOpenCameraFab.animate().translationY(0).setDuration(FAB_ANIM_DURATION);
            mOpenGalleryFab.animate().translationX(0).translationY(0).setDuration(FAB_ANIM_DURATION);
        }
    }
}
