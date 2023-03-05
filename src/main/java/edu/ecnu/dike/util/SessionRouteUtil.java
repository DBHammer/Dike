/*
 * SessionRouteUtil - Check which server the session is located on.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ecnu.dike.type.DbType;

public class SessionRouteUtil {
    
    public static String getSessionRoute(Connection conn, DbType dbType) throws SQLException {
        ResultSet rs = null;
        String instance = null;
        Statement stmt = conn.createStatement();
        switch (dbType) {
            case DB_TIDB:
                rs = stmt.executeQuery(
                    "SELECT instance FROM INFORMATION_SCHEMA.cluster_processlist WHERE info LIKE 'SELECT instance FROM INFORMATION_SCHEMA%' ORDER BY TxnStart LIMIT 1");
                if (!rs.next()) {
                    rs.close();
                    throw new SQLException("fail to get cluster processlist in tidb", "02000");
                }
                instance = rs.getString("instance").split(":")[0];
                rs.close();
                break;
            case DB_COCKROACHDB:
                rs = stmt.executeQuery("show locality;");
                if (!rs.next()) {
                    rs.close();
                    throw new SQLException("fail to get locality in cockroachdb", "02000");
                }
                String[] locality = rs.getString("locality").split(",");
                instance = locality[locality.length - 1].replace("=", "");
                break;
            default:
                break;
        }
        return instance;        
    }
}
