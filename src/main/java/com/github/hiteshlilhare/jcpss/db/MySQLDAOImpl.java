/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hitesh
 */
public class MySQLDAOImpl extends DatabaseDAOAdapter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MySQLDAOImpl.class);

    @Override
    protected DBConnection connect() {
        DBConnection connection = new DBConnection();
        try {
            // create a connection to the database
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/jcpss", "hitesh", "Mysql@1981");
            connection.setConnection(con);
            connection.setStatus(connection.getConnection() != null);
        } catch (SQLException ex) {
            logger.error("Unable to connect to database", ex);
            connection.setStatus(false);
        }
        return connection;
    }

    @Override
    protected void disconnect(DBConnection connection) {
        if (connection != null) {
            connection.setStatus(false);
            if (connection.getConnection() != null) {
                try {
                    connection.getConnection().close();
                } catch (SQLException ex) {
                    logger.info("disconnect():", ex);
                }
            }
        }
    }

}
