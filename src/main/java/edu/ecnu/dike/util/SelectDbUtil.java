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
import java.util.ArrayList;

public class SelectDbUtil {
	
	// TODO: replace the caller of getConfig/getOBTableSize with the ad hoc query interface
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

	public static String[] getOBTableSize(String db, Properties dbProps, int cnt) throws SQLException {
		Connection dbConn = DriverManager.getConnection(db, dbProps);
		Statement stmtAdHoc = dbConn.createStatement();
		String query = "SELECT svr_ip, count(1), sum(row_count), round(sum(occupy_size)/1024/1024/1024,0) AS occupy_size_GB FROM __all_virtual_storage_stat WHERE table_id in (SELECT table_id FROM __all_virtual_table WHERE table_name IN ('table_config', 'table_warehouse', 'table_district', 'table_customer', 'table_history', 'table_new_order', 'table_oorder', 'table_order_line', 'table_item', 'table_stock')) AND role = 1 AND store_type = 1 GROUP BY svr_ip ORDER BY svr_ip;";
		ResultSet rs = stmtAdHoc.executeQuery(query);
		if (!rs.next()) {
			rs.close();
			throw new SQLException("execute query '" + query + "' fail", "02000");
		}
		String storage[] = new String[cnt];
		int i = 0;
		while (rs.next()) {
			storage[i] = rs.getString("occupy_size_GB");
			i++;
		}
		return storage;
	}

	public static String[] adHocQuery(String db, Properties dbProps, String query, String colname) throws SQLException {
		Connection dbConn = DriverManager.getConnection(db, dbProps);
		Statement stmtAdHoc = dbConn.createStatement();
		ResultSet rs = stmtAdHoc.executeQuery(query);
		if (!rs.next()) {
			rs.close();
			throw new SQLException("execute query '" + query + "' fail", "02000");
		}
		ArrayList<String> res = new ArrayList<>();
		while (rs.next()) {
			res.add(rs.getString(colname));
		}
		return (String[])res.toArray();
	}

} // end SelectDbUtil
