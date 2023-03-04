/*
 * ExecJDBC - Command line program to process SQL DDL statements, from
 *             a text input file, to any JDBC Data Source
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */
package edu.ecnu.dike.jdbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.util.PrintExceptionUtil;

public class ExecJDBC {

	private static final Logger log = Logger.getLogger(ExecJDBC.class);

	public static void main(String[] args) {
		Connection conn = null;
		ConnectionProperty connProps = null;
		StringBuilder sql = new StringBuilder();

		String path = System.getProperty("prop");
		String commandFile = System.getProperty("commandFile");
		PropertyConfigurator.configure("../config/log4j.properties");
		log.info("Load sql statement from file " + commandFile);
		try {
			connProps = new ConnectionProperty(path);
			connProps.loadProperty();
			conn = DriverManager.getConnection(connProps.getConn(), connProps.getProperty());
			conn.setAutoCommit(true);
			Statement stmt = conn.createStatement();
			// loop through input file and concatenate SQL statement fragments
			String rLine = null;
			BufferedReader in = new BufferedReader(new FileReader(commandFile));
			while ((rLine = in.readLine()) != null) {
				String line = rLine.trim();
				if (line.length() != 0) {
					if (line.endsWith("\\;")) {
						sql.append(line.replaceAll("\\\\;", ";"));
						sql.append("\n");
					} else {
						sql.append(line.replaceAll("\\\\;", ";"));
						if (line.endsWith(";")) {
							String query = sql.toString();
							execJDBC(stmt, query.substring(0, query.length() - 1));
							sql = new StringBuilder();
						} else {
							sql.append("\n");
						}
					}
				}
			} // end while
			in.close();
			log.info("Execute all sql statements in file '" + commandFile + "'");
		} catch (IOException ioe) {
			log.error("Fail to load property file from path '" + path + "', " + PrintExceptionUtil.getIOExceptionInfo(ioe));
			System.exit(1);
		} catch (SQLException se) {
			log.error("Fail to get database connection, " + PrintExceptionUtil.getSQLExceptionInfo(se));
			System.exit(2);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				log.error("Fail to close database connection, " + PrintExceptionUtil.getSQLExceptionInfo(se));
			}
		}
	} // end main

	public static void execJDBC(Statement stmt, String query) {
		int retryCnt = 1;
		while (true) {
			try {
				stmt.execute(query);
				break;
			} catch (SQLException se) {
				if (retryCnt > 5) {
					log.error("Fail to execute sql statement after 5 trys, " + query + ",\n" + PrintExceptionUtil.getSQLExceptionInfo(se));
					System.exit(3);
				} else {
					retryCnt++;
				}				
			}
		}
	}
}