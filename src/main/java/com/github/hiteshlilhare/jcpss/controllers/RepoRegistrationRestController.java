package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.util.Util;
import com.github.hiteshlilhare.jcpss.bean.DeveloperGPGPublicKey;
import com.github.hiteshlilhare.jcpss.bean.ReleaseMonitorTimerMap;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.email.EmailServiceImpl;
import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.github.hiteshlilhare.jcpss.exception.UnexpectedInputLengthException;
import com.github.hiteshlilhare.jcpss.util.ReleaseMonitorTimerTask;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class RepoRegistrationRestController {

    /**
     * Number of random bytes in email link, sent for user verification.
     */
    private static final int RAND_EMIAL_TOKEN = 20;

    private enum ERROR_CODE {
        REPOREGEMAIL01/*FAILURE*/, REPOREGEMAIL02/*INITIALIZE*/,
        REGREPO01/*SendEmailUpdate*/
    };
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RepoRegistrationRestController.class);

    //private CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.DAYS.SECONDS);
    @Autowired
    public EmailServiceImpl emailService;

    @Value("${:classpath:/static/repo_status_check.html}")
    private Resource statusCheck;

    @Value("${:classpath:/static/errorpage.html}")
    private Resource errorPage;

    @GetMapping(value = {"/confirm"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity confirmClient(ModelMap model, @RequestParam("client_id") String clientID) {

        logger.info("Client ID: " + clientID);
        RepoDetail repoDetail = new RepoDetail();
        repoDetail.setClientID(clientID);
        
        DatabaseDAOAdapter databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        StatusMessage statusMessage = new StatusMessage();
        //Get Repositry URL.
        logger.info("Get repodetail wrt client id");
        databaseDAOAdapter.getRepoDetailsWRTClientID(repoDetail, statusMessage);
        logger.info("Status:"+ statusMessage);
        if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
            CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
            return ResponseEntity.ok().cacheControl(cacheControl).
                    body(getPage(errorPage, "<error-msg-place-holder>", statusMessage.getMessage()));
        }
        String currentReoStatus = repoDetail.getVerified().toString();
        logger.info("Repository current status:" + currentReoStatus);
        //Get public key from Github with respect to github user id which will be confirming to 
        //email id.
        logger.info("Get public key wrt " + repoDetail.getRepoUserID() + " repo user id");
        ArrayList<DeveloperGPGPublicKey> gpgPublicKeys = getPublicKeyFromRepository(
                repoDetail.getRepoUserID(),
                statusMessage);
        logger.info("Status: " + statusMessage);
        if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
            CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
            return ResponseEntity.ok().cacheControl(cacheControl).
                    body(getPage(statusCheck, "<repo-url-place-holder>", repoDetail.getRepoURL(),
                            "<confirmation-status-place-holder>", statusMessage.getMessage(),
                            "<guideline-display-place-holder>", "inline"));
        }

        String rawPubKey = null;
        for (DeveloperGPGPublicKey gpgPublicKey : gpgPublicKeys) {
            if (Util.isDeveloperPublicKeyValid(gpgPublicKey, repoDetail)) {
                rawPubKey = gpgPublicKey.getRawKey();
                break;
            }
        }

        if (rawPubKey == null) {
            logger.error("confirmClient:fail:Error while validating public key id "
                    + repoDetail.getSignKeyFgrPrint());
            //Update repository status to verified.
            //currentStatus will be used for displaying status on failure.
            repoDetail.setVerified(RepoDetail.Status.Verified);
            databaseDAOAdapter.updateFieldByClientID(repoDetail, RepoDetail.TableField.Verified,
                    statusMessage);
            repoDetail.setRemarks("Repository GPG key is not configured properly");
            //Created temp StatusMessage object since we are not interested in the status
            //returned by updating the remarks in repo_details table
            StatusMessage tempStatusMessage = new StatusMessage();
            databaseDAOAdapter.updateFieldByClientID(repoDetail, RepoDetail.TableField.Remarks,
                    tempStatusMessage);

            System.out.println(statusMessage);

            if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
                return ResponseEntity.ok().cacheControl(cacheControl).
                        body(getPage(errorPage, "<error-msg-place-holder>", statusMessage.getMessage()
                                + "<br/>Its current status is " + currentReoStatus));
            } else {
                CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
                return ResponseEntity.ok().cacheControl(cacheControl).
                        body(getPage(statusCheck, "<repo-url-place-holder>", repoDetail.getRepoURL(),
                                "<confirmation-status-place-holder>", "Repository GPG key is not configured properly",
                                "<guideline-display-place-holder>", "inline"));
            }
        }
        //save developer public key 
        String keyDirPath = JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_APPS_DIR
                + "/" + repoDetail.getRepoUserID() + "/" + repoDetail.getRepoName();
        File keyDirPathFileObj = new File(keyDirPath);
        if (keyDirPathFileObj.mkdirs()) {
            String keyFileName = repoDetail.getRepoUserID() + ".pub.key";
            try (BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(keyDirPath + "/" + keyFileName)))) {
                bufferedWriter.write(rawPubKey, 0, rawPubKey.length());
                bufferedWriter.flush();
            } catch (IOException ex) {
                logger.error("confirmClient:fail:IOException", ex);
                CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
                return ResponseEntity.ok().cacheControl(cacheControl).
                        body(getPage(errorPage, "<error-msg-place-holder>", 
                                "Unable to get " + repoDetail.getRepoUserID() + " public key."
                                + "<br/>Repository current status is " + currentReoStatus));
            }
        }
        //Update repository status to Pre-Registered.
        repoDetail.setVerified(RepoDetail.Status.PreRegistered);
        databaseDAOAdapter.updateFieldByClientID(repoDetail, RepoDetail.TableField.Verified, statusMessage);
        System.out.println(statusMessage);
        if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
            ReleaseMonitorTimerTask task = new ReleaseMonitorTimerTask(repoDetail);
            ReleaseMonitorTimerMap.getInstance().addTimer(task);
            CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
            return ResponseEntity.ok().cacheControl(cacheControl).
                    body(getPage(statusCheck, "<repo-url-place-holder>", repoDetail.getRepoURL(),
                            "<confirmation-status-place-holder>", "",
                            "<guideline-display-place-holder>", "none"));
        } else if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
            CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
            return ResponseEntity.ok().cacheControl(cacheControl).
                    body(getPage(errorPage, "<error-msg-place-holder>", statusMessage.getMessage()
                            + "<br/>Its current status is " + currentReoStatus));
        } else {
            CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
            return ResponseEntity.ok().cacheControl(cacheControl).
                    body(getPage(statusCheck, "<repo-url-place-holder>", repoDetail.getRepoURL(),
                            "<confirmation-status-place-holder>", statusMessage.getMessage(),
                            "<guideline-display-place-holder>", "none"));
        }
    }

    /**
     * //https://api.github.com/users/<github-user-id>/gpg_keys
     *
     * @param githubUserId
     * @param statusMessage
     * @return
     */
    private ArrayList<DeveloperGPGPublicKey> getPublicKeyFromRepository(String githubUserId,
            StatusMessage statusMessage) {
        //https://api.github.com/users/sid062010/gpg_keys
        ArrayList<DeveloperGPGPublicKey> gpgKeys = new ArrayList<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("https://api.github.com/users/"
                    + githubUserId
                    + "/gpg_keys");
            System.out.println("URL:" + "https://api.github.com/users/" + githubUserId + "/gpg_keys");
            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("responseBody : " + responseBody);
            com.google.gson.JsonParser jsonParser = new com.google.gson.JsonParser();
            com.google.gson.JsonArray gpgKeyJsonArray = jsonParser.parse(responseBody).getAsJsonArray();
            //ArrayList<GitHubRelease> gitHubReleases = new ArrayList<>();
            if (gpgKeyJsonArray.size() == 0) {
                logger.info("Repository has no GPG key configured!!!");
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage("Repository has no GPG key configured!!!");
            } else {
                for (int i = 0; i < gpgKeyJsonArray.size(); i++) {
                    DeveloperGPGPublicKey gpgPubKey = DeveloperGPGPublicKey
                            .createDeveloperGPGPublicKeyBean(
                            gpgKeyJsonArray.get(i).getAsJsonObject());
                    gpgKeys.add(gpgPubKey);
                }
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Repository has " + gpgKeyJsonArray.size() + " GPG key configured");
            }
            logger.info(gpgKeys.toString());
        } catch (IOException ex) {
            logger.error("getPublicKeyFromRepository:fail:IOException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
        } catch (FieldNotPresentException ex) {
            logger.error("getPublicKeyFromRepository:fail:FieldNotPresentException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Please try after sometime");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.info("getPublicKeyFromRepository: Exception happened "
                        + "while closing httpclient connection");
            }
        }
        return gpgKeys;
    }

    private String getPage(Resource page, String placeHoder, String message) {
        StringBuilder out = new StringBuilder();
        try ( //read html file and replace place holder with actual value.
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        page.getInputStream()))) {
            out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException ex) {
            logger.error("gePage", ex);
        }

        return out.toString().replace(placeHoder, message);
    }

    private String getPage(Resource page, String placeHoder1, String message1,
            String placeHoder2, String message2,
            String placeHoder3, String message3) {
        StringBuilder out = new StringBuilder();
        try ( //read html file and replace place holder with actual value.
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        page.getInputStream()))) {
            out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException ex) {
            logger.error("gePage", ex);
        }

        return out.toString().replace(placeHoder1, message1)
                .replace(placeHoder2, message2)
                .replace(placeHoder3, message3);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> registerRepo(@RequestBody String json) {
        try {
            //put details into database
            System.out.println("Register Repo..." + json);
            //It is required to set verify status and request variable
            //explicitly.
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);
            repoDetail.setVerified(RepoDetail.Status.Initial);
            repoDetail.setRequest(RepoDetail.Request.Unknown);

            System.out.println("RepoDetail:" + repoDetail);
            //1. Validate the repo.
            boolean status = validateRepoDetail(repoDetail);
            if (!status) {
                logger.info("Repositroy validation failed.");
                return Collections.singletonMap("Status", "Rpository does not exists/is not public.");
            }
            //2. Validate email address and send email for confirmation.
            StatusMessage statusMessage = getEmailAddressFromGithub(repoDetail.getRepoUserID(),
                    repoDetail.getRepoName());
            if (StatusMessage.Code.SUCCESS == statusMessage.getCode()) {
                if (!statusMessage.getMessage().equalsIgnoreCase(repoDetail.getEmail())) {
                    return Collections.singletonMap("Status", "Email address does not match!!!");
                }
                //3. If valid then insert into database.
                DatabaseDAOAdapter databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
                statusMessage = new StatusMessage();
                databaseDAOAdapter.insert(repoDetail, statusMessage);
                if (StatusMessage.Code.FAILURE == statusMessage.getCode()) {
                    logger.info(statusMessage.getMessage());
                    return Collections.singletonMap("Status", "Failed: Please try after some time");
                } else if (StatusMessage.Code.ALREADYEXISTS == statusMessage.getCode()) {
                    return Collections.singletonMap("Status", statusMessage.getMessage());
                }
                //4. Generate random client token.
                Util.getRandomBytes(RAND_EMIAL_TOKEN, statusMessage);
                if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                    logger.info("Unable to generate client id tocken");
                    //delete the record.
                    databaseDAOAdapter.delete(repoDetail, statusMessage);
                    if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                        logger.info("Failed to delete repo detail wrt " + repoDetail.getRepoURL()
                                + ": Database record will be in invaild state");
                    }
                    return Collections.singletonMap("Status", "Failed: Please try after some time");
                }
                repoDetail.setClientID(statusMessage.getMessage());
                databaseDAOAdapter.updateField(repoDetail, RepoDetail.TableField.ClientID, statusMessage);
                if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                    logger.info(statusMessage.getMessage());
                    //delete the record.
                    databaseDAOAdapter.delete(repoDetail, statusMessage);
                    if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                        logger.info("Failed to delete repo detail wrt " + repoDetail.getRepoURL()
                                + ": Database record will be in invaild state");
                    }
                    return Collections.singletonMap("Status", "Failed: Please try after some time");
                }
                //5. Send Confirmation email to user.
                emailService.sendSimpleMessage(repoDetail.getEmail(),
                        repoDetail.getRepoName() + "'s registration verification in Java Card Play Store",
                        "congratulations!!!" + System.lineSeparator() + System.lineSeparator() + System.lineSeparator()
                        + "Please click below link for completing repository registration." + System.lineSeparator() + System.lineSeparator()
                        + JCPSServletApplication.PUB_URL + "confirm?client_id=" + repoDetail.getClientID()
                        + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + "Regards," + System.lineSeparator() + "Team JCPS",
                        statusMessage);
                if (StatusMessage.Code.SUCCESS == statusMessage.getCode()) {
                    repoDetail.setRequest(RepoDetail.Request.Register);
                    databaseDAOAdapter.updateField(repoDetail, RepoDetail.TableField.Request, statusMessage);
                    if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
                        logger.info("Failed to delete repo detail wrt " + repoDetail.getRepoURL()
                                + ": Database record will be in invaild state");
                    }
                    return Collections.singletonMap("Status", "Verification email has been sent to user");
                } else {
                    databaseDAOAdapter.delete(repoDetail, statusMessage);
                    if (StatusMessage.Code.FAILURE == statusMessage.getCode()) {
                        logger.info("Failed to delete repo detail wrt " + repoDetail.getRepoURL()
                                + ": Database record will be in invaild state");
                    }
                    return Collections.singletonMap("Status", "Failed: Please try after some time");
                }

            } else if (StatusMessage.Code.NOTFOUND == statusMessage.getCode()) {
                return Collections.singletonMap("Status", statusMessage.getMessage());
            } else if (StatusMessage.Code.FAILURE == statusMessage.getCode()) {
                logger.info(statusMessage.getMessage());
                return Collections.singletonMap("Status", ERROR_CODE.REPOREGEMAIL01 + ":Please try after sometime");
            } else {
                logger.info(statusMessage.getMessage());
                return Collections.singletonMap("Status", ERROR_CODE.REPOREGEMAIL02 + "Please try after sometime");
            }
        } catch (UnexpectedInputLengthException ex) {
            logger.error("registerRepo", ex);
            return Collections.singletonMap("Status", getHTMLErrorMessage(ex.getMessage()));
        }
    }

    private StatusMessage getEmailAddressFromGithub(String repoUserID, String repoName) {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setCode(StatusMessage.Code.INITIALIZE);
        String repoCommitsURL = "https://api.github.com/repos/" + repoUserID + "/" + repoName + "/commits";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(repoCommitsURL);

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            com.google.gson.JsonParser jsonParser = new com.google.gson.JsonParser();
            com.google.gson.JsonArray commits = jsonParser.parse(responseBody).getAsJsonArray();
            //ArrayList<GitHubRelease> gitHubReleases = new ArrayList<>();
            if (commits.size() == 0) {
                logger.info(repoName + " repository has no commits for " + repoUserID + " user!!!");
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage(repoName + " repository has no commits for " + repoUserID + " user!!!");
            } else {
                int i = 0;
                for (; i < commits.size(); i++) {
                    JsonObject commit = commits.get(i).getAsJsonObject();
                    JsonObject commit_author = commit.getAsJsonObject("commit").getAsJsonObject("author");
                    JsonObject author = commit.getAsJsonObject("author");
                    //String authName = commit_author.get("name").getAsString();
                    String authName = author.get("login").getAsString();
                    if (repoUserID.equalsIgnoreCase(authName)) {
                        String authEmail = commit_author.get("email").getAsString();
                        if (!authEmail.contains("noreply.github.com")) {
                            statusMessage.setCode(StatusMessage.Code.SUCCESS);
                            statusMessage.setMessage(authEmail);
                            logger.info("Email:" + authEmail);
                            break;
                        }
                    }
                }
                if (i == commits.size()) {
                    statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                    statusMessage.setMessage("Email address for user id " + repoUserID + " not found");
                }
            }
        } catch (IOException ex) {
            logger.error("getEmailAddressFromGithub", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("IOException: Please check the log");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.info("getEmailAddressFromGithub", ex);
            }
        }
        return statusMessage;
    }

    //regstatus
    @RequestMapping(value = "/regstatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Map<String, String> repoRegStatus(@RequestBody String json) {
//    ResponseEntity repoRegStatus(@RequestBody String json) {
//        final HttpHeaders httpHeaders= new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            System.out.println("json: " + json);
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);
            StatusMessage statusMessage = new StatusMessage();
            DatabaseDAOAdapter databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            databaseDAOAdapter.getVerifyStatus(repoDetail, statusMessage);
            System.out.println("statusMessage:" + statusMessage);
            logger.info(statusMessage.getMessage());
