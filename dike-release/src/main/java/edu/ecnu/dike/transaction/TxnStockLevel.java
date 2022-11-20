/*
 * TxnStockLevel - StockLevel transaction workload.
 *
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.StatisticsCalc;
import edu.ecnu.dike.type.DbType;
import edu.ecnu.dike.type.TxnType;
import edu.ecnu.dike.util.PartitionMapUtil;
import edu.ecnu.dike.util.SessionRouteUtil;

public class TxnStockLevel extends TxnTemplate {

    // value initialized by randomization
    private int w_id;
    private int d_id;
    private int threshold;
    private ArrayList<Integer> w_id_list;

    // value selected from table
    private int low_stock;

    // control properties
    private int spanNode;
    private int distributedRate;
    private DbType dbType;
    private int warehouse;
    private int partitions;
    private int partitionSize;
    private HashSet<String> partitionSet;
    private HashMap<Integer, String> partitionMap;
    private StatisticsCalc statisticsCalc;

    public TxnStockLevel(DbConnection dbConn, RuntimeProperty runtimeProps, BasicRandom rnd) {
        super(dbConn, runtimeProps, rnd);
        txnType = TxnType.TXN_STOCK_LEVEL;
        stmtStockLevel = dbConn.getStmtStockLevel();
        dbType = runtimeProps.getDbType();
        partitions = runtimeProps.getPartitions();
        warehouse = runtimeProps.getWarehouses();
        partitionSize = warehouse / partitions;
        distributedRate = runtimeProps.getStockLevelDistributedRate();
        spanNode = runtimeProps.getStockLevelWIDNode();
        if (runtimeProps.getStatisticsCalc()) {
            spanNode = StatisticsCalc.getSpanNode(spanNode, runtimeProps.getPhysicalNode());
        }
    }

    @Override
    public void generateData() {
        w_id = terminalWarehouseID;
        d_id = terminalDistrictID;
        threshold = rnd.nextInt(10, 20);

        if (spanNode > 1) {
            int w_id_tmp = 0;
            w_id_list = new ArrayList<>();
            w_id_list.add(w_id);
            for (int i = 1; i < spanNode; i++) {
                do {
                    w_id_tmp = rnd.nextInt(runtimeProps.getLeftRange(), runtimeProps.getRightRange());
                } while (w_id_list.contains(w_id_tmp));
                w_id_list.add(w_id_tmp);
                addPartitionSet(w_id_tmp);
            }
        }

        if (partitionSet != null) {
            txnDistributed = partitionSet.size() > 1 ? 1 : 0;
            txnSpanNode = partitionSet.size();
        }
    }

    @Override
    public void txnExecute() throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        if (rnd.nextInt(1, 100) > distributedRate || spanNode == 1) {
            // select single warehouse
            stmt = stmtStockLevel.getStmtSelectLow();
            stmt.setInt(1, w_id);
            stmt.setInt(2, threshold);
            stmt.setInt(3, d_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("fail to get low-stock for" + " w_id=" + w_id + " d_id=" + d_id, "02000");
            }
        } else {
            // select multiple warehouses
            stmt = stmtStockLevel.getStmtSelectLowMulti(spanNode);
            int cnt = 1;
            for (int i = 0; i < spanNode; i++) {
                stmt.setInt(cnt++, w_id_list.get(i));
            }
            stmt.setInt(cnt++, threshold);
            stmt.setInt(cnt, d_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("fail to get low-stock for" + " w_id range" + w_id_list.toString() + " d_id=" + d_id, "02000");
            }
        }
        low_stock = rs.getInt("low_stock");
        rs.close();
        conn.commit();
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

    public ArrayList<Integer> getWidList() {
        return w_id_list;
    }

    @Override
    public String toString() {
        return "TxnStockLevel [d_id=" + d_id + ", dbType=" + dbType + ", distributedRate=" + distributedRate
                + ", low_stock=" + low_stock + ", partitionMap=" + partitionMap + ", partitionSet=" + partitionSet
                + ", partitionSize=" + partitionSize + ", partitions=" + partitions + ", spanNode=" + spanNode
                + ", statisticsCalc=" + statisticsCalc + ", threshold=" + threshold + ", w_id=" + w_id + ", w_id_list="
                + w_id_list + ", warehouse=" + warehouse + "]";
    }
}
