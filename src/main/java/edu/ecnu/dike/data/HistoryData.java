/*
 * HistoryData - Store History table and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class HistoryData extends TableData {

    private int h_c_id;
    private int h_c_d_id;
    private int h_c_w_id;
    private int h_d_id;
    private int h_w_id;
    private Timestamp h_date;
    private double h_amount;
    private String h_data;

    public HistoryData(int c_id, int c_d_id, int c_w_id, int d_id, int w_id, Timestamp date, double amount,
            String data) {
        h_c_id = c_id;
        h_c_d_id = c_d_id;
        h_c_w_id = c_w_id;
        h_d_id = d_id;
        h_w_id = w_id;
        h_date = date;
        h_amount = amount;
        h_data = data;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, h_c_id);
        stmt.setInt(2, h_c_d_id);
        stmt.setInt(3, h_c_w_id);
        stmt.setInt(4, h_d_id);
        stmt.setInt(5, h_w_id);
        stmt.setTimestamp(6, h_date);
        stmt.setDouble(7, h_amount);
        stmt.setString(8, h_data);
        stmt.addBatch();
    }
}
