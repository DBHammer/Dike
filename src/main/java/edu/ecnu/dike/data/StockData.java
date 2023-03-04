/*
 * StockData - Store Stock data and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StockData extends TableData {

    private int s_w_id;
    private int s_i_id;
    private int s_quantity;
    private int s_ytd;
    private int s_order_cnt;
    private int s_remote_cnt;
    private String s_data;
    private String s_dist_01;
    private String s_dist_02;
    private String s_dist_03;
    private String s_dist_04;
    private String s_dist_05;
    private String s_dist_06;
    private String s_dist_07;
    private String s_dist_08;
    private String s_dist_09;
    private String s_dist_10;

    public StockData(int w_id, int i_id, int quantity, int ytd, int order_cnt, int remote_cnt, String data,
            String dist_01, String dist_02, String dist_03, String dist_04, String dist_05, String dist_06,
            String dist_07, String dist_08, String dist_09, String dist_10) {
        s_w_id = w_id;
        s_i_id = i_id;
        s_quantity = quantity;
        s_ytd = ytd;
        s_order_cnt = order_cnt;
        s_remote_cnt = remote_cnt;
        s_data = data;
        s_dist_01 = dist_01;
        s_dist_02 = dist_02;
        s_dist_03 = dist_03;
        s_dist_04 = dist_04;
        s_dist_05 = dist_05;
        s_dist_06 = dist_06;
        s_dist_07 = dist_07;
        s_dist_08 = dist_08;
        s_dist_09 = dist_09;
        s_dist_10 = dist_10;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, s_w_id);
        stmt.setInt(2, s_i_id);
        stmt.setInt(3, s_quantity);
        stmt.setInt(4, s_ytd);
        stmt.setInt(5, s_order_cnt);
        stmt.setInt(6, s_remote_cnt);
        stmt.setString(7, s_data);
        stmt.setString(8, s_dist_01);
        stmt.setString(9, s_dist_02);
        stmt.setString(10, s_dist_03);
        stmt.setString(11, s_dist_04);
        stmt.setString(12, s_dist_05);
        stmt.setString(13, s_dist_06);
        stmt.setString(14, s_dist_07);
        stmt.setString(15, s_dist_08);
        stmt.setString(16, s_dist_09);
        stmt.setString(17, s_dist_10);
        stmt.addBatch();
    }

}
