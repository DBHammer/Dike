/*
 * PoissonRandom - Implementation of poisson sampling method with given parameters and
 *                                    maximum likelihood estimation of poisson distribution.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.random;

import java.util.Random;
import java.util.ArrayList;

import edu.ecnu.dike.util.ListUtil;

public class PoissonRandom {

    private Random rnd;
    private double lambda;
    private double L;

    // initialize poisson distribution with lambda
    // valid lambda ...
    public PoissonRandom(Random rnd, double lambda) {
        this.rnd = rnd;
        this.lambda = lambda;
        L = Math.exp(-lambda);
    }

    // randomly genenrate a value following current poisson distribution
    public int nextValue() {
        int k = 0;
        double p = 1.0;
        do {
            p = p * rnd.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }

    // maximum likelihood estimate lambda with the list of samples
    public void MLEstimation(ArrayList<Double> samples) {
        lambda = ListUtil.calcDoubleAvg(samples);
        L = Math.exp(-lambda);
    }

    public double getLambda() {
        return lambda;
    }
}
