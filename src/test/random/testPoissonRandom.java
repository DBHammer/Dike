package random;

import java.util.Random;
import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.*;

public class testPoissonRandom {
    
    private int rndRounds = 100000;

    @Test
    public void testNextValue() {
        int rndValue;
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        int sumValue = 0;
        int lambda = 50;
        Random rnd = new Random(System.nanoTime());
        HashMap<Integer, Integer> map = new HashMap<>();

        PoissonRandom prnd = new PoissonRandom(rnd, lambda);
        for (int i = 0; i < rndRounds; i++) {
            rndValue = prnd.nextValue();
            minValue = Math.min(rndValue, minValue);
            maxValue = Math.max(rndValue, maxValue);
            map.merge(rndValue, 1, (a, b) -> a + b);
            sumValue += rndValue;
        }
        System.out.println("poisson int min value: " + minValue + ", max value: " + maxValue);
        System.out.println("histogram int: " + map.toString());
        assertTrue(sumValue / rndRounds >= lambda - 1 && sumValue / rndRounds <= lambda + 1);
    }
}
