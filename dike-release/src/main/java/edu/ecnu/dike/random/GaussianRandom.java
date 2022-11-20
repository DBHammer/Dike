/*
 * GaussianRandom - Implementation of gaussian sampling method with given parameters and
 *                                    gaussian probability distribution function.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.random;

import java.util.Random;

public class GaussianRandom {

    private Random rnd;
    private int mean;
    private int sdeviation;

    // initialize gaussian distribution with mean and standard deviation
    public GaussianRandom(Random rnd, int mean, int sdeviation) {
        this.rnd = rnd;
        this.mean = mean;
        this.sdeviation = sdeviation;
    }

    // randomly genenrate a value following current gaussian distribution
    public double nextValue() {
        return sdeviation * rnd.nextGaussian() + mean;
    }

    // calculate the probability density function following standard gaussian distribution
    public static double probDistributionFunc(double xval) {
        return Math.exp(-xval * xval / 2) / Math.sqrt(2 * Math.PI);
    }

    // calculate the probability density function following the given gaussian distribution
    public static double probDistributionFunc(double xval, double mean, double sdeviation) {
        return probDistributionFunc((xval - mean) / sdeviation) / sdeviation;
    }
}