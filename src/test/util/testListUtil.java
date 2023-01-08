package util;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import edu.ecnu.dike.util.ListUtil;

public class testListUtil {
    
    @Test
    public void testCalculateAvg() {
        ArrayList<Integer> samples = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            samples.add(i);
        }
        double avg = ListUtil.calcIntegerAvg(samples);

        assertEquals(499.5, avg, 0.1);
    }
}
