/*
 * TxnOrderStatus - OrderStatus transaction workload.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;

public class TxnOrderStatus extends TxnTemplate {

    // value initialized by randomization
    private int w_id;
    private int d_id;
    private int c_id;
    private String c_last;

    // value selected from table
    private String c_first;
    private String c_middle;
    private double c_balance;
    private int o_id;
    private String o_entry_d;
    private int o_carrier_id;
    private int ol_supply_w_id[] = new int[15];
    private int ol_i_id[] = new int[15];
    private int ol_quantity[] = new int[15];
    private double ol_amount[] = new double[15];
    private String ol_delivery_ds[] = new String[15];

    public TxnOrderStatus(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_ORDER_STATUS;
        stmtOrderStatus = dbConn.getStmtOrderStatus();
    }

    @Override
    public void generateData() {
        w_id = terminalWarehouseID;
        d_id = terminalDistrictID;
        if (rnd.nextInt(1, 100) <= 60) {
            c_last = rnd.getCLast();
            c_id = 0;
        } else {
            c_last = null;
            c_id = rnd.getCustomerID();
        }
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;

        if (c_last != null) {
            // if c_last is given instead of c_id, determine the c_id
            stmt = stmtOrderStatus.getStmtSelectCustomerListByLast();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.setString(3, c_last);
            rs = stmt.executeQuery();
            ArrayList<Integer> c_id_list = new ArrayList<>();
            while (rs.next()) {
                c_id_list.add(rs.getInt("c_id"));
            }
            rs.close();
            if (c_id_list.size() == 0) {
                throw new SQLException(
                        "customers for" + " c_w_id=" + w_id + " c_id_id=" + d_id + " c_last=" + c_last + " not found",
                        "02000");
            }
            c_id = c_id_list.get((c_id_list.size() + 1) / 2 - 1);
        }

        // select the CUSTOMER
        stmt = stmtOrderStatus.getStmtSelectCustomer();
        stmt.setInt(1, w_id);
        stmt.setInt(2, d_id);
        stmt.setInt(3, c_id);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new SQLException(
                    "customers for" + " c_w_id=" + w_id + " c_d_id=" + d_id + " c_id=" + c_id + " not found", "02000");
        }
        c_first = rs.getString("c_first");
        c_middle = rs.getString("c_middle");
        if (c_last == null) {
            c_last = rs.getString("c_last");
        }
        c_balance = rs.getDouble("c_balance");
        rs.close();

        // select the last ORDER for this customer
        stmt = stmtOrderStatus.getStmtSelectLastOrder();
        stmt.setInt(1, w_id);
        stmt.setInt(2, d_id);
        stmt.setInt(3, c_id);
        stmt.setInt(4, w_id);
        stmt.setInt(5, d_id);
        stmt.setInt(6, c_id);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new SQLException(
                    "last order for" + " w_id=" + w_id + " d_id=" + d_id + " c_id=" + c_id + " not found", "02000");
        }
        o_id = rs.getInt("o_id");
        o_entry_d = rs.getTimestamp("o_entry_d").toString();
        o_carrier_id = rs.getInt("o_carrier_id");
        if (rs.wasNull())
            o_carrier_id = -1;
        rs.close();

        // select the order line
        int ol_idx = 0;
        stmt = stmtOrderStatus.getStmtSelectOrderLine();
        stmt.setInt(1, w_id);
        stmt.setInt(2, d_id);
        stmt.setInt(3, o_id);
        rs = stmt.executeQuery();
        while (rs.next()) {
            Timestamp ol_delivery_d;
            ol_i_id[ol_idx] = rs.getInt("ol_i_id");
            ol_supply_w_id[ol_idx] = rs.getInt("ol_supply_w_id");
            ol_quantity[ol_idx] = rs.getInt("ol_quantity");
            ol_amount[ol_idx] = rs.getDouble("ol_amount");
            ol_delivery_d = rs.getTimestamp("ol_delivery_d");
            if (ol_delivery_d != null) {
                ol_delivery_ds[ol_idx] = ol_delivery_ds.toString();
            } else {
                ol_delivery_ds[ol_idx] = null;
            }
            ol_idx++;
        }
        rs.close();
        conn.commit();
    }

    @Override
    public String toString() {
        return "TxnOrderStatus [w_id=" + w_id + ", d_id=" + d_id + ", c_id=" + c_id + ", c_last=" + c_last
                + ", c_first=" + c_first + ", c_middle=" + c_middle + ", c_balance=" + c_balance + ", o_id=" + o_id
                + ", o_entry_d=" + o_entry_d + ", o_carrier_id=" + o_carrier_id + ", ol_supply_w_id="
                + Arrays.toString(ol_supply_w_id) + ", ol_i_id=" + Arrays.toString(ol_i_id) + ", ol_quantity="
                + Arrays.toString(ol_quantity) + ", ol_amount=" + Arrays.toString(ol_amount) + ", ol_delivery_ds="
                + Arrays.toString(ol_delivery_ds) + "]";
    }
}
