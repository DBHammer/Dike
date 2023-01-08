package transaction;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.transaction.TxnDelivery;

public class testTxnDelivery {
    
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
        TxnDelivery delivery = new TxnDelivery(dbConn, runtimeProps, rnd);
        delivery.setTerminalWarehouseID(1);
        delivery.setTerminalDistrictID(1);
        delivery.generateData();
        System.out.println(delivery.toString());
    }
}
