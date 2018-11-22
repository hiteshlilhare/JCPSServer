/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.db;

import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import java.util.ArrayList;

/**
 *
 * @author Hitesh
 */
public interface DatabaseDAO {

    public boolean createTablesIfNotExist();

    public void getVerifyStatus(Object bean,
            StatusMessage statusMessage);
    
    public void getVerifiedReleasedAppsDetail(ArrayList<ReleasedApp> releasedApps,
            StatusMessage statusMessage);

    public void getReleasedAppsDetail(ArrayList<ReleasedApp> releasedApps,
            StatusMessage statusMessage);

    public void getRepoDetailsWRTClientID(Object bean,
            StatusMessage statusMessage);

    public void getRepoDetailsWRTRepoURL(Object bean,
            StatusMessage statusMessage);

    public void getListOfRepoDetailsWRTStatus(String status,
            ArrayList<RepoDetail> repoDetails,
            StatusMessage statusMessage);

    public void getListOfReleaseTags(String repoUrl,
            ArrayList<String> tags,
            StatusMessage statusMessage);

    public void insert(Object bean, StatusMessage status);

    public void updateField(Object bean,
            RepoDetail.TableField fieldName,
            StatusMessage statusMessage);

    public void updateField(Object bean,
            StatusMessage statusMessage);

    public void updateFieldByClientID(Object bean,
            RepoDetail.TableField fieldName,
            StatusMessage statusMessage);

    public void writeWebhookEncSecretBlob(String repoURL,
            String fileName,
            StatusMessage statusMessage);

    public void readWebhookEncSecretBlob(String repoURL,
            String fileName,
            StatusMessage statusMessage);

    public void delete(Object bean,
            StatusMessage statusMessage);

}
