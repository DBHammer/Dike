/*
 * StmtLoadData - LoadData statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtLoadData {

    // Insert Config
    private PreparedStatement stmtConfig;

    // Insert Item
    private PreparedStatement stmtItem;

    // Insert Warehouse
    private PreparedStatement stmtWarehouse;

    // Insert District
    private PreparedStatement stmtDistrict;

    // Insert Stock
    private PreparedStatement stmtStock;

    // Insert Customer
    private PreparedStatement stmtCustomer;

    // Insert History
    private PreparedStatement stmtHistory;

    // Insert Order
    private PreparedStatement stmtOrder;

    // Insert OrderLine
    private PreparedStatement stmtOrderLine;

    // Insert NewOrder
    private PreparedStatement stmtNewOrder;

    public StmtLoadData(Connection conn) throws SQLException {
        stmtConfig = conn.prepareStatement(
                "INSERT INTO table_config (" +
                        "cfg_name, cfg_value) " +
                        "VALUES (?, ?)");

        stmtItem = conn.prepareStatement(
                "INSERT INTO table_item (" +
                        "i_id,  i_name, i_price, i_data, i_im_id) " +
                        "VALUES (?, ?, ?, ?, ?)");

        stmtWarehouse = conn.prepareStatement(
                "INSERT INTO table_warehouse (" +
                        "w_id, w_ytd, w_tax, w_name, w_street_1, w_street_2, w_city, w_state, w_zip) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmtStock = conn.prepareStatement(
                "INSERT INTO table_stock (" +
                        "s_w_id, s_i_id, s_quantity, s_ytd, s_order_cnt, s_remote_cnt, s_data, s_dist_01, s_dist_02, " +
                        "s_dist_03, s_dist_04, s_dist_05, s_dist_06, " +
                        "s_dist_07, s_dist_08, s_dist_09, s_dist_10) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmtDistrict = conn.prepareStatement(
                "INSERT INTO table_district (" +
                        "d_w_id, d_id, d_ytd, d_tax, d_next_o_id, d_name, d_street_1, d_street_2, " +
                        "d_city, d_state, d_zip) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmtCustomer = conn.prepareStatement(
                "INSERT INTO table_customer (" +
                        "c_w_id, c_d_id, c_id,  c_discount, c_credit, c_last, c_first, c_credit_lim, c_balance, c_ytd_payment, "
                        +
                        "c_payment_cnt, c_delivery_cnt, c_street_1, c_street_2, c_city, c_state, c_zip, " +
                        "c_phone, c_since, c_middle, c_data) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmtHistory = conn.prepareStatement(
                "INSERT INTO table_history (" +
                        "h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, " +
                        "h_date, h_amount, h_data) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        stmtOrder = conn.prepareStatement(
                "INSERT INTO table_oorder (" +
                        "o_w_id, o_d_id, o_id, o_c_id, o_carrier_id, o_ol_cnt, " +
                        "o_all_local, o_entry_d) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        stmtOrderLine = conn.prepareStatement(
                "INSERT INTO table_order_line (" +
                        "ol_w_id, ol_d_id, ol_o_id, ol_number, ol_i_id, ol_delivery_d, " +
                        "ol_amount, ol_supply_w_id, ol_quantity, ol_dist_info) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmtNewOrder = conn.prepareStatement(
                "INSERT INTO table_new_order (" +
                        "no_w_id, no_d_id, no_o_id) " +
                        "VALUES (?, ?, ?)");
    }

    public PreparedStatement getStmtConfig() {
        return stmtConfig;
    }

    public PreparedStatement getStmtItem() {
        return stmtItem;
    }

    public PreparedStatement getStmtWarehouse() {
        return stmtWarehouse;
    }

    public PreparedStatement getStmtDistrict() {
        return stmtDistrict;
    }

    public PreparedStatement getStmtStock() {
        return stmtStock;
    }

    public PreparedStatement getStmtCustomer() {
        return stmtCustomer;
    }

    public PreparedStatement getStmtHistory() {
        return stmtHistory;
    }

    public PreparedStatement getStmtOrder() {
        return stmtOrder;
    }

    public PreparedStatement getStmtOrderLine() {
        return stmtOrderLine;
    }

    public PreparedStatement getStmtNewOrder() {
        return stmtNewOrder;
    }
}