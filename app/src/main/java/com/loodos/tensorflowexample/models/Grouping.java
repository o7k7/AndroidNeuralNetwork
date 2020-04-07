package com.loodos.tensorflowexample.models;

/**
 * Created by orhunkupeli on 04/04/2020.
 */

public class Grouping {

    private float configuration;
    private String description;

    public Grouping() {
        this.configuration = -1.0f;
        this.description = null;
    }

    public void setProperties(float configuration, String description) {
        this.configuration = configuration;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public float getConfiguration() {
        return configuration;
    }
}
