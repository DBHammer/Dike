/*
 * OrderData - Store Order data and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OrderData extends TableData {

    private int o_w_id;
    private int o_d_id;
    private int o_id;
    private int o_c_id;
    private int o_carrier_id;
    private int o_ol_cnt;
    private int o_all_local;
    private Timestamp o_entry_d;

    public OrderData(int w_id, int d_id, int id, int c_id, int carrier_id, int ol_cnt, int all_local,
            Timestamp entry_d) {
        o_w_id = w_id;
        o_d_id = d_id;
        o_id = id;
        o_c_id = c_id;
        o_carrier_id = carrier_id;
        o_ol_cnt = ol_cnt;
        o_all_local = all_local;
        o_entry_d = entry_d;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, o_w_id);
        stmt.setInt(2, o_d_id);
        stmt.setInt(3, o_id);
        stmt.setInt(4, o_c_id);
        if (o_id < 2101) {
            stmt.setInt(5, o_carrier_id);
        } else {
            stmt.setNull(5, java.sql.Types.INTEGER);
        }
        stmt.setInt(6, o_ol_cnt);
        stmt.setInt(7, o_all_local);
        stmt.setTimestamp(8, o_entry_d);
        stmt.addBatch();
    }
}
