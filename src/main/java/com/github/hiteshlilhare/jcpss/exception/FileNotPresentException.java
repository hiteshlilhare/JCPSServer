/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.exception;

/**
 *
 * @author Hitesh
 */
public class FileNotPresentException extends Exception {

    public FileNotPresentException(String s) {
        super(s);
    }
    public FileNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }
}
