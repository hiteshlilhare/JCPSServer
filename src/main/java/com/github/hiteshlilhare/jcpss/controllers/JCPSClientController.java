/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.controllers;

import com.github.hiteshlilhare.jcpss.JCPSServletApplication;
import com.github.hiteshlilhare.jcpss.StatusMessage;
import com.github.hiteshlilhare.jcpss.bean.RepoDetail;
import com.github.hiteshlilhare.jcpss.db.DAOFactory;
import com.github.hiteshlilhare.jcpss.db.DatabaseDAOAdapter;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = "/getapps", method = RequestMethod.POST,
            produces = MediaType.ALL_VALUE)
    @ResponseBody
    public ResponseEntity getApps() throws IOException {
        //Show registered repo and there releases.
        DatabaseDAOAdapter databaseDAOAdapter = 
                DAOFactory.getDatabaseDAO(JCPSServletApplication.DATABASE);
        ArrayList<RepoDetail> repoDetails = new ArrayList<>();
        StatusMessage statusMessage = new StatusMessage();
//        databaseDAOAdapter.getGetAppsResponse(
//                repoDetails, statusMessage);
        logger.info(statusMessage.getMessage());
        if(statusMessage.getCode() != StatusMessage.Code.SUCCESS){
            
        }
        
        return null;
    }
}
