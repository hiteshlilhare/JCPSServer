package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.bean.TagCommitDetails;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.github.hiteshlilhare.jcpss.util.Util;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class NewAppReleaseRestController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(
            NewAppReleaseRestController.class);

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * Handles Release event of registered repository.
     *
     * @param json
     * @param headers
     * @param eventType
     */
    @RequestMapping(value = "/newrelease", method = RequestMethod.POST)
    public @ResponseBody
    void newRelease(@RequestBody String json,
            @RequestHeader HttpHeaders headers,
            @RequestHeader(value = "X-GitHub-Event") String eventType) {
        logger.info("GitHub-Event Type: " + eventType);
        if (eventType == null
                //|| !eventType.equalsIgnoreCase("create")
                || !eventType.equalsIgnoreCase("release")) {
            logger.info("newRelease webhook listens for new release creation.");
            return;
        }

        String signature = headers.getFirst("X-Hub-Signature");
        if (signature == null) {
            return;
        }
        if (signature.indexOf("=") == -1) {
            logger.info("Signature does not have '=' character,"
                    + " therefore unable to get signature");
            return;
        }
        signature = signature.substring(signature.indexOf("=") + 1);
        logger.info("Webhook Signature(HMACSHA1) From Github:" + signature);

        JsonParser jsonParser = new JsonParser();
        JsonObject releasePayloadJsonObj = jsonParser.parse(json).getAsJsonObject();

        JsonObject releaseJSONObject = releasePayloadJsonObj
                .getAsJsonObject("release");
        if (releaseJSONObject == null || releaseJSONObject.isJsonNull()) {
            logger.error("Event payload does not have release element/value");
            return;
        }

        //get Tags URL from repository element.
        com.google.gson.JsonObject repositoryJSONObject
                = releasePayloadJsonObj.getAsJsonObject("repository");
        if (repositoryJSONObject == null || repositoryJSONObject.isJsonNull()) {
            logger.error("Event payload does not have repository element");
            return;
        }

        try {
            GitHubRelease release = GitHubRelease.createGitHubReleaseBean(
                    releaseJSONObject);
            if (release.getPrerelease() == null) {
                logger.error("release json does not have prerelease element");
                return;
            } else if (Boolean.parseBoolean(release.getPrerelease())) {
                logger.error("Reported release is a prerelease");
                return;
            }

            // The secret will be generated on server and sent to client as encrypted by client's public key. 
            DatabaseDAOAdapter databaseDAOAdapter
                    = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            StatusMessage statusMessage = new StatusMessage();
            //Has To Be Done: Store the Key in TPM and ask TPM for returing HMACSHA1 for payload
            //Fix me: It was plan to store secret in TPM and ask TPM to verify the signature.
            //But short of time lead to this buggy implementation.
            databaseDAOAdapter.getWebhookSecret(release.getCloneUrl(), statusMessage);
            //!!!Warning:Do not print status message in case of success as it contains sec. 
            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                logger.info(statusMessage.getMessage());
                return;
            }
            if (statusMessage.getMessage() == null || statusMessage.getMessage().isEmpty()) {
                logger.info("Webhook secret not present!!!");
                return;
            }
            //String sectoken = "53b0d52f7230a0bd786d7467ced000894d79dbd5";//System.getenv("SECRET_TOKEN");

            String calculatedSignature = "";

            //calculatedSignature = calculateRFC2104HMAC(json, sectoken);
            calculatedSignature = calculateRFC2104HMAC(json, statusMessage.getMessage());
            statusMessage.setMessage(json);
            logger.info("Calculated Webhook Signature(HMACSHA1):'"
                    + calculatedSignature + "'");

            if (!calculatedSignature.equalsIgnoreCase(signature)) {
                logger.error("newRelease:fail:Payload signature does not match");
                return;
            }

            //Get RepoDetails.
            RepoDetail repoDetail = new RepoDetail();
            repoDetail.setRepoURL(release.getCloneUrl());

            databaseDAOAdapter.getRepoDetailsWRTRepoURL(repoDetail, statusMessage);
            logger.info(statusMessage.getMessage());

            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                return;
            }
            //Check repository status before checking release.
            if (!JCPSServletApplication.REPO_STATUS_TO_MONITOR
                    .equalsIgnoreCase(repoDetail.getVerified().toString())) {
                logger.info(repoDetail.getRepoURL() + " is not in "
                        + JCPSServletApplication.REPO_STATUS_TO_MONITOR + " state");
                return;
            }

            String basePath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_DIR + "/"
                    + repoDetail.getRepoUserID();

            //Clone the recent release.
            File repoCloneDir = new File(basePath + "/" + repoDetail.getRepoName()
                    + "/" + release.getTagname() + ".Clone");
            FileUtils.deleteDirectory(repoCloneDir);
            boolean status = repoCloneDir.mkdirs();
            if (!status) {
                logger.info("Unable to create " + repoCloneDir + " directory");
                return;
            }
            Git git = Git.cloneRepository().setBranch(release.getTagname())
                    .setURI(release.getCloneUrl()).setDirectory(repoCloneDir).call();

            //Verify the tag signature.
            //1. Get Signed Tag object.
            File tmpTagDir = File.createTempFile(repoDetail.getRepoName()
                    + "." + release.getTagname(), "");
            if (!tmpTagDir.delete()) {
                logger.error("Unable to create temporary directory to keep tag "
                        + "objects");
                return;
            }
            status = tmpTagDir.mkdirs();
            if (!status) {
                logger.info("Unable to create " + tmpTagDir.getAbsolutePath() + " directory");
                return;
            }
            String signedTagObject = tmpTagDir.getAbsolutePath() + "/"
                    + release.getTagname() + ".signed.tag";
            FileOutputStream outputStream = new FileOutputStream(signedTagObject);
            //Git hub cloning and tag Section is Inspired by:
            //https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/
            //org/dstadler/jgit/api/ReadTagFromName.java
            Repository repository = git.getRepository();
            try (RevWalk walk = new RevWalk(repository)) {
                /*List<Ref> call = git.tagList().call();
                for (Ref ref : call) {
                    System.out.println("Tag: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
                    if(ref.getName().contains(release.getTagname())){
                        simpleTag = ref;
                        break;
                    }
                }*/
                // get reference of annotated tag.
                Ref tag = repository.findRef("refs/tags/" + release.getTagname());
                if (null == tag) {
                    return;
                }
                //Checking whether Tag is Simple Tag or Annotated Tag.
                //If Simple Tag then return else continue.
                if (tag instanceof ObjectIdRef.PeeledNonTag) {
                    logger.info(release.getTagname() + " tag is not an annotated tag.");
                    return;
                }

                ObjectLoader loader = repository.open(tag.getObjectId());
                loader.copyTo(outputStream);
                outputStream.flush();
                outputStream.close();
                walk.dispose();
            } finally {
                //Free All the resources.
                git.close();
            }

            //2. Separate signature and tag from signed tag.
            String tagObjectFilePath = tmpTagDir.getAbsolutePath() + "/"
                    + release.getTagname() + ".tag";
            String signObjectFilePath = tmpTagDir.getAbsolutePath() + "/"
                    + release.getTagname() + ".sign";
            PrintWriter tagObject = new PrintWriter(tagObjectFilePath);
            PrintWriter signObject = new PrintWriter(signObjectFilePath);
            InputStream inputStream = new FileInputStream(signedTagObject);
            String text = null;

            try (final Reader reader = new InputStreamReader(inputStream)) {
                text = CharStreams.toString(reader);
                int index = text.lastIndexOf("-----BEGIN PGP SIGNATURE-----");
                String strTag = text.substring(0, index),
                        strSign = text.substring(index);
                tagObject.print(strTag);
                signObject.print(strSign);

            } finally {
                tagObject.close();
                signObject.close();
            }
            //3. verify signature
            logger.info("Verify tag signature...");
            String pubKeyFilePath = basePath + "/"
                    + repoDetail.getRepoName() + "/"
                    + repoDetail.getRepoUserID() + ".pub.key";
            logger.info("public key:" + pubKeyFilePath);
            Util.verifySignature(tagObjectFilePath,
                    signObjectFilePath, pubKeyFilePath, statusMessage);
            logger.info(statusMessage.getMessage());
            //4. Delete temporary folder.
            FileUtils.deleteDirectory(tmpTagDir);
            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                FileUtils.deleteDirectory(repoCloneDir);
                return;
            }
            //Create diretory for app store
            String appStoreDirPath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_STORE_DIR + "/"
                    + repoDetail.getRepoUserID() + "/" + repoDetail.getRepoName()
                    + "/" + release.getTagname();
            Util.createAppStoreGitDirectory(appStoreDirPath);
            //Insert into database.
            release.setTagSignatureVerified(true);
            release.setStatus(GitHubRelease.Status.PreVerified);
            databaseDAOAdapter = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
            databaseDAOAdapter.insert(release, statusMessage);
            logger.info(statusMessage.getMessage());
            if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                return;
            }
            //Update Anchor tag in repo_details table
            if (repoDetail.getAnchorTag() == null || repoDetail.getAnchorTag().isEmpty()) {
                repoDetail.setAnchorTag(release.getTagname());
                databaseDAOAdapter.updateField(repoDetail, RepoDetail.TableField.AnchorTag, statusMessage);
                logger.info(statusMessage.getMessage());
                if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                    return;
                }
            }
            //Notify client about new release.
        } catch (GitAPIException ex) {
            logger.error("newRelease:fail:GitAPIException", ex);
        } catch (IOException ex) {
            logger.error("newRelease:fail:IOException", ex);
        } catch (FieldNotPresentException ex) {
            logger.error("newRelease:fail:FieldNotPresentException", ex);
        } catch (SignatureException | NoSuchAlgorithmException
                | InvalidKeyException ex) {
            logger.error("newRelease:fail:", ex);
            return;
        }
    }

    /**
     * Fills the Tag commit details into TagCommitDetails bean.
     *
     * @param tagsURL
     * @param tagName
     * @param tagCommitDetails
     * @param statusMessage
     */
    private void getTagCommitDetails(String tagsURL, String tagName,
            TagCommitDetails tagCommitDetails,
            StatusMessage statusMessage) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(tagsURL);
            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler
                    = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws
                        ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity)
                                : null;
                    } else {
                        throw new ClientProtocolException(
                                "Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            //System.out.println("responseBody : " + responseBody);
            com.google.gson.JsonParser jsonParser
                    = new com.google.gson.JsonParser();
            com.google.gson.JsonArray tagsJsonArray
                    = jsonParser.parse(responseBody).getAsJsonArray();
            //ArrayList<GitHubRelease> gitHubReleases = new ArrayList<>();
            if (tagsJsonArray.size() == 0) {
                logger.info("Repository has no tags ever!!!");
                statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                statusMessage.setMessage("Repository has no tags ever!!!");
            } else {
                String commitURL = null;
                for (JsonElement tagJsonElement : tagsJsonArray) {
                    if (!tagJsonElement.isJsonNull()) {
                        JsonObject tagJsonObj = tagJsonElement.getAsJsonObject();
                        JsonElement tagNameJsonElement = tagJsonObj.get("name");
                        if (tagNameJsonElement != null
                                && !tagNameJsonElement.isJsonNull()) {
                            if (tagNameJsonElement.getAsString()
                                    .equalsIgnoreCase(tagName)) {
                                JsonElement commitJsonObject
                                        = tagJsonObj.get("commit");
                                if (commitJsonObject != null
                                        && !commitJsonObject.isJsonNull()) {
                                    JsonElement commitURLJsonObject
                                            = commitJsonObject.getAsJsonObject().get("url");
                                    if (commitURLJsonObject != null && !commitURLJsonObject.isJsonNull()) {
                                        commitURL = commitURLJsonObject.getAsString();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (commitURL != null) {
                    httpget = new HttpGet(commitURL);
                    responseBody = httpclient.execute(httpget, responseHandler);
                    JsonObject commitJSONObject
                            = jsonParser.parse(responseBody).getAsJsonObject();
                    TagCommitDetails.getTagCommitDetails(commitJSONObject,
                            tagCommitDetails);
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Successfully get tag commit details");
                } else {
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Unable to find commit URL w.r.t. "
                            + tagName + " tag");
                }

            }
        } catch (IOException ex) {
            logger.error("getTagCommitDetails:fail:IOException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage(ex.getMessage());
        } catch (FieldNotPresentException ex) {
            logger.error("getTagCommitDetails:fail:FieldNotPresentException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage(ex.getMessage());
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.info("getTagCommitDetails: Exception happened "
                        + "while closing httpclient connection");
            }
        }
    }

    /**
     * Method to convert byte array to hex string.
     *
     * @param bytes
     * @return
     */
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * Returns HMAC SHA1 hash of <code>data</code> w.r.t. <code>key</code>
     *
     * @param data
     * @param key
     * @return
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException,
            InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
                HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

}
//@GetMapping ("/rbuild")
//ruby -rsecurerandom -e 'puts SecureRandom.hex(20)'
//    public @ResponseBody void newRelease(
//        HttpServletRequest request, 
    //        HttpServletResponse response) {
