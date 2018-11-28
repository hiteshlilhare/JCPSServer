package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import static com.github.hiteshlilhare.jcpss.JCPSServletApplication.REMOTE_REPO_URL;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class ReleaseManagerRestController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseManagerRestController.class);

    @PostMapping(value = {"/manageReleases"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> manageReleases() {
        HashMap<String, String> resultsMap = new HashMap<>();
        //get release information for github_releases table with PreVerified state.
        DatabaseDAOAdapter databaseDAOAdapter
                = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        ArrayList<ReleasedApp> releasedApps = new ArrayList<>();
        StatusMessage statusMessage = new StatusMessage();
        databaseDAOAdapter.getReleasedAppsDetail(releasedApps, statusMessage);
        logger.info(statusMessage.getMessage());
        String releaseTable = "<h1>Manage Releases</h1>\n"
                + "<p>Before releasing any of the repository releases please check the corresponding app store has all required artifacts.</p>\n"
                + "<table id='review_release_table' style='width:100%'>\n"
                + "<caption><b>List of Apps, which are in preverified state</b></caption>\n"
                + "<tr>\n"
                + "<th>App Name [Repo Name]</th>\n"
                + "<th>Version [Tag Name]</th>\n"
                + "<th>Developer Id [Repo User Id]</th>\n"
                + "<th style='display: none'>App Clone URL</th>\n"
                + "<th>Releasen Date</th>\n"
                + "<th>Tested On</th>\n"
                + "<th>Status</th>\n"
                + "<th>Remarks</th>\n"
                + "</tr>\n";

        if (statusMessage.getCode() == StatusMessage.Code.NOTFOUND) {
            releaseTable += "<tr>\n"
                    + "    <td colspan='9'>" + statusMessage.getMessage() + "</td>\n"
                    + "  </tr>";
            resultsMap.put("Status", StatusMessage.Code.FAILURE.toString());
        } else if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
            int idx = 1;
            for (ReleasedApp releasedApp : releasedApps) {
                releaseTable += "<tr onclick='verify_release(this);' >\n"
                        + "<td>" + releasedApp.getAppName() + "</td>\n"
                        + "<td>" + releasedApp.getVersion() + "</td>\n"
                        + "<td>" + releasedApp.getDeveloperId() + "</td>\n"
                        + "<td style='display: none'>\n"
                        + "<input type='text' value='" + (releasedApp.getCloneURL() == null ? "" : releasedApp.getCloneURL()) + "' maxlength='200' placeholder='Clone URL' name='release-cloneurl' >\n"
                        + "</td>\n"
                        + "<td>" + releasedApp.getReleaseDate() + "</td>\n"
                        + "<td><a id='testedon_anchor" + idx + "' href='javascript:toggle_javacard_list_display( " + idx + " )'>Show</a><br>"
                        + "<div id='jclist" + idx + "' style='display: none'>"
                        + "<input type='checkbox' value='Gemplus GemClub Memo'>Gemplus GemClub Memo<br>\n"
                        + "<input type='checkbox' value='Gemplus GPK4000sp'>Gemplus GPK4000sp<br>\n"
                        + "<input type='checkbox' value='Gemplus GPK2000s'>Gemplus GPK2000s<br>\n"
                        + "<input type='checkbox' value='Gemplus GPK4000s'>Gemplus GPK4000s<br>\n"
                        + "</div>\n"
                        + "</td>\n"
                        + "<td><select value='" + releasedApp.getStatus() + "'>\n"
                        + "<option value='PreVerified'>PreVerified</option>\n"
                        + "<option value='Verified'>Verified</option>\n"
                        + "<option value='Rejected'>Rejected</option>\n"
                        + "</select></td>\n"
                        + "<td>\n"
                        + "<input type='text' value='" + (releasedApp.getRemarks() == null ? "" : releasedApp.getRemarks()) + "' maxlength='1000' placeholder='Remarks' name='release-remarks' >\n"
                        + "</td>\n"
                        + "<td style='display: none'>" + releasedApp.getRating() + "</td>\n"
                        + "<td onclick='verify_release(this);'><button>Update</button></td>\n"
                        + "</tr>";
                idx++;
            }

            resultsMap.put("Status", StatusMessage.Code.SUCCESS.toString());

        } else {
            releaseTable += "<tr>\n"
                    + "    <td colspan='9'>" + "Unable to get releases, please try after sometime." + "</td>\n"
                    + "  </tr>";
            resultsMap.put("Status", StatusMessage.Code.FAILURE.toString());
        }
        releaseTable += "</table>";
        resultsMap.put("Code", releaseTable);
        return resultsMap;
    }

    @RequestMapping(value = "/verifyRelease", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> releaseApp(@RequestBody String json) {
        try {
            logger.info(json);
            ReleasedApp releasedApp = ReleasedApp.createReleasedAppBean(json);
            String sourceCloneURL = "https://github.com/" + releasedApp.getDeveloperId()
                    + "/" + releasedApp.getAppName() + ".git";
            releasedApp.setSourceCloneURL(sourceCloneURL);
            DatabaseDAOAdapter databaseDAOAdapter
                    = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            StatusMessage statusMessage = new StatusMessage();
            if (GitHubRelease.Status.Verified
                    == GitHubRelease.Status.getStatus(releasedApp.getStatus())) {
                //Check existance of all essential artifacts for applet store.
                validateAppletStoreForApp(releasedApp, statusMessage);
                if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                    return Collections.singletonMap("Status",
                            statusMessage.getMessage());
                }
                //insert into released_app table
                databaseDAOAdapter.insert(releasedApp, statusMessage);
                logger.info(statusMessage.getMessage());
                if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
                    GitHubRelease release = new GitHubRelease();
                    release.setTagname(releasedApp.getVersion());
                    release.setCloneUrl(sourceCloneURL);
                    release.setStatus(GitHubRelease.Status.Verified);
                    release.setRemarks(releasedApp.getRemarks());
                    databaseDAOAdapter.updateField(release, statusMessage);
                    logger.info(statusMessage.getMessage());
                    if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
                        statusMessage.setMessage(releasedApp.getAppName()
                                + " App's version " + releasedApp.getVersion()
                                + " released successfully");
                    }
                }
            } else if (GitHubRelease.Status.Rejected
                    == GitHubRelease.Status.getStatus(releasedApp.getStatus())
                    || GitHubRelease.Status.PreVerified
                    == GitHubRelease.Status.getStatus(releasedApp.getStatus())) {
                GitHubRelease release = new GitHubRelease();
                release.setTagname(releasedApp.getVersion());
                release.setCloneUrl(sourceCloneURL);
                release.setStatus(
                        GitHubRelease.Status.getStatus(releasedApp.getStatus()));
                release.setRemarks(releasedApp.getRemarks());
                databaseDAOAdapter.updateField(release, statusMessage);
                if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
                    statusMessage.setMessage("status and remarks upadted successfully");
                }
                logger.info(statusMessage.getMessage());
            } else {
                return Collections.singletonMap("Status",
                        "Invalid status upadate request");
            }
            return Collections.singletonMap("Status", statusMessage.getMessage());
        } catch (FieldNotPresentException ex) {
            logger.error("releaseApp:FieldNotPresentException", ex);
            return Collections.singletonMap("Status", "Please try after sometime");
        }

    }

    /**
     * Validate the structure of repository.
     *
     * @param releasedApp
     * @param statusMessage
     */
    private void validateAppletStoreForApp(ReleasedApp releasedApp,
            StatusMessage statusMessage) {
        File jcAppletStoreRepo = new File(JCPSSCOnstants.JCPS_SRV_DIR
                + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
        Git git = null;
        try {
            FileUtils.deleteDirectory(jcAppletStoreRepo);
            if (jcAppletStoreRepo.exists()) {
                logger.info("Unable to delete " + jcAppletStoreRepo + " remote repo directory");
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Applet store configuration problem, please try after sometime");
                return;
            }
            git = Git.cloneRepository().setBranch("refs/heads/master")
                    .setURI(REMOTE_REPO_URL).setDirectory(jcAppletStoreRepo).call();
        } catch (GitAPIException ex) {
            logger.error("Unable to clone java card applet store repository", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store configuration problem, please try after sometime");
            return;
        } catch (IOException ex) {
            logger.error("Unable to delete java card applet store local repository", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store configuration problem, please try after sometime");
            return;
        } finally {
            if (git != null) {
                git.close();
            }
        }
        String appBaseDir = JCPSSCOnstants.JCPS_SRV_DIR
                + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO + "/" + releasedApp.getDeveloperId()
                + "/" + releasedApp.getAppName() + "/" + releasedApp.getVersion();
        File capFile = new File(appBaseDir + "/" + releasedApp.getAppName() + ".cap");
        if (!capFile.exists() || !capFile.isFile()) {
            logger.error(capFile.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File capFileSign = new File(appBaseDir + "/" + releasedApp.getAppName() + ".cap.sig");
        if (!capFileSign.exists() || !capFileSign.isFile()) {
            logger.error(capFileSign.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File metaDataFile = new File(appBaseDir + "/" + releasedApp.getAppName() + ".xml");
        if (!metaDataFile.exists() || !metaDataFile.isFile()) {
            logger.error(metaDataFile.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File metaDataFileSign = new File(appBaseDir + "/" + releasedApp.getAppName() + ".xml.sig");
        if (!metaDataFileSign.exists() || !metaDataFileSign.isFile()) {
            logger.error(metaDataFileSign.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File buildFile = new File(appBaseDir + "/build.bat");
        if (!buildFile.exists() || !buildFile.isFile()) {
            logger.error(buildFile.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File buildFileSign = new File(appBaseDir + "/build.bat.sig");
        if (!buildFileSign.exists() || !buildFileSign.isFile()) {
            logger.error(buildFileSign.getAbsolutePath() + " file does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File appiconDir = new File(appBaseDir + "/appicon");
        if (!appiconDir.exists() || !appiconDir.isDirectory()) {
            logger.error(appiconDir.getAbsolutePath() + " directory does not exists");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        File iconsFile[] = appiconDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (!fileName.endsWith(".sig")) {
                    int idx = fileName.lastIndexOf(".") + 1;
                    if (idx < fileName.length()) {
                        String ext = fileName.substring(idx);
                        return Arrays.asList(JCPSSCOnstants.ALLOWED_ICON_EXTN).contains(ext);
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });
        if (iconsFile.length == 0) {
            logger.error(appiconDir.getAbsolutePath() + " directory is empty");
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
            return;
        }
        for (File file : iconsFile) {
            File signFile = new File(file.getAbsolutePath() + ".sig");
            if (!signFile.exists() || !signFile.isFile()) {
                logger.error(signFile.getAbsolutePath() + " file does not exist");
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Applet store confiuration problem, please try after sometime");
                return;
            }
        }

    }

}
