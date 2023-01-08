/*
 * WarehouseData - Store Warehouse data and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WarehouseData extends TableData {

    private int w_id;
    private double w_ytd;
    private double w_tax;
    private String w_name;
    private String w_street_1;
    private String w_street_2;
    private String w_city;
    private String w_state;
    private String w_zip;

    public WarehouseData(int id, double ytd, double tax, String name, String street_1, String street_2, String city,
            String state, String zip) {
        w_id = id;
        w_ytd = ytd;
        w_tax = tax;
        w_name = name;
        w_street_1 = street_1;
        w_street_2 = street_2;
        w_city = city;
        w_state = state;
        w_zip = zip;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, w_id);
        stmt.setDouble(2, w_ytd);
        stmt.setDouble(3, w_tax);
        stmt.setString(4, w_name);
        stmt.setString(5, w_street_1);
        stmt.setString(6, w_street_2);
        stmt.setString(7, w_city);
        stmt.setString(8, w_state);
        stmt.setString(9, w_zip);
        stmt.addBatch();
    }
}