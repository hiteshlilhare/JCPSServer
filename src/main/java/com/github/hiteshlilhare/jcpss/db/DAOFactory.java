/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.db;

/**
 *
 * @author Hitesh
 */
public class DAOFactory {

    public final static String SQLITE = "sqlite";
    public final static String MYSQL = "mysql";

    public static DatabaseDAOAdapter getDatabaseDAO(String type) {

        if (MYSQL.equalsIgnoreCase(type)) {

            return new MySQLDAOImpl();

        } else {
            return new SQLLiteDAOImpl();
        }
    }
}
