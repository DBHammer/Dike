/*
 * GetDistrictNumUtil - Get how many districts are there in each warehouse.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class GetDistrictNumUtil {

    private volatile static HashMap<Integer, Integer> warehouseDistrictNum;

    public static HashMap<Integer, Integer> getWarehouseDistrictNum(int numWarehouse, Connection conn)
            throws SQLException {
        if (warehouseDistrictNum == null) {
            synchronized (GetDistrictNumUtil.class) {
                if (warehouseDistrictNum == null) {
                    ResultSet rs = null;
                    warehouseDistrictNum = new HashMap<>();
                    PreparedStatement stmtGetDistrictNum = conn
                            .prepareStatement("SELECT count(*) AS district_num FROM table_district WHERE d_w_id = ?");
                    for (int i = 1; i <= numWarehouse; i++) {
                        stmtGetDistrictNum.setInt(1, i);
                        rs = stmtGetDistrictNum.executeQuery();
                        if (!rs.next()) {
                            rs.close();
                            throw new SQLException("get incomplete while search for warehouse " + i, "02000");
                        }
                        warehouseDistrictNum.put(i, rs.getInt("district_num"));
                    }
                    rs.close();
                }
            }
        }
        return warehouseDistrictNum;
    }
}
