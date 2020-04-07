package com.loodos.tensorflowexample.models;

/**
 * Created by orhunkupeli on 04/04/2020.
 */

public interface Grouper {
    String name();

    Grouping recognize(final float[] pixels);
}
