/*
 * TxnNewOrder - NewOrder transaction workload.
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
import java.util.HashMap;
import java.util.HashSet;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.control.Terminal;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.StatisticsCalc;
import edu.ecnu.dike.type.TxnType;
import edu.ecnu.dike.util.ItemCacheUtil;
import edu.ecnu.dike.util.PartitionMapUtil;
import edu.ecnu.dike.util.SessionRouteUtil;

public class TxnNewOrder extends TxnTemplate {

    // variable naming (filled in prepared statement) is consistent with schema
    // style
    // value initialized by randomization
    private int w_id;
    private int d_id;
    private int c_id;
    private int ol_supply_w_id[] = new int[15];
    private int ol_i_id[] = new int[15];
    private int ol_quantity[] = new int[15];

    // value selected from table
    private String c_last;
    private String c_credit;
    private double c_discount;
    private double w_tax;
    private double d_tax;
    private int ol_cnt;
    private int o_all_local;
    private int ol_seq[] = new int[15];

    private int o_id;
    private String o_entry_d;
    private double total_amount;
    private String execution_status;
    private String i_name[] = new String[15];
    private int s_quantity[] = new int[15];
    private String brand_generic[] = new String[15];
    private double i_price[] = new double[15];
    private double ol_amount[] = new double[15];

    // control properties
    private int spanNode;
    private int partitions;
    private int partitionSize;
    private double distributedRate;
    private Terminal terminal;
    private HashSet<String> partitionSet;
    private HashMap<Integer, String> partitionMap;

    public TxnNewOrder(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd, Terminal terminal) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_NEW_ORDER;
        stmtNewOrder = dbConn.getStmtNewOrder();
        spanNode = runtimeProps.getNewOrderSpanNode();
        partitions = runtimeProps.getPartitions();
        partitionSize = runtimeProps.getWarehouses() / partitions;
        distributedRate = runtimeProps.getNewOrderDistributedRate();
        if (runtimeProps.getStatisticsCalc()) {
            spanNode = StatisticsCalc.getSpanNode(spanNode, runtimeProps.getPhysicalNode());
        }
        this.terminal = terminal;
    } // end TxnNewOrder

    @Override
    public void generateData() {
        w_id = terminalWarehouseID;
        d_id = terminalDistrictID;
        c_id = rnd.getCustomerID();

        // randomly generate order line information(ol_i_id, ol_supply_w_id,
        // ol_quantity)
        int i = 0;
        int nodes = 1;
        int o_ol_cnt = distributedRate > 0 || spanNode > 1 ? 10 : rnd.nextInt(5, 15);
        if (rnd.nextInt(1, 100) > distributedRate || nodes == spanNode) {
            // local transaction, ol_supply_w_id equals to w_id
            while (i < o_ol_cnt) {
                ol_i_id[i] = rnd.getItemID();
                ol_supply_w_id[i] = w_id;
                ol_quantity[i] = rnd.nextInt(1, 10);
                i++;
            }
        } else {
            // distributed transaction, random generate some different ol_supply_w_id, which
            // sum up to 'spanNode'
            int w_id_tmp = 0;
            ArrayList<Integer> w_id_exists = new ArrayList<>();
            w_id_exists.add(w_id);
            while (i < o_ol_cnt) {
                if (nodes < spanNode) {
                    // TODO what about use some elegant sampling methods
                    do {
                        w_id_tmp = rnd.nextInt(runtimeProps.getLeftRange(), runtimeProps.getRightRange());
                    } while (w_id_exists.contains(w_id_tmp));
                    nodes++;
                    ol_supply_w_id[i] = w_id_tmp;
                    w_id_exists.add(w_id_tmp);
                    addPartitionSet(w_id_tmp);
                } else {
                    ol_supply_w_id[i] = w_id;
                }
                ol_i_id[i] = rnd.getItemID();
                ol_quantity[i] = rnd.nextInt(1, 10);
                i++;
            }
        }

        o_all_local = 1;
        o_entry_d = new Timestamp(System.currentTimeMillis()).toString();
        for (ol_cnt = 0; ol_cnt < 15 && ol_i_id[ol_cnt] != 0; ol_cnt++) {
            ol_seq[ol_cnt] = ol_cnt;
            if (ol_supply_w_id[ol_cnt] != w_id) {
                o_all_local = 0;
            }
        }
        o_ol_cnt = ol_cnt;

        // bubble sort
        // process orders in the order of the sequence of ol_supply_w_id, ol_i_id to
        // avoid possible deadlocks(sequential locking)
        boolean orderly = false;
        for (int x = 0; x < ol_cnt - 1 && !orderly; x++) {
            orderly = true;
            for (int y = x + 1; y < ol_cnt; y++) {
                if (ol_supply_w_id[ol_seq[y]] < ol_supply_w_id[ol_seq[x]]) {
                    int tmp = ol_seq[x];
                    ol_seq[x] = ol_seq[y];
                    ol_seq[y] = tmp;
                    orderly = false;
                } else if (ol_supply_w_id[ol_seq[y]] == ol_supply_w_id[ol_seq[x]]
                        && ol_i_id[ol_seq[y]] < ol_i_id[ol_seq[x]]) {
                    int tmp = ol_seq[x];
                    ol_seq[x] = ol_seq[y];
                    ol_seq[y] = tmp;
                    orderly = false;
                }
            }
        }

        if (partitionSet != null) {
            txnDistributed = partitionSet.size() > 1 ? 1 : 0;
            txnSpanNode = partitionSet.size();
        }
    } // end generateData

    @Override
    public void txnExecute() throws SQLException {
        try {
            // select the required data from customer and warehouse
            PreparedStatement stmt = stmtNewOrder.getStmtSelectWhseCust();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, c_id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException(
                        "warehouse or customer for w_id=" + w_id + " d_id=" + d_id + " c_id=" + c_id + " not found",
                        "02000");
            }
            w_tax = rs.getDouble("w_tax");
            c_last = rs.getString("c_last");
            c_credit = rs.getString("c_credit");
            c_discount = rs.getDouble("c_discount");
            rs.close();

            terminal.increaseCI();
            // select the required data from district
            stmt = stmtNewOrder.getStmtSelectDist();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("district for" + " w_id=" + w_id + " d_id=" + d_id + " not found", "02000");
            }
            d_tax = rs.getDouble("d_tax");
            int o_id = rs.getInt("d_next_o_id");
            rs.close();

            // update the district bumping the d_next_o_id
            stmt = stmtNewOrder.getStmtUpdateDist();
            stmt.setInt(1, w_id);
            stmt.setInt(2, d_id);
            stmt.executeUpdate();

            // insert the order row
            stmt = stmtNewOrder.getStmtInsertOrder();
            stmt.setInt(1, o_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, w_id);
            stmt.setInt(4, c_id);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(6, ol_cnt);
            stmt.setInt(7, o_all_local);
            stmt.executeUpdate();

            // insert the neworder row
            stmt = stmtNewOrder.getStmtInsertNewOrder();
            stmt.setInt(1, o_id);
            stmt.setInt(2, d_id);
            stmt.setInt(3, w_id);
            stmt.executeUpdate();

            // per orderline
            PreparedStatement insertOrderLineBatch = stmtNewOrder.getStmtInsertOrderLine();
            PreparedStatement updateStockBatch = stmtNewOrder.getStmtUpdateStock();

            for (int i = 0; i < ol_cnt; i++) {
                int ol_number = i + 1;
                int seq = ol_seq[i];

                // select item
                stmt = stmtNewOrder.getStmtSelectItem();
                stmt.setInt(1, ol_i_id[seq]);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    throw new SQLException("item " + ol_i_id[seq] + " not fount", "02000");
                }
                i_name[seq] = rs.getString("i_name");
                i_price[seq] = rs.getDouble("i_price");
                String i_data = rs.getString("i_data");
                rs.close();

                // select stock for update
                stmt = stmtNewOrder.getStmtSelectStock();
                stmt.setInt(1, ol_supply_w_id[seq]);
                stmt.setInt(2, ol_i_id[seq]);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    throw new SQLException(
                            "STOCK with" + " s_w_id=" + ol_supply_w_id[seq] + " s_i_id=" + ol_i_id[seq] + " not fount",
                            "02000");
                }
                s_quantity[seq] = rs.getInt("s_quantity");

                // leave the result set open, we need it for the s_dist_xx
                ol_amount[seq] = i_price[seq] * ol_quantity[seq];
                if (i_data.contains("ORIGINAL") && rs.getString("s_data").contains("ORIGINAL")) {
                    brand_generic[seq] = new String("B");
                } else {
                    brand_generic[seq] = new String("G");
                }
                total_amount += ol_amount[seq] * (1.0 - c_discount) * (1.0 + w_tax + d_tax);

                // update the stock row
                if (s_quantity[seq] >= ol_quantity[seq] + 10) {
                    updateStockBatch.setInt(1, s_quantity[seq] - ol_quantity[seq]);
                } else {
                    updateStockBatch.setInt(1, s_quantity[seq] + 91);
                }
                updateStockBatch.setInt(2, ol_quantity[seq]);
                if (ol_supply_w_id[seq] == w_id) {
                    updateStockBatch.setInt(3, 0);
                } else {
                    updateStockBatch.setInt(3, 1);
                }
                updateStockBatch.setInt(4, ol_supply_w_id[seq]);
                updateStockBatch.setInt(5, ol_i_id[seq]);
                updateStockBatch.addBatch();

                // insert the orderline row
                insertOrderLineBatch.setInt(1, o_id);
                insertOrderLineBatch.setInt(2, d_id);
                insertOrderLineBatch.setInt(3, w_id);
                insertOrderLineBatch.setInt(4, ol_number);
                insertOrderLineBatch.setInt(5, ol_i_id[seq]);
                insertOrderLineBatch.setInt(6, ol_supply_w_id[seq]);
                insertOrderLineBatch.setInt(7, ol_quantity[seq]);
                insertOrderLineBatch.setDouble(8, ol_amount[seq]);
                if (d_id != 10) {
                    insertOrderLineBatch.setString(9, rs.getString("s_dist_0" + d_id));
                } else {
                    insertOrderLineBatch.setString(9, rs.getString("s_dist_10"));
                }
                insertOrderLineBatch.addBatch();
            }
            rs.close();
            updateStockBatch.executeBatch();
            insertOrderLineBatch.executeBatch();
            execution_status = new String("order placed");

            conn.commit();
        } finally {
            // clear batch operations
            stmtNewOrder.getStmtInsertOrderLine().clearBatch();
            stmtNewOrder.getStmtUpdateStock().clearBatch();
            // decrease conflict intensity
            terminal.decreaseCI();
        }
    }

    // initialize partition set
    @Override
    public void getPartitionSet() throws SQLException {
        partitionMap = PartitionMapUtil.getPartitionMap(conn, runtimeProps);
        if (partitionMap != null) {
            partitionSet = new HashSet<>();
            switch (dbType) {
                case DB_OCEANBASE:
                    partitionSet.add(partitionMap.get(w_id % partitions));
                    break;
                case DB_TIDB:
                    partitionSet.add(ItemCacheUtil.getItemCache(conn, dbType));
                    partitionSet.add(SessionRouteUtil.getSessionRoute(conn, dbType));
                    int region = (w_id - 1) / partitionSize;
                    partitionSet.add(region >= partitions ? partitionMap.get(region - 1) : partitionMap.get(region));
                    break;
                case DB_COCKROACHDB:
                    partitionSet.add(SessionRouteUtil.getSessionRoute(conn, dbType));
                    int rack = (w_id - 1) / partitionSize;
                    partitionSet.add(rack >= partitions ? partitionMap.get(rack - 1) : partitionMap.get(rack));
                    break;
                default:
                    break;
            }
        }
    }

    // add partition nodes for distributed transaction
    public void addPartitionSet(int w_id_tmp) {
        if (partitionSet != null) {
            switch (dbType) {
                case DB_OCEANBASE:
                    partitionSet.add(partitionMap.get(w_id_tmp % partitions));
                    break;
                case DB_TIDB:
                    int region = (w_id_tmp - 1) / partitionSize;
                    partitionSet.add(
                            region >= partitions ? partitionMap.get(region - 1) : partitionMap.get(region));
                    break;
                case DB_COCKROACHDB:
                    int rack = (w_id_tmp - 1) / partitionSize;
                    partitionSet
                            .add(rack >= partitions ? partitionMap.get(rack - 1) : partitionMap.get(rack));
                    break;
                default:
                    break;
            }
        }
    }

    public int[] getSupplyWid() {
        return ol_supply_w_id;
    }

    public int getSpanNode() {
        return spanNode;
    }

    public int getOlCnt() {
        return ol_cnt;
    }

    @Override
    public String toString() {
        return "TxnNewOrder [w_id=" + w_id + ", d_id=" + d_id + ", c_id=" + c_id + ", ol_supply_w_id="
                + Arrays.toString(ol_supply_w_id) + ", ol_i_id=" + Arrays.toString(ol_i_id) + ", ol_quantity="
                + Arrays.toString(ol_quantity) + ", c_last=" + c_last + ", c_credit=" + c_credit + ", c_discount="
                + c_discount + ", w_tax=" + w_tax + ", d_tax=" + d_tax + ", ol_cnt=" + ol_cnt + ", o_all_local="
                + o_all_local + ", ol_seq=" + Arrays.toString(ol_seq) + ", o_id=" + o_id + ", o_entry_d=" + o_entry_d
                + ", total_amount=" + total_amount + ", execution_status=" + execution_status + ", i_name="
                + Arrays.toString(i_name) + ", s_quantity=" + Arrays.toString(s_quantity) + ", brand_generic="
                + Arrays.toString(brand_generic) + ", i_price=" + Arrays.toString(i_price) + ", ol_amount="
                + Arrays.toString(ol_amount) + ", spanNode=" + spanNode + ", partitions=" + partitions
                + ", partitionSize=" + partitionSize + ", distributedRate=" + distributedRate + ", terminal=" + terminal
                + ", partitionSet=" + partitionSet + ", partitionMap=" + partitionMap + "]";
    }

}