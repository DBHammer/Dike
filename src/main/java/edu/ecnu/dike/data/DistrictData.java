/*
 * DistrictData - Store District table and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DistrictData extends TableData {

    private int d_w_id;
    private int d_id;
    private double d_ytd;
    private double d_tax;
    private int d_next_o_id;
    private String d_name;
    private String d_street_1;
    private String d_street_2;
    private String d_city;
    private String d_state;
    private String d_zip;

    public DistrictData(int w_id, int id, double ytd, double tax, int next_o_id, String name, String street_1,
            String street_2, String city, String state, String zip) {
        d_w_id = w_id;
        d_id = id;
        d_ytd = ytd;
        d_tax = tax;
        d_next_o_id = next_o_id;
        d_name = name;
        d_street_1 = street_1;
        d_street_2 = street_2;
        d_city = city;
        d_state = state;
        d_zip = zip;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, d_w_id);
        stmt.setInt(2, d_id);
        stmt.setDouble(3, d_ytd);
        stmt.setDouble(4, d_tax);
        stmt.setInt(5, d_next_o_id);
        stmt.setString(6, d_name);
        stmt.setString(7, d_street_1);
        stmt.setString(8, d_street_2);
        stmt.setString(9, d_city);
        stmt.setString(10, d_state);
        stmt.setString(11, d_zip);
        stmt.addBatch();
    }
}
