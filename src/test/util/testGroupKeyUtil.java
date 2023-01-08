package util;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import edu.ecnu.dike.util.GroupKeyUtil;

public class testGroupKeyUtil {
    
    @Test
    public void testGroupKey() {
        int warehouses = 180;
        int groupsz = 9;
        HashMap<Integer, ArrayList<Integer>> groupWids = GroupKeyUtil.getGroupKey(warehouses, groupsz);
        int groups = warehouses / groupsz;
        assertEquals(groupWids.size(), warehouses / groupsz);
        for (int g = 0; g < groups; g++) {
            assertEquals(groupWids.get(g).size(), groupsz);
        }
        System.out.println(groupWids);
    }
}
