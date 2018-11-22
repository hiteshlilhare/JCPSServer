/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss;

import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Hitesh
 */
public class JCPSSCOnstants {
    public final static String JCPS_SRV_DIR = FileSystemView.getFileSystemView().getDefaultDirectory() + "/JCPSServer";
    public final static String JCPS_SRV_DB_DIR = "db";
    public final static String JCPS_SRV_APPS_DIR = "appsource";
    public final static String JCPS_SRV_APPS_STORE_DIR = "appstore";
    public final static String JCPS_REMOTE_REPO = "JCAppletStore";
    public final static String JCPS_SRV_TEMP_DIR = "temp";
    public final static String[] ALLOWED_ICON_EXTN = {"png","jpeg","jpg","ico","gif"};
}
