/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.util;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.DeveloperGPGPublicKey;
import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.google.common.io.CharStreams;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

/**
 *
 * @author Hitesh
 */
public class Util {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Util.class);

    public static final int NO_OF_BYTES = 20;
    public static final String ENC_RANDOM_FILE_NAME = "ran.enc.obj";

    @Value("${:classpath:/build.bat}")
    private static Resource buildBatFile;

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };// Operating systems.

    private static OS os = null;

    /**
     * This method is inspired from below mentioned stackoverflow post.
     * https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java.
     *
     * @return
     */
    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }

    /**
     * Converts DateTime string to LocalDateTime.
     *
     * @param strDateTime
     * @return
     */
    public static LocalDateTime getLocalDateTime(String strDateTime) {
        try {
            return getLocalDateTime(strDateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        } catch (ParseException ex) {
            try {
                return getLocalDateTime(strDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'");
            } catch (ParseException ex1) {
                logger.error("getLocalDateTime:fail:ParseException", ex1);
            }
        }
        return null;
    }

    private static LocalDateTime getLocalDateTime(String strDateTime,
            String format) throws ParseException {
        SimpleDateFormat localFormat = new SimpleDateFormat(format, Locale.US);
        Date date = localFormat.parse(strDateTime);
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    }

    public static String getHTMLSuccessMessage(String msg) {
        return "<span style='color:green'>" + msg + "</span>";
    }

    public static String getHTMLErrorMessage(String msg) {
        return "<span style='color:red'>" + msg + "</span>";
    }

    public static String getHTMLWarningMessage(String msg) {
        return "<span tyle='color:yellow'>" + msg + "</span>";
    }

    /**
     * Returns <code>numOfBytes</code> number of random bytes
     *
     * @param numOfBytes
     * @param statusMessage
     */
    public static void getRandomBytes(int numOfBytes, StatusMessage statusMessage) {
        try {
            byte[] b = new byte[numOfBytes];
            switch (Util.getOS()) {
                case WINDOWS:
                    SecureRandom secRan = SecureRandom.getInstance("Windows-PRNG"); // Default constructor would have returned insecure SHA1PRNG algorithm, so make an explicit call.
                    secRan.nextBytes(b);
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage(HexUtils.toHexString(b));
                    break;
                default:
                    secRan = new SecureRandom(); // In Unix like systems, default constructor uses NativePRNG, seeded by securerandom.source property
                    secRan.nextBytes(b);
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage(HexUtils.toHexString(b));
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.error("getRandomBytes:fail:NoSuchAlgorithmException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("NoSuchAlgorithmException:" + ex.getMessage());
        }
    }

    /**
     * Returns <code>numOfBytes</code> number of encrypted random bytes
     *
     * @param numOfBytes
     * @param statusMessage
     */
    public static void getEncRandomBytes(int numOfBytes, String pubKeyPath,
            String repoURL, StatusMessage statusMessage) {
        try {
            byte[] b = new byte[numOfBytes];
            byte[] enc_b = new byte[numOfBytes];
            File pubKey = new File(pubKeyPath);
            File ranEnc = new File(pubKey.getParentFile().getPath() + "/ran.enc.obj");
            DatabaseDAOAdapter databaseDAOAdapter
                    = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);

            switch (Util.getOS()) {
                case WINDOWS:
                    SecureRandom secRan = SecureRandom.getInstance("Windows-PRNG"); // Default constructor would have returned insecure SHA1PRNG algorithm, so make an explicit call.
                    secRan.nextBytes(b);
                    databaseDAOAdapter.updateWebhookSecret(repoURL, HexUtils.toHexString(b), statusMessage);
                    if (statusMessage.getCode() != StatusMessage.Code.SUCCESS) {
                        return;
                    }
                    //File ran = File.createTempFile("ran.", "");
                    //File ran = new File(pubKey.getParentFile().getPath() + "/ran.obj");
                    //FileOutputStream fos = new FileOutputStream(ran);
                    //fos.write(b);
                    //fos.flush();
                    //fos.close();
                    //File ranEnc = File.createTempFile("ran.enc.", "");

                    encryptFile(ranEnc.getAbsolutePath(),
                            b, pubKeyPath, false, true);
                    //FileInputStream fis = new FileInputStream(ranEnc);
                    //int count = numOfBytes;
                    //int r = 0;
                    //while (count > 0) {
                    //    r += fis.read(enc_b, r, count);
                    //    count = count - r;
                    //}
                    //fis.close();
                    //System.out.println("EncFileSize: " + ranEnc.length());
                    //System.out.println("Ran File Size: " + ran.length());
                    //ranEnc.delete();
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Successfully generated random number");
                    break;
                default:
                    secRan = new SecureRandom(); // In Unix like systems, default constructor uses NativePRNG, seeded by securerandom.source property
                    secRan.nextBytes(b);
                    encryptFile(ranEnc.getAbsolutePath(),
                            b, pubKeyPath, false, true);
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Successfully generated random number");
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.error("getRandomBytes:fail:NoSuchAlgorithmException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("NoSuchAlgorithmException:" + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PGPException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isDeveloperPublicKeyValid(DeveloperGPGPublicKey gpgPublicKey,
            RepoDetail repoDetail) {
        String givenKeyId = repoDetail.getSignKeyFgrPrint();
        String fetchedKeyId = gpgPublicKey.getKeyID();
        if (givenKeyId.startsWith("0x") || givenKeyId.startsWith("0X")) {
            givenKeyId = givenKeyId.substring(2);
        }

        if (fetchedKeyId.startsWith("0x") || fetchedKeyId.startsWith("0X")) {
            fetchedKeyId = fetchedKeyId.substring(2);
        }

        if (gpgPublicKey.getKeyID() != null
                && givenKeyId.equalsIgnoreCase(fetchedKeyId)
                && gpgPublicKey.getExpiresAt() != null
                && !gpgPublicKey.isExpired()
                && gpgPublicKey.isEmailIdExists(repoDetail.getEmail())
                && gpgPublicKey.isEmailIdVerified(repoDetail.getEmail())
                && gpgPublicKey.canKeyCertify()
                && gpgPublicKey.canKeySign()
                && gpgPublicKey.getSubkeyforEncryption() != null) {
            return true;
        }
        return false;
    }

    public static boolean createDirectoryStructureIfNotExist() {
        boolean flag = true;
        //Create server base directory if not exist.
        File appStoreBaseDir = new File(JCPSSCOnstants.JCPS_SRV_DIR);
        //Create Base directory.
        if (!appStoreBaseDir.exists() || appStoreBaseDir.isFile()) {
            if (!appStoreBaseDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR + " directory.");
                return flag;
            }
        }
        //Create App Directory if not exists.
        File appDir = new File(JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_APPS_DIR);
        if (!appDir.exists() || appDir.isFile()) {
            if (!appDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_APPS_DIR + " directory.");
                return flag;
            }
        }
        //Create Database Directory if not exists.
        File dbDir = new File(JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_DB_DIR);
        if (!dbDir.exists() || dbDir.isFile()) {
            if (!dbDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_DB_DIR + " directory.");
                return flag;
            }
        }
        //Create temp Directory if not exists.
        File tempDir = new File(JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR);
        if (!tempDir.exists() || tempDir.isFile()) {
            if (!tempDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR + " directory.");
                return flag;
            }
        }
        //Create appstore Directory if not exists.
        File appStoreDir = new File(JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_APPS_STORE_DIR);
        if (!appStoreDir.exists() || appStoreDir.isFile()) {
            if (!appStoreDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR + "/" + JCPSSCOnstants.JCPS_SRV_APPS_STORE_DIR + " directory.");
                return flag;
            }
        }
        //Create JCAppletStore Directory if not exists.
        File remoteRepoDir = new File(JCPSSCOnstants.JCPS_SRV_DIR 
                + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
        if (!remoteRepoDir.exists() || remoteRepoDir.isFile()) {
            if (!remoteRepoDir.mkdir()) {
                flag = false;
                logger.info("Unable to create " + JCPSSCOnstants.JCPS_SRV_DIR 
                        + "/" + JCPSSCOnstants.JCPS_SRV_APPS_STORE_DIR + " directory.");
                return flag;
            }
        }

        if (flag) {
            logger.info("Directory structure created...");
        }
        return flag;
    }

    /**
     * Fills public key as status message with status code SUCCESS.
     *
     * @param keyId_Or_Fingerpring
     */
    public static void getPublicKey(String keyId_Or_Fingerpring, StatusMessage statusMessage) {
        if (keyId_Or_Fingerpring == null) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("");
            return;
        } else {
            keyId_Or_Fingerpring = keyId_Or_Fingerpring.replaceAll("\\s+", "");
            if (!keyId_Or_Fingerpring.startsWith("0x") && !keyId_Or_Fingerpring.startsWith("0X")) {
                keyId_Or_Fingerpring = "0x" + keyId_Or_Fingerpring;
            }
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://pool.sks-keyservers.net/pks/lookup?op=get&search=" + keyId_Or_Fingerpring + "&options=mr'");

            logger.info("Executing request " + httpget.getRequestLine());

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
            String publicKey = "-----" + responseBody.substring(responseBody.indexOf("BEGIN PGP PUBLIC KEY BLOCK"),
                    responseBody.indexOf("END PGP PUBLIC KEY BLOCK") + "END PGP PUBLIC KEY BLOCK".length()) + "-----";
            //System.out.println("Public Key : \n" + publicKey);
            statusMessage.setCode(StatusMessage.Code.SUCCESS);
            statusMessage.setMessage(publicKey);
        } catch (IOException ex) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Exception:Unable to get public key w.r.t. key id/fingerprint"
                    + System.lineSeparator() + ex.getMessage());
            logger.error("getPublicKey:fail:Unable to get public key w.r.t. key id/fingerprint", ex);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.info("IOException while closing HttpClient connection");
            }
        }
    }

    /**
     * Returns list of github releases with respect to release url.
     *
     * @param releaseURL
     * @param anchorTag
     * @param statusMessage
     * @return
     */
    public static ArrayList<GitHubRelease> getListOfGitRepoReleases(String releaseURL,
            String anchorTag,
            StatusMessage statusMessage) {
        ArrayList<GitHubRelease> gitHubReleases = new ArrayList<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(releaseURL);

            logger.info("Executing request " + httpget.getRequestLine());

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
            com.google.gson.JsonArray releases = jsonParser.parse(responseBody).getAsJsonArray();
            //ArrayList<GitHubRelease> gitHubReleases = new ArrayList<>();
            if (releases.size() == 0) {
                logger.info("Repository has no releases!!!");
                statusMessage.setCode(StatusMessage.Code.NOTEXIST);
                statusMessage.setMessage("Repository has no releases!!!");
            } else {
                for (int i = 0; i < releases.size(); i++) {
                    GitHubRelease release = GitHubRelease.createGitHubReleaseBean(releases.get(i).getAsJsonObject());
                    if (anchorTag.equalsIgnoreCase(release.getTagname())) {
                        break;
                    }
                    if (release.getPrerelease() == null
                            || Boolean.parseBoolean(release.getPrerelease())) {
                        continue;
                    }
                    gitHubReleases.add(release);
                }
                if (gitHubReleases.isEmpty()) {
                    statusMessage.setCode(StatusMessage.Code.NOTFOUND);
                    statusMessage.setMessage("Repository has no releases later than " + anchorTag);
                } else {
                    statusMessage.setCode(StatusMessage.Code.SUCCESS);
                    statusMessage.setMessage("Repository has " + gitHubReleases.size()
                            + " releases later than " + anchorTag);
                }
            }
            logger.info(gitHubReleases.toString());
        } catch (IOException ex) {
            logger.error("IOException:getListOfGitRepoReleases:fail", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("IOException: Unable to get github releases");
        } catch (FieldNotPresentException ex) {
            logger.error("FieldNotPresentException:getListOfGitRepoReleases:fail", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("FieldNotPresentException: Unable to get github releases");
        } catch (JsonSyntaxException ex) {
            logger.error("JsonSyntaxException:getListOfGitRepoReleases:fail", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("JsonSyntaxException: Unable to get github releases");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.info("IOException:getListOfGitRepoReleases:fail: "
                        + "While closing httpclient connection");
            }
        }
        return gitHubReleases;
    }

    public static void validateRelease(GitHubRelease release,
            RepoDetail repoDetail,
            StatusMessage statusMessage) {
        String basePath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_SRV_TEMP_DIR + "/"
                + repoDetail.getRepoUserID();
        File repoCloneDir = new File(basePath + "/" + repoDetail.getRepoName()
                + "/" + release.getTagname() + ".Clone");
        try {
            //Clone the recent release.

            FileUtils.deleteDirectory(repoCloneDir);
            boolean status = repoCloneDir.mkdirs();
            if (!status) {
                logger.info("Unable to create " + repoCloneDir + " directory");
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Unable to create " + repoCloneDir + " directory");
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
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Unable to create temporary directory to keep tag "
                        + "objects");
                return;
            }
            status = tmpTagDir.mkdirs();
            if (!status) {
                logger.info("Unable to create " + tmpTagDir.getAbsolutePath() + " directory");
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Unable to create " + tmpTagDir.getAbsolutePath() + " directory");
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
                    logger.info("Unable to get tag ref w.r.t " + release.getTagname());
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage("Unable to get tag ref w.r.t " + release.getTagname());
                    return;
                }
                //Checking whether Tag is Simple Tag or Annotated Tag.
                //If Simple Tag then return else continue.
                if (tag instanceof ObjectIdRef.PeeledNonTag) {
                    logger.info(release.getTagname() + " tag is not an annotated tag.");
                    statusMessage.setCode(StatusMessage.Code.FAILURE);
                    statusMessage.setMessage(release.getTagname() + " tag is not an annotated tag.");
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
            String pubKeyFilePath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_DIR + "/"
                    + repoDetail.getRepoUserID() + "/"
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
            statusMessage.setCode(StatusMessage.Code.SUCCESS);
            statusMessage.setMessage("Successfully validated "
                    + release.getTagname() + " tag");
            String appStoreDirPath = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_STORE_DIR + "/"
                    + repoDetail.getRepoUserID() + "/" + repoDetail.getRepoName()
                    + "/" + release.getTagname();
            createAppStoreGitDirectory(appStoreDirPath);
        } catch (GitAPIException ex) {
            logger.error("newRelease:fail:GitAPIException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("GitAPIException: Failed to validate "
                    + release.getTagname() + " tag");
        } catch (IOException ex) {
            logger.error("newRelease:fail:IOException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("IOException: Failed to validate "
                    + release.getTagname() + " tag");
        }
    }

    public static void createAppStoreGitDirectory(String appStoreDirPath)
            throws IllegalStateException, GitAPIException, IOException {
        //Create diretory for app store
        File appStoreDir = new File(appStoreDirPath);
        boolean flag = appStoreDir.mkdirs();
        if (!flag) {
            logger.info("Unable to create corresponding app store directory.");
        } else {

            //Copy default bat/sh file.
            String buildFileName = buildBatFile.getFilename();
            PrintWriter out = null;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(buildBatFile.getInputStream()))) {
                out = new PrintWriter(appStoreDirPath + "/" + buildFileName);
                String line;
                while ((line = reader.readLine()) != null) {
                    out.println(line);
                }
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    /**
     * verify the signature in in against the file fileName.
     *
     * @param fileName
     * @param inputFileName
     * @param keyFileName
     * @param statusMessage
     */
    public static void verifySignature(
            String fileName,
            String inputFileName,
            String keyFileName,
            StatusMessage statusMessage) {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(inputFileName));
            InputStream keyIn = new BufferedInputStream(new FileInputStream(keyFileName));
            in = PGPUtil.getDecoderStream(in);

            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(in);
            PGPSignatureList p3;

            Object o = pgpFact.nextObject();
            if (o instanceof PGPCompressedData) {
                PGPCompressedData c1 = (PGPCompressedData) o;

                pgpFact = new JcaPGPObjectFactory(c1.getDataStream());

                p3 = (PGPSignatureList) pgpFact.nextObject();
            } else {
                p3 = (PGPSignatureList) o;
            }

            PGPPublicKeyRingCollection pgpPubRingCollection = new PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

            InputStream dIn = new BufferedInputStream(new FileInputStream(fileName));

            PGPSignature sig = p3.get(0);
            PGPPublicKey key = pgpPubRingCollection.getPublicKey(sig.getKeyID());

            sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);

            int ch;
            while ((ch = dIn.read()) >= 0) {
                sig.update((byte) ch);
            }

            dIn.close();
            in.close();
            keyIn.close();

            if (sig.verify()) {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Signature verified");
            } else {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Signature verification failed");
            }
        } catch (FileNotFoundException ex) {
            logger.error("verifySignature:fail:FileNotFoundException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("FileNotFoundException:Signature verification failed");
        } catch (IOException ex) {
            logger.error("verifySignature:fail:IOException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("IOException:Signature verification failed");
        } catch (PGPException ex) {
            logger.error("verifySignature:fail:PGPException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("PGPException:Signature verification failed");
        }
    }

    /**
     * verify the passed in file as being correctly signed.
     *
     * @param in
     * @param keyIn
     * @param statusMessage
     */
    public static void verifyFile(
            InputStream in,
            InputStream keyIn,
            StatusMessage statusMessage) {
        try {
            in = PGPUtil.getDecoderStream(in);

            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(in);

            PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();

            pgpFact = new JcaPGPObjectFactory(c1.getDataStream());

            PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) pgpFact.nextObject();

            PGPOnePassSignature ops = p1.get(0);

            PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();

            InputStream dIn = p2.getInputStream();
            int ch;
            PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

            PGPPublicKey key = pgpRing.getPublicKey(ops.getKeyID());
            FileOutputStream out = new FileOutputStream(p2.getFileName());
            System.out.println(new File(p2.getFileName()).getAbsolutePath());
            ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);

            while ((ch = dIn.read()) >= 0) {
                ops.update((byte) ch);
                out.write(ch);
            }

            out.close();

            PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();

            if (ops.verify(p3.get(0))) {
                statusMessage.setCode(StatusMessage.Code.SUCCESS);
                statusMessage.setMessage("Signature verified");
            } else {
                statusMessage.setCode(StatusMessage.Code.FAILURE);
                statusMessage.setMessage("Signature verification failed");
            }
        } catch (FileNotFoundException ex) {
            logger.error("verifyFile:fail:FileNotFoundException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("FileNotFoundException:Signature verification failed");
        } catch (IOException ex) {
            logger.error("verifyFile:fail:IOException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("IOException:Signature verification failed");
        } catch (PGPException ex) {
            logger.error("verifyFile:fail:PGPException", ex);
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("PGPException:Signature verification failed");
        }
    }

    /**
     * Encrypts file with given key.
     *
     * @param outputFileName
     * @param inputData
     * @param inputFileName
     * @param encKeyFileName
     * @param armor
     * @param withIntegrityCheck
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws PGPException
     */
    public static void encryptFile(
            String outputFileName,
            byte[] inputData,
            String encKeyFileName,
            boolean armor,
            boolean withIntegrityCheck)
            throws IOException, NoSuchProviderException, PGPException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFileName));
        PGPPublicKey encKey = PGPUtility.readPublicKey(encKeyFileName);
        encryptFile(out, inputData, encKey, armor, withIntegrityCheck);
        out.close();
    }

    /**
     * Encrypts the file. Inspired by:
     * https://stackoverflow.com/questions/40535236/bouncycastle-openpgp-encrypt-byte-array-as-csv-file
     *
     * @param out
     * @param fileName
     * @param encKey
     * @param armor
     * @param withIntegrityCheck
     * @throws IOException
     * @throws NoSuchProviderException
     */
    private static void encryptFile(
            OutputStream out,
            byte[] inputBytes,
            PGPPublicKey encKey,
            boolean armor,
            boolean withIntegrityCheck)
            throws IOException, NoSuchProviderException {
        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
            OutputStream cos = comData.open(bOut);
            PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

            OutputStream pOut = lData.open(cos, PGPLiteralData.BINARY, "secret", inputBytes.length, new Date());
            pOut.write(inputBytes);

            lData.close();
            comData.close();

            byte[] bytes = bOut.toByteArray();

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider("BC"));

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

            OutputStream cOut = encGen.open(out, bytes.length);

            cOut.write(bytes);
            cOut.close();

            if (armor) {
                out.close();
            }
        } catch (PGPException e) {
            System.err.println(e);
            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }

    private static void encryptFile(
            String outputFileName,
            String inputFileName,
            String encKeyFileName,
            boolean armor,
            boolean withIntegrityCheck)
            throws IOException, NoSuchProviderException, PGPException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFileName));
        PGPPublicKey encKey = PGPUtility.readPublicKey(encKeyFileName);
        encryptFile(out, inputFileName, encKey, armor, withIntegrityCheck);
        out.close();
    }

    private static void encryptFile(
            OutputStream out,
            String fileName,
            PGPPublicKey encKey,
            boolean armor,
            boolean withIntegrityCheck)
            throws IOException, NoSuchProviderException {
        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        try {
            byte[] bytes = PGPUtility.compressFile(fileName, CompressionAlgorithmTags.ZIP);

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider("BC"));

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

            OutputStream cOut = encGen.open(out, bytes.length);

            cOut.write(bytes);
            cOut.close();

            if (armor) {
                out.close();
            }
        } catch (PGPException e) {
            System.err.println(e);
            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }
}
