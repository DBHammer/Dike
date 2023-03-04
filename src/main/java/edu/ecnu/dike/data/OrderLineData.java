/*
 * OrderLineData - Store OrderLine data and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OrderLineData extends TableData {

    private int ol_w_id;
    private int ol_d_id;
    private int ol_o_id;
    private int ol_number;
    private int ol_i_id;
    private Timestamp ol_delivery_d;
    private double ol_amount;
    private int ol_supply_w_id;
    private int ol_quantity;
    private String ol_dist_info;

    public OrderLineData(int w_id, int d_id, int o_id, int number, int i_id, Timestamp delivery_d, double amount,
            int supply_w_id, int quantity, String dist_info) {
        ol_w_id = w_id;
        ol_d_id = d_id;
        ol_o_id = o_id;
        ol_number = number;
        ol_i_id = i_id;
        ol_delivery_d = delivery_d;
        ol_amount = amount;
        ol_supply_w_id = supply_w_id;
        ol_quantity = quantity;
        ol_dist_info = dist_info;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, ol_w_id);
        stmt.setInt(2, ol_d_id);
        stmt.setInt(3, ol_o_id);
        stmt.setInt(4, ol_number);
        stmt.setInt(5, ol_i_id);
        if (ol_o_id < 2101) {
            stmt.setTimestamp(6, ol_delivery_d);
        } else {
            stmt.setNull(6, java.sql.Types.TIMESTAMP);
        }
        if (ol_o_id < 2101) {
            stmt.setDouble(7, 0.00);
        } else {
            stmt.setDouble(7, ol_amount);
        }
        stmt.setInt(8, ol_supply_w_id);
        stmt.setInt(9, ol_quantity);
        stmt.setString(10, ol_dist_info);
        stmt.addBatch();
    }
}
