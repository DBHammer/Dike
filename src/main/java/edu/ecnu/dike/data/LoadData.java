/*
 * LoadData - Load data directly into database tables or into csv files using multiple parallel workers.
 *
 * Copyright (C) 2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.ZipFianRandom;
import edu.ecnu.dike.util.PrintExceptionUtil;

public class LoadData {
	private static Logger log = Logger.getLogger(LoadData.class);

	private static int numWarehouses;
	private static int[] jobList;
	private static int nextJob;
	private static Object nextJobLock = new Object();

	private static BufferedWriter configCSV;
	private static BufferedWriter itemCSV;
	private static BufferedWriter warehouseCSV;
	private static BufferedWriter districtCSV;
	private static BufferedWriter stockCSV;
	private static BufferedWriter customerCSV;
	private static BufferedWriter historyCSV;
	private static BufferedWriter orderCSV;
	private static BufferedWriter orderLineCSV;
	private static BufferedWriter newOrderCSV;

	public static void main(String[] args) {
		PropertyConfigurator.configure("../config/log4j.properties");

		// load properties
		ConnectionProperty connProps = null;
		RuntimeProperty runtimeProps = null;
		String path = System.getProperty("prop");
		try {
			runtimeProps = new RuntimeProperty(path);
			runtimeProps.loadProperty();
			connProps = new ConnectionProperty(path);
			connProps.loadProperty();
		} catch (IOException ioe) {
			log.error(
					"Fail to load property file from path '" + path + "'" + PrintExceptionUtil.getIOExceptionInfo(ioe));
			System.exit(1);
		}
		numWarehouses = runtimeProps.getWarehouses();
		int numWorkers = runtimeProps.getLoadWorkers();
		boolean writeCSV = runtimeProps.getWriteCSV();
		String fileLocation = runtimeProps.getFileLocation();
		Properties dbProps = connProps.getProperty();

		// shuffle job list
		jobList = new int[numWarehouses];
		for (int i = 0; i < numWarehouses; i++) {
			jobList[i] = i + 1;
		}
		shuffleList(jobList);

		// open csv files in case of write data to file
		if (writeCSV) {
			try {
				configCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_config.csv").toString()));
				itemCSV = new BufferedWriter(new FileWriter(Paths.get(fileLocation, "dike.table_item.csv").toString()));
				warehouseCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_warehouse.csv").toString()));
				districtCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_district.csv").toString()));
				stockCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_stock.csv").toString()));
				customerCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_customer.csv").toString()));
				historyCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_history.csv").toString()));
				orderCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_oorder.csv").toString()));
				orderLineCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_order_line.csv").toString()));
				newOrderCSV = new BufferedWriter(
						new FileWriter(Paths.get(fileLocation, "dike.table_new_order.csv").toString()));
			} catch (IOException ie) {
				log.error("Fail to create result csv file" + PrintExceptionUtil.getIOExceptionInfo(ie));
				System.exit(2);
			}
		}

		// create the number of requested workers and start them
		String csvNullValue = "NULL";
		LoadDataWorker[] workers = new LoadDataWorker[numWorkers];
		Thread[] workerThreads = new Thread[numWorkers];
		BasicRandom rnd = new BasicRandom();
		// Alternative PoissonRandom
		ZipFianRandom zrnd = runtimeProps.isDynamicDistrict() ? new ZipFianRandom(numWarehouses * 10, 1.5) : null;
		for (int i = 0; i < numWorkers; i++) {
			if (writeCSV) {
				workers[i] = new LoadDataWorker(csvNullValue, rnd.newRandom(), zrnd, log);
			} else {
				try {
					Connection conn = DriverManager.getConnection(connProps.getConn(), dbProps);
					conn.setAutoCommit(false);
					workers[i] = new LoadDataWorker(conn, rnd.newRandom(), zrnd, log);
				} catch (SQLException se) {
					log.error("Fail to get database connection" + PrintExceptionUtil.getSQLExceptionInfo(se));
					System.exit(3);
				}
			}
			workerThreads[i] = new Thread(workers[i]);
			workerThreads[i].start();
		}

		for (int i = 0; i < numWorkers; i++) {
			try {
				workerThreads[i].join();
			} catch (InterruptedException ie) {
				log.error(ie.getMessage());
			}
		}

		if (writeCSV) {
			try {
				configCSV.close();
				itemCSV.close();
				warehouseCSV.close();
				districtCSV.close();
				stockCSV.close();
				customerCSV.close();
				historyCSV.close();
				orderCSV.close();
				orderLineCSV.close();
				newOrderCSV.close();
			} catch (IOException ie) {
				log.error("Fail to close data csv files" + PrintExceptionUtil.getIOExceptionInfo(ie));
			}
		}
	} // end main

	public static void configAppend(StringBuffer buf) throws IOException {
		synchronized (configCSV) {
			configCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void itemAppend(StringBuffer buf) throws IOException {
		synchronized (itemCSV) {
			itemCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void warehouseAppend(StringBuffer buf) throws IOException {
		synchronized (warehouseCSV) {
			warehouseCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void districtAppend(StringBuffer buf) throws IOException {
		synchronized (districtCSV) {
			districtCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void stockAppend(StringBuffer buf) throws IOException {
		synchronized (stockCSV) {
			stockCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void customerAppend(StringBuffer buf) throws IOException {
		synchronized (customerCSV) {
			customerCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void historyAppend(StringBuffer buf) throws IOException {
		synchronized (historyCSV) {
			historyCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void orderAppend(StringBuffer buf) throws IOException {
		synchronized (orderCSV) {
			orderCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void orderLineAppend(StringBuffer buf) throws IOException {
		synchronized (orderLineCSV) {
			orderLineCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static void newOrderAppend(StringBuffer buf) throws IOException {
		synchronized (newOrderCSV) {
			newOrderCSV.write(buf.toString());
		}
		buf.setLength(0);
	}

	public static int getNextJob() {
		int job;
		synchronized (nextJobLock) {
			if (nextJob > numWarehouses) {
				job = -1;
			} else {
				job = nextJob++;
				return job == 0 ? job : jobList[job - 1];
			}
		}
		return job;
	}

	public static int getNumWarehouses() {
		return numWarehouses;
	}

	private static void shuffleList(int[] ar) {
		Random rnd = new Random(System.nanoTime());
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
}
