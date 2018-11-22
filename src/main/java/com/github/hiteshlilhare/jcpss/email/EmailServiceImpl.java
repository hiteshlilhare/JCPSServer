package com.github.hiteshlilhare.jcpss.email;

import com.github.hiteshlilhare.jcpss.StatusMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import java.io.File;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;

/**
 * This class is copied from https://github.com/eugenp/tutorials as it is.
 * Created by Olga on 7/15/2016.
 *
 */
@Component
public class EmailServiceImpl implements EmailService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    @Autowired
    public JavaMailSender emailSender;

    @Override
    public void sendSimpleMessage(String to, 
            String subject, 
            String text, 
            StatusMessage statusMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            statusMessage.setCode(StatusMessage.Code.SUCCESS);
            statusMessage.setMessage("Email sent to user");
        } catch (MailException exception) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Faied to send email");
            logger.error("sendSimpleMessage",exception);
        }
    }

    @Override
    public void sendSimpleMessageUsingTemplate(String to,
            String subject,
            SimpleMailMessage template,
            StatusMessage statusMessage,
            String... templateArgs) {
        String text = String.format(template.getText(), templateArgs);
        sendSimpleMessage(to, subject, text, statusMessage);
    }

    @Override
    public void sendMessageWithAttachment(String to,
            String subject,
            String text,
            String pathToAttachment,
            StatusMessage statusMessage) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // pass 'true' to the constructor to create a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment("Invoice", file);
            emailSender.send(message);
            statusMessage.setCode(StatusMessage.Code.SUCCESS);
            statusMessage.setMessage("Email sent to user");
        } catch (MessagingException exception) {
            statusMessage.setCode(StatusMessage.Code.FAILURE);
            statusMessage.setMessage("Faied to send email");
            logger.error("sendMessageWithAttachment",exception);
        }
    }

}