//            return new ResponseEntity<String>("{\"Status\": \""+getHTMLMessage(statusMessage)+"\"}", httpHeaders, HttpStatus.OK);
            return Collections.singletonMap("Status", getHTMLMessage(statusMessage));
        } catch (UnexpectedInputLengthException ex) {
            logger.error("repoRegStatus", ex);
//            return new ResponseEntity<String>("{\"Status\": \""+getHTMLErrorMessage(ex.getMessage())+"\"}", httpHeaders, HttpStatus.OK);
            return Collections.singletonMap("Status", getHTMLErrorMessage(ex.getMessage()));
        }
    }

    @RequestMapping(value = "/unreg", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> unregisterRepo(@RequestBody String json) {
        try {
            System.out.println("json: " + json);
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);
            repoDetail.setRequest(RepoDetail.Request.Delete);

            StatusMessage statusMessage = new StatusMessage();
            DatabaseDAOAdapter databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            databaseDAOAdapter.updateField(repoDetail, RepoDetail.TableField.Request, statusMessage);
            return Collections.singletonMap("Status", getHTMLMessage(statusMessage));
        } catch (UnexpectedInputLengthException ex) {
            logger.error("unregisterRepo", ex);
            return Collections.singletonMap("Status", getHTMLErrorMessage(ex.getMessage()));
        }
    }

    /**
     * Returns html message.
     *
     * @param statusMessage
     */
    private String getHTMLMessage(StatusMessage statusMessage) {
        String htmlMessage = statusMessage.getMessage();
        if (statusMessage.getCode() == StatusMessage.Code.FAILURE) {
            htmlMessage = getHTMLErrorMessage(htmlMessage);
        } else {
            htmlMessage = getHTMLSuccessMessage(htmlMessage);
        }
        return htmlMessage;
    }

    private String getHTMLSuccessMessage(String msg) {
        return "<span class=\"blink_text\" style=\"color:green\">" + msg + "</span>";
    }

    private String getHTMLErrorMessage(String msg) {
        return "<span class=\"blink_text\" style=\"color:red\">" + msg + "</span>";
    }

    private String getHTMLWarningMessage(String msg) {
        return "<span class=\"blink_text\" style=\"color:yellow\">" + msg + "</span>";
    }

    private boolean validateRepoDetail(RepoDetail repoDetail) {
        String repoURL = repoDetail.getRepoURL();
        if (repoURL == null || repoURL.length() == 0) {
            logger.info("Remote repository url is null or empty");
            return false;
        }
        try {
            LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository();
            lsRemoteCommand.setRemote(repoURL);
//        lsRemoteCommand.setCredentialsProvider(
//                new UsernamePasswordCredentialsProvider(repoDetail.getRepoUserID(), ""));
            Collection<Ref> refs = lsRemoteCommand.call();
            if (refs.size() > 0) {
                return true;
            }
        } catch (TransportException e) {
            logger.error("validateRepoDetail", e);
        } catch (GitAPIException e) {
            logger.error("validateRepoDetail", e);
        }
        return false;
    }
}
