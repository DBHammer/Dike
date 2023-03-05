package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import edu.ecnu.dike.util.GroupKeyUtil;

public class testConcurrentSingleton {
    
    private int concurrency = 500;

    @Test
    public void testGroupKey() {
        int groupsz = 9;
        int warehouses = 180;
        int groups = warehouses / groupsz;
        Thread[] workers = new Thread[concurrency];
        for (int i = 0; i < concurrency; i++) {
            workers[i] = new Thread(() -> {
                HashMap<Integer, ArrayList<Integer>> groupWids = GroupKeyUtil.getGroupKey(warehouses, groupsz);
                assertNotNull(groupWids);
                assertEquals(groupWids.size(), warehouses / groupsz);
                for (int g = 0; g < groups; g++) {
                    assertEquals(groupWids.get(g).size(), groupsz);
                }
            });
        }
        for (Thread worker : workers) {
            worker.start();
        }
        try {
            for (Thread worker : workers) {
                worker.join();
            }
        } catch (InterruptedException ie) {
            fail(ie.getMessage());
        }
    }
}
