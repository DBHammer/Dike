/*
 * PartitionMap - Get the mapping relations between partition id and server ip.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ecnu.dike.config.RuntimeProperty;

public class PartitionMapUtil {

    // use default tenant name and table name in oceanbase
    private static int tenant = 1001;
    private static String table = "table_warehouse";
    private volatile static HashMap<Integer, String> partitionMap;

    public static HashMap<Integer, String> getPartitionMap(Connection conn, RuntimeProperty runtimeProps)
            throws SQLException {
        if (partitionMap == null) {
            synchronized (PartitionMapUtil.class) {
                if (partitionMap == null) {
                    ResultSet rs = null;
                    int partition = runtimeProps.getPartitions();
                    switch (runtimeProps.getDbType()) {
                        case DB_OCEANBASE:
                            PreparedStatement stmtGetPartition = conn.prepareStatement(
                                    "SELECT t4.partition_Id, t4.svr_ip FROM oceanbase.gv$tenant AS t1 " +
                                            "JOIN oceanbase.gv$database AS t2 ON (t1.tenant_id = t2.tenant_id) " +
                                            "JOIN oceanbase.gv$table AS t3 ON (t2.tenant_id = t3.tenant_id  AND t2.database_id = t3.database_id AND t3.index_type = 0) "
                                            +
                                            "LEFT JOIN oceanbase.gv$partition AS t4 ON (t2.tenant_id = t4.tenant_id AND (t3.table_id = t4.table_id OR t3.tablegroup_id = t4.table_id) AND t4.role IN (1))"
                                            +
                                            "WHERE t1.tenant_id=? AND t2.database_name=? AND table_name=? " +
                                            "ORDER BY t4.partition_Id");
                            stmtGetPartition.setInt(1, tenant);
                            stmtGetPartition.setString(2, runtimeProps.getSet());
                            stmtGetPartition.setString(3, table);
                            rs = stmtGetPartition.executeQuery();
                            partitionMap = new HashMap<Integer, String>();
                            while (rs.next()) {
                                partitionMap.put(rs.getInt("t4.partition_Id"), rs.getString("t4.svr_ip"));
                            }
                            rs.close();
                            break;
                        case DB_TIDB:
                            String tracelog = null;
                            int partitionSize = runtimeProps.getWarehouses() / partition;
                            Statement stmtTraceWID = conn.createStatement();
                            Pattern pattern = Pattern.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");
                            HashSet<String> matchSet = new HashSet<>();
                            partitionMap = new HashMap<Integer, String>();
                            for (int i = 0; i < partition; i++) {
                                rs = stmtTraceWID
                                        .executeQuery("trace format='log' SELECT * FROM table_warehouse WHERE w_id = "
                                                + (i + 1) * partitionSize);
                                while (rs.next()) {
                                    tracelog = rs.getString("event");
                                    Matcher matcher = pattern.matcher(tracelog);
                                    if (matcher.find()) {
                                        matchSet.add(matcher.group(0));
                                    }
                                }
                                assert matchSet.size() == 1;
                                partitionMap.put(i, new ArrayList<>(matchSet).get(0));
                                matchSet.clear();
                            }
                            rs.close();
                            break;
                        case DB_COCKROACHDB:
                            partitionMap = new HashMap<Integer, String>();
                            for (int i = 0; i < partition; i++) {
                                partitionMap.put(i, "rack" + i);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return partitionMap;
    } // end getPartitionMap

} // end PartitionMap
