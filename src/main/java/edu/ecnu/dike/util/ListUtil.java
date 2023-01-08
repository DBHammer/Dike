/*
 * ListUtil - Caculate the average value in the given list.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.util.List;
import java.util.stream.Collectors;

public class ListUtil {

    public static double calcIntegerAvg(List<Integer> list) {
        return list.stream()
                .collect(Collectors.averagingInt(x -> x));
    }

    public static double calcLongAvg(List<Long> list) {
        return list.stream()
                .collect(Collectors.averagingLong(x -> x));
    }

    public static double calcDoubleAvg(List<Double> list) {
        return list.stream()
                .collect(Collectors.averagingDouble(x -> x));
    }
}
