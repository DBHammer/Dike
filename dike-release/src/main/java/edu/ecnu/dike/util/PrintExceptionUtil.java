/*
 * PrintSQLExceptionUtil - Print out detailed sql exception information.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.io.IOException;
import java.sql.SQLException;

public class PrintExceptionUtil {
    
    public static String getSQLExceptionInfo(SQLException se) {
        StringBuilder sb = new StringBuilder();
        sb.append(", sql state: " + se.getSQLState());
        sb.append(", error code: " + se.getErrorCode());
        sb.append(", message: " + se.getMessage());
        sb.append(", cause: " + se.getCause());
        return sb.toString(); 
    }

    public static String getIOExceptionInfo(IOException ie) {
        StringBuilder sb = new StringBuilder();
        sb.append(", message: " + ie.getMessage());
        sb.append(", cause: " + ie.getCause());
        return sb.toString();
    }
}
