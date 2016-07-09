package com.doodlefun;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.RemoteViews;

public class DoodleFunAppWidget extends AppWidgetProvider{

    private static final String IC_CLICKED = "com.doodlefun.LaunchApp_";

    private static final String BITMAP_BYTE_ARRAY = "byteArray";
    private Bitmap mCurrentBitmap;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        new UpdateFrontImage(context, appWidgetManager).execute();

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * This method just waits for 100 ms for Bitmap update. Initially it was written to update
     * Bitmap from Uri, but it turned out that widgets can't access internal memory, unless
     * alot of changes are made to the content provider. Now, we simple receive a byte array in
     * onReceive and decode Bitmap from
     * @return always returns true
     */
    private boolean updateCurrentImage(){
        int i = 0;
        while(i<10){
            try {
                Thread.sleep(10);
            }catch (Exception e){
                e.printStackTrace();
            }
            i++;
        }
        return true;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action.startsWith(IC_CLICKED)) {
            Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            launch.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            context.startActivity(launch);
        } else if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            if (intent.hasExtra(BITMAP_BYTE_ARRAY)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentBitmap = BitmapFactory.decodeByteArray(
                                intent.getByteArrayExtra(BITMAP_BYTE_ARRAY),0,intent.getByteArrayExtra(BITMAP_BYTE_ARRAY).length);
                    }
                });
            }else{
                mCurrentBitmap = null;
            }
        }
    }

    //must be defined outside of async class
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    //must be defined outside of async class
    public void updateWidget(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, DoodleFunAppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }

    private class UpdateFrontImage extends AsyncTask<Void, Void, Boolean>{

        private Context mContext;
        private AppWidgetManager mAppWidgetManager;

        public UpdateFrontImage(Context context, AppWidgetManager appWidgetManager){
            mContext = context;
            mAppWidgetManager = appWidgetManager;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return updateCurrentImage();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            ComponentName thisWidget = new ComponentName(mContext,
                    DoodleFunAppWidget.class);
            int[] allWidgetIds = mAppWidgetManager.getAppWidgetIds(thisWidget);

            final int N = allWidgetIds.length;

            for (int i = 0; i < N; ++i) {
                RemoteViews remoteViews = updateWidgetListView(mContext,
                        allWidgetIds[i]);
                updateWidget(mContext, remoteViews);
            }
        }

        private RemoteViews updateWidgetListView(Context context,
                                                 int appWidgetId) {

            RemoteViews remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.doodle_fun_app_widget);


            remoteViews.setImageViewBitmap(R.id.image_widget, null);
            if (mCurrentBitmap != null) {
                remoteViews.setImageViewBitmap(R.id.image_widget, mCurrentBitmap);
                remoteViews.setTextViewText(R.id.no_doodle_available_text_view , "");
            }else{
                remoteViews.setImageViewBitmap(R.id.image_widget, null);
                remoteViews.setTextViewText(R.id.no_doodle_available_text_view , context.getString(R.string.widget_text_no_doodle_yet));
            }

            Intent svcIntent = new Intent(context, DoodleFunAppWidget.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            svcIntent.setData(Uri.parse(
                    svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            remoteViews.setOnClickPendingIntent(R.id.image_widget, getPendingSelfIntent(mContext, IC_CLICKED + appWidgetId));

            return remoteViews;
        }
    }
}
