/*
 * TableData - Abstract class to store table data.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class TableData {
    
    public abstract void setParameters(PreparedStatement stmt) throws SQLException;
}
