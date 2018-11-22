package com.github.hiteshlilhare.jcpss.email;

import com.github.hiteshlilhare.jcpss.StatusMessage;
import javax.mail.MessagingException;
import org.springframework.mail.SimpleMailMessage;


/**
 * This class is copied from https://github.com/eugenp/tutorials as it is.
 * Created by Olga on 8/22/2016.
 */

public interface EmailService {

    void sendSimpleMessage(String to,
                           String subject,
                           String text,
                           StatusMessage statusMessage);

    void sendSimpleMessageUsingTemplate(String to,
                                        String subject,
                                        SimpleMailMessage template,
                                        StatusMessage statusMessage,
                                        String ...templateArgs);

    void sendMessageWithAttachment(String to,
                                   String subject,
                                   String text,
                                   String pathToAttachment,
                                   StatusMessage statusMessage);

}