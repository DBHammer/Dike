/*
 * TxnUpdateItem - UpdateItem transaction workload.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;

public class TxnUpdateItem extends TxnTemplate {

    // value initialized by randomization
    private int i_id;
    private double i_price;

    // control properties
    private boolean isBatchUpdate;
    private int accessUpdateItemRate;

    public TxnUpdateItem(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_UPDATE_ITEM;
        isBatchUpdate = runtimeProps.getBatchUpdate();
        stmtUpdateItem = dbConn.getStmtUpdateItem();
        accessUpdateItemRate = runtimeProps.getAccesssUpdateItemRate();
    }

    @Override
    public void generateData() {
        i_id = rnd.getItemID();
        i_price = ((double) rnd.nextLong(100, 10000)) / 100.0;
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt = null;
        if (isBatchUpdate) {
            stmt = stmtUpdateItem.getStmtBatchUpdateReferenceItemPrice();
            stmt.setDouble(1, i_price);
            stmt.setInt(2, (int)(accessUpdateItemRate * 100000 * 0.01));
            stmt.executeUpdate();
        } else {
            stmt = stmtUpdateItem.getStmtUpdateReferenceItemPrice();
            stmt.setDouble(1, i_price);
            stmt.setInt(2, i_id);
            stmt.executeUpdate();
        }
        conn.commit();
    }
    
    @Override
    public String toString() {
        return "TxnUpdateItem [accessUpdateItemRate=" + accessUpdateItemRate + ", i_id=" + i_id + ", i_price=" + i_price
                + ", isBatchUpdate=" + isBatchUpdate + "]";
    }
}
