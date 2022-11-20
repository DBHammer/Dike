/*
 * SelectDbUtil - Select config parameters from table_config.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class SelectDbUtil {
	
	public static String getConfig(String db, Properties dbProps, String option) throws SQLException {
		Connection dbConn = DriverManager.getConnection(db, dbProps);
		String getConfig = "SELECT cfg_value FROM table_config WHERE cfg_name = '" + option + "'";
		Statement stmtGetConfig = dbConn.createStatement();
		ResultSet rs = stmtGetConfig.executeQuery(getConfig);
		if (!rs.next()) {
			rs.close();
			throw new SQLException("configuration parameter '" + option + "' not found", "02000");
		}
		String value = rs.getString("cfg_value");
		rs.close();
		return value;
	}

} // end SelectDbUtil
