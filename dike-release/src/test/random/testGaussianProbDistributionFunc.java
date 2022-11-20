package random;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.GaussianRandom;

public class testGaussianProbDistributionFunc {
    
    private int mean = 500;
    private int sdeviation = 100;

    @Test
    public void testProbDistributionFunc() {
        int range = 1000;
        double rndValue;
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;

        for (int i = 0; i < range; i++) {
            rndValue = GaussianRandom.probDistributionFunc(i, mean, sdeviation);
            maxValue = Math.max(maxValue, rndValue);
            minValue = Math.min(minValue, rndValue);
            if (i % 100 == 0) {
                System.out.println(i / 100 + " boudary probability: " + rndValue);
            }
        }

        assertTrue(maxValue == GaussianRandom.probDistributionFunc(mean, mean, sdeviation));
        System.out.println("min value: " + minValue + ", max value:" + maxValue);
    }
}
