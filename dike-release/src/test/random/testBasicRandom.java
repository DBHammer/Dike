package random;

import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.BasicRandom;

public class testBasicRandom {
    
    private int rndRounds = 100000;

    @Test
    public void testNextInt() {
        int rndValue;
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int leftBound = 1;
        int rightBound = 50;
        BasicRandom rnd = new BasicRandom();
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.nextInt(leftBound, rightBound);
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
            map.merge(rndValue, 1, (a, b) -> a + b);
        }
        System.out.println("random int min value: " + minValue + ", max value: " + maxValue);
        System.out.println("histogram int: " + map.toString());
        assertTrue(minValue >= leftBound);
        assertTrue(maxValue <= rightBound);
    }

    @Test
    public void testNextLong() {
        long rndValue;
        long minValue = Long.MAX_VALUE;
        long maxValue = Long.MIN_VALUE;
        long leftBound = 1;
        long rightBound = 100;
        BasicRandom rnd = new BasicRandom();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.nextLong(leftBound, rightBound);
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
        }
        System.out.println("random long min value: " + minValue + ", max value: " + maxValue);
        assertTrue(minValue >= leftBound);
        assertTrue(maxValue <= rightBound);
    }

    @Test
    public void testNextDouble() {
        double rndValue;
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        double leftBound = 0.0;
        double rightBound = 1.0;
        BasicRandom rnd = new BasicRandom();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.nextDouble(leftBound, rightBound);
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
        }
        System.out.println("random double min value: " + minValue + ", max value: " + maxValue);
        assertTrue(minValue >= leftBound);
        assertTrue(maxValue <= rightBound);
    }
}
