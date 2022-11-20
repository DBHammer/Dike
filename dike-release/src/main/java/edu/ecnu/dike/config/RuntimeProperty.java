/*
 * RuntimeProperty - Get properties used during benchmark runtime.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.config;

import java.io.IOException;
import java.util.Arrays;

import edu.ecnu.dike.type.DbType;
import edu.ecnu.dike.util.CheckParamsUtil;
import edu.ecnu.dike.util.GetDbTypeUtil;

public class RuntimeProperty extends Property {

	// database type
	private String set;
	private DbType dbType;

	// scale factor(data size, workload size, cluster scale and running time)
	private int warehouses;
	private int terminals;
	private int physicalNode;
	private double runMins;
	private int leftRange;
	private int rightRange;

	// schema
	private int partitions;

	// transaction mixture rate
	private int[] transactionsWeight;

	// rollback retry
	private boolean rollbackRetry;

	// distributed transaction
	private int newOrderSpanNode;
	private int newOrderDistributedRate;
	private String warehouseDistribution;
	private boolean terminalWarehouseFixed;

	// distributed query
	private int stockLevelDistributedRate;
	private int stockLevelWIDNode;
	private boolean statisticsCalc;

	// broadcast
	private boolean broadcastTest;
	private boolean batchUpdate;
	private int accesssUpdateItemRate;

	// read write seperation
	private boolean readWriteSeperation;
	private boolean bandTransaction;

	// dynamic district
	private boolean dynamicDistrict;

	// dynamic load
	private boolean dynamicLoad;

	// dynamic conflict
	private boolean dynamicConflict;
	private double conflictChangeInterval;
	private int[] ciList;

	// dynamic transaction
	private boolean dynamicTransaction;
	private int[][] changeTransactions;
	private double[] changePoints;

	// update stock
	private int coaccessNumber;

	// global snapshot
	private int snapshotTimes;

	// global deadlock
	private int deadlockTimes;

	// result directory
	private String resultDirectory;

	// operating system information collector
	private boolean osCollector;
	private String osCollectorScript;
	private String osCollectorSSHAddr;
	private String osCollectorDevices;
	private int txnReportInterval;

	// chaos test
	private boolean cpuLoad;
	private boolean stressMemory;
	private boolean diskRead;
	private boolean diskWrite;
	private boolean networkDelay;
	private boolean shutdown;
	private double chaosTime;

	// data file
	private boolean writeCSV;
	private int loadWorkers;
	private String fileLocation;

	public RuntimeProperty(String path) throws IOException {
		super(path);
	}

	private void checkProperty() throws RuntimeException {
		// check numeric properties with CheckParamsUtil
		CheckParamsUtil.checkPositive(warehouses, "warehouses");
		CheckParamsUtil.checkRange(partitions, "partitions", 1, warehouses);
		CheckParamsUtil.checkPositive(terminals, "terminals");
		CheckParamsUtil.checkTerminalRange(leftRange, rightRange, warehouses);
		CheckParamsUtil.checkPositive(physicalNode, "physicalNode");
		CheckParamsUtil.checkPositive(runMins, "runMins");
		CheckParamsUtil.checkWarehouseDistribution(warehouseDistribution);
		CheckParamsUtil.checkEqual(9, transactionsWeight.length, "transactionsWeight(length)");
		for (int weight : transactionsWeight) {
			CheckParamsUtil.checkRange(weight, "transactionsWeight", 0, 100);
		}
		CheckParamsUtil.checkMixtureTransactions(transactionsWeight);
		CheckParamsUtil.checkRange(newOrderDistributedRate, "newOrderDistributedRate", 0, 100);
		CheckParamsUtil.checkRange(stockLevelDistributedRate, "stockLevelDistributedRate", 0, 100);
		if (statisticsCalc) {
			CheckParamsUtil.checkRange(newOrderSpanNode, "newOrderSpanNode", 1, physicalNode);
			CheckParamsUtil.checkRange(stockLevelWIDNode, "stockLevelWIDNode", 1, physicalNode);
		} else {
			CheckParamsUtil.checkRange(newOrderSpanNode, "newOrderSpanNode", 1, warehouses);
			CheckParamsUtil.checkRange(stockLevelWIDNode, "stockLevelWIDNode", 1, warehouses);
		}
		if (broadcastTest && batchUpdate) {
			CheckParamsUtil.checkRange(accesssUpdateItemRate, "accesssUpdateItemRate", 0, 100);
		}
		if (dynamicDistrict) {
			CheckParamsUtil.checkEqual(warehouseDistribution, "uniform", "warehouseDistribution(dynamicDistrict Mode)");
		}
		CheckParamsUtil.checkRange(coaccessNumber, "coaccessNumber", 1, warehouses / physicalNode);
		CheckParamsUtil.checkRange(snapshotTimes, "snapshotTimes", 1, warehouses);
		CheckParamsUtil.checkRange(deadlockTimes, "deadlockTimes", 1, warehouses);
		if (cpuLoad || stressMemory || diskRead || diskWrite || networkDelay || shutdown) {
			CheckParamsUtil.checkRange(chaosTime, "chaosTime", 0, runMins);
		}
		if (dynamicConflict) {
			for (int ci : ciList) {
				CheckParamsUtil.checkRange(ci, "cilist", 0, terminals);
			}
		}
		if (dynamicTransaction) {
			CheckParamsUtil.checkEqual(changeTransactions.length, changePoints.length, "changeTransactions/changePoints");
			for (int[] changeTransaction : changeTransactions) {
				CheckParamsUtil.checkEqual(changeTransaction.length, 9, "changeTransaction(transaction number)");
				CheckParamsUtil.checkMixtureTransactions(changeTransaction);
			}
			for (double changePoint : changePoints) {
				CheckParamsUtil.checkRange(changePoint, "changePoints", 0, runMins);
			}
		}
	}

	@Override
	public void loadProperty() {
		// database type
		set = props.getProperty("set", "dike");
		dbType = GetDbTypeUtil.getDbType(props.getProperty("db", null));

		// scale factor
		warehouses = Integer.parseInt(props.getProperty("warehouses", "60"));
		terminals = Integer.parseInt(props.getProperty("terminals", "60"));
		physicalNode = Integer.parseInt(props.getProperty("physicalNode", "1"));
		runMins = Double.parseDouble(props.getProperty("runMins", "5"));
		String[] range = props.getProperty("terminalRange", "1," + warehouses).split(",");
		leftRange = Integer.parseInt(range[0]);
		rightRange = Integer.parseInt(range[1]);

		// partitions
		partitions = Integer.parseInt(props.getProperty("partitions", Integer.toString(physicalNode)));

		// mixture transaction rate
		String[] transactions = props.getProperty("transactions", "45,43,4,4,4,0,0,0,0").split(",");
		transactionsWeight = new int[transactions.length];
		for (int i = 0; i < transactionsWeight.length; i++) {
			transactionsWeight[i] = Integer.parseInt(transactions[i]);
		}

		// rollback retry
		rollbackRetry = Boolean.parseBoolean(props.getProperty("rollbackRetry", "false"));

		// distributed transaction
		newOrderDistributedRate = Integer.parseInt(props.getProperty("newOrderDistributedRate", "0"));
		newOrderSpanNode = Integer.parseInt(props.getProperty("newOrderSpanNode", "1"));
		warehouseDistribution = props.getProperty("warehouseDistribution", "uniform");
		terminalWarehouseFixed = Boolean.parseBoolean(props.getProperty("terminalWarehouseFixed", "false"));

		// distributed query
		stockLevelDistributedRate = Integer.parseInt(props.getProperty("stockLevelDistributedRate", "0"));
		stockLevelWIDNode = Integer.parseInt(props.getProperty("stockLevelWIDNode", "1"));
		statisticsCalc = Boolean.parseBoolean(props.getProperty("statisticsCalc", "false"));

		// broadcast test
		broadcastTest = Boolean.parseBoolean(props.getProperty("broadcastTest", "false"));
		accesssUpdateItemRate = Integer.parseInt(props.getProperty("accesssUpdateItemRate", "20"));
		batchUpdate = Boolean.parseBoolean(props.getProperty("batchUpdate", "true"));

		// read write separation
		readWriteSeperation = Boolean.parseBoolean(props.getProperty("readWriteSeperation", "false"));
		bandTransaction = Boolean.parseBoolean(props.getProperty("bandTransaction", "false"));

		// dynamic district
		dynamicDistrict = Boolean.parseBoolean(props.getProperty("dynamicDistrict", "false"));

		// dynamic load
		dynamicLoad = Boolean.parseBoolean(props.getProperty("dynamicLoad", "false"));

		// dynamic conflict
		dynamicConflict = Boolean.parseBoolean(props.getProperty("dynamicConflict", "false"));
		conflictChangeInterval = Double
				.parseDouble(props.getProperty("conflictChangeInterval", Double.toString(runMins)));
		String[] ciStrList = props.getProperty("cilist", "0").split(",");
		ciList = new int[ciStrList.length];
		for (int i = 0; i < ciStrList.length; i++) {
			ciList[i] = Integer.parseInt(ciStrList[i]);
		}

		// dynamic transaction
		dynamicTransaction = Boolean.parseBoolean(props.getProperty("dynamicTransaction", "false"));
		String[] transactionsStrList = props.getProperty("changeTransactions", "45,43,4,4,4,0,0,0,0").split(";");
		changeTransactions = new int[transactionsStrList.length][9];
		for (int i = 0; i < transactionsStrList.length; i++) {
			String[] transactionList = transactionsStrList[i].split(",");
			for (int j = 0; j < 9; j++) {
				changeTransactions[i][j] = Integer.parseInt(transactionList[j]);
			}
		}
		String[] changePointList = props.getProperty("changePoints", Double.toString(runMins)).split(",");
		changePoints = new double[changePointList.length];
		for (int i = 0; i < changePointList.length; i++) {
			changePoints[i] = Double.parseDouble(changePointList[i]);
		}

		// update stock
		coaccessNumber = Integer.parseInt(props.getProperty("coaccessNumber", "1"));

		// global snapshot
		snapshotTimes = Integer.parseInt(props.getProperty("snapshotTimes", "4"));

		// global deadlock
		deadlockTimes = Integer.parseInt(props.getProperty("deadlockTimes", "5"));

		// result directory
		resultDirectory = props.getProperty("resultDirectory",
				"results/" + dbType.getName() + "/test/my_result_%tY-%tm-%td_%tH%tM%tS");

		// operating system information collector
		osCollector = Boolean.parseBoolean(props.getProperty("osCollector", "false"));
		osCollectorScript = props.getProperty("osCollectorScript", null);
		osCollectorSSHAddr = props.getProperty("osCollectorSSHAddr", null);
		osCollectorDevices = props.getProperty("osCollectorDevices", "net_eth0,blk_vdb");
		txnReportInterval = Integer.parseInt(props.getProperty("txnReportInterval", "1"));

		// chaos test
		cpuLoad = Boolean.parseBoolean(props.getProperty("cpuLoad", "false"));
		stressMemory = Boolean.parseBoolean(props.getProperty("stressMemory", "false"));
		diskRead = Boolean.parseBoolean(props.getProperty("diskRead", "false"));
		diskWrite = Boolean.parseBoolean(props.getProperty("diskWrite", "false"));
		networkDelay = Boolean.parseBoolean(props.getProperty("networkDelay", "false"));
		shutdown = Boolean.parseBoolean(props.getProperty("shutdown", "false"));
		chaosTime = Double.parseDouble(props.getProperty("chaosTime", "1"));

		// data file
		writeCSV = Boolean.parseBoolean(props.getProperty("writeCSV", "false"));
		loadWorkers = Integer.parseInt(props.getProperty("loadWorkers", "50"));
		fileLocation = props.getProperty("fileLocation", "/tmp/dike");

		// check the validity of each parameters
		checkProperty();

	} // end loadProperty

	public String getSet() {
		return set;
	}

	public DbType getDbType() {
		return dbType;
	}

	public int getWarehouses() {
		return warehouses;
	}

	public int getTerminals() {
		return terminals;
	}

	public int getLeftRange() {
		return leftRange;
	}

	public int getRightRange() {
		return rightRange;
	}

	public int getPhysicalNode() {
		return physicalNode;
	}

	public double getRunMins() {
		return runMins;
	}

	public int[] getTransactionsWeight() {
		return transactionsWeight; 
	}

	public boolean getRollbackRetry() {
		return rollbackRetry;
	}

	public int getPartitions() {
		return partitions;
	}

	public boolean getStatisticsCalc() {
		return statisticsCalc;
	}

	public int getNewOrderDistributedRate() {
		return newOrderDistributedRate;
	}

	public int getNewOrderSpanNode() {
		return newOrderSpanNode;
	}

	public String getWarehouseDistribution() {
		return warehouseDistribution;
	}

	public boolean isTerminalWarehouseFixed() {
		return terminalWarehouseFixed;
	}

	public int getStockLevelDistributedRate() {
		return stockLevelDistributedRate;
	}

	public int getStockLevelWIDNode() {
		return stockLevelWIDNode;
	}

	public boolean getBroadcastTest() {
		return broadcastTest;
	}

	public int getAccesssUpdateItemRate() {
		return accesssUpdateItemRate;
	}

	public boolean getBatchUpdate() {
		return batchUpdate;
	}

	public boolean isReadWriteSeperation() {
		return readWriteSeperation;
	}

	public boolean isBandTransaction() {
		return bandTransaction;
	}

	public boolean isDynamicDistrict() {
		return dynamicDistrict;
	}

	public boolean isDynamicLoad() {
		return dynamicLoad;
	}

	public boolean isDynamicConflict() {
		return dynamicConflict;
	}

	public double getConflictChangeInterval() {
		return conflictChangeInterval;
	}

	public int[] getCiList() {
		return ciList;
	}

	public boolean isDynamicTransaction() {
		return dynamicTransaction;
	}

	public int[][] getChangeTransactions() {
		return changeTransactions;
	}

	public double[] getChangePoints() {
		return changePoints;
	}

	public int getCoaccessNumber() {
		return coaccessNumber;
	}

	public int getSnapshotTimes() {
		return snapshotTimes;
	}

	public int getDeadlockTimes() {
		return deadlockTimes;
	}

	public String getResultDirectory() {
		return resultDirectory;
	}

	public boolean isOsCollector() {
		return osCollector;
	}

	public String getOsCollectorScript() {
		return osCollectorScript;
	}

	public String getOsCollectorSSHAddr() {
		return osCollectorSSHAddr;
	}

	public String getOsCollectorDevices() {
		return osCollectorDevices;
	}

	public int getTxnReportInterval() {
		return txnReportInterval;
	}

	public double getChaosTime() {
		return chaosTime;
	}

	public boolean getWriteCSV() {
		return writeCSV;
	}

	public int getLoadWorkers() {
		return loadWorkers;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	@Override
	public String toString() {
		return "RuntimeProperty [set=" + set + ", dbType=" + dbType + ", warehouses=" + warehouses + ", terminals="
				+ terminals + ", physicalNode=" + physicalNode + ", runMins=" + runMins + ", leftRange=" + leftRange
				+ ", rightRange=" + rightRange + ", partitions=" + partitions + ", transactionsWeight="
				+ Arrays.toString(transactionsWeight) + ", newOrderSpanNode=" + newOrderSpanNode
				+ ", newOrderDistributedRate=" + newOrderDistributedRate + ", warehouseDistribution="
				+ warehouseDistribution + ", terminalWarehouseFixed=" + terminalWarehouseFixed
				+ ", stockLevelDistributedRate=" + stockLevelDistributedRate + ", stockLevelWIDNode="
				+ stockLevelWIDNode + ", statisticsCalc=" + statisticsCalc + ", broadcastTest=" + broadcastTest
				+ ", batchUpdate=" + batchUpdate + ", accesssUpdateItemRate=" + accesssUpdateItemRate
				+ ", readWriteSeperation=" + readWriteSeperation + ", bandTransaction=" + bandTransaction
				+ ", dynamicDistrict=" + dynamicDistrict + ", dynamicLoad=" + dynamicLoad + ", dynamicConflict="
				+ dynamicConflict + ", conflictChangeInterval=" + conflictChangeInterval + ", ciList="
				+ Arrays.toString(ciList) + ", coaccessNumber=" + coaccessNumber + ", snapshotTimes=" + snapshotTimes
				+ ", deadlockTimes=" + deadlockTimes + ", resultDirectory=" + resultDirectory + ", osCollector="
				+ osCollector + ", osCollectorScript=" + osCollectorScript + ", osCollectorSSHAddr="
				+ osCollectorSSHAddr + ", osCollectorDevices=" + osCollectorDevices + ", txnReportInterval="
				+ txnReportInterval + ", chaosTime=" + chaosTime + ", writeCSV=" + writeCSV + ", loadWorkers="
				+ loadWorkers + ", fileLocation=" + fileLocation + "]";
	}
}