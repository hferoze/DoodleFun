package com.doodlefun.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.doodlefun.data.db.DoodleFunProvider.Tables;

public class DoodleFunDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "doodlefun.db";
    private static final int DATABASE_VERSION = 1;

    public DoodleFunDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + DoodleFunContract.ItemsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DoodleFunContract.ItemsColumns.PROJECT_NAME + " TEXT NOT NULL,"
                + DoodleFunContract.ItemsColumns.DATE_CREATED + " INTEGER NOT NULL DEFAULT 0,"
                + DoodleFunContract.ItemsColumns.DATE_MODIFIED + " INTEGER NOT NULL DEFAULT 0,"
                + DoodleFunContract.ItemsColumns.HAS_BACKGROUND_IMAGE + " INTEGER NOT NULL DEFAULT 0" //Future use
                + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
        onCreate(db);
    }
}
