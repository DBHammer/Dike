package transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Test;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.random.StatisticsCalc;
import edu.ecnu.dike.transaction.TxnNewOrder;

public class testTxnNewOrder {
    
    private RuntimeProperty runtimeProps;
    private ConnectionProperty connProps;

    private void loadProperty(String path) {
        try {
            runtimeProps = new RuntimeProperty(path);
            runtimeProps.loadProperty();
            connProps = new ConnectionProperty(path);
            connProps.loadProperty();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }

    @Test
    public void testGenerateData() {
        String path = "config/template/oceanbase/dike.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnNewOrder newOrder= new TxnNewOrder(dbConn, runtimeProps, rnd, null);
        newOrder.setTerminalWarehouseID(1);
        newOrder.setTerminalDistrictID(1);
        newOrder.generateData();
        System.out.println(newOrder.toString());
    }

    @Test
    public void testDistributedRate() {
        String path = "config/template/oceanbase/distributedTransaction.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnNewOrder newOrder;
        int loops = 600000;
        int multiSupplyWid = 0;
        HashSet<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < loops; ++i) {
            newOrder = new TxnNewOrder(dbConn, runtimeProps, rnd, null);
            newOrder.setTerminalWarehouseID(i % runtimeProps.getWarehouses() + 1);
            newOrder.setTerminalDistrictID(i % 10 + 1);
            newOrder.generateData();
            for (int wid : newOrder.getSupplyWid()) {
                if (wid != 0) {
                    set.add(wid);
                }
            }
            if (set.size() > 1) {
                multiSupplyWid += 1;
            }
            set.clear();
        }
        int distributedRate = runtimeProps.getNewOrderDistributedRate();
        assertTrue((double)multiSupplyWid / (double)loops > (double)distributedRate / 100 - 0.01);
        assertTrue((double)multiSupplyWid / (double)loops < (double)distributedRate / 100 + 0.01);
    }

    @Test
    public void testDiffWidNumber() {
        String path = "config/template/oceanbase/distributedTransaction.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnNewOrder newOrder;
        int loops = 600000;
        double targetDiffWids = StatisticsCalc.getProbability(runtimeProps.getNewOrderSpanNode(), runtimeProps.getPhysicalNode());
        double diffWids = 0;
        int diffWidsCnt = 0;
        HashSet<Integer> widSet = new HashSet<Integer>();
        for (int i = 0; i < loops; ++i) {
            newOrder = new TxnNewOrder(dbConn, runtimeProps, rnd, null);
            newOrder.setTerminalWarehouseID(i % runtimeProps.getWarehouses() + 1);
            newOrder.setTerminalDistrictID(i % 10 + 1);
            newOrder.generateData();
            for (int wid : newOrder.getSupplyWid()) {
                if (wid != 0) {
                    widSet.add(wid);
                }
            }
            if (widSet.size() > 1) {
                diffWids += widSet.size();
                diffWidsCnt++;
            }
            widSet.clear();
        }
        assertTrue(diffWids / diffWidsCnt > targetDiffWids - 0.01);
        assertTrue(diffWids / diffWidsCnt < targetDiffWids + 0.01);
    }

    @Test
    public void testItemNumberFixed() {
        String path = "config/template/oceanbase/distributedTransaction.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnNewOrder newOrder;
        int loops = 100;
        for (int i = 0; i < loops; ++i) {
            newOrder = new TxnNewOrder(dbConn, runtimeProps, rnd, null);
            newOrder.generateData();
            assertEquals(newOrder.getOlCnt(), 10);
        }
    }

    @Test
    public void testItemNumber() {
        String path = "config/template/oceanbase/dike.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnNewOrder newOrder;
        int loops = 100000;
        int itemCnts = 0;
        for (int i = 0; i < loops; ++i) {
            newOrder = new TxnNewOrder(dbConn, runtimeProps, rnd, null);
            newOrder.generateData();
            itemCnts += newOrder.getOlCnt();
        }
        assertTrue((double)itemCnts / (double)loops > 10 - 0.01);
        assertTrue((double)itemCnts / (double)loops < 10 + 0.01);
    }
}
