package transaction;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.transaction.TxnStockLevel;
import edu.ecnu.dike.random.StatisticsCalc;

public class testTxnStockLevel {
    
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
        TxnStockLevel stockLevel = new TxnStockLevel(dbConn, runtimeProps, rnd);
        stockLevel.setTerminalWarehouseID(1);
        stockLevel.setTerminalDistrictID(1);
        stockLevel.generateData();
        System.out.println(stockLevel.toString());
    }

    @Test
    public void testDistributedRate() {
        String path = "config/template/oceanbase/distributedQuery.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnStockLevel stockLevel;
        int loops = 600000;
        int multiSupplyWid = 0;
        for (int i = 0; i < loops; ++i) {
            stockLevel = new TxnStockLevel(dbConn, runtimeProps, rnd);
            stockLevel.setTerminalWarehouseID(i % runtimeProps.getWarehouses() + 1);
            stockLevel.setTerminalDistrictID(i % 10 + 1);
            stockLevel.generateData();
            if (stockLevel.getWidList() != null) {
                multiSupplyWid += 1;
            }
        }
        int distributedRate = runtimeProps.getStockLevelDistributedRate();
        assertTrue((double)multiSupplyWid / (double)loops > (double)distributedRate / 100 - 0.01);
        assertTrue((double)multiSupplyWid / (double)loops < (double)distributedRate / 100 + 0.01);
    }

    @Test
    public void testDiffWidNumber() {
        String path = "config/template/oceanbase/distributedQuery.properties";
        loadProperty(path);
        BasicRandom rnd = new BasicRandom();
        DbConnection dbConn = new DbConnection(connProps);
        TxnStockLevel stockLevel;
        int loops = 600000;
        double targetDiffWids = StatisticsCalc.getProbability(runtimeProps.getStockLevelWIDNode(), runtimeProps.getPhysicalNode());
        double diffWids = 0;
        int diffWidsCnt = 0;
        HashSet<Integer> widSet = new HashSet<Integer>();
        for (int i = 0; i < loops; ++i) {
            stockLevel = new TxnStockLevel(dbConn, runtimeProps, rnd);
            stockLevel.setTerminalWarehouseID(i % runtimeProps.getWarehouses() + 1);
            stockLevel.setTerminalDistrictID(i % 10 + 1);
            stockLevel.generateData();
            ArrayList<Integer> widList = stockLevel.getWidList();
            if (widList != null) {
                for (int wid : widList) {
                    widSet.add(wid);
                }
                if (widSet.size() > 1) {
                    diffWids += widSet.size();
                    diffWidsCnt++;
                }
                widSet.clear();
            }
        }
        assertTrue(diffWids / diffWidsCnt > targetDiffWids - 0.01);
        assertTrue(diffWids / diffWidsCnt < targetDiffWids + 0.01);
    }
}
