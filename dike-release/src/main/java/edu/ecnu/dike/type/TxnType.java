/*
 * TxnType - The list of transaction type composing Dike workload.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.type;

public enum TxnType {
    TXN_NONE("NONE"),
    TXN_NEW_ORDER("NEW_ORDER"),
    TXN_PAYMENT("PAYMENT"),
    TXN_ORDER_STATUS("ORDER_STATUS"),
    TXN_STOCK_LEVEL("STOCK_LEVEL"),
    TXN_DELIVERY("DELIVERY"),
    TXN_UPDATE_ITEM("UPDATE_ITEM"),
    TXN_UPDATE_STOCK("UPDATE_STOCK"),
    TXN_GLOBAL_SNAPSHOT("GLOBAL_SNAPSHOT"),
    TXN_GLOBAL_DEADLOCK("GLOBAL_DEADLOCK"),
    TXN_UNKOWN("UNKOWN");

    private final String name;

    private TxnType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
