/*
 * Property - Abstract class to read properties from config file.
 *
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.config;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

public abstract class Property {

    protected String path;
    protected Properties props;

    public Property(String path) throws IOException {
        this.path = path;
        props = new Properties();
        props.load(new FileInputStream(path));
    } // end Property

    public void addProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public abstract void loadProperty() throws RuntimeException;
}