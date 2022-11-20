/*
 * DbType - The list of database type that Dike supports.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.type;

public enum DbType {
    DB_NONE("none"),
    DB_POSTGRES("postgres"),
    DB_CITUS("citus"),
    DB_COCKROACHDB("cockroachdb"),
    DB_MYSQL("mysql"),
    DB_OCEANBASE("oceanbase"),
    DB_TIDB("tidb"),
    DB_POLARDB("polardb"),
    DB_UNKNOWN("unknown");

    private final String name;

    private DbType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
