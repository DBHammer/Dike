/*
 * ItemCache - Get the cache location of table_item for TiDB.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ecnu.dike.type.DbType;

public class ItemCacheUtil {

    private String itemCache;
    private volatile static ItemCacheUtil instance;
    private ItemCacheUtil(Connection conn, DbType dbType) throws SQLException {
        switch (dbType) {
            case DB_TIDB:
                String tracelog = null;
                Statement stmt = conn.createStatement();
                HashSet<String> matchSet = new HashSet<>();
                Pattern pattern = Pattern.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");
                ResultSet rs = stmt
                        .executeQuery("trace format='log' SELECT * FROM table_item WHERE i_id = 10000");
                while (rs.next()) {
                    tracelog = rs.getString("event");
                    Matcher matcher = pattern.matcher(tracelog);
                    if (matcher.find()) {
                        matchSet.add(matcher.group(0));
                    }
                }
                assert matchSet.size() == 1;
                itemCache = new ArrayList<>(matchSet).get(0);
                rs.close();
                break;
            default:
                break;
        }
    }

    public static String getItemCache(Connection conn, DbType dbType) throws SQLException {
        if (instance == null) {
            synchronized (ItemCacheUtil.class) {
                if (instance == null) {
                    instance = new ItemCacheUtil(conn, dbType);
                }
            }
        }
        return instance.itemCache;
    } // end itemCacheInit

} // end ItemCache
