/*
 * Terminal - Maintain a session with database, keep generating transaction,
 *            and change workload pattern if necessary.
 *
 * Copyright (C) 2003, Raul Barbosa
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.control;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.AtomicDouble;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.transaction.TxnDelivery;
import edu.ecnu.dike.transaction.TxnGlobalDeadlock;
import edu.ecnu.dike.transaction.TxnGlobalSnapshot;
import edu.ecnu.dike.transaction.TxnNewOrder;
import edu.ecnu.dike.transaction.TxnOrderStatus;
import edu.ecnu.dike.transaction.TxnPayment;
import edu.ecnu.dike.transaction.TxnStockLevel;
import edu.ecnu.dike.transaction.TxnTemplate;
import edu.ecnu.dike.transaction.TxnUpdateItem;
import edu.ecnu.dike.transaction.TxnUpdateStock;
import edu.ecnu.dike.util.GetDistrictNumUtil;
import edu.ecnu.dike.util.PrintExceptionUtil;

public class Terminal implements Runnable {

    private final Logger log = Logger.getLogger(Terminal.class);

    private BasicRandom rnd;
    private Client client;
    private DbConnection dbConn;
    private int threadID;
    private int terminalID;
    private int[] cumWeightList;
    private int numWarehouses;
    private int leftBound;
    private int rightBound;
    private int terminalWarehouseID;
    private int terminalDistrictID;
    private String warehouseDistribution;
    private boolean bandTransaction;
    private boolean terminalWarehouseFixed;
    private boolean readWriteSeperation;
    private boolean dynamicDistrict;
    private RuntimeProperty runtimeProps;
    private HashMap<Integer, Integer> warehouseDistrictNum;
    private AtomicDouble conflictProb = new AtomicDouble(0);
    private LinkedList<Integer> sleepTimeList = new LinkedList<>();
    private volatile boolean conflictFixed = false;
    private volatile boolean stopRunningSignal = false;

    public Terminal(RuntimeProperty runtimeProps, ConnectionProperty connProps, Client client, BasicRandom rnd,
            int threadID) {
        this.client = client;
        this.rnd = rnd;
        this.threadID = threadID;
        this.terminalID = threadID + runtimeProps.getLeftRange() - 1;
        this.runtimeProps = runtimeProps;

        // change random seed for each terminal
        rnd = client.getRnd().newRandom();

        // get mixture transaction rate
        int[] weightList = runtimeProps.getTransactionsWeight();
        cumWeightList = new int[weightList.length];
        cumWeightList[0] =  weightList[0];
        for (int i = 1; i < cumWeightList.length; i++) {
            cumWeightList[i] = cumWeightList[i - 1] + weightList[i];
        }

        // control properties
        numWarehouses = runtimeProps.getWarehouses();
        leftBound = runtimeProps.getLeftRange();
        rightBound = runtimeProps.getRightRange();
        warehouseDistribution = runtimeProps.getWarehouseDistribution();
        terminalWarehouseFixed = runtimeProps.isTerminalWarehouseFixed();
        readWriteSeperation = runtimeProps.isReadWriteSeperation();
        bandTransaction = runtimeProps.isBandTransaction();
        dynamicDistrict = runtimeProps.isDynamicDistrict();

        // start database connection
        dbConn = new DbConnection(connProps);
        try {
            dbConn.startConnection();
        } catch (SQLException se) {
            log.error("Terminal-" + terminalID + " fail to get database connection, " + se.getMessage());
        }

        // init/get the map from warehouse id to district num
        if (dynamicDistrict) {
            try {
                warehouseDistrictNum = GetDistrictNumUtil.getWarehouseDistrictNum(numWarehouses,
                        dbConn.getConnection());
            } catch (SQLException se) {
                log.error("Fail to initialize partition map, " + se.getMessage());
            }
        }
    } // end Terminal

    // get thread sleep signal
    private void sleepIfNecessary() {
        if (sleepTimeList != null && !sleepTimeList.isEmpty()) {
            int sleepTime;
            synchronized (sleepTimeList) {
                sleepTime = sleepTimeList.pollFirst();
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                log.error("Terminal-" + terminalID + " catch interrupted exception while sleeping");
            }
        }
    }

    // determine terminal warehouse id and terminal district id
    private void setWarehouseDistrict(TxnTemplate txn) {
        if (conflictProb.get() != 0) {
            // dynamically change terminal warehouse, put all conflicts in warehouse 1 and
            // district 1
            if (conflictFixed == true) {
                if (terminalID < conflictProb.get()) {
                    terminalWarehouseID = 1;
                    terminalDistrictID = 1;
                } else {
                    terminalWarehouseID = terminalID + 1;
                    terminalDistrictID = rnd.getDistrictID();
                }
            } else {
                if (rnd.nextDouble(0, 100) <= conflictProb.get()) {
                    terminalWarehouseID = 1;
                    terminalDistrictID = 1;
                } else {
                    terminalWarehouseID = terminalID + 1;
                    terminalDistrictID = terminalID == 0 ? 2 : rnd.getDistrictID();
                }
            }
        } else {
            if (warehouseDistribution.equals("uniform")) {
                // uniform distribution
                terminalWarehouseID = terminalWarehouseFixed ? terminalID + 1
                        : rnd.getWarehouseID(leftBound, rightBound);
                terminalDistrictID = dynamicDistrict
                        ? rnd.getDynamicDistrictID(terminalWarehouseID, warehouseDistrictNum)
                        : rnd.getDistrictID();
            } else {
                // zipfian distribution
                Pair<Integer, Integer> pairID = rnd.getZipfianWarehouseDistrict(numWarehouses, warehouseDistribution,
                        leftBound, rightBound);
                terminalWarehouseID = terminalWarehouseFixed ? terminalID + 1 : pairID.getKey();
                terminalDistrictID = pairID.getValue();
            }
        }
        txn.setTerminalID(terminalID);
        txn.setTerminalWarehouseID(terminalWarehouseID);
        txn.setTerminalDistrictID(terminalDistrictID);
    }

    @Override
    public void run() {
        while (!stopRunningSignal) {
            sleepIfNecessary();
            // randomly generate number between 1 and 100, determine to run which
            // transaction
            TxnTemplate txn = null;
            try {
                int rndNumber = bandTransaction ? terminalID + 1 : rnd.nextInt(1, 100);
                if (rndNumber <= cumWeightList[0]) {
                    // construct new order (performance test)
                    txn = new TxnNewOrder(dbConn, runtimeProps, rnd, this);
                    txn.getPartitionSet();
                } else if (rndNumber <= cumWeightList[1]) {
                    // construct payment (performance test)
                    txn = new TxnPayment(dbConn, runtimeProps, rnd);
                } else if (rndNumber <= cumWeightList[2]) {
                    // construct order status (performance test)
                    txn = new TxnOrderStatus(dbConn, runtimeProps, rnd);
                } else if (rndNumber <= cumWeightList[3]) {
                    // construct stock level (performance test)
                    if (readWriteSeperation) {
                        dbConn.setWeakRead();
                    }
                    txn = new TxnDelivery(dbConn, runtimeProps, rnd);
                } else if (rndNumber <= cumWeightList[4]) {
                    // construct delivery (performance test)
                    txn = new TxnStockLevel(dbConn, runtimeProps, rnd);
                    txn.getPartitionSet();
                } else if (rndNumber <= cumWeightList[5]) {
                    // construct update item (performance test)
                    txn = new TxnUpdateItem(dbConn, runtimeProps, rnd);
                } else if (rndNumber <= cumWeightList[6]) {
                    // construct update stock (performance test)
                    txn = new TxnUpdateStock(dbConn, runtimeProps, rnd, terminalID);
                } else if (rndNumber <= cumWeightList[7]) {
                    // construct global snapshot (function test)
                    txn = new TxnGlobalSnapshot(dbConn, runtimeProps, rnd);
                } else if (rndNumber <= cumWeightList[8]) {
                    // construct global deadlock (function test)
                    txn = new TxnGlobalDeadlock(dbConn, runtimeProps, rnd);
                }
                setWarehouseDistrict(txn);
                txn.generateData();
                txn.txnStart();
                txn.txnExecute();
            } catch (SQLException se) {
                // handle sql exceptions
                int retryCnt = 0;
                while (se != null) {
                    String sqlState = se.getSQLState();
                    switch (sqlState) {
                        case "08S01":
                        case "S1009":
                        case "08003":
                        case "57P01":
                        case "08001":
                            // catch communications link failure and restart connection
                            // try reconnect to database at most to 10 times
                            txn.setError();
                            log.debug("Get communication link failure" + PrintExceptionUtil.getSQLExceptionInfo(se));
                            while (true) {
                                try {
                                    retryCnt++;
                                    restartConnection();
                                    break;
                                } catch (SQLException serc) {
                                    log.debug("Get sql exception while restart connection"
                                            + PrintExceptionUtil.getSQLExceptionInfo(serc));
                                    if (retryCnt > 10) {
                                        log.fatal("Terminal-" + terminalID
                                                + " lose connection to database while running benchmark");
                                        client.signalTerminalEnd(threadID);
                                        return;
                                    }
                                }
                            }
                            break;
                        case "25000":
                        case "40001":
                            if (runtimeProps.getRollbackRetry()) {
                                log.debug(
                                        "Get transaction retry exception" + PrintExceptionUtil.getSQLExceptionInfo(se));
                                while (true) {
                                    try {
                                        retryCnt++;
                                        txn.txnRetry();
                                        break;
                                    } catch (SQLException sse) {
                                        log.debug("Get exception while retry transaction"
                                                + PrintExceptionUtil.getSQLExceptionInfo(sse));
                                        try {
                                            if (retryCnt > 5) {
                                                txn.txnRollback();
                                                log.debug("Fail to execute transaction after retry 5 times");
                                            }
                                        } catch (SQLException sec) {

                                        }
                                    }
                                }
                                break;
                            }
                        default:
                            // rollback sql exceptions by default
                            try {
                                txn.txnRollback();
                            } catch (SQLException serbk) {
                                txn.setError();
                                log.debug("Get unexpected SQLException on rollback"
                                        + PrintExceptionUtil.getSQLExceptionInfo(serbk));
                            }
                            log.debug("Catch unexpected SQLException when executing transaction " + txn.getTxnType()
                                    + PrintExceptionUtil.getSQLExceptionInfo(se));
                            break;
                    }
                    se = se.getNextException();
                }
            } finally {
                txn.txnEnd();
                logTrace(txn);
                // record all transactions including rollback and error
                client.resultAppend(txn.resultLine());
                client.signalTxnEnd(terminalID, terminalWarehouseID, txn.isRollback(), txn.isError());
            }
        } // end while
        dbConn.closeConnectionIfAlive();
        client.signalTerminalEnd(terminalID);
    } // end run

    // client send terminate signal to terminals
    public void signalStopRunning() {
        stopRunningSignal = true;
    }

    // try to close current jdbc connection and start a new one
    public void restartConnection() throws SQLException {
        dbConn.closeConnectionIfAlive();
        dbConn.startConnection();
    }

    // client append sleep time to sleepTimeList every second to simulate dynamic
    // workload
    public void appendSleepTime(int sleepTime) {
        synchronized (sleepTimeList) {
            sleepTimeList.add(sleepTime);
        }
    }

    // client dynamically set contention probability
    public void setConflictProb(double prob) {
        conflictProb.set(prob);
    }

    // client fix conflicts to front terminals
    public void fixConflict() {
        conflictFixed = true;
    }

    // client unfix conflicts
    public void unFixConflict() {
        conflictFixed = false;
    }

    // terminal increase conflict intensity
    public void increaseCI() {
        if (terminalWarehouseID == 1 && terminalDistrictID == 1) {
            client.increaseCI();
        }
    }

    // terminal decrease conflict intensity
    public void decreaseCI() {
        if (terminalWarehouseID == 1 && terminalDistrictID == 1) {
            client.decreaseCI();
        }
    }

    // terminal set transaction mixture rate
    public void setTransactions(int[] transactions) {
        synchronized (cumWeightList) {
            cumWeightList[0] = transactions[0];
            for (int i = 1; i < cumWeightList.length; i++) {
                cumWeightList[i] = cumWeightList[i - 1] + transactions[i];
            }
        }
    }

    // trace transaction parameters
    public void logTrace(TxnTemplate txn) {
        if (log.isTraceEnabled()) {
            log.trace(txn.toString());
        }
    }
}