/*
 * StmtDelivery - Delivery transaction statement.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtDelivery {

    // SelectOldestNewOrder for update
    private PreparedStatement stmtSelectOldestNewOrder;
    private PreparedStatement stmtNoLockSelectOldestNewOrder;

    // DeleteOldestNewOrder
    private PreparedStatement stmtDeleteOldestNewOrder;

    // SelectOrder
    private PreparedStatement stmtSelectOrder;

    // UpdateOrder
    private PreparedStatement stmtUpdateOrder;

    // SelectSumOLAmount
    private PreparedStatement stmtSelectSumOLAmount;

    // UpdateOrderLine
    private PreparedStatement stmtUpdateOrderLine;

    // UpdateCustomer
    private PreparedStatement stmtUpdateCustomer;

    public StmtDelivery(Connection conn) throws SQLException {

        // CockroachDB not support "FOR UPDATE"
        stmtNoLockSelectOldestNewOrder = conn.prepareStatement(
                "SELECT no_o_id " +
                        "FROM table_new_order " +
                        "WHERE no_w_id = ? AND no_d_id = ? " +
                        "ORDER BY no_o_id ASC ");

        // Delivery transaction
        stmtSelectOldestNewOrder = conn.prepareStatement(
                "SELECT no_o_id " +
                        "FROM table_new_order " +
                        "WHERE no_w_id = ? AND no_d_id = ? " +
                        "ORDER BY no_o_id ASC ");

        stmtDeleteOldestNewOrder = conn.prepareStatement(
                "DELETE FROM table_new_order " +
                        "WHERE no_w_id = ? AND no_d_id = ? AND no_o_id = ?");

        stmtSelectOrder = conn.prepareStatement(
                "SELECT o_c_id " +
                        "FROM table_oorder " +
                        "WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?");

        stmtUpdateOrder = conn.prepareStatement(
                "UPDATE table_oorder " +
                        "SET o_carrier_id = ? " +
                        "WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?");

        stmtSelectSumOLAmount = conn.prepareStatement(
                "SELECT sum(ol_amount) AS sum_ol_amount " +
                        "FROM table_order_line " +
                        "WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?");

        stmtUpdateOrderLine = conn.prepareStatement(
                "UPDATE table_order_line " +
                        "SET ol_delivery_d = ? " +
                        "WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?");

        stmtUpdateCustomer = conn.prepareStatement(
                "UPDATE table_customer " +
                        "SET c_balance = c_balance + ?, " +
                        "c_delivery_cnt = c_delivery_cnt + 1 " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");
    }

    public PreparedStatement getStmtSelectOldestNewOrder() {
        return stmtSelectOldestNewOrder;
    }

    public PreparedStatement getStmtNoLockSelectOldestNewOrder() {
        return stmtNoLockSelectOldestNewOrder;
    }

    public PreparedStatement getStmtDeleteOldestNewOrder() {
        return stmtDeleteOldestNewOrder;
    }

    public PreparedStatement getStmtSelectOrder() {
        return stmtSelectOrder;
    }

    public PreparedStatement getStmtUpdateOrder() {
        return stmtUpdateOrder;
    }

    public PreparedStatement getStmtSelectSumOLAmount() {
        return stmtSelectSumOLAmount;
    }

    public PreparedStatement getStmtUpdateOrderLine() {
        return stmtUpdateOrderLine;
    }

    public PreparedStatement getStmtUpdateCustomer() {
        return stmtUpdateCustomer;
    }
} // end StmtDelivery
