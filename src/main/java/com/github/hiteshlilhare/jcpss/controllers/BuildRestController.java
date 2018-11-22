/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.email.EmailServiceImpl;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class BuildRestController {

    @Autowired
    public EmailServiceImpl emailService;
    
    @Autowired
    private JavaMailSender sender;



    @Value("${:classpath:/static/error.html}")
    private Resource defaultErrorPage;

    //@GetMapping ("/rbuild")
    //@RequestMapping(method = RequestMethod.GET)
    @RequestMapping("/rbuild")
    Map<String, String> reproducibleBuild() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "upsert.bat");
            File dir = new File("C:/Program Files/salesforce.com/Data Loader/cliq_process/upsert");
            pb.directory(dir); Process p = pb.start();
        } catch (IOException ex) {
            Logger.getLogger(BuildRestController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.singletonMap("Status", "Request for build...");
    }
    //Another way to send mail.
    @RequestMapping("/sendMailAtt")
    public String sendMailAttachment() throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
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
