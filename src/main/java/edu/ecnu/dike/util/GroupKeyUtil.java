/*
 * GroupKeyUtil - Group warehouses w_id according to numWarehouse and groupSize.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.util;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupKeyUtil {

    private HashMap<Integer, ArrayList<Integer>> groupWids;
    private volatile static GroupKeyUtil instance;
    private GroupKeyUtil(int warehouses, int groupsz) {
        int groups = warehouses / groupsz;
        groupWids = new HashMap<>(groups);
        for (int w = 1; w <= warehouses; w++) {
            // add warehouse id to corresponding key group
            groupWids.computeIfAbsent(w % groups, k -> new ArrayList<>()).add(w);
        }
    }

    public static HashMap<Integer, ArrayList<Integer>> getGroupKey(int warehouses, int groupsz) {
        if (instance == null) {
            synchronized (GroupKeyUtil.class) {
                if (instance == null) {
                    instance = new GroupKeyUtil(warehouses, groupsz);
                }
            }
        }
        return instance.groupWids;
    }
}
