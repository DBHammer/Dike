/*
 * DbConnection - Manage jdbc connection and session parameters.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.statement.StmtDelivery;
import edu.ecnu.dike.statement.StmtGlobalDeadlock;
import edu.ecnu.dike.statement.StmtGlobalSnapshot;
import edu.ecnu.dike.statement.StmtNewOrder;
import edu.ecnu.dike.statement.StmtOrderStatus;
import edu.ecnu.dike.statement.StmtPayment;
import edu.ecnu.dike.statement.StmtStockLevel;
import edu.ecnu.dike.statement.StmtUpdateItem;
import edu.ecnu.dike.statement.StmtUpdateStock;

public class DbConnection {

    private final Logger log = Logger.getLogger(DbConnection.class);

    // jdbc connection
    private Connection conn;
    private ConnectionProperty connectionProps;

    // prepared statement
    private StmtNewOrder stmtNewOrder;
    private StmtPayment stmtPayment;
    private StmtOrderStatus stmtOrderStatus;
    private StmtStockLevel stmtStockLevel;
    private StmtDelivery stmtDelivery;
    private StmtUpdateItem stmtUpdateItem;
    private StmtUpdateStock stmtUpdateStock;
    private StmtGlobalSnapshot stmtGlobalSnapshot;
    private StmtGlobalDeadlock stmtGlobalDeadlock;

    // weakly consistent read
    private boolean isWeakRead = false;

    public DbConnection(ConnectionProperty connectionProps) {
        this.connectionProps = connectionProps;
    }

    public void startConnection() throws SQLException {
        conn = DriverManager.getConnection(connectionProps.getConn(), connectionProps.getProperty());
        conn.setAutoCommit(false);
        switch (connectionProps.getIsolationLevel()) {
            case 0:
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                break;
            case 1:
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                break;
            case 2:
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                break;
            case 3:
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                break;
            default:
                break;
        }
        stmtNewOrder = new StmtNewOrder(conn);
        stmtPayment = new StmtPayment(conn);
        stmtOrderStatus = new StmtOrderStatus(conn);
        stmtStockLevel = new StmtStockLevel(conn);
        stmtDelivery = new StmtDelivery(conn);
        stmtUpdateItem = new StmtUpdateItem(conn);
        stmtUpdateStock = new StmtUpdateStock(conn);
        stmtGlobalSnapshot = new StmtGlobalSnapshot(conn);
        stmtGlobalDeadlock = new StmtGlobalDeadlock(conn);
    }

    public void closeConnectionIfAlive() {
        if (isAlive()) {
            try {
                conn.close();
            } catch (SQLException se) {
                log.error("Fail to close database connection, " + se.getMessage());
            }
        }
    }

    public void setWeakRead() {
        if (!isWeakRead) {
            try {
                Statement stmt = conn.createStatement();
                switch (connectionProps.getDbType()) {
                    case DB_OCEANBASE:
                        stmt.execute("SET @@ob_read_consistency = 'weak';");
                        break;
                    case DB_TIDB:
                        stmt.execute("set @@tidb_replica_read = 'follower';");
                        break;
                    default:
                        break;
                }
                isWeakRead = true;
            } catch (SQLException se) {
                log.error("Fail to set weakly consistent read, " + se.getMessage());
            }
        }
    }

    public Boolean isAlive() {
        try {
            if (conn != null && !conn.isClosed() && conn.isValid(20)) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException se) {
            return false;
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public StmtNewOrder getStmtNewOrder() {
        return stmtNewOrder;
    }

    public StmtPayment getStmtPayment() {
        return stmtPayment;
    }

    public StmtOrderStatus getStmtOrderStatus() {
        return stmtOrderStatus;
    }

    public StmtStockLevel getStmtStockLevel() {
        return stmtStockLevel;
    }

    public StmtDelivery getStmtDelivery() {
        return stmtDelivery;
    }

    public StmtUpdateItem getStmtUpdateItem() {
        return stmtUpdateItem;
    }

    public StmtUpdateStock getStmtUpdateStock() {
        return stmtUpdateStock;
    }

    public StmtGlobalSnapshot getStmtGlobalSnapshot() {
        return stmtGlobalSnapshot;
    }

    public StmtGlobalDeadlock getStmtGlobalDeadlock() {
        return stmtGlobalDeadlock;
    }
}
