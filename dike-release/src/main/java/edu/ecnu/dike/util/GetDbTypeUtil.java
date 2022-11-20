/*
 * GetDbTypeUtil - Transform string db to DbType.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import edu.ecnu.dike.type.DbType;

public class GetDbTypeUtil {
    
    public static DbType getDbType(String db) {
        DbType dbType = null;
        switch (db) {
			case "postgres":
				dbType = DbType.DB_POSTGRES;
				break;
			case "citus":
				dbType = DbType.DB_CITUS;
				break;
			case "cockroachdb":
				dbType = DbType.DB_COCKROACHDB;
				break;
			case "mysql":
				dbType = DbType.DB_MYSQL;
				break;
			case "oceanbase":
				dbType = DbType.DB_OCEANBASE;
				break;
			case "tidb":
				dbType = DbType.DB_TIDB;
				break;
			case "polardb":
				dbType = DbType.DB_POLARDB;
				break;
			default:
				dbType = DbType.DB_UNKNOWN;
				throw new RuntimeException("get unexpected database type, check input 'db': " + db);
		}
        return dbType;
    }
}
