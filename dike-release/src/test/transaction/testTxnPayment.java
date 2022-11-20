package transaction;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import edu.ecnu.dike.config.ConnectionProperty;
import edu.ecnu.dike.config.RuntimeProperty;
import edu.ecnu.dike.connection.DbConnection;
import edu.ecnu.dike.random.BasicRandom;
import edu.ecnu.dike.transaction.TxnPayment;

public class testTxnPayment {
    
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
        TxnPayment payment = new TxnPayment(dbConn, runtimeProps, rnd);
        payment.setTerminalWarehouseID(1);
        payment.setTerminalDistrictID(1);
        payment.generateData();
        System.out.println(payment.toString());
    }
}
