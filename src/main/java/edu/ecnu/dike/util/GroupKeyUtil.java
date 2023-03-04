/*
 * GroupKeyUtil - Group warehouses w_id according to numWarehouse and groupSize.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupKeyUtil {

    private volatile static HashMap<Integer, ArrayList<Integer>> groupWids;

    public static HashMap<Integer, ArrayList<Integer>> getGroupKey(int warehouses, int groupsz) {
        if (groupWids == null) {
            synchronized (GroupKeyUtil.class) {
                if (groupWids == null) {
                    int groups = warehouses / groupsz;
                    groupWids = new HashMap<>(groups);
                    for (int w = 1; w <= warehouses; w++) {
                        // add warehouse id to corresponding key group
                        groupWids.computeIfAbsent(w % groups, k -> new ArrayList<>()).add(w);
                    }
                }
            }
        }
        return groupWids;
    }

}
