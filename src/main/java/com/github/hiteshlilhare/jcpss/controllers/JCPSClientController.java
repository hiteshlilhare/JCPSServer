/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import static com.github.hiteshlilhare.jcpss.JCPSServletApplication.REMOTE_REPO_URL;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import com.github.hiteshlilhare.jcpss.metadata.bean.CardAppMetaData;
import com.github.hiteshlilhare.jcpss.metadata.parser.CardAppXmlParser;
import static com.github.hiteshlilhare.jcpss.util.ZipUtility.zip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.LoggerFactory;
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
public class JCPSClientController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(
            JCPSClientController.class);

    @RequestMapping(value = "/gvrad", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> getVerifiedReleasedAppsDetail(@RequestBody String json) {
        HashMap<String, String> result = new HashMap<>();
        DatabaseDAOAdapter databaseDAOAdapter
                = DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        ArrayList<ReleasedApp> releasedApps = new ArrayList<>();
        StatusMessage statusMessage = new StatusMessage();
        databaseDAOAdapter.getVerifiedReleasedAppsDetail(releasedApps, statusMessage);
        if (statusMessage.getCode() == StatusMessage.Code.SUCCESS) {

            //1. Clone the Java Card Application Store into temp directory.
            File jcAppletStoreRepo = new File(JCPSSCOnstants.JCPS_SRV_DIR
                    + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR
                    + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
            Git git = null;
            try {
                FileUtils.deleteDirectory(jcAppletStoreRepo);
                if (jcAppletStoreRepo.exists()) {
                    logger.info("Unable to delete " + jcAppletStoreRepo
                            + " remote repo directory from temp");
                    Collections.singletonMap("Status",
                            "Please try after sometime");
                }
                git = Git.cloneRepository().setBranch("refs/heads/master")
                        .setURI(REMOTE_REPO_URL)
                        .setDirectory(jcAppletStoreRepo).call();
            } catch (GitAPIException ex) {
                logger.error("getVerifiedReleasedAppsDetail:Unable to clone "
                        + "java card applet store repository",
                        ex);
            } catch (IOException ex) {
                logger.error(
                        "getVerifiedReleasedAppsDetail:Unable to delete java "
                        + "card applet store local repository",
                        ex);
            } finally {
                if (git != null) {
                    git.close();
                }
            }
            //2. Get meta data about application.
            for (ReleasedApp releasedApp : releasedApps) {
                File xmlFileDir = new File(JCPSSCOnstants.JCPS_SRV_DIR
                        + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR
                        + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO
                        + "/" + releasedApp.getDeveloperId()
                        + "/" + releasedApp.getAppName()
                        + "/" + releasedApp.getVersion());
                File xmlFiles[] = xmlFileDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".xml");
                    }
                });
                if (xmlFiles.length > 0) {
                    try {
                        //Create the parser instance
                        CardAppXmlParser parser = new CardAppXmlParser();
                        //Parse the file
                        CardAppMetaData appMetaData = parser.parseXml(
                                new FileInputStream(xmlFiles[0]));
                        releasedApp.setAppMetaData(appMetaData);
                    } catch (FileNotFoundException ex) {
                        logger.info("getVerifiedReleasedAppsDetail", ex);
                    }
                }
            }
            //System.out.println(jsonFromPojo);
            Gson gsonBuilder = new GsonBuilder().create();
            String jsonFromPojo = gsonBuilder.toJson(releasedApps);
            result.put("Status", StatusMessage.Code.SUCCESS.toString());
            result.put("Apps", jsonFromPojo);
            return result;
        } else if (statusMessage.getCode() == StatusMessage.Code.NOTFOUND) {
            return Collections.singletonMap("Status", statusMessage.getMessage());
        } else {
            return Collections.singletonMap("Status", "Please try after sometime");
        }
    }

    @RequestMapping(value = "/gaa", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> getAppArtifacts(@RequestBody String json) {
        Gson gsonBuilder = new GsonBuilder().create();
        ReleasedApp releasedApp = gsonBuilder.fromJson(json, ReleasedApp.class);
        //1. Clone the Java Card Application Store into temp directory.
        File jcAppletStoreRepo = new File(JCPSSCOnstants.JCPS_SRV_DIR
                + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR
                + "/" + JCPSSCOnstants.JCPS_REMOTE_REPO);
        Git git = null;
        try {
            FileUtils.deleteDirectory(jcAppletStoreRepo);
            if (jcAppletStoreRepo.exists()) {
                logger.info("Unable to delete " + jcAppletStoreRepo
                        + " remote repo directory from temp");
                Collections.singletonMap("Status", "Please try after sometime");
            }
            git = Git.cloneRepository().setBranch("refs/heads/master")
                    .setURI(REMOTE_REPO_URL).setDirectory(jcAppletStoreRepo).call();
        } catch (GitAPIException ex) {
            logger.error("getAppArtifacts:Unable to clone java card applet store repository",
                    ex);
            return Collections.singletonMap("Status",
                    "Applet store configuration problem, please try after sometime");
        } catch (IOException ex) {
            logger.error(
                    "getAppArtifacts:Unable to delete java card applet store local repository",
                    ex);
            return Collections.singletonMap("Status",
                    "Applet store configuration problem, please try after sometime");
        } finally {
            if (git != null) {
                git.close();
            }
        }

        //2. Zip the required Application from cloned repo
        final String sourceFile = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_SRV_TEMP_DIR + "/"
                + JCPSSCOnstants.JCPS_REMOTE_REPO + "/"
                + releasedApp.getDeveloperId() + "/"
                + releasedApp.getAppName() + "/"
                + releasedApp.getVersion();
        final String destFile = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_SRV_TEMP_DIR + "/"
                + releasedApp.getDeveloperId() + "."
                + releasedApp.getAppName() + "."
                + releasedApp.getVersion() + ".zip";
        //Delete unwanted files.
        File sourceDir = new File(sourceFile);
        File[] buildScripts = sourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith(".bat")
                        || file.getName().endsWith(".bat.sig")
                        || file.getName().endsWith(".sh")
                        || file.getName().endsWith(".sh.sig")) {
                    return true;
                }
                return false;
            }
        });
        for (File buildScript : buildScripts) {
            try {
                FileUtils.forceDelete(buildScript);
            } catch (IOException ex) {
                logger.error("getAppArtifacts:Problem while deleting build scripts from application directory", ex);
                return Collections.singletonMap("Status",
                        "Application packing problem, please try after sometime");
            }
        }
        try {
            zip(sourceFile, destFile);
        } catch (IOException ex) {
            logger.error("getAppArtifacts:Problem while zipping requested application artifacts", ex);
            return Collections.singletonMap("Status",
                    "Application packing problem, please try after sometime");
        }
        return Collections.singletonMap("Status", StatusMessage.Code.SUCCESS.toString());
    }

    @RequestMapping(value = "/downloadAppZip", method = RequestMethod.GET)
    public void downloadAppZip(HttpServletRequest request,
            HttpServletResponse response) {
        String fileName = request.getParameter("fileName");
        if (fileName == null) {
            logger.info("'fileName' request parameter not found. "
                    + "Please check client query string");
            return;
        }
        String path = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_SRV_TEMP_DIR;
        OutputStream out = null;
        FileInputStream in = null;
        Path appZipPath = Paths.get(path, fileName);
        try {
            // Try to determine file's content type
            String contentType = request.getServletContext()
                    .getMimeType(appZipPath.toString());

            // Fallback to the default content type if type could not 
            // be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);

            out = response.getOutputStream();
            in = new FileInputStream(appZipPath.toFile());

            // copy from in to out
            IOUtils.copy(in, out);

        } catch (IOException ex) {
            logger.info("downloadAppZip:", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    //ignore.
                }
            }
            if (in != null) {
                try {
                    in.close();
                    //Delete the app zip file
                    FileUtils.forceDelete(appZipPath.toFile());
                } catch (IOException ex) {
                    //ignore.
                }
            }
        }

    }

}
