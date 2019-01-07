package com.example.jakesetton.myfirstapp;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import android.content.Context;
import android.util.Log;

public class TensorFlowClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }
    private static final float THRESHOLD = 0.1f;

    private static TensorFlowInferenceInterface inferenceInterface;

    //declare all the model-related variables here

    private static final String MODEL_FILE = "file:///android_asset/frozen_lrmodel.pb";
    private static final String INPUT_NODE = "input";

    private static final String OUTPUT_NODE = "output";
    private static final String[] OUTPUT_NODES = {"output"};

    private static final long[] INPUT_SIZE = {1, 8}; // I think?
    private static final int OUTPUT_SIZE = 11;

    private static float highest = -1;
    static int scoreval;

    public TensorFlowClassifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    public static float[] predictWelfare(float[] data) {
        String input = "";
        for (int a= 0; a<data.length; a++) {
            input += String.valueOf(data[a]) + ",";
        }
        Log.v("Classifying: ", input);
        highest = -1;
        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);
        String s = "";
        for (int i=0; i<OUTPUT_SIZE; i++) {
            s += String.valueOf(result[i]) + " || ";
            if (result[i] > highest) {
                highest = result[i];
                scoreval = i;
            }
        }
        Log.v("result:", s + " " + String.valueOf(scoreval));
        return result;
    }

}
