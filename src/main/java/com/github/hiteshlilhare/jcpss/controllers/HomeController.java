/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.controllers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hitesh
 */
@RestController
public class HomeController {
    @Value("${:classpath:/static/index.html}")
    private Resource index;

    @Value("${:classpath:/static/manager.html}")
    private Resource manager;

    @GetMapping(value = {"/"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity actions() throws IOException {
        CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
        return ResponseEntity.ok().cacheControl(cacheControl).
                body(new InputStreamResource(index.getInputStream()));
    }

    @GetMapping(value = {"/manager"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity manage() throws IOException {
        CacheControl cacheControl = CacheControl.maxAge(0, TimeUnit.SECONDS);
        return ResponseEntity.ok().cacheControl(cacheControl).
                body(new InputStreamResource(manager.getInputStream()));
    }
}
