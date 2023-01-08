/*
 * CheckParamsUtil - Check the validity of input properties.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.util.Arrays;

public class CheckParamsUtil {

    public static void checkEqual(double target, double actual, String name) {
        if (target != actual) {
            throw new RuntimeException(name + " should equals to " + target + ", check input '" + name + "': " + actual);
        }
    }

    public static void checkEqual(String target, String actual, String name) {
        if (target.equals(actual)) {
            throw new RuntimeException(name + " should equals to " + target + ", check input '" + name + "': " + actual);
        }
    }

    public static void checkPositive(double param, String name) {
        if (param < 1) {
            throw new RuntimeException(name + " should be positive double, check input '" + name + "': " + param);
        }
    }

    public static void checkNonNegative(double param, String name) {
        if (param < 0) {
            throw new RuntimeException(name + " should be non-negative integer, check input '" + name + "': " + param);
        }
    }

    public static void checkRange(double param, String name, double leftRange, double rightRange) {
        if (param < leftRange || param > rightRange) {
            throw new RuntimeException(name + " should among [" + leftRange + "," + rightRange + "], check input '"
                    + name + "': " + param);
        }
    }

    public static void checkTerminalRange(int leftRange, int rightRange, int warehouses) {
        checkRange(leftRange, "terminalRange(left)", 1, warehouses);
        checkRange(rightRange, "terminalRange(right)", 1, warehouses);
        if (leftRange > rightRange) {
            throw new RuntimeException("illegal terminal range inequality, check input 'terminalRange': "
                    + leftRange + "," + rightRange);
        }
    }

    public static void checkWarehouseDistribution(String distribution) {
        String[] distributions = distribution.split(":");
        switch (distributions[0]) {
            case "uniform":
                break;
            case "zipfian":
                checkPositive(Double.parseDouble(distributions[1]), "warehouseDistribution(zipfian factor)");
                break;
            default:
                throw new RuntimeException("get unexpected warehouse distribution (only uniform and zipfian are valid), check input 'warehouseDistribution' :" + distribution);
        }
    }

    public static void checkMixtureTransactions(int... weights) {
        if (weights.length != 9) {
            throw new RuntimeException("number of transaction type shoud be 9, check input transaction mixture " + Arrays.toString(weights));
        }
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        if (totalWeight != 100) {
            throw new RuntimeException("get unexpected mixture transaction rate, check input 'newOrderWeight': "
                    + weights[0] + ", 'paymentWeight': " + weights[1] + ", 'orderStatusWeight': "
                    + weights[2] + ", 'deliveryWeight': " + weights[3] + ", 'stockLevelWeight': "
                    + weights[4] + ", 'updateItemWeight': " + weights[5] + ", 'updateStockWeight': "
                    + weights[6] + ", 'globalSnapshotWeight': " + weights[7]
                    + ", 'globalDeadlockWeight': " + weights[8]);
        }
    }

    public static void checkNull(String str, String name) {
        if (str.equals("")) {
            throw new RuntimeException("can not get requisite property, check input '" + name + "'");
        }
    }
}
