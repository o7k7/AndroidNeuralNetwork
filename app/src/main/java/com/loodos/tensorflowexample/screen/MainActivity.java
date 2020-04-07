package com.loodos.tensorflowexample.screen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.loodos.tensorflowexample.R;
import com.loodos.tensorflowexample.TensorFlowGrouper;
import com.loodos.tensorflowexample.models.Grouper;
import com.loodos.tensorflowexample.models.Grouping;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ParentChildCommunication {

    private static final int PIXEL_WIDTH = 28;

    private static List<Grouper> mGroupers = new ArrayList<>();

    private TextView mTxtResult;

    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtResult = findViewById(R.id.txtResult);
        prepareModel();
        commitScannerFragment();
    }

    private void commitScannerFragment() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new ScannerFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void prepareModel() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGroupers.add(
                            TensorFlowGrouper.create(getAssets(), "TensorFlow",
                                    "mnist_tf.pb", PIXEL_WIDTH,
                                    "input", "output", true));
                    mGroupers.add(
                            TensorFlowGrouper.create(getAssets(), "Keras",
                                    "mnist_keras.pb", PIXEL_WIDTH,
                                    "conv2d_1_input", "dense_2/Softmax", false));
                } catch (Exception ex) {
                    throw new RuntimeException("Error:: ", ex);
                }
            }
        });
        mThread.start();
    }

    private String calculateResults(float[] pixels) {
        StringBuilder text = new StringBuilder();
        for (Grouper grouper : mGroupers) {
            final Grouping res = grouper.recognize(pixels);
            if (res.getDescription() == null) {
                text.append(grouper.name()).append(": unpredicted\n");
            } else {
                text.append(grouper.name()).append(": ");
                text.append(res.getDescription()).append(" - ");
                text.append(res.getConfiguration()).append(" Prediction\n");
            }
        }
        return text.toString();
    }

    /**
     * 28 x 28 bitmap that adapts tensor data
     *
     * @param offscreenBitmap
     * @return
     */
    private void getPixelData(Bitmap offscreenBitmap) {
        if (offscreenBitmap == null) {
            return;
        }

        int width = offscreenBitmap.getWidth();
        int height = offscreenBitmap.getHeight();
        int[] pixels = new int[width * height];
        offscreenBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] retPixels = new float[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            // Mark black and white
            int pix = pixels[i];
            int b = pix & 0xff;
            retPixels[i] = (float) ((0xff - b) / 255.0);
        }
        mTxtResult.setText(calculateResults(retPixels));
    }

    @Override
    public void processImage(Bitmap offscreenBitmap) {
        getPixelData(offscreenBitmap);
    }
}
