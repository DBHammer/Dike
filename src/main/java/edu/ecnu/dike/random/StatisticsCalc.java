/*
 * StaticticsCalc - Use probabilistic model to calculate the target number of warehouse ids.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.random;

public class StatisticsCalc {

    /*
     * use probability model to calculate the actual warehouse number(statistics span node) in distributed transaction and query
     * @param target span node
     * @return statistics span node
     * some mapping relations are listed as follows:
     *  physical node |    3    |    6    |    9    |    12    |    15    |   
     *  target ------------------------------------------------------------
     *        2       |   2.71  |   2.22  |   2.13  |   2.10   |   2.07   |
     *        3       |   8.39  |   3.80  |   3.44  |   3.31   |   3.23   |
     *        4       |    -    |   6.03  |   4.99  |   4.66   |   4.50   |
     *        5       |    -    |   9.83  |   6.88  |   6.19   |   5.88   |
     *        6       |    -    |  22.46  |   9.33  |   7.97   |   7.41   |
     */
    public static int getSpanNode(int target, int physicalNode) {
        if (target == 1) {
            return 1;
        }

        double limits = getProbability(target, physicalNode);
        // calculation result is not integer type, use first decimal part randomly plus 1
        int floor = (int) Math.floor(limits);
        double prob = limits - floor;
        if (Math.random() > prob) {
            return floor;
        } else {
            return floor + 1;
        }
    }

    public static double getProbability(int target, int physicalNode) {
        double limits;
        if (target == physicalNode) {
            // target equals to physicalNode is not a reasonable situation
            // parameter(0.1) can be adjusted for different accuracy
            limits = Math.log(1 - ((double) target - 0.1) / (double) physicalNode)
                    / Math.log(1 - 1 / (double) physicalNode);
        } else {
            limits = Math.log(1 - (double) target / (double) physicalNode) / Math.log(1 - 1 / (double) physicalNode);
        }
        return limits;
    }
}
