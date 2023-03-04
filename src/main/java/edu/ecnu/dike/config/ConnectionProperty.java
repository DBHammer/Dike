/*
 * ConnectionProperty - Get jdbc connection properties.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.config;

import java.io.IOException;
import java.util.Properties;

import edu.ecnu.dike.type.DbType;
import edu.ecnu.dike.util.GetDbTypeUtil;
import edu.ecnu.dike.util.CheckParamsUtil;

public class ConnectionProperty extends Property {

    private DbType dbType;
    private String conn;
    private String host;
    private String port;
    private String set;
    private String user;
    private String password;

    // jdbc connection parameters
    private Properties dbProps;
    private String useSSL;
    private String rewriteBatchedStatements;
    private String allowMultiQueries;
    private String useLocalSessionState;
    private String useUnicode;
    private String characterEncoding;
    private String socketTimeout;
    private String connectionTimeout;
    private String allowLoadLocalInfile;
    private String autoReconnect;
    private String useServerPrepStmts;
    private String useConfigs;
    private int isolationLevel;
    private String sslmode;
    private String reWriteBatchedInserts;

    private void checkProperty() throws RuntimeException {
        // check jdbc connection properties with CheckParamsUtil
        CheckParamsUtil.checkNull(host, "host");
        CheckParamsUtil.checkNull(port, "port");
        CheckParamsUtil.checkNull(user, "user");
        CheckParamsUtil.checkNull(set, "set");
        CheckParamsUtil.checkRange(isolationLevel, "isolationLevel", 0, 3);
    }

    public ConnectionProperty(String path) throws IOException {
        super(path);
        dbProps = new Properties();
    }

    @Override
    public void loadProperty() throws RuntimeException {

        // database type
        dbType = GetDbTypeUtil.getDbType(props.getProperty("db", null));

        // jdbc connection (necessary)
        host = props.getProperty("host", null);
        port = props.getProperty("port", null);
        user = props.getProperty("user", null);
        password = props.getProperty("password", null);
        set = props.getProperty("set", null);

        // jdbc connection parameters
        useSSL = props.getProperty("useSSL", "false");
        rewriteBatchedStatements = props.getProperty("rewriteBatchedStatements", "false");
        allowMultiQueries = props.getProperty("allowMultiQueries", "false");
        useLocalSessionState = props.getProperty("useLocalSessionState", "false");
        socketTimeout = props.getProperty("socketTimeout", "300000");
        allowLoadLocalInfile = props.getProperty("allowLoadLocalInfile", "true");
        autoReconnect = props.getProperty("autoReconnect", "false");
        useServerPrepStmts = props.getProperty("useServerPrepStmts", "false");
        useConfigs = props.getProperty("useConfigs", null);
        isolationLevel = Integer.parseInt(props.getProperty("transactionIsolation", "1"));
        sslmode = props.getProperty("sslmode", "false");
        reWriteBatchedInserts = props.getProperty("reWriteBatchedInserts", "true");

        if (dbProps != null) {
            dbProps.setProperty("user", user);
            dbProps.setProperty("password", password);
            switch (dbType) {
                // mysql jdbc connection parameters
                case DB_MYSQL:
                case DB_OCEANBASE:
                case DB_TIDB:
                case DB_POLARDB:
                    conn = String.format("jdbc:mysql://%s:%s/", host, port);
                    dbProps.setProperty("useSSL", useSSL);
                    dbProps.setProperty("rewriteBatchedStatements", rewriteBatchedStatements);
                    dbProps.setProperty("allowMultiQueries", allowMultiQueries);
                    dbProps.setProperty("useLocalSessionState", useLocalSessionState);
                    dbProps.setProperty("allowLoadLocalInfile", allowLoadLocalInfile);
                    dbProps.setProperty("socketTimeout", socketTimeout);
                    dbProps.setProperty("autoReconnect", autoReconnect);
                    dbProps.setProperty("useServerPrepStmts", useServerPrepStmts);
                    dbProps.setProperty("useConfigs", useConfigs);
                    break;

                // postgres jdbc connection parameters
                case DB_POSTGRES:
                case DB_CITUS:
                case DB_COCKROACHDB:
                    conn = String.format("jdbc:postgresql://%s:%s/", host, port);
                    dbProps.setProperty("autoReconnect", autoReconnect);
                    dbProps.setProperty("sslmode", sslmode);
                    dbProps.setProperty("reWriteBatchedInserts", reWriteBatchedInserts);
                    dbProps.setProperty("socketTimeout", socketTimeout);
                    break;
                default:
                    break;
            }
        }

        checkProperty();
    } // end loadProperty

    public String getSet() {
        return set;
    }

    public DbType getDbType() {
        return dbType;
    }

    public String getConn() {
        return conn + set;
    }

    public int getIsolationLevel() {
        return isolationLevel;
    }

    public Properties getProperty() {
        return dbProps;
    }

    @Override
    public String toString() {
        return "ConnectionProperty [allowLoadLocalInfile=" + allowLoadLocalInfile + ", allowMultiQueries="
                + allowMultiQueries + ", autoReconnect=" + autoReconnect + ", characterEncoding=" + characterEncoding
                + ", conn=" + conn + ", connectionTimeout=" + connectionTimeout + ", dbProps=" + dbProps + ", dbType="
                + dbType + ", isolationLevel=" + isolationLevel + ", password=" + password + ", reWriteBatchedInserts="
                + reWriteBatchedInserts + ", rewriteBatchedStatements=" + rewriteBatchedStatements + ", set=" + set
                + ", socketTimeout=" + socketTimeout + ", sslmode=" + sslmode + ", useConfigs=" + useConfigs
                + ", useLocalSessionState=" + useLocalSessionState + ", useSSL=" + useSSL + ", useServerPrepStmts="
                + useServerPrepStmts + ", useUnicode=" + useUnicode + ", user=" + user + "]";
    }
}