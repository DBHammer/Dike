package random;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.ecnu.dike.random.StatisticsCalc;

public class testStatisticsCalc {

    private int rndRounds = 100000;

    @Test
    public void testSpanNode() {
        int[] physicalNode = { 3, 6, 9, 12, 15 };
        int[] targets = { 2, 3, 4, 5, 6 };
        double[][] calcs = { { 2.71, 8.39, 1, 1, 1 }, { 2.22, 3.80, 6.03, 9.83, 22.46 },
                { 2.13, 3.44, 4.99, 6.88, 9.33 }, { 2.10, 3.31, 4.66, 6.19, 7.97 }, { 2.07, 3.23, 4.50, 5.88, 7.41 } };
        int rndValue;
        for (int i = 0; i < physicalNode.length; i++) {
            for (int j = 0; j < targets.length; j++) {
                int sumValue = 0;
                for (int k = 0; k < rndRounds; k++) {
                    rndValue = StatisticsCalc.getSpanNode(targets[j], physicalNode[i]);
                    sumValue += rndValue;
                }
                double avg = (double) sumValue / rndRounds;
                assertTrue(Math.max(avg, calcs[i][j]) - Math.min(avg, calcs[i][j]) < 0.01 * calcs[i][j]);
            }
        }
    }
}
