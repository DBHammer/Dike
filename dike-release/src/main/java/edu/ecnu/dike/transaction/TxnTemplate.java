/*
 * TxnTemplate - Abstract class of transaction types, provided with several apis.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Formatter;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.statement.StmtDelivery;
import edu.ecnu.dike.statement.StmtGlobalDeadlock;
import edu.ecnu.dike.statement.StmtGlobalSnapshot;
import edu.ecnu.dike.statement.StmtNewOrder;
import edu.ecnu.dike.statement.StmtOrderStatus;
import edu.ecnu.dike.statement.StmtPayment;
import edu.ecnu.dike.statement.StmtStockLevel;
import edu.ecnu.dike.statement.StmtUpdateItem;
import edu.ecnu.dike.statement.StmtUpdateStock;
import edu.ecnu.dike.type.DbType;
import edu.ecnu.dike.type.TxnType;

public abstract class TxnTemplate {

    protected DbType dbType;
    protected Connection conn;
    protected BasicRandom rnd;
    protected RuntimeProperty runtimeProps;

    // transaction statements
    protected StmtNewOrder stmtNewOrder;
    protected StmtPayment stmtPayment;
    protected StmtOrderStatus stmtOrderStatus;
    protected StmtStockLevel stmtStockLevel;
    protected StmtDelivery stmtDelivery;
    protected StmtUpdateItem stmtUpdateItem;
    protected StmtUpdateStock stmtUpdateStock;
    protected StmtGlobalSnapshot stmtGlobalSnapshot;
    protected StmtGlobalDeadlock stmtGlobalDeadlock;

    // record line statistics
    protected TxnType txnType;
    protected long txnStart;
    protected long txnEnd;
    protected int txnRbk;
    protected int txnError;
    protected int txnDistributed;
    protected int txnSpanNode;
    protected int terminalID;
    protected int terminalWarehouseID;
    protected int terminalDistrictID;

    public void getPartitionSet() throws SQLException {
    };

    public abstract void generateData();

    public abstract void txnExecute() throws SQLException;

    public TxnTemplate(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        this.rnd = rnd;
        this.runtimeProps = runtimeProps;
        conn = dbConn.getConnection();
        dbType = runtimeProps.getDbType();
    }

    public void txnStart() {
        txnStart = System.currentTimeMillis();
    }

    public void txnEnd() {
        txnEnd = System.currentTimeMillis();
    }

    public void txnRollback() throws SQLException {
        conn.rollback();
        txnRbk = 1;
    }

    public void txnRetry() throws SQLException {
        conn.rollback();
        txnExecute();
    }

    public void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    public void setTerminalWarehouseID(int terminalWarehouseID) {
        this.terminalWarehouseID = terminalWarehouseID;
    }

    public void setTerminalDistrictID(int terminalDistrictID) {
        this.terminalDistrictID = terminalDistrictID;
    }

    public TxnType getTxnType() {
        return txnType;
    }

    public Long getLatency() {
        return txnEnd - txnStart;
    }

    public String resultLine() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("%d,%s,%d,%d,%d,%d,%d,%d\n", txnEnd - txnStart, txnType.getName(), txnRbk, txnError, txnDistributed,
                txnSpanNode, terminalWarehouseID, terminalID);
        fmt.close();
        return sb.toString();
    }

    public void setError() {
        txnError = 1;
    }

    public int isRollback() {
        return txnRbk;
    }

    public int isError() {
        return txnError;
    }

} // end TxnTemplate