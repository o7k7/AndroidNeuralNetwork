package com.loodos.tensorflowexample;

import android.content.res.AssetManager;

import com.loodos.tensorflowexample.models.Grouper;
import com.loodos.tensorflowexample.models.Grouping;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by orhunkupeli on 04/04/2020.
 */

public class TensorFlowGrouper implements Grouper {

    // Possible smallest grouping percentage
    private static final float THRESHOLD = 0.1f;

    private TensorFlowInferenceInterface mTensorFlowInferenceInterface;

    private String mName;
    private String mInputName;
    private String mOutputName;
    private int mInputSize;
    private boolean mFeedKeepProb;

    private List<String> mLabels;
    private float[] mOutput;
    private String[] mOutputNames;

    /**
     * Returns the labels 0-9
     *
     * @return
     */
    private static List<String> getLabels() {
        final List<String> labels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    /**
     * @param assetManager
     * @param name
     * @param modelPath
     * @param inputSize
     * @param inputName
     * @param outputName
     * @param feedKeepProb
     * @return
     * @throws IOException
     */
    public static TensorFlowGrouper create(AssetManager assetManager, String name,
                                           String modelPath, int inputSize,
                                           String inputName, String outputName,
                                           boolean feedKeepProb) {
        final TensorFlowGrouper grouper = new TensorFlowGrouper();
        grouper.mName = name;
        grouper.mInputName = inputName;
        grouper.mOutputName = outputName;
        grouper.mLabels = getLabels();

        grouper.mTensorFlowInferenceInterface = new TensorFlowInferenceInterface(assetManager, modelPath);
        int numClasses = 10;
        grouper.mInputSize = inputSize;
        grouper.mOutputNames = new String[]{outputName};
        grouper.mOutputName = outputName;
        grouper.mOutput = new float[numClasses];

        grouper.mFeedKeepProb = feedKeepProb;

        return grouper;
    }

    @Override
    public String name() {
        return mName;
    }

    /**
     *
     * @param pixels is the float array of the given bitmap
     * @return
     */
    @Override
    public Grouping recognize(final float[] pixels) {
        mTensorFlowInferenceInterface.feed(mInputName, pixels, 1, mInputSize, mInputSize, 1);

        if (mFeedKeepProb) {
            // Probabilities
            mTensorFlowInferenceInterface.feed("keep_prob", new float[]{1});
        }

        mTensorFlowInferenceInterface.run(mOutputNames);
        mTensorFlowInferenceInterface.fetch(mOutputName, mOutput);

        // Obtain the best prediction. The highest probability
        final Grouping grouping = new Grouping();
        for (int i = 0; i < mOutput.length; ++i) {
            if (mOutput[i] > THRESHOLD && mOutput[i] > grouping.getConfiguration()) {
                grouping.setProperties(mOutput[i], mLabels.get(i));
            }
        }
        return grouping;
    }
}
