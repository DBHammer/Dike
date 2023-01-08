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
                                    "SELECT PARTITION_NAME, SVR_IP FROM OCEANBASE.DBA_OB_TABLE_LOCATIONS " +
                                            "WHERE TABLE_NAME = ? AND ROLE = 'LEADER'");
                            stmtGetPartition.setString(1, table);
                            rs = stmtGetPartition.executeQuery();
                            partitionMap = new HashMap<Integer, String>();
                            while (rs.next()) {
                                partitionMap.put(Integer.parseInt(rs.getString("PARTITION_NAME").substring(1)), rs.getString("SVR_IP"));
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
