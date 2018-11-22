package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.exception.UnexpectedInputLengthException;
import com.github.hiteshlilhare.jcpss.util.Util;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
public class SecretShareRestController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RepoRegistrationRestController.class);
    private static final int WEBHOOK_SEC_LENGTH = 20;

    @Value("${jcpss.pubkey.fingerprint}")
    private String pubkeyFingerprint;

    @RequestMapping(value = "/getrandom", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> getWebHookSecurityAndServerPubKey(@RequestBody String json) {
        HashMap<String, String> resultsMap = new HashMap<>();
        //Random bytes encrypted by public key of developer
        if (pubkeyFingerprint == null) {
            logger.error("getWebHookSecurityAndServerPubKey:fail:jcpss.pubkey.fingerprint property of "
                    + "application.properties doen not exist");
            return Collections.singletonMap(
                    "Status", "We are fixing it, please try after sometime");
        }

        try {
            System.out.println("json:" + json);
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);
            repoDetail.setRepoURL("https://github.com/" + repoDetail.getRepoUserID()
                    + "/" + repoDetail.getRepoName() + ".git");

            StatusMessage statusMessage = new StatusMessage();
            DatabaseDAOAdapter databaseDAOAdapter = 
                    DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            databaseDAOAdapter.getRepoDetailsWRTRepoURL(repoDetail, statusMessage);
            logger.info(statusMessage.getMessage());
            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                return Collections.singletonMap(
                        "Status", statusMessage.getMessage());
            }
            if (!repoDetail.getVerified().toString().equalsIgnoreCase(JCPSServletApplication.REPO_STATUS_TO_MONITOR)) {
                return Collections.singletonMap("Status", "Repository is not in "
                        + JCPSServletApplication.REPO_STATUS_TO_MONITOR
                        + " state");
            }

            //Path of developer public key. 
            String repoPath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_DIR + "/"
                    + repoDetail.getRepoUserID() + "/"
                    + repoDetail.getRepoName();
            String pubKeyPath = repoPath + "/"
                    + repoDetail.getRepoUserID() + ".pub.key";

            //Check wheter encrypted secret already generated.
            databaseDAOAdapter.readWebhookEncSecretBlob(repoDetail.getRepoURL(),
                    repoPath + "/ran.enc.obj", statusMessage);
            if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {
                logger.info("Webhook secret has already been generated");
                resultsMap.put("SERVER_PUBKEY_FINGERPRINT", pubkeyFingerprint);
                resultsMap.put("Status", "Success");

                return resultsMap;
            } else if (statusMessage.getCode() == StatusMessage.Code.NOTFOUND) {

                //Generate encrypted secret.
                Util.getEncRandomBytes(WEBHOOK_SEC_LENGTH, pubKeyPath, repoDetail.getRepoURL(), statusMessage);
                if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                    logger.error("getWebHookSecurityAndServerPubKey:fail:" + statusMessage.getMessage());
                    return Collections.singletonMap(
                            "Status", "We are fixing it, please try after sometime");
                }
                //Update into database.
                databaseDAOAdapter.writeWebhookEncSecretBlob(repoDetail.getRepoURL(),
                        repoPath + "/ran.enc.obj", statusMessage);
                if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                    logger.error("getWebHookSecurityAndServerPubKey:fail:" + statusMessage.getMessage());
                    return Collections.singletonMap(
                            "Status", "We are fixing it, please try after sometime");
                }

                resultsMap.put("SERVER_PUBKEY_FINGERPRINT", pubkeyFingerprint);
                resultsMap.put("Status", "Success");

                return resultsMap;
            }else{
                logger.info(statusMessage.getMessage());
                return Collections.singletonMap(
                        "Status", "We are fixing it, please try after sometime");
            }
        } catch (UnexpectedInputLengthException ex) {
            logger.error("getWebHookSecurityAndServerPubKey:UnexpectedInputLengthException:", ex);
            return Collections.singletonMap(
                    "Status", "We are fixing it, please try after sometime");
        }
    }
}
