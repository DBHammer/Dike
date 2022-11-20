/*
 * StmtOrderStatus - OrderStatus transaction statement.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtOrderStatus {

    // SelectCustomerListByLast
    private PreparedStatement stmtSelectCustomerListByLast;

    // SelectCustomerIDByPhone
    private PreparedStatement stmtSelectCustomerIDByPhone;

    // SelectCustomer
    private PreparedStatement stmtSelectCustomer;

    // SelectLastOrder
    private PreparedStatement stmtSelectLastOrder;

    // SelectOrderLine
    private PreparedStatement stmtSelectOrderLine;

    public StmtOrderStatus(Connection conn) throws SQLException {

        stmtSelectCustomerListByLast = conn.prepareStatement(
                "SELECT c_id " +
                        "FROM table_customer " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_last = ? " +
                        "ORDER BY c_first");

        stmtSelectCustomerIDByPhone = conn.prepareStatement(
                "SELECT c_w_id, c_d_id, c_id " +
                        "FROM table_customer " +
                        "WHERE c_phone = ?");

        stmtSelectCustomer = conn.prepareStatement(
                "SELECT c_first, c_middle, c_last, c_balance " +
                        "FROM table_customer " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtSelectLastOrder = conn.prepareStatement(
                "SELECT o_id, o_entry_d, o_carrier_id " +
                        "FROM table_oorder " +
                        "WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ? " +
                        "AND o_id = (" +
                        "SELECT max(o_id) " +
                        "FROM table_oorder " +
                        "WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ?" +
                        ")");

        stmtSelectOrderLine = conn.prepareStatement(
                "SELECT ol_i_id, ol_supply_w_id, ol_quantity, " +
                        "ol_amount, ol_delivery_d " +
                        "FROM table_order_line " +
                        "WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ? " +
                        "ORDER BY ol_w_id, ol_d_id, ol_o_id, ol_number");
    }

    public PreparedStatement getStmtSelectCustomerListByLast() {
        return stmtSelectCustomerListByLast;
    }

    public PreparedStatement getStmtSelectCustomerIDByPhone() {
        return stmtSelectCustomerIDByPhone;
    }

    public PreparedStatement getStmtSelectCustomer() {
        return stmtSelectCustomer;
    }

    public PreparedStatement getStmtSelectLastOrder() {
        return stmtSelectLastOrder;
    }

    public PreparedStatement getStmtSelectOrderLine() {
        return stmtSelectOrderLine;
    }
} // end StmtOrderStatus