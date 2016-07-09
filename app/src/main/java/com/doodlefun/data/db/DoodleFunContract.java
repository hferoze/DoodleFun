package com.doodlefun.data.db;

import android.net.Uri;

public class DoodleFunContract {

    public static final String CONTENT_AUTHORITY = "com.doodlefun";
    public static final Uri BASE_URI = Uri.parse("content://com.doodlefun");


    interface ItemsColumns {
        /** Type: INTEGER PRIMARY KEY AUTOINCREMENT */
        String _ID = "_id";
        /** Type: TEXT NOT NULL */
        String PROJECT_NAME = "project_name_id";
        /** Type: INTEGER NOT NULL DEFAULT 0 */
        String DATE_CREATED = "date_created_id";
        /** Type: INTEGER NOT NULL DEFAULT 0 */
        String DATE_MODIFIED = "date_modified_id";
        /** Type: INTEGER NOT NULL DEFAULT 0 */
        String HAS_BACKGROUND_IMAGE = "has_background_image_id";
    }

    public static class Items implements ItemsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.doodlefun.items";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.doodlefun.items";

        public static final String DEFAULT_SORT = DATE_MODIFIED + " DESC";

        /** Matches: /items/ */
        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("items").build();
        }

        /** Matches: /items/[_id]/ */
        public static Uri buildItemUri(long _id) {
            return BASE_URI.buildUpon().appendPath("items").appendPath(Long.toString(_id)).build();
        }


        /** Read item ID item detail URI. */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
    }

    private DoodleFunContract() {
    }
}
