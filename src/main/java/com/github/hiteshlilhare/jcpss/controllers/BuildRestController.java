/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.ReleasedApp;
import com.github.hiteshlilhare.jcpss.email.EmailServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class BuildRestController {

    private static final org.slf4j.Logger logger
            = LoggerFactory.getLogger(BuildRestController.class);
    @Autowired
    public EmailServiceImpl emailService;

    @Autowired
    private JavaMailSender sender;

    @Value("${:classpath:/static/error.html}")
    private Resource defaultErrorPage;

    //@GetMapping ("/rbuild")
    //@RequestMapping(method = RequestMethod.GET)
    @RequestMapping("/rbuild")
    Map<String, String> reproducibleBuild(@RequestBody String json) {
        Gson gsonBuilder = new GsonBuilder().create();
        ReleasedApp releasedApp = gsonBuilder.fromJson(json, ReleasedApp.class);
        try {
            //1. Clone source code from original repository
            File tempCloneDir = new File(JCPSSCOnstants.JCPS_SRV_DIR
                    + "/" + JCPSSCOnstants.JCPS_SRV_TEMP_DIR
                    + "/" + releasedApp.getDeveloperId()
                    + "." + releasedApp.getAppName()
                    + "." + releasedApp.getVersion());
            if(tempCloneDir.exists()){
                FileUtils.forceDelete(tempCloneDir);
            }
            FileUtils.forceMkdir(tempCloneDir);
            Git git = Git.cloneRepository().setBranch(releasedApp.getVersion())
                    .setURI(releasedApp.getSourceCloneURL())
                    .setDirectory(tempCloneDir).call();
            git.close();
            //2. Build application 
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "build.bat");
            pb.directory(tempCloneDir);
            Process p = pb.start();
        } catch (IOException | GitAPIException ex) {
            logger.info("reproducibleBuild", ex);
            return Collections.singletonMap("Status",
                    StatusMessage.Code.FAILURE.toString());
        }
        return Collections.singletonMap("Status",
                StatusMessage.Code.SUCCESS.toString());
    }

    //Another way to send mail.
    @RequestMapping("/sendMailAtt")
    public String sendMailAttachment() throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        try {
            helper.setTo("hitesh.lilhare@gmail.com");
            helper.setText("Greetings :)\n Please find the attached docuemnt for your reference.");
            helper.setSubject("Mail From Spring Boot");
            ClassPathResource file = new ClassPathResource("document.PNG");
            helper.addAttachment("document.PNG", file);
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error while sending mail ..";
        }
        sender.send(message);
        return "Mail Sent Success!";
    }

    @GetMapping(value = {"/error"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity errorPage() throws IOException {
        CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
        return ResponseEntity.ok().cacheControl(cacheControl).
                body(new InputStreamResource(defaultErrorPage.getInputStream()));
    }
}
