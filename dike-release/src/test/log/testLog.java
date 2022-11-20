package log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class testLog {
 
    @Test
    public void testDifferentLogs() {
        int loopTimes = 100;
        String path = "src/test/log/log4j.properties";
        PropertyConfigurator.configure(path);
        Logger log = Logger.getLogger(testLog.class);
        for (int i = 0; i < loopTimes; i++) {
            log.trace("this is trace log " + i);
            log.debug("this is debug log " + i);
            log.info("this is info log " + i);
            log.error("this is error log " + i);
            log.fatal("this is fatal log " + i);
        }
        String pathErrorLog = "log/dike-test.error.log";
        String pathInfoLog = "log/dike-test.info.log";
        String pathLogDebug = "log/dike-test.debug.log";
        String pathLogTrace = "log/dike-test.trace.log";
        try {
            int errorLogSize = Files.readAllLines(Paths.get(pathErrorLog)).size();
            int infoLogSize = Files.readAllLines(Paths.get(pathInfoLog)).size();
            int debugLogSize = Files.readAllLines(Paths.get(pathLogDebug)).size();
            int traceLogSize = Files.readAllLines(Paths.get(pathLogTrace)).size();
            assertEquals(errorLogSize, loopTimes * 2);
            assertEquals(infoLogSize, loopTimes * 3);
            assertEquals(debugLogSize, loopTimes * 4);
            assertEquals(traceLogSize, loopTimes * 5);
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }
}
