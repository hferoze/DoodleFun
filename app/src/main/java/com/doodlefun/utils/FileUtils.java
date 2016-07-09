package com.doodlefun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FileUtils {


    /*
    * Create folder structure
    */

    /**
     * create external Dirs under /sdcard/ if permission granted
     * @param context Context
     * @return true of false
     */
    public static boolean createExternalDirs(Context context){
        boolean success = true;

        ArrayList<File> folders = new ArrayList<>();
        folders.add(new File(getExternalFolderName(context))); /* /sdcard/DoodleFun*/
        folders.add(new File(getExternalProjectFolder(context))); /* /sdcard/DoodleFun/Projects */

        for (File folder: folders){
            Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
            if (!folder.exists()) {
                Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
                success = folder.mkdir();
            } else {
                Utils.log(AppConst.FILEUTILS_TAG, "Folder exists: " + folder);
            }
        }

        return success;
    }

    public static boolean createInternalProjectDirs(Context context, String projectName){
        boolean success = true;

        ArrayList<File> folders = new ArrayList<>();
        folders.add(new File(getInternalTempFolder(context))); /* /data/com.doodlefun/temp */
        folders.add(new File(getInternalProjectFolder(context))); /* /sdcard/DoodleFun/Projects */
        folders.add(new File(getInternalProjectFolder(context) + projectName)); /* /sdcard/DoodleFun/Projects/projectName */

        for (File folder : folders){
            if (!folder.exists()) {
                Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
                success = folder.mkdir();
            } else {
                Utils.log(AppConst.FILEUTILS_TAG, "Folder exists: " + folder);
            }
        }
        return success;

    }

    public static boolean createPrivateDirs(Context context) {
        boolean success = true;
        //create internal storage folder for temp files
        File folder = new File(getInternalTempFolder(context));

        if (!folder.exists()) {
            Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
            success = folder.mkdir();
        } else {
            Utils.log(AppConst.FILEUTILS_TAG, "Folder exists: " + folder);
        }

        return success;
    }

    /*
    Save File
     */

    /**
     * Save bitmap image with tag in the folder with name projectName.
     * Saves in internal memory if private else saves on sdcard
     * @param context
     * @param image Bitmap image to be saved
     * @param projectName name of the folder where the image is saved
     * @param tag filename tag of this image
     * @param privateFiles whether this should be stored in internal memory or external
     * @return 1 if successful else -1
     */

    public static int saveFile(Context context, Bitmap image, String projectName, String tag, boolean privateFiles)
    {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        String fileType = AppConst.STD_FILE_TYPE;
        int quality = 50;

        boolean success = true;
        File folder = null;
        if(privateFiles) folder = new File(getInternalProjectFolder(context)+projectName);
        else folder = new File(getExternalProjectFolder(context)+projectName);

        if(tag.equals("background") || tag.equals("thumb")){
            format =Bitmap.CompressFormat.JPEG;
            fileType = AppConst.FILE_TYPE_JPEG;
            quality = tag.equals("background")?50:25;
        }

        Utils.log(AppConst.FILEUTILS_TAG, "filetype: " + fileType + " quality " + quality);

        String fileName = AppConst.FILE_NAME_CONST + tag + fileType;
        File file = null;
        if (!folder.exists()) {
            Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
            success = folder.mkdir();
        } else {
            Utils.log(AppConst.FILEUTILS_TAG, "Folder exists: " + folder);
        }
        if (success) {
            file = new File(folder + "/" + fileName);
            Utils.log(AppConst.FILEUTILS_TAG, "saving image... " + file);
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                image.compress(format, quality, ostream);
                ostream.close();
            } catch (Exception e) {
                Log.e(AppConst.FILEUTILS_TAG, "Error failed to save image... " + e.toString());
                return -1;
            }
        } else {
            Log.e(AppConst.FILEUTILS_TAG, "Couldn't create directory to save images... ");
            return -1;
        }

        return 1;
    }

    /**
     * Saves image in temporary internal folder. Use while sharing current image without saving.
     * @param context
     * @param image Bitmap to be saved
     * @return 1 if successful, else -1
     */
    public static int saveTempFile(Context context, Bitmap image){
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 75;

        boolean success = true;
        File folder = new File(FileUtils.getInternalTempFolder(context));

        File file = null;
        if (!folder.exists()) {
            Utils.log(AppConst.FILEUTILS_TAG, "creating: " + folder);
            success = folder.mkdir();
        } else {
            Utils.log(AppConst.FILEUTILS_TAG, "Folder exists: " + folder);
        }
        if (success) {
            file = new File(folder, "temp.jpeg");
            Utils.log(AppConst.FILEUTILS_TAG, "saving image... " + file);
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                image.compress(format, quality, ostream);
                ostream.close();
            } catch (Exception e) {
                Log.e(AppConst.FILEUTILS_TAG, "Error failed to save image... " + e.toString());
                return -1;
            }
        } else {
            Log.e(AppConst.FILEUTILS_TAG, "Couldn't create directory to save images... ");
            return -1;
        }

        return 1;

    }

    /**
     * Gives the uri of the file provider image location
     * @param context
     * @return uri of the file provider image location
     */

    public static Uri getFileProviderImage(Context context){
        File dir = new File(FileUtils.getInternalTempFolder(context));
        File output=new File(dir, AppConst.FILE_PROVIDER_TEMP_IMG);
        return FileProvider.getUriForFile(context, AppConst.CAPTURE_IMAGE_FILE_PROVIDER, output);
    }

    /**
     * Deletes project folder from internal memory
     * @param context
     * @param dir folder to be deleted
     * @return true if successful, else false
     */

    public static boolean deleteInternalProjectFolder(Context context, String dir){
        boolean success = true;

        File folder = new File(getInternalProjectFolder(context)+dir);
        Utils.log(AppConst.FILEUTILS_TAG,"deleting folder: " + folder.toString());
        String[] children = folder.list();
        if (children!=null) {
            for (int i = 0; i < children.length; i++) {
                File f = new File(folder, children[i]);

                success = f.delete();
                Utils.log(AppConst.FILEUTILS_TAG, "deleting internal copy: " + f.toString());
            }
            success = folder.delete();
        }
        return success;
    }

    /**
     * Deletes dir
     * @param dir folder to be deleted
     * @return true if successful, else false
     */
    public static boolean deleteProjectFolder(String dir){
        boolean success = true;

        File folder = new File(dir);
        Utils.log(AppConst.FILEUTILS_TAG,"deleting folder: " + folder.toString());
        String[] children = folder.list();
        if (children!=null) {
            for (int i = 0; i < children.length; i++) {
                File f = new File(folder, children[i]);

                success = f.delete();
                Utils.log(AppConst.FILEUTILS_TAG, "deleting project: " + f.toString());
            }
            success = folder.delete();
        }
        return success;
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getInternalTempFolder(Context context){
        return context.getFilesDir()+AppConst.TEMP_FOLDER_NAME;
    }

    public static String getInternalProjectFolder(Context context){
        return context.getFilesDir()+AppConst.TEMP_FOLDER_NAME+AppConst.PROJECT_FOLDER_NAME_CONST;
    }

    public static String getExternalFolderName(Context context){
        return AppConst.EXTERNAL_STORAGE + AppConst.FOLDER_NAME;
    }

    public static String getExternalProjectFolder(Context context){
        return AppConst.EXTERNAL_STORAGE+AppConst.FOLDER_NAME+AppConst.PROJECT_FOLDER_NAME_CONST;
    }
}
