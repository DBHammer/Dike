/*
 * TxnCounter - Record realtime txn count, rollback count and error count, 
 *              print status every second and make a report at the end of benchmark.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.perf;

import java.io.*;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import edu.ecnu.dike.control.Client;
import edu.ecnu.dike.util.ListUtil;
import edu.ecnu.dike.type.DbType;
import edu.ecnu.dike.type.TxnType;
import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.util.PrintExceptionUtil;
import edu.ecnu.dike.util.SelectDbUtil;

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

    // deamon get finished conflict transaction number during last second and
    // reset conflictCnt to zero
    public int getConflictNum() {
        return conflictCnt.getAndSet(0);
    }

    // deamon get finished non-conflict transaction number during last second and
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

    // group results by seconds and record down to files
    public void generateAggr(DbType dbType, String resultDirName) {
        // metrics holder
        int rollbacks = 0;
        int errors = 0;
        int second = 1;
        TxnType[] txnList = TxnType.values();
        int[] tpsList = new int[txnList.length];
        long[] latencyList = new long[txnList.length];
        int[] distributedList = new int[txnList.length];
        int[] crossNodeList = new int[txnList.length];
        // file holder
        String dataDir = Paths.get(resultDirName, "data").toString();
        String resultcsv = Paths.get(dataDir, "result.csv").toString();
        String aggrcsv = Paths.get(dataDir, "aggregation.csv").toString();
        String cicsv = Paths.get(dataDir, "ci.csv").toString();
        File[] subDirs = new File(dataDir).listFiles();
        ArrayList<String> resourceName = new ArrayList<>();
        ArrayList<String> resourcecsv = new ArrayList<>();
        if (subDirs != null) {
            for (File subDir : subDirs) {
                if (subDir.isDirectory()) {
                    resourceName.add(subDir.getName());
                    resourcecsv.add(Paths.get(subDir.getPath(), "sys_info.csv").toString());
                }
            }
        }

        // get table size
        String storage[] = new String[resourcecsv.size()];
        try {
            ConnectionProperty connProps = client.getConnProp();
            switch (dbType) {
                case DB_OCEANBASE:
                    storage = SelectDbUtil.getOBTableSize(connProps.getConn(), connProps.getProperty(), resourcecsv.size());
                default:
                    // TODO: get the table size of other databases
                    break;
            }
        } catch (SQLException se) {
            log.error(PrintExceptionUtil.getSQLExceptionInfo(se));
        }
        try {
            // open buffered io
            BufferedReader reader = new BufferedReader(new FileReader(resultcsv));
            BufferedReader cireader = new BufferedReader(new FileReader(cicsv));
            BufferedReader[] resourcereaders = new BufferedReader[resourcecsv.size()];
            for (int i = 0; i < resourcecsv.size(); i++) {
                resourcereaders[i] = new BufferedReader(new FileReader(resourcecsv.get(i)));
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(aggrcsv));
            // write down aggregation header
            StringBuilder header = new StringBuilder();
            header.append("elapsed,");
            for (TxnType txnType : txnList) {
                header.append(txnType.getName())
                      .append("-tps,")
                      .append(txnType.getName())
                      .append("-latency,")
                      .append(txnType.getName())
                      .append("-distributed,")
                      .append(txnType.getName())
                      .append("-crossnode,");
            }
            for (String name : resourceName) {
                header.append("cpu-")
                      .append(name)
                      .append(",");
            }
            for (String name : resourceName) {
                header.append("storage-")
                      .append(name)
                      .append(",");
            }
            header.append("rollback,error,ci\n");
            writer.write(header.toString());
            StringBuilder sb = new StringBuilder();
            Formatter fmt = new Formatter(sb);
            // read header line
            String ci, line;
            reader.readLine();
            cireader.readLine();
            for (BufferedReader resourcereader : resourcereaders) {
                resourcereader.readLine();
            }
            while ((line = reader.readLine()) != null) {
                // read and parse transaction record line
                String[] item = line.split(",");
                long elapsed = Long.parseLong(item[0]);
                long latency = Long.parseLong(item[1]);
                String txntype = item[2];
                boolean rbk = Boolean.parseBoolean(item[3]);
                boolean err = Boolean.parseBoolean(item[4]);
                int distributed = Integer.parseInt(item[5]);
                int crossNode = Integer.parseInt(item[6]);
                if (err) {
                    errors++;
                } else if (rbk) {
                    rollbacks++;
                } else {
                    for (int i = 0; i < txnList.length; ++i) {
                        if (txntype.equals(txnList[i].getName())) {
                            tpsList[i]++;
                            latencyList[i] += latency;
                            distributedList[i] += distributed;
                            crossNodeList[i] += crossNode;
                            break;
                        }
                    }
                }
                // write down aggregation result every second
                int elapsedSecond = (int) (elapsed / 1000);
                int runSeconds = (int) (runMins * 60);
                while (elapsedSecond >= second && elapsedSecond <= runSeconds) {
                    fmt.format("%d,", second);
                    if (elapsedSecond == second) {
                        for (int i = 0; i < txnList.length; i++) {
                            if (tpsList[i] == 0) {
                                fmt.format("%d,%.2f,%.2f,%.2f,", 0, 0.0, 0.0, 0.0);
                            } else {
                                fmt.format("%d,%.2f,%.2f,%.2f,", tpsList[i], (double)latencyList[i]/tpsList[i], (double)distributedList[i]*100/tpsList[i], (double)crossNodeList[i]/tpsList[i]);
                            }
                            tpsList[i] = 0;
                            latencyList[i] = 0;
                            distributedList[i] = 0;
                            crossNodeList[i] = 0;
                        }
                    } else {
                        for (int i = 0; i < 4*txnList.length; i++) {
                            fmt.format("%d,", 0);
                        }
                    }
                    String[] resouceItem;
                    for (BufferedReader resourcereader : resourcereaders) {
                        String resourceLine = resourcereader.readLine();
                        if (resourceLine != null) {
                            resouceItem = resourceLine.split(",");
                            double cpuUsage = Double.parseDouble(resouceItem[1]) + Double.parseDouble(resouceItem[3]);
                            fmt.format("%.2f,", cpuUsage * 100);
                        } else {
                            fmt.format("%.2f,", 0.0);
                        }

                    }
                    for (int i = 0; i < resourcecsv.size(); i++) {
                        fmt.format("%.2f,", storage[i]);
                    }
                    ci = cireader.readLine();
                    int ciparse = ci != null ? Integer.parseInt(ci.split(",")[1]) : 0;
                    fmt.format("%d,%d,%d\n", rollbacks, errors, ciparse);
                    rollbacks = 0;
                    errors = 0;
                    second++;
                    writer.write(sb.toString());
                    sb.setLength(0);
                }
            }
            reader.close();
            cireader.close();
            writer.close();
        } catch (FileNotFoundException fnfe) {
            log.error("fail to open result csv file, " + resultcsv + ", " + fnfe.getMessage());
        } catch (IOException ioe) {
            log.error("fail to read lines from result csv file, " + resultcsv + ", " + ioe.getMessage());
        }
    } // end generateAggr

    // give detailed report at the end of benchmark
    public void endReport(String resultDirName) {
        // TODO: format endReport code in generateAggr's way
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

            // read header line
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
