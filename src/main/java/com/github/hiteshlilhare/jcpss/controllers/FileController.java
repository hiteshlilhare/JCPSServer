package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.exception.FieldNotPresentException;
import com.github.hiteshlilhare.jcpss.exception.FileNotPresentException;
import com.github.hiteshlilhare.jcpss.exception.UnexpectedInputLengthException;
import com.github.hiteshlilhare.jcpss.util.Util;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class FileController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(
            FileController.class);

    @Value("${:classpath:/keys/jcps.server.pub.gpg}")
    private Resource serverPubKey;

    @Value("${:classpath:/static/resource_error.json}")
    private Resource resourceError;

    /**
     * Handles downloading of encrypted random number.
     *
     * @param json
     * @param request
     * @return
     */
    @RequestMapping(value = "/downloadEncRand", method = RequestMethod.POST)
    public ResponseEntity<Resource> downloadEncRand(@RequestBody String json,
            HttpServletRequest request) {
        try {
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);

            // Load file as Resource
            String path = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                    + JCPSSCOnstants.JCPS_SRV_APPS_DIR + "/"
                    + repoDetail.getRepoUserID() + "/"
                    + repoDetail.getRepoName();
            String fileName = Util.ENC_RANDOM_FILE_NAME;
            Resource resource = Util.loadFileAsResource(path, fileName);
            // Try to determine file's content type
            String contentType = request.getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());

            // Fallback to the default content type if type could not 
            // be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            logger.info("downloadEncRand:IOException:Could not determine file type.");
        } catch (FileNotPresentException | UnexpectedInputLengthException ex) {
            logger.info("downloadEncRand:IOException:" + ex.getMessage());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/json"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resourceError.getFilename() + "\"")
                .body(resourceError);
    }

    @RequestMapping(value = "/downloadServPubkey", method = RequestMethod.POST)
    public ResponseEntity<Resource> downloadServPubkey(@RequestBody String json,
            HttpServletRequest request) {
        try {
            RepoDetail repoDetail = RepoDetail.createRepoDetailBean(json);

            // Try to determine file's content type
            String contentType = request.getServletContext()
                    .getMimeType(serverPubKey.getFile().getAbsolutePath());

            // Fallback to the default content type if type could not 
            // be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + serverPubKey.getFilename() + "\"")
                    .body(serverPubKey);
        } catch (IOException ex) {
            logger.info("downloadServPubkey:IOException:Could not determine file type.");
        } catch (UnexpectedInputLengthException ex) {
            logger.info("downloadServPubkey:UnexpectedInputLengthException:" + ex.getMessage());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/json"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resourceError.getFilename() + "\"")
                .body(resourceError);
    }
}
