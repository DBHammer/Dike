package config;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.config.ConnectionProperty;

public class testPropertyFile {
    
    private RuntimeProperty runtimeProps;
    private ConnectionProperty connProps;

    private void loadProperty(String path) {
        try {
            runtimeProps = new RuntimeProperty(path);
            connProps = new ConnectionProperty(path);
            runtimeProps.loadProperty();
            connProps.loadProperty();            
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }

    private void printProperties() {
        System.out.println("Runtime Properties:");
        System.out.println(runtimeProps.toString());
        System.out.println("Connection Properties:");
        System.out.println(connProps.toString());
    }

    @Test
    public void testDikeProperty() {
        String path = "config/template/oceanbase/dike.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testDynamicLoadProperty() {
        String path = "config/template/oceanbase/dynamicLoad.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testDynamicConflictProperty() {
        String path = "config/template/oceanbase/dynamicConflict.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testDistributedTransactionProperty() {
        String path = "config/template/oceanbase/distributedTransaction.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testDistributedQueryProperty() {
        String path = "config/template/oceanbase/distributedQuery.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testErrorMixtureTransactionsProperty() {
        String path = "src/test/config/errorMixture.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "get unexpected mixture transaction rate";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testErrorIsolationProperty() {
        String path = "src/test/config/errorIsolation.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "isolationLevel should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testChaosTimeOutOfRangeProperty() {
        String path = "src/test/config/chaosTimeOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "chaosTime should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCoaccessNumberOutOfRangeProperty() {
        String path = "src/test/config/coaccessNumberOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "coaccessNumber should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testDeadlockTimesOutOfRangeProperty() {
        String path = "src/test/config/deadlockTimesOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "deadlockTimes should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testDistributedRateOutOfRangeProperty() {
        String path = "src/test/config/distributedRateOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "newOrderDistributedRate should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNoStatisticsProperty() {
        String path = "src/test/config/noStatistics.properties";
        loadProperty(path);
        printProperties();
    }

    @Test
    public void testNoStatisticsSpanNodeOutOfRangeProperty() {
        String path = "src/test/config/noStatisticsSpanNodeOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "stockLevelWIDNode should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testStatisticsSpanNodeOutOfRangeProperty() {
        String path = "src/test/config/statisticsSpanNodeOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "stockLevelWIDNode should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullHostProperty() {
        String path = "src/test/config/nullHost.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "can not get requisite property, check input 'host'";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullPortProperty() {
        String path = "src/test/config/nullPort.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "can not get requisite property, check input 'port'";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullSetProperty() {
        String path = "src/test/config/nullSet.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "can not get requisite property, check input 'set'";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullUserProperty() {
        String path = "src/test/config/nullUser.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "can not get requisite property, check input 'user'";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testPartitionOutOfRangeProperty() {
        String path = "src/test/config/partitionOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "partitions should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testSnapshotTimesOutOfRangeProperty() {
        String path = "src/test/config/snapshotTimesOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "snapshotTimes should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testTerminalOutOfRangeProperty() {
        String path = "src/test/config/terminalOutOfRange.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "terminalRange(right) should among";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testUnknownProperty() {
        String path = "src/test/config/unknownDistribution.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "get unexpected warehouse distribution";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testZipfianFactorZeroProperty() {
        String path = "src/test/config/zipfianFactorZero.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "warehouseDistribution(zipfian factor) should be positive";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testDynamicTransactionErrorLength() {
        String path = "src/test/config/dynamicTransactionErrorLength.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "changeTransactions/changePoints should equals to ";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testDynamicTransactionErrorMixture() {
        String path = "src/test/config/dynamicTransactionErrorMixture.properties";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loadProperty(path);
            printProperties();
        });
        String expectedMessage = "get unexpected mixture transaction rate";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
