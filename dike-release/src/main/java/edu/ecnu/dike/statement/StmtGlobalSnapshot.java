/*
 * StmtGlobalSnapshot - GlobalSnapshot transaction statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtGlobalSnapshot {

    // UpdateWarehouseBalance
    private PreparedStatement stmtUpdateBalance;

    // Select WarehouseBalance
    private PreparedStatement stmtSelectBalance;

    public StmtGlobalSnapshot(Connection conn) throws SQLException {
        stmtUpdateBalance = conn.prepareStatement(
                "UPDATE table_warehouse " +
                        "SET w_ytd = w_ytd + 10 " +
                        "WHERE w_id = ?");

        stmtSelectBalance = conn.prepareStatement(
                "SELECT w_ytd " +
                        "FROM table_warehouse " +
                        "WHERE w_id = ?");
    }

    public PreparedStatement getStmtUpdateBalance() {
        return stmtUpdateBalance;
    }

    public PreparedStatement getStmtSelectBalance() {
        return stmtSelectBalance;
    }
}