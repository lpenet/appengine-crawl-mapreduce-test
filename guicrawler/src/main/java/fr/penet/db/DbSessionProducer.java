/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.db;

import com.google.appengine.api.utils.SystemProperty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Named
@ApplicationScoped
@Log
public class DbSessionProducer {
    DbUtils dbUtils = new DbUtils();
    
    @Produces
    @RequestScoped
    public Connection createConnection() {
        return dbUtils.createConnection();
    }
    
    public void closeConnection(@Disposes Connection conn) {
        dbUtils.closeConnection(conn);
    }
}