/*
 * TxnGlobalDeadlock - GlobalDeadlock transaction workload.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.ZipFianRandom;
import edu.ecnu.dike.type.TxnType;

public class TxnGlobalDeadlock extends TxnTemplate {
    
    private int deadlockTimes;
    private ZipFianRandom zrnd;
    private ArrayList<Integer> w_ids;

    public TxnGlobalDeadlock(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_GLOBAL_DEADLOCK;
        stmtGlobalDeadlock = dbConn.getStmtGlobalDeadlock();
        deadlockTimes = runtimeProps.getDeadlockTimes();
        int numWarehouse = runtimeProps.getWarehouses();
        zrnd = new ZipFianRandom(numWarehouse, 0.8);
    }

    @Override
    public void generateData() {
        w_ids = new ArrayList<>();
        for (int i = 0; i < deadlockTimes; i++) {
            w_ids.add(zrnd.nextValue());
        }
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt;
        for (int i = 0; i < deadlockTimes; i++) {
            stmt = stmtGlobalDeadlock.getUpdateBalance();
            stmt.setInt(1, w_ids.get(i));
            stmt.executeUpdate();
        }
        conn.commit();    
    }

    @Override
    public String toString() {
        return "TxnGlobalDeadlock [deadlockTimes=" + deadlockTimes + ", w_ids=" + w_ids + ", zrnd=" + zrnd + "]";
    }
}
