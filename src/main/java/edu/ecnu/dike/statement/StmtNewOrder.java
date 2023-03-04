/*
 * StmtNewOrder - NewOrder transaction statement.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtNewOrder {

    // SelectWhseCust
    private PreparedStatement stmtSelectWhseCust;
    private PreparedStatement stmtTraceSelectWhseCust;

    // SelectDist for update
    private PreparedStatement stmtSelectDist;
    private PreparedStatement stmtHintElrSelectDist;
    private PreparedStatement stmtTraceSelectDist;
    private PreparedStatement stmtNoLockSelectDist;

    // UpdateDist
    private PreparedStatement stmtUpdateDist;
    private PreparedStatement stmtTraceUpdateDist;

    // InsertOrder
    private PreparedStatement stmtInsertOrder;
    private PreparedStatement stmtTraceInsertOrder;

    // InsertNewOrder
    private PreparedStatement stmtInsertNewOrder;
    private PreparedStatement stmtTraceInsertNewOrder;

    // SelectStock, for update, multiple execution
    private PreparedStatement stmtSelectStock;
    private PreparedStatement stmtHintElrSelectStock;
    private PreparedStatement stmtTraceSelectStock;
    private PreparedStatement stmtNoLockSelectStock;

    // SelectItem, multiple execution
    private PreparedStatement stmtSelectItem;
    private PreparedStatement stmtTraceSelectItem;

    // UpdateStock, multiple execution
    private PreparedStatement stmtUpdateStock;
    private PreparedStatement stmtTraceUpdateStock;

    // InsertOrderLine, multiple execution
    private PreparedStatement stmtInsertOrderLine;
    private PreparedStatement stmtTraceInsertOrderLine;

    public StmtNewOrder(Connection conn) throws SQLException {

        // OceanBase hint to release lock earlier in high conflict situation
        stmtHintElrSelectDist = conn.prepareStatement(
                "SELECT /*+ trans_param('enable_early_lock_release','true') */ d_tax, d_next_o_id " +
                        "FROM table_district " +
                        "WHERE d_w_id = ? AND d_id = ? " +
                        "FOR UPDATE");

        stmtHintElrSelectStock = conn.prepareStatement(
                "SELECT /*+ trans_param('enable_early_lock_release','true') */ s_quantity, s_data, " +
                        "s_dist_01, s_dist_02, s_dist_03, s_dist_04, " +
                        "s_dist_05, s_dist_06, s_dist_07, s_dist_08, " +
                        "s_dist_09, s_dist_10 " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_i_id = ? " +
                        "FOR UPDATE");

        // TiDB trace sql execution log
        stmtTraceSelectWhseCust = conn.prepareStatement(
                "trace format='log' SELECT c_discount, c_last, c_credit, w_tax " +
                        "FROM table_customer " +
                        "JOIN table_warehouse ON (w_id = c_w_id) " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtTraceSelectDist = conn.prepareStatement(
                "trace format='log' SELECT d_tax, d_next_o_id " +
                        "FROM table_district " +
                        "WHERE d_w_id = ? AND d_id = ? " +
                        "FOR UPDATE");

        stmtTraceUpdateDist = conn.prepareStatement(
                "trace format='log' UPDATE table_district " +
                        "SET d_next_o_id = d_next_o_id + 1 " +
                        "WHERE d_w_id = ? AND d_id = ?");

        stmtTraceInsertOrder = conn.prepareStatement(
                "trace format='log' INSERT INTO table_oorder (" +
                        "o_id, o_d_id, o_w_id, o_c_id, o_entry_d, " +
                        "o_ol_cnt, o_all_local) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        stmtTraceInsertNewOrder = conn.prepareStatement(
                "trace format='log' INSERT INTO table_new_order (" +
                        "no_o_id, no_d_id, no_w_id) " +
                        "VALUES (?, ?, ?)");

        stmtTraceSelectStock = conn.prepareStatement(
                "trace format='log' SELECT s_quantity, s_data, " +
                        "s_dist_01, s_dist_02, s_dist_03, s_dist_04, " +
                        "s_dist_05, s_dist_06, s_dist_07, s_dist_08, " +
                        "s_dist_09, s_dist_10 " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_i_id = ? " +
                        "FOR UPDATE");

        stmtTraceSelectItem = conn.prepareStatement(
                "trace format='log' SELECT i_price, i_name, i_data " +
                        "FROM table_item " +
                        "WHERE i_id = ?");

        stmtTraceUpdateStock = conn.prepareStatement(
                "trace format='log' UPDATE table_stock " +
                        "SET s_quantity = ?, s_ytd = s_ytd + ?, " +
                        "s_order_cnt = s_order_cnt + 1, " +
                        "s_remote_cnt = s_remote_cnt + ? " +
                        "WHERE s_w_id = ? AND s_i_id = ?");

        stmtTraceInsertOrderLine = conn.prepareStatement(
                "trace format='log' INSERT INTO table_order_line (" +
                        "ol_o_id, ol_d_id, ol_w_id, ol_number, " +
                        "ol_i_id, ol_supply_w_id, ol_quantity, " +
                        "ol_amount, ol_dist_info) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        // CockroachDB not support "FOR UPDATE"
        stmtNoLockSelectDist = conn.prepareStatement(
                "SELECT d_tax, d_next_o_id " +
                        "FROM table_district " +
                        "WHERE d_w_id = ? AND d_id = ? ");

        stmtNoLockSelectStock = conn.prepareStatement(
                "SELECT s_quantity, s_data, " +
                        "s_dist_01, s_dist_02, s_dist_03, s_dist_04, " +
                        "s_dist_05, s_dist_06, s_dist_07, s_dist_08, " +
                        "s_dist_09, s_dist_10 " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_i_id = ? ");

        // NewOrder transaction
        stmtSelectWhseCust = conn.prepareStatement(
                "SELECT c_discount, c_last, c_credit, w_tax " +
                        "FROM table_customer " +
                        "JOIN table_warehouse ON (w_id = c_w_id) " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtSelectDist = conn.prepareStatement(
                "SELECT d_tax, d_next_o_id " +
                        "FROM table_district " +
                        "WHERE d_w_id = ? AND d_id = ? " +
                        "FOR UPDATE");

        stmtUpdateDist = conn.prepareStatement(
                "UPDATE table_district " +
                        "SET d_next_o_id = d_next_o_id + 1 " +
                        "WHERE d_w_id = ? AND d_id = ?");

        stmtInsertOrder = conn.prepareStatement(
                "INSERT INTO table_oorder (" +
                        "o_id, o_d_id, o_w_id, o_c_id, o_entry_d, " +
                        "o_ol_cnt, o_all_local) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        stmtInsertNewOrder = conn.prepareStatement(
                "INSERT INTO table_new_order (" +
                        "no_o_id, no_d_id, no_w_id) " +
                        "VALUES (?, ?, ?)");

        stmtSelectStock = conn.prepareStatement(
                "SELECT s_quantity, s_data, " +
                        "s_dist_01, s_dist_02, s_dist_03, s_dist_04, " +
                        "s_dist_05, s_dist_06, s_dist_07, s_dist_08, " +
                        "s_dist_09, s_dist_10 " +
                        "FROM table_stock " +
                        "WHERE s_w_id = ? AND s_i_id = ? " +
                        "FOR UPDATE");

        stmtSelectItem = conn.prepareStatement(
                "SELECT i_price, i_name, i_data " +
                        "FROM table_item " +
                        "WHERE i_id = ?");

        stmtUpdateStock = conn.prepareStatement(
                "UPDATE table_stock " +
                        "SET s_quantity = ?, s_ytd = s_ytd + ?, " +
                        "s_order_cnt = s_order_cnt + 1, " +
                        "s_remote_cnt = s_remote_cnt + ? " +
                        "WHERE s_w_id = ? AND s_i_id = ?");

        stmtInsertOrderLine = conn.prepareStatement(
                "INSERT INTO table_order_line (" +
                        "ol_o_id, ol_d_id, ol_w_id, ol_number, " +
                        "ol_i_id, ol_supply_w_id, ol_quantity, " +
                        "ol_amount, ol_dist_info) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }

    public PreparedStatement getStmtSelectWhseCust() {
        return stmtSelectWhseCust;
    }

    public PreparedStatement getStmtTraceSelectWhseCust() {
        return stmtTraceSelectWhseCust;
    }

    public PreparedStatement getStmtSelectDist() {
        return stmtSelectDist;
    }

    public PreparedStatement getStmtHintElrSelectDist() {
        return stmtHintElrSelectDist;
    }

    public PreparedStatement getStmtTraceSelectDist() {
        return stmtTraceSelectDist;
    }

    public PreparedStatement getStmtNoLockSelectDist() {
        return stmtNoLockSelectDist;
    }

    public PreparedStatement getStmtUpdateDist() {
        return stmtUpdateDist;
    }

    public PreparedStatement getStmtTraceUpdateDist() {
        return stmtTraceUpdateDist;
    }

    public PreparedStatement getStmtInsertOrder() {
        return stmtInsertOrder;
    }

    public PreparedStatement getStmtTraceInsertOrder() {
        return stmtTraceInsertOrder;
    }

    public PreparedStatement getStmtInsertNewOrder() {
        return stmtInsertNewOrder;
    }

    public PreparedStatement getStmtTraceInsertNewOrder() {
        return stmtTraceInsertNewOrder;
    }

    public PreparedStatement getStmtSelectStock() {
        return stmtSelectStock;
    }

    public PreparedStatement getStmtHintElrSelectStock() {
        return stmtHintElrSelectStock;
    }

    public PreparedStatement getStmtTraceSelectStock() {
        return stmtTraceSelectStock;
    }

    public PreparedStatement getStmtNoLockSelectStock() {
        return stmtNoLockSelectStock;
    }

    public PreparedStatement getStmtSelectItem() {
        return stmtSelectItem;
    }

    public PreparedStatement getStmtTraceSelectItem() {
        return stmtTraceSelectItem;
    }

    public PreparedStatement getStmtUpdateStock() {
        return stmtUpdateStock;
    }

    public PreparedStatement getStmtTraceUpdateStock() {
        return stmtTraceUpdateStock;
    }

    public PreparedStatement getStmtInsertOrderLine() {
        return stmtInsertOrderLine;
    }

    public PreparedStatement getStmtTraceInsertOrderLine() {
        return stmtTraceInsertOrderLine;
    }
} // end StmtNewOrder