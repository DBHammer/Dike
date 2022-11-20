/*
 * Client - Load properties from the configuration file, make directories to store benchmark results for analysis, 
 *          start each worker thread and coordinate between Terminal, Daemon, TxnCounter.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.perf.OsCollector;
import edu.ecnu.dike.perf.TxnCounter;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.util.PrintExceptionUtil;
import edu.ecnu.dike.util.SelectDbUtil;

public class Client {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Logger log = Logger.getLogger(Client.class);

    public static void main(String[] args) {
        // each client run several threads to benchmark
        Client client = new Client();
        client.loadProperty();
        client.resultDirectory();
        client.executeBenchmark();
    }

    private String path;
    private BasicRandom rnd;

    // result buffer writer
    private String resultDirName;
    private BufferedWriter resultCI;
    private BufferedWriter resultCSV;

    // connection and runtime properties
    private ConnectionProperty connProps;
    private RuntimeProperty runtimeProps;

    // auxiliary components for performance monitor and static/dynamic worload
    // control
    private OsCollector osCollector;
    private TxnCounter txnCounter;
    private Thread txnCounterThread;
    private Daemon daemon;
    private Thread daemonThread;
    private Terminal[] terminals;

    // benchmark stop signal
    private long sessionStartTime;
    private volatile int terminalAlive;

    public Client() {

        // get property file location
        path = System.getProperty("prop");

        // load log properties
        PropertyConfigurator.configure("../config/log4j.properties");
        log.info("\n\n    ,---,                   ,-.\n" +
                ".'  .' `\\    ,--,     ,--/ /|\n" +
                ",---.'     \\ ,--.'|   ,--. :/ |\n" +
                "|   |  .`\\  ||  |,    :  : ' /\n" +
                ":   : |  '  |`--'_    |  '  /      ,---.\n" +
                "|   ' '  ;  :,' ,'|   '  |  :     /     \\\n" +
                "'   | ;  .  |'  | |   |  |   \\   /    /  |\n" +
                "|   | :  |  '|  | :   '  : |. \\ .    ' / |\n" +
                "'   : | /  ; '  : |__ |  | ' \\ \\'   ;   /|\n" +
                "|   | '` ,/  |  | '.'|'  : |--' '   |  / |\n" +
                ";   :  .'    ;  :    ;;  |,'    |   :    |\n" +
                "|   ,.'      |  ,   / '--'       \\   \\  /\n" +
                "'---'         ---`-'              `----'\n");
    } // end Client

    private void loadProperty() {
        try {
            // parse properties into runtime properties and connection properties
            runtimeProps = new RuntimeProperty(path);
            connProps = new ConnectionProperty(path);
            connProps.loadProperty();
            runtimeProps.loadProperty();

            // select warehouse from database
            int warehouse = Integer
                    .parseInt(SelectDbUtil.getConfig(connProps.getConn(), connProps.getProperty(), "warehouses"));
            if (warehouse != runtimeProps.getWarehouses()) {
                log.error(
                        "Warehouse number in property file does not match that selected from database, check property warehouse: "
                                + runtimeProps.getWarehouses() + ", database warehouse: " + warehouse);
                System.exit(1);
            }

            // select random seed from database
            long nURandCLast = Long
                    .parseLong(SelectDbUtil.getConfig(connProps.getConn(), connProps.getProperty(), "nURandCLast"));
            rnd = new BasicRandom(nURandCLast);
        } catch (IOException ioe) {
            log.error("Fail to load property file from path '" + path + "', " + PrintExceptionUtil.getIOExceptionInfo(ioe));
            System.exit(1);
        } catch (SQLException se) {
            log.error(PrintExceptionUtil.getSQLExceptionInfo(se));
            System.exit(1);
        }
        log.info("Load runtime property, " + runtimeProps.toString());
        log.info("Load connection property, " + connProps.toString());
    } // end loadProperty

    private void resultDirectory() {
        // parse result directory name
        String resultDirectory = runtimeProps.getResultDirectory();
        Pattern p = Pattern.compile("%t");
        String[] parts = p.split(resultDirectory, -1);
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        Formatter fmt = new Formatter(sb);
        Calendar cal = Calendar.getInstance();
        for (int i = 1; i < parts.length; i++) {
            fmt.format("%t" + parts[i].substring(0, 1), cal);
            sb.append(parts[i].substring(1));
        }
        resultDirName = sb.toString();
        fmt.close();

        // create the output directory structure
        File resultDir = new File(resultDirName);
        try {
            // create multi-level directory
            if (!resultDir.mkdirs()) {
                throw new IOException("Fail to create result directory '" + resultDir.getPath() + "'");
            }
            log.info("Create result directory '" + resultDir.getPath() + "'");

            // create data directory
            File resultDataDir = new File(resultDir, "data");
            if (!resultDataDir.mkdir()) {
                throw new IOException("Fail to create result data directory '" + resultDataDir.getPath() + "'");
            }

            // copy the used properties file into the result directory.
            try {
                Files.copy(new File(path).toPath(), new File(resultDir, "run.properties").toPath());
            } catch (IOException ioe) {
                throw ioe;
            }
            log.info("Copy property file " + path + " to " + new File(resultDir, "run.properties").toPath());

            // open result csv file to write down transaction information
            try {
                String resultCSVName = new File(resultDataDir, "result.csv").getPath();
                resultCSV = new BufferedWriter(new FileWriter(resultCSVName));
                resultCSV.write("elapsed,latency,txntype,rbk,error,distributed,spannode,warehouseid,terminalid\n");
                log.info("Write down transaction statistics information to " + resultCSVName);
            } catch (IOException ioe) {
                throw ioe;
            }

            // open ci.csv file to write down conflict intensity information
            try {
                String resultCIName = new File(resultDataDir, "ci.csv").getPath();
                resultCI = new BufferedWriter(new FileWriter(resultCIName));
                resultCI.write("elapsed,ci\n");
                log.info("Write down conflict intensity results to " + resultCIName);
            } catch (IOException ioe) {
                throw ioe;
            }

            // create os resource usage results directory
            if (runtimeProps.isOsCollector()) {
                osCollector = new OsCollector(runtimeProps, resultDataDir);
            }
        } catch (IOException ioe) {
            log.error("Get unexpected io exceptions while making result directory" + PrintExceptionUtil.getIOExceptionInfo(ioe));
            System.exit(2);
        }
    } // end resultDirectory

    public void executeBenchmark() {
        // create transaction counter thread
        txnCounter = new TxnCounter(this, runtimeProps.getTxnReportInterval(), runtimeProps.getRunMins());
        txnCounterThread = new Thread(txnCounter);

        // create daemon thread
        if (runtimeProps.isDynamicLoad() || runtimeProps.isDynamicConflict() || runtimeProps.isDynamicTransaction()) {
            daemon = new Daemon(runtimeProps, this, txnCounter);
            daemonThread = new Thread(daemon);
        }

        // create terminals
        terminalAlive = runtimeProps.getTerminals();
        terminals = new Terminal[terminalAlive];
        for (int i = 0; i < terminalAlive; i++) {
            Terminal terminal = new Terminal(runtimeProps, connProps, this, rnd, i);
            terminals[i] = terminal;
        }

        // start all terminals and start timing
        sessionStartTime = System.currentTimeMillis();
        for (int i = 0; i < terminalAlive; i++) {
            (new Thread(terminals[i])).start();
        }

        // start transaction counter thread
        if (txnCounterThread != null) {
            txnCounterThread.start();
        } else {
            log.error("Fail to start transaction counter");
        }

        // start daemon thread
        if (daemonThread != null) {
            daemonThread.start();
            log.info("Start daemon thread for dynamic workload patterns");
        }

        // start os resource usage collector
        if (osCollector != null) {
            osCollector.start();
            log.info("Start collecting server runtime resource usage");
        }
        log.info("All terminal start working at " + getCurrentTime());
    } // end executeBenchmark

    // client send terminate signals to terminals
    public void signalTerminalEndWhenPossible() {
        // try to stop all terminals
        synchronized (terminals) {
            for (int i = 0; i < terminals.length; i++) {
                if (terminals[i] != null) {
                    terminals[i].signalStopRunning();
                }
            }
        }
    }

    // client add transaction statistics to transaction counter
    public void signalTxnEnd(int terminalID, int warehouseID, int rbk, int error) {
        txnCounter.addTxn(terminalID, warehouseID, rbk, error);
    }

    // terminal send termination signal to client
    public void signalTerminalEnd(int terminalID) {
        // receive terminal end signal
        synchronized (terminals) {
            terminalAlive--;
            terminals[terminalID] = null;
        }

        // no alive terminal, stop all threads
        if (terminalAlive == 0) {
            // send stop signal to transaction counter thread
            if (txnCounter != null) {
                txnCounter.stop();
            }

            // send stop signal to daemon thread
            if (daemon != null) {
                daemon.stop();
            }

            // wait for threads terminate
            try {
                if (txnCounterThread != null) {
                    txnCounterThread.join();
                }
                if (daemonThread != null) {
                    daemonThread.join();
                }
            } catch (InterruptedException ie) {
                log.error("Get interruption while waiting for txn counter thread and daemon thread to finish "
                        + ie.getMessage());
            }

            // close result/ci csv buffer writer
            try {
                resultCSV.close();
                resultCI.close();
            } catch (IOException ioe) {
                log.error("Fail to close result csv file, " + ioe.getMessage());
            }

            // send stop signal to system resource usage collector process
            if (osCollector != null) {
                osCollector.stop();
            }

            txnCounter.endReport(resultDirName);
            log.info("All terminals end working at " + getCurrentTime());
        }
    } // end signalTerminalEnd

    // terminals write down transaction information
    public void resultAppend(String resultLine) {
        try {
            long elapsed = System.currentTimeMillis() - sessionStartTime;
            resultCSV.write(elapsed + "," + resultLine);
        } catch (IOException ioe) {
            log.error("Fail to record result line: " + resultLine + ", " + ioe.getMessage());
        }
    }

    // daemon write down conflict intensity information
    public void ciAppend() {
        int ci = txnCounter.getCI();
        try {
            long elapsed = System.currentTimeMillis() - sessionStartTime;
            resultCI.write(elapsed + "," + ci + "\n");
        } catch (IOException ioe) {
            log.error("Fail to record current conflict intensity " + ci + ", " + ioe.getMessage());
        }
    }

    // client suspend terminals through thread sleep to simulate dynamic workload
    // size
    public void suspendWorker(int sleepTime) {
        for (Terminal terminal : terminals) {
            if (terminal != null) {
                terminal.appendSleepTime(sleepTime);
            }
        }
    }

    // client set dynamic conflict probability to terminals
    public void setConflictProb(double prob) {
        for (Terminal terminal : terminals) {
            if (terminal != null) {
                terminal.setConflictProb(prob);
            }
        }
        log.info("Change statistics conflict probability to " + prob);
    }

    // client set dynamic transaction mixture rate
    public void setTransactionWeight(int[] transactions) {
        for (Terminal terminal : terminals) {
            if (terminal != null) {
                terminal.setTransactions(transactions);
            }
        }
        log.info("Change transaction mixture weights to " + Arrays.toString(transactions));
    }

    // terminal send signal to increase CI count(CI: the number of threads in
    // conflict warehouse at some point)
    public void increaseCI() {
        txnCounter.increaseCI();
    }

    // terminal send signal to decrease CI count
    public void decreaseCI() {
        txnCounter.decreaseCI();
    }

    // daemon send fix conflict signal
    public void fixConflict() {
        for (Terminal terminal : terminals) {
            if (terminal != null) {
                terminal.fixConflict();
            }
        }
    }

    // daemon send unfix conflict signal
    public void unFixConflict() {
        for (Terminal terminal : terminals) {
            if (terminal != null) {
                terminal.unFixConflict();
            }
        }
    }

    public int getAliveTerminals() {
        return terminalAlive;
    }

    private String getCurrentTime() {
        return dateFormat.format(new java.util.Date());
    }

    public BasicRandom getRnd() {
        return rnd;
    }
}