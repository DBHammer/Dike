/*
 * StmtGlobalDeadlock - GlobalDeadlock transaction statement.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StmtGlobalDeadlock {

    // UpdateWarehouseBalance
    private PreparedStatement stmtUpdateBalance;

    public StmtGlobalDeadlock(Connection conn) throws SQLException {
        stmtUpdateBalance = conn.prepareStatement(
                "UPDATE table_warehouse " +
                        "SET w_ytd = w_ytd + 10 " +
                        "WHERE w_id = ?");
    }

    public PreparedStatement getUpdateBalance() {
        return stmtUpdateBalance;
    }
}
