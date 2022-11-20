/*
 * TxnCounter - Record realtime txn count, rollback count and error count, 
 *              print status every second and make a report at the end of benchmark.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.perf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import edu.ecnu.dike.control.Client;
import edu.ecnu.dike.util.ListUtil;

public class TxnCounter implements Runnable {

    private final Logger log = Logger.getLogger(TxnCounter.class);
    
    private Client client;
    private int interval;
    private double runMins;

    // transaction counter in an interval
    private AtomicInteger txnCnt = new AtomicInteger();
    private AtomicInteger rollbackCnt = new AtomicInteger();
    private AtomicInteger errorCnt = new AtomicInteger();
    private AtomicInteger conflictCnt = new AtomicInteger();
    private AtomicInteger nonConflictCnt = new AtomicInteger();
    private AtomicInteger CI = new AtomicInteger();

    // benchmark stop signal
    private long startTime;
    private long targetEndTime;
    private volatile boolean stopRunningSignal = false;

    public TxnCounter(Client client, int interval, double runMins) {
        this.interval = interval;
        this.runMins = runMins;
        this.client = client;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        targetEndTime = startTime + (int) (runMins * 60 * 1000);
        while (!stopRunningSignal) {
            try {
                Thread.sleep(interval * 1000);
                if (System.currentTimeMillis() >= targetEndTime) {
                    client.signalTerminalEndWhenPossible();
                    break;
                }
            } catch (InterruptedException ie) {
                String message = "transaction counter thread catch interrupted exception while sleeping";
                log.error(message);
                throw new RuntimeException(message);
            }
            printStatus();
        }
    } // end run

    // client send stop signal to transaction counter
    public void stop() {
        stopRunningSignal = true;
    }

    // client add transaction to counter
    public void addTxn(int terminalID, int warehouseID, int rbk, int error) {
        if (rbk == 1) {
            // add rollback transaction
            rollbackCnt.getAndIncrement();
            return;
        } else if (error == 1) {
            // add error transaction
            errorCnt.getAndIncrement();
            return;
        } else {
            // add realtime transaction count
            txnCnt.getAndIncrement();
        }

        if (warehouseID == 1) {
            // add conflict transaction
            conflictCnt.getAndIncrement();
        } else {
            // add noncontention transaction
            nonConflictCnt.getAndIncrement();
        }

    } // end addTxn

    // client increase current conflict intensity
    public void increaseCI() {
        CI.incrementAndGet();
    }

    // client decrease current conflict intensity
    public void decreaseCI() {
        CI.decrementAndGet();
    }

    // client get current conflict intensity
    public int getCI() {
        return CI.get();
    }

    // daemon get finished conflict transaction number during last second and
    // reset conflictCnt to zero
    public int getConflictNum() {
        return conflictCnt.getAndSet(0);
    }

    // daemon get finished non-conflict transaction number during last second and
    // reset conflictCnt to zero
    public int getNonConflictNum() {
        return nonConflictCnt.getAndSet(0);
    }

    // print transactions, rollbacks and errors per seconds
    public void printStatus() {
        int tpsTxn = txnCnt.getAndSet(0) / interval;
        int tpsRollback = rollbackCnt.getAndSet(0) / interval;
        int tpsError = errorCnt.getAndSet(0) / interval;

        StringBuilder tpsReport = new StringBuilder("Transaction Per Second: ");
        tpsReport.append(tpsTxn);
        tpsReport.append(" Rollback: ");
        tpsReport.append(tpsRollback);
        tpsReport.append(" Error: ");
        tpsReport.append(tpsError);
        tpsReport.append(" TerminalAlive: ");
        tpsReport.append(client.getAliveTerminals());
        log.info(tpsReport.toString());
    } // end printStatus

    // give detailed report at the end of benchmark
    public void endReport(String resultDirName) {
        ArrayList<Long> newOrderLatency = new ArrayList<>();
        ArrayList<Long> paymentLatency = new ArrayList<>();
        ArrayList<Long> orderStatusLatency = new ArrayList<>();
        ArrayList<Long> stockLevelLatency = new ArrayList<>();
        ArrayList<Long> deliveryLatency = new ArrayList<>();
        ArrayList<Long> updateItemLatency = new ArrayList<>();
        ArrayList<Long> updateStockLatency = new ArrayList<>();
        ArrayList<Long> deadLockLatency = new ArrayList<>();
        ArrayList<Long> snapShotLatency = new ArrayList<>();
        int newOrderSum = 0;
        int paymentSum = 0;
        int orderStatusSum = 0;
        int stockLevelSum = 0;
        int deliverySum = 0;
        int updateItemSum = 0;
        int updateStockSum = 0;
        int deadLockSum = 0;
        int snapShotSum = 0;
        int txnSum = 0;
        int rollbackSum = 0;
        int errorSum = 0;

        Path resultcsv = Paths.get(resultDirName, "data", "result.csv");
        try {
            String line = null;
            BufferedReader reader = new BufferedReader(new FileReader(resultcsv.toString()));

            // read head line
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String item[] = line.split(",");
                long latency = Long.parseLong(item[1]);
                String txntype = item[2];
                int rbk = Integer.parseInt(item[3]);
                int error = Integer.parseInt(item[4]);
                if (rbk == 0 && error == 0) {
                    switch (txntype) {
                        case "NEW_ORDER":
                            newOrderSum++;
                            newOrderLatency.add(latency);
                            break;
                        case "PAYMENT":
                            paymentSum++;
                            paymentLatency.add(latency);
                            break;
                        case "ORDER_STATUS":
                            orderStatusSum++;
                            orderStatusLatency.add(latency);
                            break;
                        case "STOCK_LEVEL":
                            stockLevelSum++;
                            stockLevelLatency.add(latency);
                            break;
                        case "DELIVERY":
                            deliverySum++;
                            deliveryLatency.add(latency);
                            break;
                        case "UPDATE_ITEM":
                            updateItemSum++;
                            updateItemLatency.add(latency);
                            break;
                        case "UPDATE_STOCK":
                            updateStockSum++;
                            updateStockLatency.add(latency);
                            break;
                        case "GLOBAL_DEADLOCK":
                            deadLockSum++;
                            deadLockLatency.add(latency);
                            break;
                        case "GLOBAL_SNAPSHOT":
                            snapShotSum++;
                            snapShotLatency.add(latency);
                            break;
                        default:
                            break;
                    }
                    txnSum++;
                } else if (error != 0) {
                    errorSum++;
                } else {
                    rollbackSum++;
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("fail to open result csv file, " + resultcsv + ", " + fnfe.getMessage());
        } catch (IOException ioe) {
            log.error("fail to read lines from result csv file, " + resultcsv + ", " + ioe.getMessage());
        }

        Collections.sort(newOrderLatency);
        Collections.sort(paymentLatency);
        Collections.sort(orderStatusLatency);
        Collections.sort(stockLevelLatency);
        Collections.sort(deliveryLatency);
        Collections.sort(updateItemLatency);
        Collections.sort(updateStockLatency);
        Collections.sort(deadLockLatency);
        Collections.sort(snapShotLatency);

        log.info(" ");
        log.info("+-------------------------------------------------------------+");
        log.info("                            End Report                         ");
        log.info("+-------------------------------------------------------------+");
        log.info("Run Minutes                              = " + runMins);
        log.info("Total Transaction                        = " + txnSum);
        log.info("Transaction Per Minute                   = " + txnSum / runMins);
        log.info("Transaction Per Second                   = " + txnSum / (runMins * 60));
        log.info("Total NewOrder                           = " + newOrderSum);
        log.info("Total Payment                            = " + paymentSum);
        log.info("Total OrderStatus                        = " + orderStatusSum);
        log.info("Total StockLevel                         = " + stockLevelSum);
        log.info("Total Delivery                           = " + deliverySum);
        log.info("Total UpdateItem                         = " + updateItemSum);
        log.info("Total UpdateStock                        = " + updateStockSum);
        log.info("Total GlobalDeadlock                     = " + deadLockSum);
        log.info("Total GlobalSnapshot                     = " + snapShotSum);
        log.info("Total Rollback                           = " + rollbackSum);
        log.info("Total Error                              = " + errorSum);
        log.info(" ");
        log.info("+-------------------------------------------------------------+");
        log.info("Measured min Millisecond NewOrder        = " + (newOrderSum == 0 ? 0 : newOrderLatency.get(0)));
        log.info("Measured min Millisecond Payment         = " + (paymentSum == 0 ? 0 : paymentLatency.get(0)));
        log.info("Measured min Millisecond OrderStatus     = " + (orderStatusSum == 0 ? 0 : orderStatusLatency.get(0)));
        log.info("Measured min Millisecond StockLevel      = " + (stockLevelSum == 0 ? 0 : stockLevelLatency.get(0)));
        log.info("Measured min Millisecond Delivery        = " + (deliverySum == 0 ? 0 : deliveryLatency.get(0)));
        log.info("Measured min Millisecond UpdateItem      = " + (updateItemSum == 0 ? 0 : updateItemLatency.get(0)));
        log.info("Measured min Millisecond UpdateStock      = " + (updateStockSum == 0 ? 0 : updateStockLatency.get(0)));
        log.info("Measured min Millisecond GlobalDeadlock  = " + (deadLockSum == 0 ? 0 : deadLockLatency.get(0)));
        log.info("Measured min Millisecond GlobalSnapshot  = " + (snapShotSum == 0 ? 0 : snapShotLatency.get(0)));
        log.info(" ");
        log.info("Measured avg Millisecond NewOrder        = "
                + (newOrderSum == 0 ? 0 : (long) ListUtil.calcLongAvg(newOrderLatency)));
        log.info("Measured avg Millisecond Payment         = "
                + (paymentSum == 0 ? 0 : (long) ListUtil.calcLongAvg(paymentLatency)));
        log.info("Measured avg Millisecond OrderStatus     = "
                + (orderStatusSum == 0 ? 0 : (long) ListUtil.calcLongAvg(orderStatusLatency)));
        log.info("Measured avg Millisecond StockLevel      = "
                + (stockLevelSum == 0 ? 0 : (long) ListUtil.calcLongAvg(stockLevelLatency)));
        log.info("Measured avg Millisecond Delivery        = "
                + (deliverySum == 0 ? 0 : (long) ListUtil.calcLongAvg(deliveryLatency)));
        log.info("Measured avg Millisecond UpdateItem      = "
                + (updateItemSum == 0 ? 0 : (long) ListUtil.calcLongAvg(updateItemLatency)));
        log.info("Measured avg Millisecond UpdateStock     = "
                + (updateItemSum == 0 ? 0 : (long) ListUtil.calcLongAvg(updateStockLatency)));
        log.info("Measured avg Millisecond GlobalDeadlock  = "
                + (deadLockSum == 0 ? 0 : (long) ListUtil.calcLongAvg(deadLockLatency)));
        log.info("Measured avg Millisecond GlobalSnapshot  = "
                + (snapShotSum == 0 ? 0 : (long) ListUtil.calcLongAvg(snapShotLatency)));
        log.info(" ");
        log.info("Measured 50th Millisecond NewOrder       = "
                + (newOrderSum == 0 ? 0 : newOrderLatency.get((int) (0.5 * newOrderSum))));
        log.info("Measured 50th Millisecond Payment        = "
                + (paymentSum == 0 ? 0 : paymentLatency.get((int) (0.5 * paymentSum))));
        log.info("Measured 50th Millisecond OrderStatus    = "
                + (orderStatusSum == 0 ? 0 : orderStatusLatency.get((int) (0.5 * orderStatusSum))));
        log.info("Measured 50th Millisecond StockLevel     = "
                + (stockLevelSum == 0 ? 0 : stockLevelLatency.get((int) (0.5 * stockLevelSum))));
        log.info("Measured 50th Millisecond Delivery       = "
                + (deliverySum == 0 ? 0 : deliveryLatency.get((int) (0.5 * deliverySum))));
        log.info("Measured 50th Millisecond UpdateItem     = "
                + (updateItemSum == 0 ? 0 : updateItemLatency.get((int) (0.5 * updateItemSum))));
        log.info("Measured 50th Millisecond UpdateStock    = "
                + (updateItemSum == 0 ? 0 : updateStockLatency.get((int) (0.5 * updateStockSum))));
        log.info("Measured 50th Millisecond GlobalDeadlock = "
                + (deadLockSum == 0 ? 0 : deadLockLatency.get((int) (0.5 * deadLockSum))));
        log.info("Measured 50th Millisecond GlobalSnapshot = "
                + (snapShotSum == 0 ? 0 : snapShotLatency.get((int) (0.5 * snapShotSum))));
        log.info(" ");
        log.info("Measured 99th Millisecond NewOrder       = "
                + (newOrderSum == 0 ? 0 : newOrderLatency.get((int) (0.99 * newOrderSum))));
        log.info("Measured 99th Millisecond Payment        = "
                + (paymentSum == 0 ? 0 : paymentLatency.get((int) (0.99 * paymentSum))));
        log.info("Measured 99th Millisecond OrderStatus    = "
                + (orderStatusSum == 0 ? 0 : orderStatusLatency.get((int) (0.99 * orderStatusSum))));
        log.info("Measured 99th Millisecond StockLevel     = "
                + (stockLevelSum == 0 ? 0 : stockLevelLatency.get((int) (0.99 * stockLevelSum))));
        log.info("Measured 99th Millisecond Delivery       = "
                + (deliverySum == 0 ? 0 : deliveryLatency.get((int) (0.99 * deliverySum))));
        log.info("Measured 99th Millisecond UpdateItem     = "
                + (updateItemSum == 0 ? 0 : updateItemLatency.get((int) (0.99 * updateItemSum))));
        log.info("Measured 99th Millisecond UpdateStock     = "
                + (updateItemSum == 0 ? 0 : updateStockLatency.get((int) (0.99 * updateStockSum))));
        log.info("Measured 99th Millisecond GlobalDeadlock = "
                + (deadLockSum == 0 ? 0 : deadLockLatency.get((int) (0.99 * deadLockSum))));
        log.info("Measured 99th Millisecond GlobalSnapshot = "
                + (snapShotSum == 0 ? 0 : snapShotLatency.get((int) (0.99 * snapShotSum))));
        log.info(" ");
        log.info("Measured max Millisecond NewOrder        = "
                + (newOrderSum == 0 ? 0 : newOrderLatency.get(newOrderSum - 1)));
        log.info(
                "Measured max Millisecond Payment          = " + (paymentSum == 0 ? 0 : paymentLatency.get(paymentSum - 1)));
        log.info("Measured max Millisecond OrderStatus     = "
                + (orderStatusSum == 0 ? 0 : orderStatusLatency.get(orderStatusSum - 1)));
        log.info("Measured max Millisecond StockLevel      = "
                + (stockLevelSum == 0 ? 0 : stockLevelLatency.get(stockLevelSum - 1)));
        log.info("Measured max Millisecond Delivery        = "
                + (deliverySum == 0 ? 0 : deliveryLatency.get(deliverySum - 1)));
        log.info("Measured max Millisecond UpdateItem      = "
                + (updateItemSum == 0 ? 0 : updateItemLatency.get(updateItemSum - 1)));
        log.info("Measured max Millisecond UpdateStock     = "
                + (updateItemSum == 0 ? 0 : updateStockLatency.get(updateStockSum - 1)));
        log.info("Measured max Millisecond GlobalDeadlock  = "
                + (deadLockSum == 0 ? 0 : deadLockLatency.get(deadLockSum - 1)));
        log.info("Measured max Millisecond GlobalSnapshot  = "
                + (snapShotSum == 0 ? 0 : snapShotLatency.get(snapShotSum - 1)));
    } // end endReport
}
