/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.db;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hitesh
 */
public class SQLLiteDAOImpl extends DatabaseDAOAdapter {

    public final static String JCPS_SRV_DB_FILE = "jcpsssqlite.db";
    public final static String JCPS_SRV_DB_URL = "jdbc:sqlite:" + JCPSSCOnstants.JCPS_SRV_DIR
            + "/" + JCPSSCOnstants.JCPS_SRV_DB_DIR + "/" + JCPS_SRV_DB_FILE;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SQLLiteDAOImpl.class);

    @Override
    protected DBConnection connect() {
        DBConnection connection = new DBConnection();
        try {
            // create a connection to the database
            connection.setConnection(DriverManager.getConnection(JCPS_SRV_DB_URL));
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
