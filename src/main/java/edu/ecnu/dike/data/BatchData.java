/*
 * BatchData - Template to store batch data while loading data into database, for the purpose of transaction retry.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.data;

import java.util.ArrayList;

public class BatchData<T> {

    private ArrayList<T> batch = new ArrayList<>();

    public void addBatch(T data) {
        batch.add(data);
    }

    public ArrayList<T> getBatch() {
        return batch;
    }

    public void clearBatch() {
        batch.clear();
    }
}
