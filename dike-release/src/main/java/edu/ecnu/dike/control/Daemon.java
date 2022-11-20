/*
 * Daemon - Dynamically change workload pattern and send control signals to Client.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.control;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.perf.TxnCounter;
import edu.ecnu.dike.random.GaussianRandom;
import edu.ecnu.dike.util.ListUtil;

public class Daemon implements Runnable {

    private final Logger log = Logger.getLogger(Daemon.class);

    // keep a reference to client and transaction counter
    private Client client;
    private TxnCounter txnCnt;

    // wake up every 'interval' milliseconds to dynamically change workload and
    // conflict
    private int terminals;
    private int runSeconds;
    private int elapsedMilliSeconds;
    private int interval = 1000;
    private volatile boolean stopRunningSignal = false;

    /*
     * gaussian distribution to simulate dynamic workloads
     * mean: average awake time(milliseconds) for each thread within one second
     * sdeviation: standard deviation milliseconds
     * maxVal: maximum probabilty in Gaussian(mean, sdeviation)
     */
    private boolean isDynamicLoad;
    private int mean = 500;
    private int sdeviation = 150;
    private double maxVal;

    // poisson distribution to simulate dynamic contentions
    private boolean isDynamicConflict;
    private int[] ciList;
    private int ciIdx = 0;

    // dynamic transaction mixture rate
    private boolean isDynamicTransaction;
    private int[][] changeTransactions;
    private double[] changePoints;
    private int transactionsIdx = 0;

    // sampleLeft/sampleRight determine how many seconds/milliseconds we choose to infer conNum/nonConNum
    // both between [0, changeInterval]
    private int sampleLeft = 10000;
    private int sampleRight = 20000;
    private double changeInterval;
    private double conNum = 0;
    private int txnConNum = 0;
    private double nonConNum = 0;
    private int txnNonConNum = 0;
    private double curProb = 0;
    private ArrayList<Integer> samplesContention = new ArrayList<>();
    private ArrayList<Integer> samplesNonContention = new ArrayList<>();

    public Daemon(RuntimeProperty runtimeProps, Client client, TxnCounter txnCnt) {
        this.client = client;
        this.txnCnt = txnCnt;
        runSeconds = (int) (runtimeProps.getRunMins() * 60);
        isDynamicLoad = runtimeProps.isDynamicLoad();
        if (isDynamicLoad) {
            maxVal = GaussianRandom.probDistributionFunc(mean, mean, sdeviation);
        }
        isDynamicConflict = runtimeProps.isDynamicConflict();
        if (isDynamicConflict) {
            ciList = runtimeProps.getCiList();
            terminals = runtimeProps.getTerminals();
            changeInterval = runtimeProps.getConflictChangeInterval() * 60000;
        }
        isDynamicTransaction = runtimeProps.isDynamicTransaction();
        if (isDynamicTransaction) {
            changeTransactions = runtimeProps.getChangeTransactions();
            changePoints = runtimeProps.getChangePoints();
            for (int i = 0; i < changePoints.length; i++) {
                changePoints[i] *= 60000;
            }
        }
    }

    @Override
    public void run() {
        while (!stopRunningSignal) {
            // dynamic workload
            if (isDynamicLoad && client != null) {
                client.suspendWorker(getCurSleepTime());
            }

            // dynamic conflict
            if (isDynamicConflict && client != null) {
                txnConNum = txnCnt.getConflictNum();
                txnNonConNum = txnCnt.getNonConflictNum();

                // first stage, set init conflict probability p0 and bind first p0% threads on
                // conflict warehouse
                if (elapsedMilliSeconds == 0) {
                    client.setConflictProb(getCurProb());
                }

                // second stage, sample conflict and non-conflict transaction every interval
                // millisecond
                if (elapsedMilliSeconds >= sampleLeft && elapsedMilliSeconds <= sampleRight) {
                    samplesContention.add(txnConNum);
                    samplesNonContention.add(txnNonConNum);
                    if (elapsedMilliSeconds == sampleRight) {
                        conNum = ListUtil.calcIntegerAvg(samplesContention);
                        nonConNum = ListUtil.calcIntegerAvg(samplesNonContention);
                    }
                }

                // third stage, calculate and set current conflict probability
                if (elapsedMilliSeconds % changeInterval == sampleRight) {
                    client.setConflictProb(getCurProb());
                }

                // send record ci signal to client
                client.ciAppend();
            }

            // dynamic transactions
            if (isDynamicTransaction && client != null) {
                if (elapsedMilliSeconds >= changePoints[transactionsIdx]) {
                    client.setTransactionWeight(changeTransactions[transactionsIdx]);
                    transactionsIdx += 1;
                }
            }

            // wait for interval time
            try {
                Thread.sleep(interval);
                elapsedMilliSeconds += interval;
            } catch (InterruptedException ie) {
                log.error("daemon thread catch interrupted exception while sleeping");
            }
        }
    } // end run

    // get current terminal thread sleep time based on current elapsed milliseconds
    // and Gaussian(mean, sdeviation)
    public int getCurSleepTime() {
        double xVal = (double) (elapsedMilliSeconds / runSeconds);
        double gVal = GaussianRandom.probDistributionFunc(xVal, mean, sdeviation);
        return (int) ((1 - gVal / maxVal) * 1000);
    }

    // get current conflict probability based on sampled conflict transaction
    // number and non-conflict transaction number
    public double getCurProb() {
        if (curProb == 0) {
            curProb = ciList[ciIdx];
            client.fixConflict();
        } else if (ciIdx < ciList.length) {
            if (ciIdx > 0) {
                nonConNum = nonConNum * (terminals - ciList[ciIdx]) / (terminals - ciList[ciIdx - 1]);
            }
            curProb = (100 * conNum) / (conNum + nonConNum);
            client.unFixConflict();
            ciIdx++;
        }
        log.info("get conflict number: " + conNum + ", non conflict number: " + nonConNum
                + ", calculated conflict probabilty: " + curProb);
        return curProb;
    }

    // client send termination signal to daemon
    public void stop() {
        stopRunningSignal = true;
    }
}