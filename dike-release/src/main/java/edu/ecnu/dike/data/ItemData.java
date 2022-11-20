/*
 * ItemData - Store Item table and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ItemData extends TableData {

    private int i_id;
    private String i_name;
    private double i_price;
    private String i_data;
    private int i_im_id;

    public ItemData(int id, String name, double price, String data, int im_id) {
        i_id = id;
        i_name = name;
        i_price = price;
        i_data = data;
        i_im_id = im_id;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, i_id);
        stmt.setString(2, i_name);
        stmt.setDouble(3, i_price);
        stmt.setString(4, i_data);
        stmt.setInt(5, i_im_id);
        stmt.addBatch();
    }
}
