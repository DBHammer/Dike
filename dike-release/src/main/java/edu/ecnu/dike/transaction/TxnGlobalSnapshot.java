/*
 * TxnGlobalSnapshot - GlobalSnapshot transaction workload.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;
import edu.ecnu.dike.util.GroupKeyUtil;

public class TxnGlobalSnapshot extends TxnTemplate {
    
    private int snapshotTimes;
    private ArrayList<Integer> w_ids;
    private HashSet<Double> w_ytds;
    private HashMap<Integer, ArrayList<Integer>> widGroup;

    public TxnGlobalSnapshot(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_GLOBAL_SNAPSHOT;
        stmtGlobalSnapshot = dbConn.getStmtGlobalSnapshot();
        int numWarehouse = runtimeProps.getWarehouses();
        snapshotTimes = runtimeProps.getSnapshotTimes();
        widGroup = GroupKeyUtil.getGroupKey(numWarehouse, snapshotTimes);
    }

    @Override
    public void generateData() {
        int groupID = rnd.nextInt(0, widGroup.size() - 1);
        w_ids = widGroup.get(groupID);
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        if (rnd.nextInt(1, 100) <= 50) {
            // execute snapshot read
            w_ytds = new HashSet<>();
            for (int i = 0; i < snapshotTimes; i++) {
                stmt = stmtGlobalSnapshot.getStmtSelectBalance();
                stmt.setInt(1, w_ids.get(i));
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    throw new SQLException("w_id=" + w_ids.get(i) + " not found", "02000");
                }
                double w_ytd = rs.getDouble("w_ytd");
                w_ytds.add(w_ytd);
                rs.close();
            }

            if (w_ytds.size() > 1) {
                throw new SQLException("fail to pass global snapshot test", "02000");
            }
        } else {
            // execute snapshot write
            for (int i = 0; i < snapshotTimes; i++) {
                stmt = stmtGlobalSnapshot.getStmtUpdateBalance();
                stmt.setInt(1, w_ids.get(i));
                stmt.executeUpdate();
            }
        }

        conn.commit();
    }

    @Override
    public String toString() {
        return "TxnGlobalSnapshot [snapshotTimes=" + snapshotTimes + ", w_ids=" + w_ids + ", w_ytds=" + w_ytds
                + ", widGroup=" + widGroup + "]";
    }    
}
