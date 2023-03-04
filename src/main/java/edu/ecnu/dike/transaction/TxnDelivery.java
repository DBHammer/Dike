/*
 * TxnDelivery - Delivery transaction workload.
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
import java.util.Arrays;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.type.TxnType;

public class TxnDelivery extends TxnTemplate {

    // value initialized by randomization
    private int w_id;
    private int o_carrier_id;
    private String ol_delivery;

    // value selected from table
    private String execution_status = null;
    private int delivered_o_id[] = new int[10];

    public TxnDelivery(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_DELIVERY;
        stmtDelivery = dbConn.getStmtDelivery();
    }

    @Override
    public void generateData() {
        w_id = terminalWarehouseID;
        o_carrier_id = rnd.nextInt(1, 10);
        ol_delivery = new Timestamp(System.currentTimeMillis()).toString();
        for (int i = 0; i < 10; i++) {
            delivered_o_id[i] = -1;
        }
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        int d_id = 0;
        int o_id = 0;
        int c_id = 0;
        double sum_ol_amount = 0;
        long now = System.currentTimeMillis();
        for (d_id = 1; d_id <= 10; d_id++) {
            // try to find the oldest undelivered order for this district
            stmt = stmtDelivery.getStmtSelectOldestNewOrder();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                continue;
            }
            o_id = rs.getInt("no_o_id"); // only one o_id
            rs.close();

            // delete the lastest new order line in each district
            stmt = stmtDelivery.getStmtDeleteOldestNewOrder();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, o_id);
            stmt.executeUpdate();

            // update the order setting the o_carrier_id
            stmt = stmtDelivery.getStmtUpdateOrder();
            stmt.setInt(1, o_carrier_id);
            stmt.setInt(2, w_id);
            stmt.setInt(3, d_id);
            stmt.setInt(4, o_id);
            stmt.executeUpdate();

            // get the o_c_id from the order
            stmt = stmtDelivery.getStmtSelectOrder();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, o_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("order in DELIVERY for" + " o_w_id=" + w_id + " o_d_id=" + d_id + " o_id="
                        + o_id + " not found", "02000");
            }
            c_id = rs.getInt("o_c_id");
            rs.close();

            // update order_line setting the ol_delivery_d
            stmt = stmtDelivery.getStmtUpdateOrderLine();
            stmt.setTimestamp(1, new java.sql.Timestamp(now));
            stmt.setInt(2, w_id);
            stmt.setInt(3, d_id);
            stmt.setInt(4, o_id);
            stmt.executeUpdate();

            // select the sum(ol_amount) from order_line
            stmt = stmtDelivery.getStmtSelectSumOLAmount();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, o_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("sum(OL_AMOUNT) for ORDER_LINEs with " + " ol_w_id=" + w_id + " ol_d_id=" + d_id
                        + " ol_o_id=" + o_id + " not found", "02000");
            }
            sum_ol_amount = rs.getDouble("sum_ol_amount");
            rs.close();

            // update the customer
            stmt = stmtDelivery.getStmtUpdateCustomer();
            switch (dbType) {
                case DB_COCKROACHDB:
                    stmt.setBigDecimal(1, new BigDecimal(sum_ol_amount));
                    break;
                default:
                    stmt.setDouble(1, sum_ol_amount);
            }
            stmt.setInt(2, w_id);
            stmt.setInt(3, d_id);
            stmt.setInt(4, c_id);
            stmt.executeUpdate();

            // recored the delivered o_id
            delivered_o_id[d_id - 1] = o_id;
        }
        conn.commit();
    }

    @Override
    public String toString() {
        return "TxnDelivery [delivered_o_id=" + Arrays.toString(delivered_o_id) + ", execution_status="
                + execution_status + ", o_carrier_id=" + o_carrier_id + ", ol_delivery=" + ol_delivery + ", w_id="
                + w_id + "]";
    }
}
