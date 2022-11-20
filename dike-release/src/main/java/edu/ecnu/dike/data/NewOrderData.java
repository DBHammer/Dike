/*
 * NewOrderData - Store NewOrder data and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NewOrderData extends TableData {
    
    private int no_w_id;
    private int no_d_id;
    private int no_o_id;

    public NewOrderData(int w_id, int d_id, int o_id) {
        no_w_id = w_id;
        no_d_id = d_id;
        no_o_id = o_id;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, no_w_id);
        stmt.setInt(2, no_d_id);
        stmt.setInt(3, no_o_id);
        stmt.addBatch();
    }
}
