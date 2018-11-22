package com.github.hiteshlilhare.jcpss.db;

import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.JavaCardAppRating;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hitesh
 */
public abstract class DatabaseDAOAdapter implements DatabaseDAO {

    /**
     * Logger to log log various events.
     */
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DatabaseDAOAdapter.class);

    /**
     * Abstract method implementation has to be provided by corresponding
     * database DAO implementation.
     *
     * @return
     */
    protected abstract DBConnection connect();

    /**
     * Abstract method implementation has to be provided by corresponding
     * database DAO implementation.
     *
     * @param connection
     */
    protected abstract void disconnect(DBConnection connection);

    /**
     * Returns Verify status w.r.t. repository
     *
     * @param bean
     * @param statusMessage
     */
    @Override
    public void getVerifyStatus(Object bean, StatusMessage statusMessage) {
        if (bean instanceof RepoDetail) {
            DBConnection sqlc = connect();
            if (!sqlc.isConnected()) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                return;
            }
            try {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String currentStatus = rs.getString(RepoDetail.TableField.Verified.toString());
                    repoDetail.setVerified(RepoDetail.Status.getStatus(currentStatus));
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository is in " + currentStatus + " state");
                } else {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository " + repoDetail.getRepoURL()
                            + " yet not registered!!! ");
                }
            } catch (SQLException ex) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                logger.error("getVerifyStatus", ex);
            } finally {
                disconnect(sqlc);
            }
        }
    }

    /**
     * Returns repository URL with respect to client id.
     *
     * @param bean
     * @param statusMessage
     */
    @Override
    public void getRepoDetailsWRTClientID(Object bean, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        if (bean instanceof RepoDetail) {
            try {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatementWRTClientID();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String fieldValue = rs.getString(RepoDetail.TableField.RepoURL.toString());
                    repoDetail.setRepoURL(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.RepoUserID.toString());
                    repoDetail.setRepoUserID(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.RepoName.toString());
                    repoDetail.setRepoName(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.EmailID.toString());
                    repoDetail.setEmail(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.SignKeyFgrPrint.toString());
                    repoDetail.setSignKeyFingerPrint(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.Verified.toString());
                    repoDetail.setVerified(RepoDetail.Status.getStatus(fieldValue));

                    fieldValue = rs.getString(RepoDetail.TableField.Request.toString());
                    repoDetail.setRequest(RepoDetail.Request.getRequest(fieldValue));

                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository url is " + repoDetail.getRepoURL());
                } else {
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Client ID " + repoDetail.getClientID()
                            + " does not exists!!! ");
                }
            } catch (SQLException ex) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                logger.error("getRepoURLWRTClientID", ex);
            } finally {
                disconnect(sqlc);
            }
        }
    }

    /**
     * Get Verified Released Apps.
     *
     * @param releasedApps
     * @param statusMessage
     */
    @Override
    public void getVerifiedReleasedAppsDetail(ArrayList<ReleasedApp> releasedApps,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Unable to get database connection.");
            return;
        }
        try {
            releasedApps.clear();
            String selectStmt = ReleasedApp.getSelectStatement(
                    GitHubRelease.Status.Verified.toString(),
                    ReleasedApp.TableField.Status);
            Statement statement = sqlc.getConnection().createStatement();
            ResultSet rsReleasedApps = statement.executeQuery(selectStmt);
            while (rsReleasedApps.next()) {
                ReleasedApp releasedApp = new ReleasedApp();
                //Set App name.
                String fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.AppName.toString());
                releasedApp.setAppName(fieldValue);
                //Set version
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.Version.toString());
                releasedApp.setVersion(fieldValue);
                //Set Developer Id
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.DeveloperId.toString());
                releasedApp.setDeveloperId(fieldValue);
                //Set clone URL
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.CloneURL.toString());
                releasedApp.setCloneURL(fieldValue);
                //Set clone URL
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.ReleaseDate.toString());
                releasedApp.setReleaseDate(fieldValue);
                //Set tested on
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.TestedOn.toString());
                releasedApp.setTestedOn(fieldValue);
                //Set source clone URL
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.SourceCloneURL.toString());
                releasedApp.setSourceCloneURL(fieldValue);
                //Set Status
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.Status.toString());
                releasedApp.setStatus(fieldValue);
                //Set Remarks
                fieldValue = rsReleasedApps.getString(
                        ReleasedApp.TableField.Remarks.toString());
                releasedApp.setRemarks(fieldValue);
                //Set Rating
                float rating = rsReleasedApps.getFloat(
                        ReleasedApp.TableField.Rating.toString());
                releasedApp.setRating(rating);
                //Add to released app list
                releasedApps.add(releasedApp);
            }
            if (releasedApps.isEmpty()) {
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage("Applet store have no released applet");
            } else {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Applet store have " 
                        + releasedApps.size() + " released applet");
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("getVerifiedReleasedAppsDetail:fail:SQLException", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    /**
     * Returns Released Apps detail.
     *
     * @param releasedApps
     * @param statusMessage
     */
    @Override
    public void getReleasedAppsDetail(ArrayList<ReleasedApp> releasedApps,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Unable to get database connection.");
            return;
        }
        try {
            String selectStmt = GitHubRelease.getSelectStatement(
                    JCPSServletApplication.RELEASE_STATUS_TO_MONITOR,
                    GitHubRelease.TableField.Status);
            Statement statement = sqlc.getConnection().createStatement();
            ResultSet rsGithubRelease = statement.executeQuery(selectStmt);

            while (rsGithubRelease.next()) {
                String repoURL = rsGithubRelease.getString(GitHubRelease.TableField.CloneURL.toString());
                selectStmt = RepoDetail.getSelectStatement(
                        JCPSServletApplication.REPO_STATUS_TO_MONITOR,
                        RepoDetail.TableField.Verified,
                        repoURL, RepoDetail.TableField.RepoURL);
                statement = sqlc.getConnection().createStatement();
                ResultSet rsRepoDetails = statement.executeQuery(selectStmt);
                if (rsRepoDetails.next()) {
                    ReleasedApp releasedApp = new ReleasedApp();
                    //Set app name as repository name.
                    String fieldValue = rsRepoDetails.getString(RepoDetail.TableField.RepoName.toString());
                    releasedApp.setAppName(fieldValue);
                    //Set tag name as app version.
                    fieldValue = rsGithubRelease.getString(GitHubRelease.TableField.TagName.toString());
                    releasedApp.setVersion(fieldValue);
                    //Set developer id as repository user id.
                    fieldValue = rsRepoDetails.getString(RepoDetail.TableField.RepoUserID.toString());
                    releasedApp.setDeveloperId(fieldValue);
                    //Set release date as tag's publisedAt.
                    fieldValue = rsGithubRelease.getString(GitHubRelease.TableField.PublishedAt.toString());
                    releasedApp.setReleaseDate(fieldValue);
                    //Set Clone URL for source code of Application.
                    fieldValue = rsRepoDetails.getString(RepoDetail.TableField.RepoURL.toString());
                    releasedApp.setSourceCloneURL(fieldValue);
                    //Set status as github_releases's status
                    fieldValue = rsGithubRelease.getString(GitHubRelease.TableField.Status.toString());
                    releasedApp.setStatus(fieldValue);
                    //Set Remarks as github_releases's Remarks 
                    fieldValue = rsGithubRelease.getString(GitHubRelease.TableField.Remarks.toString());
                    releasedApp.setRemarks(fieldValue);
                    //Add to list of releases apps.
                    releasedApps.add(releasedApp);
                }
            }
            if (releasedApps.isEmpty()) {
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage(
                        "No releases found with status "
                        + JCPSServletApplication.RELEASE_STATUS_TO_MONITOR);
            } else {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage(releasedApps.size()
                        + " releases found with "
                        + JCPSServletApplication.RELEASE_STATUS_TO_MONITOR
                        + " status");
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("getReleasedAppsDetail:fail:SQLException", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    public void getWebhookSecret(String repoURL, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            String selectStmt = RepoDetail.getSelectStatement(repoURL,
                    RepoDetail.TableField.RepoURL);
            Statement statement = sqlc.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(selectStmt);
            if (rs.next()) {
                statusMessage.setMessage(
                        rs.getString(
                                RepoDetail.TableField.WebhookSecret.toString()));
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
            } else {
                statusMessage.setMessage("No repository found for " + repoURL);
                statusMessage.setCode(StatusMessage.Code.FAILURE);
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("getWebhookSecret", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    public void updateWebhookSecret(String repoURL, String webhookSec, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            String updateStmt = RepoDetail.getUpadteStatement(
                    webhookSec,
                    RepoDetail.TableField.WebhookSecret,
                    repoURL,
                    RepoDetail.TableField.RepoURL);
            Statement statement = sqlc.getConnection().createStatement();
            int ret = statement.executeUpdate(updateStmt);
            if (ret == 1) {
                statusMessage.setMessage("Successfully updated the webhook secret for " + repoURL);
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
            } else {
                statusMessage.setMessage("unable to update webhook secret for  " + repoURL);
                statusMessage.setCode(StatusMessage.Code.FAILURE);
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("updateWebhookSecret", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    /**
     * Returns repository URL with respect to repo url.
     *
     * @param bean
     * @param statusMessage
     */
    @Override
    public void getRepoDetailsWRTRepoURL(Object bean, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        if (bean instanceof RepoDetail) {
            try {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatementWRTRepoURL();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String fieldValue = rs.getString(RepoDetail.TableField.ClientID.toString());
                    repoDetail.setClientID(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.RepoURL.toString());
                    repoDetail.setRepoURL(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.RepoUserID.toString());
                    repoDetail.setRepoUserID(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.RepoName.toString());
                    repoDetail.setRepoName(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.SignKeyFgrPrint.toString());
                    repoDetail.setSignKeyFingerPrint(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.KeyServerURL.toString());
                    repoDetail.setKeyServeURL(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.EmailID.toString());
                    repoDetail.setEmail(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.Verified.toString());
                    repoDetail.setVerified(RepoDetail.Status.getStatus(fieldValue));

                    fieldValue = rs.getString(RepoDetail.TableField.Request.toString());
                    repoDetail.setRequest(RepoDetail.Request.getRequest(fieldValue));

                    fieldValue = rs.getString(RepoDetail.TableField.AnchorTag.toString());
                    repoDetail.setAnchorTag(fieldValue);

                    fieldValue = rs.getString(RepoDetail.TableField.Remarks.toString());
                    repoDetail.setRemarks(fieldValue);

                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository details fetched successfully");
                } else {
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Repo " + repoDetail.getRepoURL()
                            + " does not exists!!! ");
                }
            } catch (SQLException ex) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                logger.error("getRepoDetailsWRTRepoURL", ex);
            } finally {
                disconnect(sqlc);
            }
        }
    }

    /**
     *
     * @param repoURL
     * @param fileName
     * @param statusMessage
     */
    @Override
    public void writeWebhookEncSecretBlob(String repoURL,
            String fileName,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        FileInputStream input = null;
        try {
            String updateStmt
                    = RepoDetail.getUpdateWebhookEncSecretBlobStatement();
            PreparedStatement pstmt
                    = sqlc.getConnection().prepareStatement(updateStmt);
            // read the file
            File file = new File(fileName);
            input = new FileInputStream(file);

            // set parameters
            pstmt.setBinaryStream(1, input);
            pstmt.setString(2, repoURL);

            int ret = pstmt.executeUpdate();

            if (ret == 1) {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Webhook encrypted secret written successfully");
            } else {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Failed to write webhook encrypted secret into databse");
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("writeWebhookEncSecretBlob:SQLException", ex);
        } catch (FileNotFoundException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("writeWebhookEncSecretBlob:FileNotFoundException", ex);
        } finally {
            disconnect(sqlc);
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }

    /**
     *
     * @param repoURL
     * @param fileName
     * @param statusMessage
     */
    @Override
    public void readWebhookEncSecretBlob(String repoURL,
            String fileName,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        FileOutputStream output = null;
        InputStream input = null;
        try {
            String selectStmt
                    = RepoDetail.getSelectWebhookEncSecretBlobStatement();
            PreparedStatement pstmt
                    = sqlc.getConnection().prepareStatement(selectStmt);

            // set parameter;
            pstmt.setString(1, repoURL);

            // write binary stream into file
            File file = new File(fileName);
            output = new FileOutputStream(file);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                input = rs.getBinaryStream("WebhookEncSecret");
                if (input == null) {
                    statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                    statusMessage.setMessage(repoURL + " repository does not "
                            + "have webhook encrypted secret blob");
                    return;
                }
                byte[] buffer = new byte[1024];
                int noOfByesRead = 0;
                while ((noOfByesRead = input.read(buffer)) > 0) {
                    output.write(buffer, 0, noOfByesRead);
                }
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Webhook's encrypted secret has been read successfully");
            } else {
                statusMessage.setCode(StatusMessage.Code.NOTEXIST);
                statusMessage.setMessage(repoURL + " repository does not exist");
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("writeWebhookEncSecretBlob:SQLException", ex);
        } catch (FileNotFoundException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("writeWebhookEncSecretBlob:FileNotFoundException", ex);
        } catch (IOException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("writeWebhookEncSecretBlob:IOException", ex);
        } finally {
            disconnect(sqlc);
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }

    /**
     * Fills list of release tags.
     *
     * @param repoUrl
     * @param tags
     * @param statusMessage
     */
    @Override
    public void getListOfReleaseTags(String repoUrl,
            ArrayList<String> tags,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            String selectStmt = GitHubRelease.getSelectStatement(repoUrl,
                    GitHubRelease.TableField.CloneURL);
            if (selectStmt == null) {
                logger.info("GitHubRelease.getSelectStatement returns null");
                return;
            }
            Statement statement = sqlc.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(selectStmt);
            while (rs.next()) {
                tags.add(rs.getString(
                        GitHubRelease.TableField.TagName.toString()));
            }
            if (tags.isEmpty()) {
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage(
                        "No releases found for " + repoUrl + " repository");
            } else {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage(tags.size()
                        + " releases found for " + repoUrl + " repository");
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("getListOfReleaseTags", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    @Override
    public void getListOfRepoDetailsWRTStatus(String status,
            ArrayList<RepoDetail> repoDetails,
            StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            String selectStmt = RepoDetail.getSelectStatement(status,
                    RepoDetail.TableField.Verified);
            Statement statement = sqlc.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(selectStmt);
            while (rs.next()) {
                RepoDetail repoDetail = new RepoDetail();
                String fieldValue = rs.getString(
                        RepoDetail.TableField.ClientID.toString());
                repoDetail.setClientID(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.RepoURL.toString());
                repoDetail.setRepoURL(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.RepoUserID.toString());
                repoDetail.setRepoUserID(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.RepoName.toString());
                repoDetail.setRepoName(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.SignKeyFgrPrint.toString());
                repoDetail.setSignKeyFingerPrint(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.KeyServerURL.toString());
                repoDetail.setKeyServeURL(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.EmailID.toString());
                repoDetail.setEmail(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.Verified.toString());
                repoDetail.setVerified(
                        RepoDetail.Status.getStatus(fieldValue));

                fieldValue = rs.getString(
                        RepoDetail.TableField.Request.toString());
                repoDetail.setRequest(
                        RepoDetail.Request.getRequest(fieldValue));

                fieldValue = rs.getString(
                        RepoDetail.TableField.AnchorTag.toString());
                repoDetail.setAnchorTag(fieldValue);

                fieldValue = rs.getString(
                        RepoDetail.TableField.Remarks.toString());
                repoDetail.setRemarks(fieldValue);

                repoDetails.add(repoDetail);
            }
            if (repoDetails.isEmpty()) {
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage(
                        "No repository with registration status " + status);
            } else {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage(repoDetails.size()
                        + " repository found with status " + status);
            }
        } catch (SQLException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            logger.error("getRepoDetailsWRTRepoURL", ex);
        } finally {
            disconnect(sqlc);
        }
    }

    /**
     * Insert into database.
     *
     * @param bean
     * @param statusMessage
     */
    @Override
    public void insert(Object bean, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Repository Registration failed!!!");
            return;
        }
        try {
            if (bean instanceof RepoDetail) {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String currentStatus = rs.getString(RepoDetail.TableField.Verified.toString());
                    statusMessage.setCode(StatusMessage.Code.ALREADYEXISTS);
                    statusMessage.setMessage("Repository already registered "
                            + "while current status is " + currentStatus);
                    return;
                }
                String inserStmt = repoDetail.getSQLInsertStatement();
                int ret = statement.executeUpdate(inserStmt);
                if (ret == 1) {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository registration successful");
                }

            } else if (bean instanceof GitHubRelease) {
                GitHubRelease gitHubRelease = (GitHubRelease) bean;
                String selectStmt = gitHubRelease.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    statusMessage.setCode(StatusMessage.Code.ALREADYEXISTS);
                    statusMessage.setMessage(gitHubRelease.getTagname() + " tag is already present for "
                            + gitHubRelease.getCloneUrl() + " repository");
                    return;
                }
                String inserStmt = gitHubRelease.getInsertStatement();
                int ret = statement.executeUpdate(inserStmt);
                if (ret == 1) {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage(gitHubRelease.getTagname() + " tag is inserted successfully for "
                            + gitHubRelease.getCloneUrl() + " repository");
                } else {
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Unable to insert " + gitHubRelease.getTagname() + " tag into database for "
                            + gitHubRelease.getCloneUrl() + " repository");
                }
            } else if (bean instanceof ReleasedApp) {
                ReleasedApp releasedApp = (ReleasedApp) bean;
                String selectStmt = releasedApp.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String updateStmt = releasedApp.getUpdateRemarksAndTestedOnFieldStatement();
                    int ret = statement.executeUpdate(updateStmt);
                    if (ret == 1) {
                        statusMessage.setCode(StatusMessage.Code.SUCCESS);
                        statusMessage.setMessage("remarks and tested on fileds are updated successfully");
                    } else {
                        statusMessage.setCode(StatusMessage.Code.FAILURE);
                        statusMessage.setMessage("faied to update remarks and tested on fields");
                    }
                    return;
                }
                String inserStmt = releasedApp.getInsertStatement();
                int ret = statement.executeUpdate(inserStmt);
                if (ret == 1) {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage(releasedApp.getVersion() + " tag is inserted successfully for "
                            + releasedApp.getSourceCloneURL() + " repository");
                } else {
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Unable to insert " + releasedApp.getVersion() + " tag into database for "
                            + releasedApp.getSourceCloneURL() + " repository");
                }
            }
        } catch (SQLException ex) {
            logger.error("insert", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("insert:SQLException:Operation failed!!!");
        } finally {
            disconnect(sqlc);
        }
    }

    /**
     *
     * @param bean
     * @param fieldName
     * @param statusMessage
     */
    @Override
    public void updateField(Object bean, RepoDetail.TableField fieldName, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            if (bean instanceof RepoDetail) {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    if (RepoDetail.TableField.Request == fieldName) {
                        String updateStmt = repoDetail.getUpdateRequestFieldStatement();
                        int ret = statement.executeUpdate(updateStmt);
                        if (ret == 1) {
                            statusMessage.setCode(StatusMessage.Code.SUCCESS);
                            statusMessage.setMessage("Request submitted successfully");
                        } else {
                            statusMessage.setCode(StatusMessage.Code.FAILURE);
                            statusMessage.setMessage("executeUpdate:Failed:" + updateStmt);
                        }
                    } else if (RepoDetail.TableField.ClientID == fieldName) {
                        String updateStmt = repoDetail.getUpdateClientIDFieldStatement();
                        int ret = statement.executeUpdate(updateStmt);
                        if (ret == 1) {
                            statusMessage.setCode(StatusMessage.Code.SUCCESS);
                            statusMessage.setMessage("Client ID updated successfully");
                        } else {
                            statusMessage.setCode(StatusMessage.Code.FAILURE);
                            statusMessage.setMessage("executeUpdate:Failed:" + updateStmt);
                        }
                    } else if (RepoDetail.TableField.AnchorTag == fieldName) {
                        String updateStmt = repoDetail.getUpdateAnchorTagFieldStatement();
                        int ret = statement.executeUpdate(updateStmt);
                        if (ret == 1) {
                            statusMessage.setCode(StatusMessage.Code.SUCCESS);
                            statusMessage.setMessage("Anchor tag updated successfully");
                        } else {
                            statusMessage.setCode(StatusMessage.Code.FAILURE);
                            statusMessage.setMessage("executeUpdate:Failed:" + updateStmt);
                        }
                    }

                } else {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository " + repoDetail.getRepoURL()
                            + " yet not registered!!! ");
                }

            }
        } catch (SQLException ex) {
            logger.error("updateField", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
        } finally {
            disconnect(sqlc);
        }
    }

    @Override
    public void updateField(Object bean, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
            return;
        }
        try {
            if (bean instanceof GitHubRelease) {
                GitHubRelease release = (GitHubRelease) bean;
                String selectStmt = release.getSelectStatement();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    String updateStmt = release.getUpdateStatusAndRemarksStatement();
                    int ret = statement.executeUpdate(updateStmt);
                    if (ret != 1) {
                        statusMessage.setCode(StatusMessage.Code.FAILURE);
                        statusMessage.setMessage("Unable to update status and remarks for "
                                + release.getCloneUrl());
                    }
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Successfully updated status and remarks for "
                            + release.getCloneUrl());
                } else {
                    statusMessage.setCode(StatusMessage.Code.NOTEXIST);
                    statusMessage.setMessage("Repository " + release.getCloneUrl()
                            + " with " + release.getTagname() + " tag does not exist");
                }
            }
        } catch (SQLException ex) {
            logger.error("updateField", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
        } finally {
            disconnect(sqlc);
        }
    }

    /**
     *
     * @param bean
     * @param fieldName
     * @param statusMessage
     */
    @Override
    public void updateFieldByClientID(Object bean, RepoDetail.TableField fieldName,
            StatusMessage statusMessage) {
        if (bean instanceof RepoDetail) {
            DBConnection sqlc = connect();
            if (!sqlc.isConnected()) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                return;
            }
            try {
                RepoDetail repoDetail = (RepoDetail) bean;
                String selectStmt = repoDetail.getSelectStatementWRTClientID();
                Statement statement = sqlc.getConnection().createStatement();
                ResultSet rs = statement.executeQuery(selectStmt);
                if (rs.next()) {
                    if (RepoDetail.TableField.Verified == fieldName) {
                        //String request = rsRepoDetails.getString(RepoDetail.TableField.Request.toString());
                        String currentStatus = rs.getString(RepoDetail.TableField.Verified.toString());
                        if (repoDetail.getVerified().toString()
                                .equalsIgnoreCase(currentStatus)) {
                            statusMessage.setCode(StatusMessage.Code.ALREADYEXISTS);
                            statusMessage.setMessage("Repository is already " + repoDetail.getVerified());
                        } else {
                            //Update the verified field.
                            String updateStmt = repoDetail.getUpdateVerifyFieldWRTClientIDStatement();
                            int ret = statement.executeUpdate(updateStmt);
                            if (ret == 1) {
                                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                                statusMessage.setMessage("Repository status updated to "
                                        + repoDetail.getVerified() + " successfully");
                                repoDetail.setRequest(RepoDetail.Request.Done);
                                //Update the request field.
                                updateStmt = repoDetail.getUpdateRequestFieldWRTClientIDStatement();
                                ret = statement.executeUpdate(updateStmt);
                                if (ret != 1) {
                                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                                    statusMessage.setMessage("Repository status updated to verified successfully,"
                                            + System.lineSeparator()
                                            + "but not request field to done.");
                                }
                            }
                        }

                    } else if (RepoDetail.TableField.Remarks == fieldName) {
                        String updateStmt = repoDetail.getUpdateRemarksFieldWRTClientIDStatement();
                        int ret = statement.executeUpdate(updateStmt);
                        if (ret == 1) {
                            statusMessage.setCode(StatusMessage.Code.SUCCESS);
                            statusMessage.setMessage("Remarks for repository updated successfully");
                        }
                    }
                } else {
                    statusMessage.setCode(StatusMessage.Code.INVAILD);
                    statusMessage.setMessage("Repository " + repoDetail.getRepoURL()
                            + " yet not confirmed!!! ");
                }
            } catch (SQLException ex) {
                logger.error("updateFieldByClientID", ex);
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
            } finally {
                disconnect(sqlc);
            }
        }
    }

    @Override
    public void delete(Object bean, StatusMessage statusMessage) {
        DBConnection sqlc = connect();
        if (!sqlc.isConnected()) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Repository Registration failed!!!");
            return;
        }
        if (bean instanceof RepoDetail) {
            try {
                RepoDetail repoDetail = (RepoDetail) bean;
                String deleteStmt = repoDetail.getDeleteStatement();
                Statement statement = sqlc.getConnection().createStatement();
                int ret = statement.executeUpdate(deleteStmt);
                if (ret == 1) {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage(repoDetail.getRepoURL() + " repository detail deteted successfully");
                } else {
                    statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                    statusMessage.setMessage(repoDetail.getRepoURL() + " repository does not exists");
                }
            } catch (SQLException ex) {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Please try after sometime");
                logger.error("delete", ex);
            } finally {
                disconnect(sqlc);
            }
        }
    }

    /**
     * Creates database schema.
     *
     * @return
     */
    @Override
    public boolean createTablesIfNotExist() {
        DBConnection connection = new DBConnection();
        try {
            // create a connection to the database
            connection = connect();
            boolean status = connection.isConnected();
            if (!status) {
                return false;
            }
            logger.info("Connection to database has been established.");
            Statement statement = connection.getConnection().createStatement();
            int ret = statement.executeUpdate(RepoDetail.getCreateTableStatement());
            if (ret == 0) {
                logger.info("repo_details table created successfully!!!");
            } else {
                logger.info("Failed to create repo_details table");
                return false;
            }
            ret = statement.executeUpdate(GitHubRelease.getCreateTableStatement());
            if (ret == 0) {
                logger.info("github_releases table created successfully!!!");
            } else {
                logger.info("Failed to create github_releases table");
                return false;
            }
            ret = statement.executeUpdate(JavaCardAppRating.getCreateTableStatement());
            if (ret == 0) {
                logger.info("app_rating table created successfully!!!");
            } else {
                logger.info("Failed to create app_rating table");
                return false;
            }
            ret = statement.executeUpdate(ReleasedApp.getCreateTableStatement());
            if (ret == 0) {
                logger.info("released_app table created successfully!!!");
            } else {
                logger.info("Failed to create released_app table");
                return false;
            }
        } catch (SQLException e) {
            logger.error("createSchema", e);
            return false;
        } finally {
            disconnect(connection);
        }
        return true;
    }

    /**
     * DBConnection to encapsulate connection object along with connection
     * status.
     */
    protected class DBConnection {

        /**
         * Connection instance for database.
         */
        private Connection connection = null;
        /**
         * Connection object status.
         */
        private boolean status = false;

        /**
         * Returns Connection object.
         *
         * @return
         */
        public Connection getConnection() {
            return connection;
        }

        /**
         * Sets connection object.
         *
         * @param connection
         */
        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        /**
         * Returns true if connection object is valid.
         *
         * @return
         */
        public boolean isConnected() {
            return status;
        }

        /**
         * Sets connection status.
         *
         * @param status
         */
        public void setStatus(boolean status) {
            this.status = status;
        }

    }
}
