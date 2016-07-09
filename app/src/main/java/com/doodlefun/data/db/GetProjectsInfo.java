package com.doodlefun.data.db;

import java.io.File;
import java.util.ArrayList;

public class GetProjectsInfo {

    private static final String DRAWING_KEY = "drawing";
    private static final String BACKGROUND_KEY = "background";

    public static ArrayList<DoodleFunProjectData> getAllProjectInfo(File internalProjFolder, File externalProjFolder){
        ArrayList<DoodleFunProjectData> doodleFunInternalProjects = getProjects(internalProjFolder);
        ArrayList<DoodleFunProjectData> doodleFunExternalProjects = getProjects(externalProjFolder);
        ArrayList<DoodleFunProjectData> returnArray = new ArrayList<>();
        returnArray.addAll(doodleFunInternalProjects);
        returnArray.addAll(doodleFunExternalProjects);
        return returnArray;
    }

    public static ArrayList<DoodleFunProjectData> getProjects(File dir) {
        ArrayList<DoodleFunProjectData> projectsList = new ArrayList<>();
        File listFile[] = dir.listFiles();

        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                DoodleFunProjectData doodleFunProjectData = new DoodleFunProjectData();
                if (listFile[i].isDirectory()){
                    doodleFunProjectData.setProjectName(listFile[i].toString());
                    long lastModDate = listFile[i].lastModified();
                    doodleFunProjectData.setDateCreated(lastModDate);
                    File projFile[] = listFile[i].listFiles();
                    for (File file : projFile) {
                        if(file.getName().contains(DRAWING_KEY)) {
                            lastModDate = file.lastModified();
                            doodleFunProjectData.setDateModified(lastModDate);
                        }else if(file.getName().contains(BACKGROUND_KEY)){
                            doodleFunProjectData.setHasBackgroundImage(true);
                        }
                    }
                }
                projectsList.add(doodleFunProjectData);
            }
        }
        return projectsList;
    }
}
