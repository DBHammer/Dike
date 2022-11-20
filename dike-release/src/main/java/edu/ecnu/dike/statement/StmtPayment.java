/*
 * StmtPayment - Payment transaction statement.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtPayment {

    // SelectWarehouse
    private PreparedStatement stmtSelectWarehouse;

    // SelectDistrict
    private PreparedStatement stmtSelectDistrict;

    // SelectCustomerListByLast
    private PreparedStatement stmtSelectCustomerListByLast;

    // SelectCustomerIDByPhone
    private PreparedStatement stmtSelectCustomerIDByPhone;

    // SelectCustomer
    private PreparedStatement stmtSelectCustomer;
    private PreparedStatement stmtNoLockSelectCustomer;

    // SelectCustomerData
    private PreparedStatement stmtSelectCustomerData;

    // UpdateWarehouse
    private PreparedStatement stmtUpdateWarehouse;

    // UpdateDistrict
    private PreparedStatement stmtUpdateDistrict;

    // UpdateCustomer
    private PreparedStatement stmtUpdateCustomer;

    // UpdateCustomerWithData
    private PreparedStatement stmtUpdateCustomerWithData;

    // InsertHistory
    private PreparedStatement stmtInsertHistory;

    public StmtPayment(Connection conn) throws SQLException {

        // CockroachDB not support "FOR UPDATE"
        stmtNoLockSelectCustomer = conn.prepareStatement(
                "SELECT c_first, c_middle, c_last, c_street_1, c_street_2, " +
                        "c_city, c_state, c_zip, c_phone, c_since, c_credit, " +
                        "c_credit_lim, c_discount, c_balance " +
                        "FROM table_customer " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ? ");


        // Payment transaction
        stmtSelectWarehouse = conn.prepareStatement(
                "SELECT w_name, w_street_1, w_street_2, w_city, w_state, w_zip " +
                        "FROM table_warehouse " +
                        "WHERE w_id = ?");

        stmtSelectDistrict = conn.prepareStatement(
                "SELECT d_name, d_street_1, d_street_2, d_city, d_state, d_zip " +
                        "FROM table_district " +
                        "WHERE d_w_id = ? AND d_id = ?");

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
                "SELECT c_first, c_middle, c_last, c_street_1, c_street_2, " +
                        "c_city, c_state, c_zip, c_phone, c_since, c_credit, " +
                        "c_credit_lim, c_discount, c_balance " +
                        "FROM table_customer " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ? " +
                        "FOR UPDATE");

        stmtSelectCustomerData = conn.prepareStatement(
                "SELECT c_data " +
                        "FROM table_customer " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtUpdateWarehouse = conn.prepareStatement(
                "UPDATE table_warehouse " +
                        "SET w_ytd = w_ytd + ? " +
                        "WHERE w_id = ?");

        stmtUpdateDistrict = conn.prepareStatement(
                "UPDATE table_district " +
                        "SET d_ytd = d_ytd + ? " +
                        "WHERE d_w_id = ? AND d_id = ?");

        stmtUpdateCustomer = conn.prepareStatement(
                "UPDATE table_customer " +
                        "SET c_balance = c_balance - ?, " +
                        "c_ytd_payment = c_ytd_payment + ?, " +
                        "c_payment_cnt = c_payment_cnt + 1 " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtUpdateCustomerWithData = conn.prepareStatement(
                "UPDATE table_customer " +
                        "SET c_balance = c_balance - ?, " +
                        "c_ytd_payment = c_ytd_payment + ?, " +
                        "c_payment_cnt = c_payment_cnt + 1, " +
                        "c_data = ? " +
                        "WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?");

        stmtInsertHistory = conn.prepareStatement(
                "INSERT INTO table_history (" +
                        "h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, " +
                        "h_date, h_amount, h_data) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    }

    public PreparedStatement getStmtSelectWarehouse() {
        return stmtSelectWarehouse;
    }

    public PreparedStatement getStmtSelectDistrict() {
        return stmtSelectDistrict;
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

    public PreparedStatement getStmtNoLockSelectCustomer() {
        return stmtNoLockSelectCustomer;
    }

    public PreparedStatement getStmtSelectCustomerData() {
        return stmtSelectCustomerData;
    }

    public PreparedStatement getStmtUpdateWarehouse() {
        return stmtUpdateWarehouse;
    }

    public PreparedStatement getStmtUpdateDistrict() {
        return stmtUpdateDistrict;
    }

    public PreparedStatement getStmtUpdateCustomer() {
        return stmtUpdateCustomer;
    }

    public PreparedStatement getStmtUpdateCustomerWithData() {
        return stmtUpdateCustomerWithData;
    }

    public PreparedStatement getStmtInsertHistory() {
        return stmtInsertHistory;
    }
} // end StmtPayment
