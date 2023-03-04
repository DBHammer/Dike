/*
 * StmtStockLevel - StockLevel transaction statement.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtStockLevel {

    private Connection conn;

    // SelectLow
    private PreparedStatement stmtSelectLow;

    // SelectLowMulti
    private PreparedStatement stmtSelectLowMulti;

    public StmtStockLevel(Connection conn) throws SQLException {
        stmtSelectLow = conn.prepareStatement(
                "SELECT count(*) AS low_stock FROM (" +
                        "SELECT s_w_id, s_i_id, s_quantity " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_quantity < ? AND s_i_id IN (" +
                        "SELECT ol_i_id " +
                        "FROM table_district " +
                        "JOIN table_order_line ON ol_w_id = d_w_id " +
                        "AND ol_d_id = d_id " +
                        "AND ol_o_id >= d_next_o_id - 20 " +
                        "AND ol_o_id < d_next_o_id " +
                        "WHERE d_w_id = s_w_id AND d_id = ? " +
                        ")" +
                        ") AS L");
        this.conn = conn;
    }

    public PreparedStatement getStmtSelectLow() {
        return stmtSelectLow;
    }

    public PreparedStatement getStmtSelectLowMulti(int nodes) throws SQLException {
        String sql = "SELECT count(*) AS low_stock FROM ( " +
                "SELECT s_w_id, s_i_id, s_quantity " +
                "FROM table_stock  " +
                "WHERE s_w_id in (";

        for (int i = 0; i < nodes; i++) {
            sql += "? ";
            if (i != nodes - 1) {
                sql += ", ";
            } else {
                sql += ")";
            }
        }

        sql += "AND s_quantity < ? " +
                "AND s_i_id IN (" +
                "SELECT ol_i_id  " +
                "FROM table_district  " +
                "JOIN table_order_line ON ol_w_id = d_w_id  " +
                "AND ol_d_id = d_id  AND ol_o_id >= d_next_o_id - 20 " +
                "AND ol_o_id < d_next_o_id   " +
                "WHERE d_w_id = s_w_id AND d_id = ?)" +
                ") AS L";

        stmtSelectLowMulti = conn.prepareStatement(sql);
        return stmtSelectLowMulti;
    }
} // end StmtStockLevel
