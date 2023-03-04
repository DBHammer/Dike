/*
 * StmtUpdateItem - UpdateItem transaction statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtUpdateItem {

    private PreparedStatement stmtUpdateReferenceItemPrice;
    private PreparedStatement stmtBatchUpdateReferenceItemPrice;

    public StmtUpdateItem(Connection conn) throws SQLException {

        stmtUpdateReferenceItemPrice = conn.prepareStatement(
                "Update table_item " +
                        "SET i_price = ? " +
                        "WHERE i_id = ?");

        stmtBatchUpdateReferenceItemPrice = conn.prepareStatement(
                "Update table_item " +
                        "SET i_price = ? " +
                        "where i_id <= ?");
    }

    public PreparedStatement getStmtUpdateReferenceItemPrice() {
        return stmtUpdateReferenceItemPrice;
    }

    public PreparedStatement getStmtBatchUpdateReferenceItemPrice() {
        return stmtBatchUpdateReferenceItemPrice;
    }
} // end StmtUpdateItem
