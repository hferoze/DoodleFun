package com.doodlefun.data.db;

public class DoodleFunProjectData {

    private String mProjectName;
    private long mDateCreated, mDateModified;
    private boolean mHasBackgroundImage;

    public DoodleFunProjectData(){
    }

    public DoodleFunProjectData(String projectName, long dateCreated, long dateModified, boolean hasBackgroundImage){
        mProjectName = projectName;
        mDateCreated = dateCreated;
        mDateModified = dateModified;
        mHasBackgroundImage = hasBackgroundImage;
    }

    public void setProjectName(String projectName){
        mProjectName = projectName;
    }

    public String getProjectName(){
        return mProjectName;
    }

    public void setDateCreated(long dateCreated){
        mDateCreated = dateCreated;
    }

    public long getDateCreated(){
        return mDateCreated;
    }

    public void setDateModified(long dateModified){
        mDateModified = dateModified;
    }

    public long getDateModified(){
        return mDateModified;
    }

    public void setHasBackgroundImage(boolean hasBackgroundImage){
        mHasBackgroundImage = hasBackgroundImage;
    }

    public boolean getHasBackgroundImage(){
        return mHasBackgroundImage;
    }

}
