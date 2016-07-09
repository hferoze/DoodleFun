package com.doodlefun.data.db;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

public class DoodleFunLoader extends CursorLoader {
    public static DoodleFunLoader newAllSnapsInstance(Context context) {
        return new DoodleFunLoader(context, DoodleFunContract.Items.buildDirUri());
    }

    public static DoodleFunLoader newInstanceForItemId(Context context, long itemId) {
        return new DoodleFunLoader(context, DoodleFunContract.Items.buildItemUri(itemId));
    }

    private DoodleFunLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, DoodleFunContract.Items.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                DoodleFunContract.Items._ID,
                DoodleFunContract.Items.PROJECT_NAME,
                DoodleFunContract.Items.DATE_CREATED,
                DoodleFunContract.Items.DATE_MODIFIED,
                DoodleFunContract.Items.HAS_BACKGROUND_IMAGE, //Future use
        };



        int _ID = 0;
        int PROJECT_NAME = 1;
        int DATE_CREATED = 2;
        int DATE_MODIFIED = 3;
        int HAS_BACKGROUND_IMAGE = 4;
    }
}
