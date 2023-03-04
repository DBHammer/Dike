/*
 * TxnUpdateStock - UpdateStock transaction workload.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;
import edu.ecnu.dike.util.GroupKeyUtil;

public class TxnUpdateStock extends TxnTemplate {

    // value initialized by randomization
    private int i_id;
    private int terminalID;
    private int coaccessNumber;
    private HashMap<Integer, ArrayList<Integer>> groupWids;

    public TxnUpdateStock(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd, int terminalID) {
        super(dbConn, runtimeProps, rnd);
        this.terminalID = terminalID;
        txnType = TxnType.TXN_UPDATE_STOCK;
        stmtUpdateStock = dbConn.getStmtUpdateStock();
        coaccessNumber = runtimeProps.getCoaccessNumber();
        groupWids = GroupKeyUtil.getGroupKey(runtimeProps.getWarehouses(), runtimeProps.getPhysicalNode());
    }

    @Override
    public void generateData() {
        i_id = (terminalID * 10 + rnd.nextInt(1, 10)) % 100000;
    }

    @Override
    public void txnExecute() throws SQLException {
        ResultSet rs; 
        PreparedStatement stmt;
        ArrayList<Integer> groupWid = groupWids.get(0);
        for (int i = 0; i < coaccessNumber; i++) {
            stmt = stmtUpdateStock.getStmtSelectStock();
            stmt.setInt(1, groupWid.get(i));
            stmt.setInt(2, i_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("fail to get ol_amount for" + " w_id=" + groupWid.get(i) + " i_id=" + i_id, "02000");
            }
            int s_quantity = rs.getInt("s_quantity");
            stmt = stmtUpdateStock.getStmtUpdateStock();
            stmt.setInt(1, (s_quantity + 1) % 10);
            stmt.setInt(2, groupWid.get(i));
            stmt.setInt(3, i_id);
            stmt.executeUpdate();
            conn.commit();
            rs.close();
        }
    }

    @Override
    public String toString() {
        return "TxnUpdateStock [coaccessNumber=" + coaccessNumber + ", groupWids=" + groupWids + ", i_id=" + i_id
                + ", terminalID=" + terminalID + "]";
    }

}