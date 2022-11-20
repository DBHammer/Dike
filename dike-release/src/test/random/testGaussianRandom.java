package random;

import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.GaussianRandom;

public class testGaussianRandom {
    
    private int rndRounds = 1000000;

    @Test
    public void testNextValue() {
        int mean = 500;
        int sdeviation = 150;
        double rndValue;
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;
        double sumValue = 0;
        Random rnd = new Random(System.currentTimeMillis());
        GaussianRandom rndGaussian = new GaussianRandom(rnd, mean, sdeviation);
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rndGaussian.nextValue();
            maxValue = Math.max(maxValue, rndValue);
            minValue = Math.min(minValue, rndValue);
            sumValue += rndValue;
        }
        System.out.println("random gaussian, max value: " + maxValue + ", min value: " + minValue);
        assertTrue(sumValue / rndRounds >= mean - 1 && sumValue / rndRounds <= mean + 1);
    }
}
