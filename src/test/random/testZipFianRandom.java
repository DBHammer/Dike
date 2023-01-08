package random;

import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.ZipFianRandom;

public class testZipFianRandom {
 
    private int rndRounds = 100000;

    @Test
    public void testNextInt() {
        int rndValue;
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int size = 180;
        double zipfian = 1.5;
        ZipFianRandom rnd = new ZipFianRandom(size, zipfian);
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.nextValue();
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
            map.merge(rndValue, 1, (a, b) -> a + b);
        }
        System.out.println("zipfian: " + zipfian + " int min value: " + minValue + ", max value: " + maxValue);
        System.out.println("histogram int: " + map.toString());
        assertTrue(minValue >= 1);
        assertTrue(maxValue <= size);
    }

    @Test
    public void testGetProbability() {
        int size = 100;
        double skew = 1.5;
        ZipFianRandom rnd = new ZipFianRandom(size, skew);

        double prob = 0;
        for (int i = 1; i <= size; i++) {
            prob += rnd.getProbability(i);
        }
        assertTrue(prob < 1.01);
        assertTrue(prob > 0.99);
    }
}
