/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.util;

import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimerTask;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hitesh
 */
public class ReleaseMonitorTimerTask extends TimerTask {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseMonitorTimerTask.class);
    /**
     * Reference of repository detail for monitoring.
     */
    private final RepoDetail repoDetail;

    /**
     * Constructor
     *
     * @param repoDetail
     */
    public ReleaseMonitorTimerTask(RepoDetail repoDetail) {
        this.repoDetail = repoDetail;
    }

    @Override
    public void run() {
        //Anchor is not being set. Anchor is set by first reception of release event form 
        //registered github repository.
        if (repoDetail.getAnchorTag() == null) {
            return;
        }
        
        String releaseURL = "https://api.github.com/repos/"
                + repoDetail.getRepoUserID() + "/"
                + repoDetail.getRepoName() + "/releases";
        StatusMessage statusMessage = new StatusMessage();
        ArrayList<GitHubRelease> releases
                = Util.getListOfGitRepoReleases(releaseURL,
                        repoDetail.getAnchorTag(), statusMessage);
        Collections.sort(releases, new SortByLocalDateTime());
        ArrayList<String> availableReleaseTags = new ArrayList<>();
        DatabaseDAOAdapter databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        databaseDAOAdapter.getListOfReleaseTags(repoDetail.getRepoURL(),
                availableReleaseTags, statusMessage);
        for (GitHubRelease release : releases) {
            if (availableReleaseTags.contains(release.getTagname())) {
                continue;
            }
            Util.validateRelease(release, repoDetail, statusMessage);
            logger.info(statusMessage.getMessage());
            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                continue;
            }
            //Insert into database.
            release.setTagSignatureVerified(true);
            release.setStatus(GitHubRelease.Status.PreVerified);

            databaseDAOAdapter.insert(release, statusMessage);
            logger.info(statusMessage.getMessage());
        }

    }

    public String getRepoID() {
        return repoDetail.getRepoUserID() + "/" + repoDetail.getRepoName();
    }

}
