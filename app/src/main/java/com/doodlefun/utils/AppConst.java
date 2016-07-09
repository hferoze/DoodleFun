package com.doodlefun.utils;

import android.os.Environment;

import java.io.File;

public class AppConst {

    public static final boolean DEBUG = true;

    //File
    public static final File EXTERNAL_STORAGE = Environment.getExternalStorageDirectory();
    public static final String FOLDER_NAME = "/DoodleFun/";
    public static final String TEMP_FOLDER_NAME = "/temp/";
    public static final String PROJECT_FOLDER_NAME_CONST = "Projects/";
    public static String FILE_URI_EXT="file:///";

    public static final String FILE_NAME_CONST="DoodleFun_";
    public static final String STD_FILE_TYPE = ".png";
    public static final String FILE_TYPE_JPEG = ".jpg";
    public static final String THUMB_FILE_NAME = FILE_NAME_CONST+"thumb"+FILE_TYPE_JPEG;
    public static final String DRAWING_FILE_NAME = FILE_NAME_CONST+"drawing"+STD_FILE_TYPE;
    public static final String BACKGROUND_FILE_NAME = FILE_NAME_CONST+"background"+FILE_TYPE_JPEG;

    //TAG
    public static final String MAIN_TAG = "DoodleFun";
    public static final String UTILS_TAG = "Utils";
    public static final String FILEUTILS_TAG = "FileUtils";

    public static final String OLD_PROJECT_FOLDER_KEY = "old_project_folder_key";
    public static final String IS_SAVED_ON_SDCARD_KEY = "is_saved_on_sdcard_key";
    public static final String IS_NEW_PROJECT = "is_new_project_key";


    public static final String DEFAULT_ORIENTATION = "PORTRAIT";
    public static final String LANDSCAPE_ORIENTATION = "LANDSCAPE";

    //save service
    public static final String PROJ_NAME_EXTRA = "prj_name";
    public static final String PROJ_IS_PRIVATE_EXTRA = "prj_is_private_extra";
    public static final String PROJ_BUNDLE_EXTRA = "prj_data";

    //File provider
    public static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.doodlefun.fileprovider";
    public static final String FILE_PROVIDER_TEMP_IMG = "temp.jpeg";

    //circular reveal
    public static final int PEN_OPTIONS_REVEAL_DURATION = 350;



    //Bundle
    public static final String DRAWING_BUNDLE_KEY = "drawing_bundle";

    //share
    public static final int SHARE_IMG_REQ = 0;

    //adview
    public static final String ADVIEW_TEST_DEVICE_CODE = "E01C7FF09832E03B2A72363D23F13F39";
}
