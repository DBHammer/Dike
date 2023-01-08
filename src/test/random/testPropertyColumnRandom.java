package random;

import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.BasicRandom;

public class testPropertyColumnRandom {
    
    int rndRounds = 1000000;

    @Test
    public void testItemID() {
        int rndValue;
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        BasicRandom rnd = new BasicRandom();
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getItemID();
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
            map.merge(rndValue, 1, (a, b) -> a + b);
        }
        System.out.println("random item min value: " + minValue + ", max value: " + maxValue);
        // System.out.println("histogram int: " + map.toString());
        assertTrue(minValue >= 1);
        assertTrue(maxValue <= 100000);
    }

    @Test
    public void testCustomerID() {
        int rndValue;
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        BasicRandom rnd = new BasicRandom();
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getCustomerID();
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
            map.merge(rndValue, 1, (a, b) -> a + b);
        }
        System.out.println("random customer min value: " + minValue + ", max value: " + maxValue);
        System.out.println("histogram int: " + map.toString());
        assertTrue(minValue >= 1);
        assertTrue(maxValue <= 3000);
    }
}
