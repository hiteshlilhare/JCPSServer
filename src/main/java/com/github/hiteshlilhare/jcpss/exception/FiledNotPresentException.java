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
public class FiledNotPresentException extends Exception {

    public FiledNotPresentException(String s) {
        super(s);
    }
    public FiledNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }
}
