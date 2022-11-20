/*
 * ZipFianRandom - Implementation of zipfian sampling method with given parameters.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.random;

import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipFianRandom {

    private int size;
    private double skew;
    private double bottom = 0;
    private ZipfDistribution zipfDistribution;

    // initialize zipfian distribution with size and skew rate
    // zipfian random number in the range of [1, size]
    public ZipFianRandom(int size, double skew) {
        this.size = size;
        this.skew = skew;
        for (int i = 1; i < size; i++) {
            bottom += (1/Math.pow(i, skew));
        }
        zipfDistribution = new ZipfDistribution(size, skew);
    }

    // randomly genenrate a value following current zipfian distribution in the range of [min, max]
    public int nextValue() {
        return zipfDistribution.sample();
    }

    // get the probability of given zipfian rank
    public double getProbability(int rank) {
        return (1 / Math.pow(rank, skew)) / bottom;
    }

    // get current zipfian size
    public int getSize() {
        return size;
    }
}