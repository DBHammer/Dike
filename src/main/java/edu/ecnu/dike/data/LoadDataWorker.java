/*
 * LoadDataWorker - Load one warehouse or item table.
 *
 * Copyright (C) 2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Formatter;

import org.apache.log4j.Logger;

import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.ZipFianRandom;
import edu.ecnu.dike.statement.StmtLoadData;
import edu.ecnu.dike.util.PrintExceptionUtil;

public class LoadDataWorker implements Runnable {
	private Connection conn;
	private BasicRandom rnd;
	private ZipFianRandom zrnd;
	private Logger log;

	private String csvNull;
	private boolean writeCSV;

	private PreparedStatement stmtConfig;
	private PreparedStatement stmtItem;
	private BatchData<ItemData> batchItem;
	private PreparedStatement stmtWarehouse;
	private PreparedStatement stmtDistrict;
	private PreparedStatement stmtStock;
	private BatchData<StockData> batchStock;
	private PreparedStatement stmtCustomer;
	private BatchData<CustomerData> batchCustomer;
	private PreparedStatement stmtHistory;
	private BatchData<HistoryData> batchHistory;
	private PreparedStatement stmtOrder;
	private BatchData<OrderData> batchOrder;
	private PreparedStatement stmtOrderLine;
	private BatchData<OrderLineData> batchOrderLine;
	private PreparedStatement stmtNewOrder;
	private BatchData<NewOrderData> batchNewOrder;

	private StringBuffer sbConfig;
	private Formatter fmtConfig;
	private StringBuffer sbItem;
	private Formatter fmtItem;
	private StringBuffer sbWarehouse;
	private Formatter fmtWarehouse;
	private StringBuffer sbDistrict;
	private Formatter fmtDistrict;
	private StringBuffer sbStock;
	private Formatter fmtStock;
	private StringBuffer sbCustomer;
	private Formatter fmtCustomer;
	private StringBuffer sbHistory;
	private Formatter fmtHistory;
	private StringBuffer sbOrder;
	private Formatter fmtOrder;
	private StringBuffer sbOrderLine;
	private Formatter fmtOrderLine;
	private StringBuffer sbNewOrder;
	private Formatter fmtNewOrder;

	// write data into csv files
	LoadDataWorker(String csvNull, BasicRandom rnd, ZipFianRandom zrnd, Logger log) {
		this.csvNull = csvNull;
		this.rnd = rnd;
		this.zrnd = zrnd;
		this.log = log;

		writeCSV = true;
		sbConfig = new StringBuffer();
		fmtConfig = new Formatter(sbConfig);
		sbItem = new StringBuffer();
		fmtItem = new Formatter(sbItem);
		sbWarehouse = new StringBuffer();
		fmtWarehouse = new Formatter(sbWarehouse);
		sbDistrict = new StringBuffer();
		fmtDistrict = new Formatter(sbDistrict);
		sbStock = new StringBuffer();
		fmtStock = new Formatter(sbStock);
		sbCustomer = new StringBuffer();
		fmtCustomer = new Formatter(sbCustomer);
		sbHistory = new StringBuffer();
		fmtHistory = new Formatter(sbHistory);
		sbOrder = new StringBuffer();
		fmtOrder = new Formatter(sbOrder);
		sbOrderLine = new StringBuffer();
		fmtOrderLine = new Formatter(sbOrderLine);
		sbNewOrder = new StringBuffer();
		fmtNewOrder = new Formatter(sbNewOrder);
	}

	// load data into database
	LoadDataWorker(Connection conn, BasicRandom rnd, ZipFianRandom zrnd, Logger log) throws SQLException {
		this.conn = conn;
		this.rnd = rnd;
		this.zrnd = zrnd;
		this.log = log;

		writeCSV = false;
		StmtLoadData stmtLoadData = new StmtLoadData(conn);
		stmtConfig = stmtLoadData.getStmtConfig();
		stmtItem = stmtLoadData.getStmtItem();
		batchItem = new BatchData<>();
		stmtWarehouse = stmtLoadData.getStmtWarehouse();
		stmtDistrict = stmtLoadData.getStmtDistrict();
		stmtStock = stmtLoadData.getStmtStock();
		batchStock = new BatchData<>();
		stmtCustomer = stmtLoadData.getStmtCustomer();
		batchCustomer = new BatchData<>();
		stmtHistory = stmtLoadData.getStmtHistory();
		batchHistory = new BatchData<>();
		stmtOrder = stmtLoadData.getStmtOrder();
		batchOrder = new BatchData<>();
		stmtOrderLine = stmtLoadData.getStmtOrderLine();
		batchOrderLine = new BatchData<>();
		stmtNewOrder = stmtLoadData.getStmtNewOrder();
		batchNewOrder = new BatchData<>();
	}

	@Override
	public void run() {
		if (writeCSV) {
			try {
				runWrite();
			} catch (IOException ioe) {
				log.error("Fail to write tables into csv files, " + PrintExceptionUtil.getIOExceptionInfo(ioe));
				System.exit(1);
			}

		} else {
			try {
				runLoad();
			} catch (SQLException se) {
				log.error("Fail to load batch data into database after retry 10 times, " + PrintExceptionUtil.getSQLExceptionInfo(se));
				System.exit(2);
			}
		}

	}

	private void runLoad() throws SQLException {
		int job;
		while ((job = LoadData.getNextJob()) >= 0) {
			if (job == 0) {
				// the first job is to load config table and item table
				log.info("Loading item");
				loadItem();
				log.info("Loading item done");
			} else {
				// the remaining jobs are to load warehouse tables according to w_id
				log.info("Loading warehouse " + job);
				loadWarehouse(job);
				log.info("Loading warehouse " + job + " done");
			}
		}
		conn.close();
	}

	private void runWrite() throws IOException {
		int job;
		while ((job = LoadData.getNextJob()) >= 0) {
			if (job == 0) {
				// the first job is to load config table and item table
				log.info("Loading item");
				writeItem();
				log.info("Loading item done");
			} else {
				// the remaining jobs are to load warehouse tables according to w_id
				log.info("Loading warehouse " + job);
				writeWarehouse(job);
				log.info("Loading warehouse " + job + " done");
			}
		}
	}

	private void writeItem() throws IOException {
		// save table config in csv mode
		fmtConfig.format("warehouses,%d\n", LoadData.getNumWarehouses());
		fmtConfig.format("nURandCLast,%d\n", rnd.getNURandCLast());
		fmtConfig.format("nURandCC_ID,%d\n", rnd.getNURandCCID());
		fmtConfig.format("nURandCI_ID,%d\n", rnd.getNURandCIID());
		LoadData.configAppend(sbConfig);

		// save table item in csv mode
		int i_id = 0;
		for (i_id = 1; i_id <= 100000; i_id++) {
			String iData;

			// write items in batch of 1000 rows
			if (i_id != 1 && (i_id - 1) % 1000 == 0) {
				LoadData.itemAppend(sbItem);
			}

			if (rnd.nextInt(1, 100) <= 10) {
				int len = rnd.nextInt(26, 50);
				int off = rnd.nextInt(0, len - 8);
				iData = rnd.getAString(off, off) + "ORIGINAL" + rnd.getAString(len - off - 8, len - off - 8);
			} else {
				iData = rnd.getAString(26, 50);
			}

			fmtItem.format("%d,%s,%.2f,%s,%d\n",
					i_id,
					rnd.getAString(14, 24),
					((double) rnd.nextLong(100, 10000)) / 100.0,
					iData,
					rnd.nextInt(1, 10000));
		} // end item for
		LoadData.itemAppend(sbItem);
	}

	private void writeWarehouse(int w_id) throws IOException {
		// save table warehouse in csv mode
		fmtWarehouse.format("%d,%.2f,%.4f,%s,%s,%s,%s,%s,%s\n",
				w_id,
				300000.0,
				((double) rnd.nextLong(0, 2000)) / 10000.0,
				rnd.getAString(6, 10),
				rnd.getAString(10, 20),
				rnd.getAString(10, 20),
				rnd.getAString(10, 20),
				rnd.getState(),
				rnd.getNString(4, 4) + "11111");
		LoadData.warehouseAppend(sbWarehouse);

		// for each warehouse, there are 100,000 stock rows
		// save table stock in csv mode
		for (int s_i_id = 1; s_i_id <= 100000; s_i_id++) {
			String sData;
			// write stocks in batch of 10,000 rows
			if (s_i_id != 1 && (s_i_id - 1) % 10000 == 0) {
				LoadData.stockAppend(sbStock);
			}

			if (rnd.nextInt(1, 100) <= 10) {
				int len = rnd.nextInt(26, 50);
				int off = rnd.nextInt(0, len - 8);
				sData = rnd.getAString(off, off) + "ORIGINAL" + rnd.getAString(len - off - 8, len - off - 8);
			} else {
				sData = rnd.getAString(26, 50);
			}

			fmtStock.format("%d,%d,%d,%d,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
					w_id,
					s_i_id,
					rnd.nextInt(10, 100),
					0,
					0,
					0,
					sData,
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24));
		} // end for stock
		LoadData.stockAppend(sbStock);

		// for each warehouse there are 10 (or randomly generated) district rows
		// Alternative PoissonRandom
		int d_num = zrnd == null ? 10 : (int) Math.ceil(zrnd.getSize() * zrnd.getProbability(w_id));
		for (int d_id = 1; d_id <= d_num; d_id++) {

			// save table district in csv mode
			fmtDistrict.format("%d,%d,%.2f,%.4f,%d,%s,%s,%s,%s,%s,%s\n",
					w_id,
					d_id,
					30000.0,
					((double) rnd.nextLong(0, 2000)) / 10000.0,
					3001,
					rnd.getAString(6, 10),
					rnd.getAString(10, 20),
					rnd.getAString(10, 20),
					rnd.getAString(10, 20),
					rnd.getState(),
					rnd.getNString(4, 4) + "11111");
			LoadData.districtAppend(sbDistrict);
			// within each district there are 3,000 customers
			for (int c_id = 1; c_id <= 3000; c_id++) {
				// write customers and historys in batch of 300 rows
				if (c_id != 1 && (c_id - 1) % 300 == 0) {
					LoadData.customerAppend(sbCustomer);
					LoadData.historyAppend(sbHistory);
				}

				fmtCustomer.format("%d,%d,%d,%.4f,%s,%s,%s,%.2f,%.2f,%.2f,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
						w_id,
						d_id,
						c_id,
						((double) rnd.nextLong(0, 5000)) / 10000.0,
						(rnd.nextInt(1, 100) <= 90) ? "GC" : "BC",
						(c_id <= 1000) ? rnd.getCLast(c_id - 1) : rnd.getCLast(),
						rnd.getAString(8, 16),
						50000.00,
						-10.00,
						10.00,
						1,
						0,
						rnd.getAString(10, 20),
						rnd.getAString(10, 20),
						rnd.getAString(10, 20),
						rnd.getState(),
						rnd.getNString(4, 4) + "11111",
						rnd.getCPhone(w_id, d_id, c_id),
						new java.sql.Timestamp(System.currentTimeMillis()).toString(),
						"OE",
						rnd.getAString(300, 500));

				// for each customer there is one row in history
				fmtHistory.format("%d,%d,%d,%d,%d,%d,%s,%.2f,%s\n",
						(w_id - 1) * 30000 + (d_id - 1) * 3000 + c_id,
						c_id,
						d_id,
						w_id,
						d_id,
						w_id,
						new java.sql.Timestamp(System.currentTimeMillis()).toString(),
						10.00,
						rnd.getAString(12, 24));
			} // end for customer, history
			LoadData.customerAppend(sbCustomer);
			LoadData.historyAppend(sbHistory);

			// generate one order row for each customer and shuffle
			int randomCID[] = new int[3000];
			for (int i = 0; i < 3000; i++) {
				randomCID[i] = i + 1;
			}

			for (int i = 0; i < 3000; i++) {
				int x = rnd.nextInt(0, 2999);
				int y = rnd.nextInt(0, 2999);
				int tmp = randomCID[x];
				randomCID[x] = randomCID[y];
				randomCID[y] = tmp;
			}

			for (int o_id = 1; o_id <= 3000; o_id++) {
				int o_ol_cnt = rnd.nextInt(5, 15);
				// write orders in batch of 300
				if (o_id != 1 && (o_id - 1) % 300 == 0) {
					LoadData.orderAppend(sbOrder);
					LoadData.orderLineAppend(sbOrderLine);
					LoadData.newOrderAppend(sbNewOrder);
				}
				fmtOrder.format("%d,%d,%d,%d,%s,%d,%d,%s\n",
						w_id,
						d_id,
						o_id,
						randomCID[o_id - 1],
						(o_id < 2101) ? rnd.nextInt(1, 10) : csvNull,
						o_ol_cnt,
						1,
						new java.sql.Timestamp(System.currentTimeMillis()).toString());

				// create the order_line rows for this order
				for (int ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
					long now = System.currentTimeMillis();
					fmtOrderLine.format("%d,%d,%d,%d,%d,%s,%.2f,%d,%d,%s\n",
							w_id,
							d_id,
							o_id,
							ol_number,
							rnd.nextInt(1, 100000),
							(o_id < 2101) ? new java.sql.Timestamp(now).toString() : csvNull,
							(o_id < 2101) ? 0.00 : ((double) rnd.nextLong(1, 999999)) / 100.0,
							w_id,
							5,
							rnd.getAString(24, 24));
				} // end for orderline

				// the last 900 orders are not yet delieverd and have a row in new_order
				if (o_id >= 2101) {
					fmtNewOrder.format("%d,%d,%d\n",
							w_id,
							d_id,
							o_id);
				}
			} // end for order, neworder
			LoadData.orderAppend(sbOrder);
			LoadData.orderLineAppend(sbOrderLine);
			LoadData.newOrderAppend(sbNewOrder);
		} // end for district
	}

	private void loadItem() throws SQLException {
		// save table config in database mode
		stmtConfig.setString(1, "warehouses");
		stmtConfig.setString(2, "" + LoadData.getNumWarehouses());
		stmtConfig.execute();

		stmtConfig.setString(1, "nURandCLast");
		stmtConfig.setString(2, "" + rnd.getNURandCLast());
		stmtConfig.execute();

		stmtConfig.setString(1, "nURandCCID");
		stmtConfig.setString(2, "" + rnd.getNURandCCID());
		stmtConfig.execute();

		stmtConfig.setString(1, "nURandCIID");
		stmtConfig.setString(2, "" + rnd.getNURandCIID());
		stmtConfig.execute();
		conn.commit();
		stmtConfig.close();

		// save table item in database mode
		int i_id;
		for (i_id = 1; i_id <= 100000; i_id++) {
			String iData;
			if (rnd.nextInt(1, 100) <= 10) {
				int len = rnd.nextInt(26, 50);
				int off = rnd.nextInt(0, len - 8);
				iData = rnd.getAString(off, off) + "ORIGINAL" + rnd.getAString(len - off - 8, len - off - 8);
			} else {
				iData = rnd.getAString(26, 50);
			}
			ItemData newItem = new ItemData(
					i_id,
					rnd.getAString(14, 24),
					((double) rnd.nextLong(100, 10000)) / 100.0,
					iData, rnd.nextInt(1, 10000));
			newItem.setParameters(stmtItem);
			batchItem.addBatch(newItem);

			// load items in batch of 1000
			if (i_id % 1000 == 0) {
				commitWithRetry(stmtItem, batchItem);
			}
		} // end for item
		stmtItem.close();
	} // end loadItem

	private void loadWarehouse(int w_id) throws SQLException {
		// save table warehouse in database mode
		stmtWarehouse.setInt(1, w_id);
		stmtWarehouse.setDouble(2, 300000.0);
		stmtWarehouse.setDouble(3, ((double) rnd.nextLong(0, 2000)) / 10000.0);
		stmtWarehouse.setString(4, rnd.getAString(6, 10));
		stmtWarehouse.setString(5, rnd.getAString(10, 20));
		stmtWarehouse.setString(6, rnd.getAString(10, 20));
		stmtWarehouse.setString(7, rnd.getAString(10, 20));
		stmtWarehouse.setString(8, rnd.getState());
		stmtWarehouse.setString(9, rnd.getNString(4, 4) + "11111");
		stmtWarehouse.execute();
		conn.commit();

		// for each warehouse, there are 100,000 stock rows
		// save table stock in database mode
		for (int s_i_id = 1; s_i_id <= 100000; s_i_id++) {
			String sData;
			if (rnd.nextInt(1, 100) <= 10) {
				int len = rnd.nextInt(26, 50);
				int off = rnd.nextInt(0, len - 8);
				sData = rnd.getAString(off, off) + "ORIGINAL" + rnd.getAString(len - off - 8, len - off - 8);
			} else {
				sData = rnd.getAString(26, 50);
			}
			StockData newStock = new StockData(
					w_id,
					s_i_id,
					rnd.nextInt(10, 100),
					0,
					0,
					0,
					sData,
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24),
					rnd.getAString(24, 24));
			newStock.setParameters(stmtStock);
			batchStock.addBatch(newStock);

			// load stocks in batch of 100 rows
			if (s_i_id % 1000 == 0) {
				commitWithRetry(stmtStock, batchStock);
			}
		} // end for stock

		// for each warehouse there are 10 (or randomly generated) district rows
		// Alternative PoissonRandom
		int d_num = zrnd == null ? 10 : (int) Math.ceil(zrnd.getSize() * zrnd.getProbability(w_id));
		for (int d_id = 1; d_id <= d_num; d_id++) {
			// save table district in database mode
			stmtDistrict.setInt(1, w_id);
			stmtDistrict.setInt(2, d_id);
			stmtDistrict.setDouble(3, 30000.0);
			stmtDistrict.setDouble(4, ((double) rnd.nextLong(0, 2000)) / 10000.0);
			stmtDistrict.setInt(5, 3001);
			stmtDistrict.setString(6, rnd.getAString(6, 10));
			stmtDistrict.setString(7, rnd.getAString(10, 20));
			stmtDistrict.setString(8, rnd.getAString(10, 20));
			stmtDistrict.setString(9, rnd.getAString(10, 20));
			stmtDistrict.setString(10, rnd.getState());
			stmtDistrict.setString(11, rnd.getNString(4, 4) + "11111");
			stmtDistrict.execute();
			conn.commit();

			// within each district there are 3,000 customers
			// save table customer and history in database mode
			for (int c_id = 1; c_id <= 3000; c_id++) {
				CustomerData newCustomer = new CustomerData(
						w_id,
						d_id,
						c_id,
						((double) rnd.nextLong(0, 5000)) / 10000.0,
						rnd.nextInt(1, 100) <= 90 ? "GC" : "BC",
						c_id <= 1000 ? rnd.getCLast(c_id - 1) : rnd.getCLast(),
						rnd.getAString(8, 16),
						50000.00,
						-10.00,
						10.00,
						1,
						0,
						rnd.getAString(10, 20),
						rnd.getAString(10, 20),
						rnd.getAString(10, 20),
						rnd.getState(),
						rnd.getNString(4, 4) + "11111",
						rnd.getCPhone(w_id, d_id, c_id),
						new java.sql.Timestamp(System.currentTimeMillis()),
						"OE",
						rnd.getAString(300, 500));
				newCustomer.setParameters(stmtCustomer);
				batchCustomer.addBatch(newCustomer);

				// for each customer there is one row in history
				// stmtHistory.setInt(1, (w_id - 1) * 30000 + (d_id - 1) * 3000 + c_id);
				HistoryData newHistory = new HistoryData(
						c_id,
						d_id,
						w_id,
						d_id,
						w_id,
						new java.sql.Timestamp(System.currentTimeMillis()),
						10.00,
						rnd.getAString(12, 24));
				newHistory.setParameters(stmtHistory);
				batchHistory.addBatch(newHistory);
				// load customers and historys in batch of 100 rows
				if (c_id % 300 == 0) {
					commitWithRetry(stmtCustomer, batchCustomer);
					commitWithRetry(stmtHistory, batchHistory);
				}
			} // end for customer, history

			// generate one order row for each customer and shuffle
			int randomCID[] = new int[3000];
			for (int i = 0; i < 3000; i++) {
				randomCID[i] = i + 1;
			}
			for (int i = 0; i < 3000; i++) {
				int x = rnd.nextInt(0, 2999);
				int y = rnd.nextInt(0, 2999);
				int tmp = randomCID[x];
				randomCID[x] = randomCID[y];
				randomCID[y] = tmp;
			}

			// save table order, orderline and neworder in database mode
			for (int o_id = 1; o_id <= 3000; o_id++) {
				int o_ol_cnt = rnd.nextInt(5, 15);
				OrderData newOrder = new OrderData(
						w_id,
						d_id,
						o_id,
						randomCID[o_id - 1],
						rnd.nextInt(1, 10),
						o_ol_cnt,
						1,
						new java.sql.Timestamp(System.currentTimeMillis()));
				newOrder.setParameters(stmtOrder);
				batchOrder.addBatch(newOrder);

				// create the orderline rows for this order
				for (int ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
					long now = System.currentTimeMillis();
					OrderLineData newOrderLine = new OrderLineData(
							w_id,
							d_id,
							o_id,
							ol_number,
							rnd.nextInt(1, 100000),
							new java.sql.Timestamp(now),
							((double) rnd.nextLong(1, 999999)) / 100.0,
							w_id,
							5,
							rnd.getAString(24, 24));
					newOrderLine.setParameters(stmtOrderLine);
					batchOrderLine.addBatch(newOrderLine);
				} // end for orderline

				// the last 900 orders are not yet delieverd and have a row in neworder
				if (o_id >= 2101) {
					NewOrderData newNewOrder = new NewOrderData(
							w_id,
							d_id,
							o_id);
					newNewOrder.setParameters(stmtNewOrder);
					batchNewOrder.addBatch(newNewOrder);
				}

				// load orders in batch of 300
				if (o_id % 300 == 0) {
					commitWithRetry(stmtOrder, batchOrder);
					commitWithRetry(stmtOrderLine, batchOrderLine);
					commitWithRetry(stmtNewOrder, batchNewOrder);
				}
			} // end for order, neworder
		} // end for stock
	} // end loadWarehouse

	private <T> void commitWithRetry(PreparedStatement stmt, BatchData<T> batch) throws SQLException {
		int retryCnt = 1;
		while (true) {
			try {
				if (retryCnt > 1) {
					for (T data : batch.getBatch()) {
						if (data instanceof TableData) {
							TableData tmp = (TableData) data;
							tmp.setParameters(stmt);
						}
					}
				}
				stmt.executeBatch();
				conn.commit();
				stmt.clearBatch();
				batch.clearBatch();
				break;
			} catch (SQLException se) {
				conn.rollback();
				stmt.clearBatch();
				if (retryCnt > 10) {
					throw se;
				}
				retryCnt++;
			}
		}
	}
}