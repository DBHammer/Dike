/*
 * TxnPayment - Payment transaction workload.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;

public class TxnPayment extends TxnTemplate {

    // value initialized by randomization
    private int w_id;
    private int d_id;
    private int c_id;
    private int c_d_id;
    private int c_w_id;
    private String c_last;
    private double h_amount;
    private BigDecimal h_amount_d;

    // value selected from table
    private String w_name;
    private String w_street_1;
    private String w_street_2;
    private String w_city;
    private String w_state;
    private String w_zip;
    private String d_name;
    private String d_street_1;
    private String d_street_2;
    private String d_city;
    private String d_state;
    private String d_zip;
    private String c_first;
    private String c_middle;
    private String c_street_1;
    private String c_street_2;
    private String c_city;
    private String c_state;
    private String c_zip;
    private String c_phone;
    private String c_since;
    private String c_credit;
    private double c_credit_lim;
    private double c_discount;
    private double c_balance;
    private String c_data;
    private String h_date;

    public TxnPayment(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_PAYMENT;
        stmtPayment = dbConn.getStmtPayment();
    }

    @Override
    public void generateData() {
        w_id = terminalWarehouseID;
        d_id = terminalDistrictID;
        c_w_id = w_id;
        c_d_id = d_id;
        if (rnd.nextInt(1, 100) <= 60) {
            c_last = rnd.getCLast();
            c_id = 0;
        } else {
            c_last = null;
            c_id = rnd.getCustomerID();
        }
        h_amount = ((double) rnd.nextLong(100, 500000)) / 100.0;
        h_amount_d = new BigDecimal(h_amount);
    }

    @Override
    public void txnExecute() throws SQLException {
        // update the DISTRICT
        PreparedStatement stmt = stmtPayment.getStmtUpdateDistrict();
        switch (dbType) {
            case DB_COCKROACHDB:
                stmt.setBigDecimal(1, h_amount_d);
                break;
            default:
                stmt.setDouble(1, h_amount);
                break;
        }

        stmt.setInt(2, w_id);
        stmt.setInt(3, d_id);
        stmt.executeUpdate();

        // select the DISTRICT
        stmt = stmtPayment.getStmtSelectDistrict();
        stmt.setInt(1, w_id);
        stmt.setInt(2, d_id);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            rs.close();
            throw new SQLException("district for" + " w_id=" + w_id + " d_id=" + d_id + " not found", "02000");
        }
        d_name = rs.getString("d_name");
        d_street_1 = rs.getString("d_street_1");
        d_street_2 = rs.getString("d_street_2");
        d_city = rs.getString("d_city");
        d_state = rs.getString("d_state");
        d_zip = rs.getString("d_zip");
        rs.close();

        // update the WAREHOUSE
        stmt = stmtPayment.getStmtUpdateWarehouse();
        switch (dbType) {
            case DB_COCKROACHDB:
                stmt.setBigDecimal(1, h_amount_d);
                break;
            default:
                stmt.setDouble(1, h_amount);
                break;
        }
        stmt.setInt(2, w_id);
        stmt.executeUpdate();

        // select the WAREHOUSE
        stmt = stmtPayment.getStmtSelectWarehouse();
        stmt.setInt(1, w_id);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            rs.close();
            throw new SQLException("warehouse for" + " w_id=" + w_id + " not found", "02000");
        }
        w_name = rs.getString("w_name");
        w_street_1 = rs.getString("w_street_1");
        w_street_2 = rs.getString("w_street_2");
        w_city = rs.getString("w_city");
        w_state = rs.getString("w_state");
        w_zip = rs.getString("w_zip");
        rs.close();

        // if c_last is given instead of c_id, determine the c_id
        ArrayList<Integer> c_id_list = new ArrayList<>();
        if (c_last != null) {
            stmt = stmtPayment.getStmtSelectCustomerListByLast();
            stmt.setInt(1, c_w_id);
            stmt.setInt(2, c_d_id);
            stmt.setString(3, c_last);
            rs = stmt.executeQuery();
            while (rs.next()) {
                c_id_list.add(rs.getInt("c_id"));
            }
            rs.close();
            if (c_id_list.size() == 0) {
                throw new SQLException("customers for" + " c_w_id=" + c_w_id + " c_d_id=" + c_d_id + " c_last=" + c_last
                        + " not found", "02000");
            }
            c_id = c_id_list.get((c_id_list.size() + 1) / 2 - 1);
        }

        // select the CUSTOMER
        stmt = stmtPayment.getStmtSelectCustomer();
        stmt.setInt(1, c_w_id);
        stmt.setInt(2, c_d_id);
        stmt.setInt(3, c_id);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            rs.close();
            throw new SQLException(
                    "customer for" + " c_w_id=" + c_w_id + " c_d_id=" + c_d_id + " c_id=" + c_id + " not found",
                    "02000");
        }
        c_first = rs.getString("c_first");
        c_middle = rs.getString("c_middle");
        c_last = rs.getString("c_last");
        c_street_1 = rs.getString("c_street_1");
        c_street_2 = rs.getString("c_street_2");
        c_city = rs.getString("c_city");
        c_state = rs.getString("c_state");
        c_zip = rs.getString("c_zip");
        c_phone = rs.getString("c_phone");
        c_since = rs.getTimestamp("c_since").toString();
        c_credit = rs.getString("c_credit");
        c_credit_lim = rs.getDouble("c_credit_lim");
        c_discount = rs.getDouble("c_discount");
        c_balance = rs.getDouble("c_balance");
        c_data = new String("");
        rs.close();

        // update the CUSTOMER
        c_balance -= h_amount;
        if (c_credit.equals("GC")) {
            // customer with good credit, do not update c_data
            stmt = stmtPayment.getStmtUpdateCustomer();
            switch (dbType) {
                case DB_COCKROACHDB:
                    stmt.setBigDecimal(1, h_amount_d);
                    stmt.setBigDecimal(2, h_amount_d);
                    break;
                default:
                    stmt.setDouble(1, h_amount);
                    stmt.setDouble(2, h_amount);
                    break;
            }
            stmt.setInt(3, c_w_id);
            stmt.setInt(4, c_d_id);
            stmt.setInt(5, c_id);
            stmt.executeUpdate();
        } else {
            // customer with bad credit, need to do the C_DATA work
            stmt = stmtPayment.getStmtSelectCustomerData();
            stmt.setInt(1, c_w_id);
            stmt.setInt(2, c_d_id);
            stmt.setInt(3, c_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("customer c_data for" + " c_w_id=" + c_w_id + " c_d_id=" + c_d_id + " c_id="
                        + c_id + " not found", "02000");
            }
            c_data = rs.getString("c_data");
            rs.close();

            stmt = stmtPayment.getStmtUpdateCustomerWithData();
            switch (dbType) {
                case DB_COCKROACHDB:
                    stmt.setBigDecimal(1, h_amount_d);
                    stmt.setBigDecimal(2, h_amount_d);
                    break;
                default:
                    stmt.setDouble(1, h_amount);
                    stmt.setDouble(2, h_amount);
                    break;
            }

            StringBuilder sbData = new StringBuilder();
            Formatter fmtData = new Formatter(sbData);
            fmtData.format("c_id=%d c_d_id=%d c_w_id=%d " + "d_id=%d w_id=%d h_amount=%.2f ", c_id, c_d_id, c_w_id,
                    d_id, w_id, h_amount);
            sbData.append(c_data);
            if (sbData.length() > 500) {
                sbData.setLength(500);
            }
            c_data = sbData.toString();
            fmtData.close();
            stmt.setString(3, c_data);
            stmt.setInt(4, c_w_id);
            stmt.setInt(5, c_d_id);
            stmt.setInt(6, c_id);
            stmt.executeUpdate();
        }

        // insert the HISORY row
        stmt = stmtPayment.getStmtInsertHistory();
        stmt.setInt(1, c_id);
        stmt.setInt(2, c_d_id);
        stmt.setInt(3, c_w_id);
        stmt.setInt(4, d_id);
        stmt.setInt(5, w_id);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        h_date = ts.toString();
        stmt.setTimestamp(6, ts);
        switch (dbType) {
            case DB_COCKROACHDB:
                stmt.setBigDecimal(7, h_amount_d);
                break;
            default:
                stmt.setDouble(7, h_amount);
                break;
        }
        stmt.setString(8, w_name + "    " + d_name);
        stmt.executeUpdate();
        conn.commit();
    }

    @Override
    public String toString() {
        return "TxnPayment [c_balance=" + c_balance + ", c_city=" + c_city + ", c_credit=" + c_credit
                + ", c_credit_lim=" + c_credit_lim + ", c_d_id=" + c_d_id + ", c_data=" + c_data + ", c_discount="
                + c_discount + ", c_first=" + c_first + ", c_id=" + c_id + ", c_last=" + c_last + ", c_middle="
                + c_middle + ", c_phone=" + c_phone + ", c_since=" + c_since + ", c_state=" + c_state + ", c_street_1="
                + c_street_1 + ", c_street_2=" + c_street_2 + ", c_w_id=" + c_w_id + ", c_zip=" + c_zip + ", d_city="
                + d_city + ", d_id=" + d_id + ", d_name=" + d_name + ", d_state=" + d_state + ", d_street_1="
                + d_street_1 + ", d_street_2=" + d_street_2 + ", d_zip=" + d_zip + ", h_amount=" + h_amount
                + ", h_amount_d=" + h_amount_d + ", h_date=" + h_date + ", w_city=" + w_city + ", w_id=" + w_id
                + ", w_name=" + w_name + ", w_state=" + w_state + ", w_street_1=" + w_street_1 + ", w_street_2="
                + w_street_2 + ", w_zip=" + w_zip + "]";
    }
}
