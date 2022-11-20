/*
 * StmtUpdateStock - Select for update stock table.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtUpdateStock {

    // Select Stock for update
    private PreparedStatement stmtSelectStock;

    // Update Stock
    private PreparedStatement stmtUpdateStock;

    public StmtUpdateStock(Connection conn) throws SQLException {
        stmtSelectStock = conn.prepareStatement(
                "SELECT s_quantity " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_i_id = ? " +
                        "FOR UPDATE");

        stmtUpdateStock = conn.prepareStatement(
                "UPDATE table_stock " +
                        "SET s_quantity = ? " +
                        "WHERE s_w_id = ? AND s_i_id = ? ");
    }

    public PreparedStatement getStmtSelectStock() {
        return stmtSelectStock;
    }

    public PreparedStatement getStmtUpdateStock() {
        return stmtUpdateStock;
    }

} // end StmtUpdateStock