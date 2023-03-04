/*
 * CustomerData - Store Customer table and set to prepared statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CustomerData extends TableData {

    private int c_w_id;
    private int c_d_id;
    private int c_id;
    private double c_discount;
    private String c_credit;
    private String c_last;
    private String c_first;
    private double c_credit_lim;
    private double c_balance;
    private double c_ytd_payment;
    private int c_payment_cnt;
    private int c_delivery_cnt;
    private String c_street_1;
    private String c_street_2;
    private String c_city;
    private String c_state;
    private String c_zip;
    private String c_phone;
    private Timestamp c_since;
    private String c_middle;
    private String c_data;

    public CustomerData(int w_id, int d_id, int id, double discount, String credit, String last, String first,
            double credit_lim, double balance, double ytd_payment, int payment_cnt, int delivery_cnt, String street_1,
            String street_2, String city, String state, String zip, String phone, Timestamp since, String middle,
            String data) {
        c_w_id = w_id;
        c_d_id = d_id;
        c_id = id;
        c_discount = discount;
        c_credit = credit;
        c_last = last;
        c_first = first;
        c_credit_lim = credit_lim;
        c_balance = balance;
        c_ytd_payment = ytd_payment;
        c_payment_cnt = payment_cnt;
        c_delivery_cnt = delivery_cnt;
        c_street_1 = street_1;
        c_street_2 = street_2;
        c_city = city;
        c_state = state;
        c_zip = zip;
        c_phone = phone;
        c_since = since;
        c_middle = middle;
        c_data = data;
    }

    @Override
    public void setParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, c_w_id);
        stmt.setInt(2, c_d_id);
        stmt.setInt(3, c_id);
        stmt.setDouble(4, c_discount);
        stmt.setString(5, c_credit);
        stmt.setString(6, c_last);
        stmt.setString(7, c_first);
        stmt.setDouble(8, c_credit_lim);
        stmt.setDouble(9, c_balance);
        stmt.setDouble(10, c_ytd_payment);
        stmt.setInt(11, c_payment_cnt);
        stmt.setInt(12, c_delivery_cnt);
        stmt.setString(13, c_street_1);
        stmt.setString(14, c_street_2);
        stmt.setString(15, c_city);
        stmt.setString(16, c_state);
        stmt.setString(17, c_zip);
        stmt.setString(18, c_phone);
        stmt.setTimestamp(19, c_since);
        stmt.setString(20, c_middle);
        stmt.setString(21, c_data);
        stmt.addBatch();
    }
}
