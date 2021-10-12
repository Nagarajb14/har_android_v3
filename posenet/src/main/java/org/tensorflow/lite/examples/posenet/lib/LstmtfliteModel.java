package org.tensorflow.lite.examples.posenet.lib;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class LstmtfliteModel {

    private static Interpreter lstm_tflite;


    private MappedByteBuffer tfLiteModel;
    private Interpreter.Options tfLiteOptions;
    private static final int NUM_THREADS = 4;
    public float[][][][][] lstmInputBuff= new float[1][10][1][18][2];

    public static void create(final Context context) {


        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(NUM_THREADS);
            options.setUseXNNPACK(true);

            lstm_tflite = new Interpreter(loadModelFile(context), options);
            Log.d("lstm Model", "Trying to Initialize LSTM");

        } catch (final IOException e) {
            e.printStackTrace();
            Log.d("lstm tflite", "Exception initializing lstm tflite!");
            Toast toast =
                    Toast.makeText(
                            context.getApplicationContext(), "LSTM could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
        float [][][][][] input1 = new float[1][10][1][18][2];
        float[][] output1 = new float[1][5];
        Log.d("running lstm Model", "Running LSTM");

       lstm_tflite.run(input1,output1);
       float tmpFloat;
        for(int i=0;i<5;i++) {
            tmpFloat = output1[0][i];

            Log.d("running lstm Model result", String.valueOf(tmpFloat));
        }
    }

    public float[][][][][] getLstmInputBuff() {
        return lstmInputBuff;
    }

    public HumanActivity inferHumanActivity (){

        float [][][][][] input1 = new float[1][10][1][18][2];
        float[][] output1 = new float[1][5];
        HumanActivity tmpActivity;
        Log.d("running lstm Model", "Running LSTM");
/*
        for(int i=0;i<10;i++) {

            for (int j = 0; j < 18; j++) {

                //Log.d("processFrame", "lstmInputBuff x "+ String.valueOf(i)+ " "+ String.valueOf(j)+" "+ String.valueOf(lstmInputBuff[0][i][0][j][0]));
                //Log.d("processFrame", "lstmInputBuff y "+ String.valueOf(i)+ " "+ String.valueOf(j)+" "+  String.valueOf(lstmInputBuff[0][i][0][j][1]));

            }
        }
*/
        lstm_tflite.run(lstmInputBuff,output1);
        float max=0;
        float prob=0;
        float maxIdx=0;
        for(int i=0;i<5;i++) {
            prob = output1[0][i];
            if(prob>max){
                max = prob;
                maxIdx=i;
            }
            Log.d("inferHumanActivity", "Score for idx "+ String.valueOf(i)+" Score "+ String.valueOf(prob));
        }
       if(maxIdx==0){
           tmpActivity = HumanActivity.BOXING;
       }else if(maxIdx==1){
           tmpActivity = HumanActivity.HANDCLAPPING;
       }else if (maxIdx ==2){
           tmpActivity = HumanActivity.RUNNING;
       }else if (maxIdx ==3){
           tmpActivity = HumanActivity.WALKING;
       }else{
           tmpActivity = HumanActivity.HANDWAVING;
       }
        return tmpActivity;
    }

    //{'boxing' : 0, 'handclapping' : 1, 'running' : 2, 'walking' : 3, 'handwaving' : 4}
    /*
[  1 257 257   3]
<class 'numpy.float32'>
            [ 1  9  9 17]
<class 'numpy.float32'>

[ 1 10  1 18  2]
<class 'numpy.float32'>
[1 5]
<class 'numpy.float32'>
    */
    private static void finish() {
    }


    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("lstm_model_v4.tflite");
        Log.d("load model", fileDescriptor.toString());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}