package org.tensorflow.lite.examples.posenet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.OnProgressListener;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.examples.posenet.lib.HumanActivity;
import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.LstmtfliteModel;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Position;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

class OneKeyPoint{
    float x = 0;
    float y = 0;
    float x_debug=0;
    float y_debug=0;
    float FrameNum_debug=0;
    //constructor
    float confidence=0;
    boolean valid=false;
}
class PersonKeyPoints {
    OneKeyPoint[] keypoints = new OneKeyPoint[18];
    int numInvalidKps=0;
    boolean valid=false;
}



public class EstimateActivity {

    private int FrameCount=0;
    private PersonKeyPoints[] lastTwoKeypoints= new PersonKeyPoints[2];

    private ArrayList<PersonKeyPoints> arrayListForModel = new ArrayList<PersonKeyPoints>();
    public float[][][][][] modelBuffer= new float[1][10][1][18][2];
    private int indexlastTwoKeypoints=0;
    private HumanActivity currentActivity = HumanActivity.UNKNOWN;
    private int InvalidCount=0;
    private static Context activityContext;
    float InputData[][];
    //protected Interpreter lstm_tflite;
    private static LstmtfliteModel lstmModel;

    ArrayList<byte[]> savedImagesForDataCollectionVideo = new ArrayList<byte[]>();
    ArrayList<byte[]> savedImagesForActivityVideo = new ArrayList<byte[]>();
    public int numFramesDataCollection =4000;
    public HumanActivity dataCollectionActivity = HumanActivity.UNKNOWN;
    /*Should be configurable*/

    public enum  Mode {
        DataCollection,
        ActivityRecognition
    }
    public Mode AppMode;
    public int checkKeypointData(int Keypoint) {
        return 1;
    }

    public  EstimateActivity (Context context){
        //activityContext = context;
        //initializeLstm(0);
        lastTwoKeypoints = new PersonKeyPoints[2];
        for (int i=0;i<2;i++){
            lastTwoKeypoints[i] = new PersonKeyPoints();
            lastTwoKeypoints[i].keypoints = new OneKeyPoint[18];
            lastTwoKeypoints[i].numInvalidKps=0;
            lastTwoKeypoints[i].valid=false;

            for (int j=0;j<18;j++){
                lastTwoKeypoints[i].keypoints[j] = new OneKeyPoint();
                lastTwoKeypoints[i].keypoints[j].valid = false;
            }
        }
        AppMode = Mode.DataCollection;
        lstmModel = new LstmtfliteModel();
    }
    public  void InitEstimateActivity (Context context){
        activityContext = context;
        initializeLstm(0);
    }
    public void printHelloWorld(int a) {
        Log.d("Estimate Keypoint", "Hello World" + FrameCount);
        FrameCount = FrameCount + 1;
        Log.d("Estimate Keypoint", String.valueOf(FrameCount));

    }


    public void passArray(int[] tmpArray) {


        for (int a : tmpArray) {
            //Log.d("Estimate Keypoint", "Array item" + String.valueOf(a));

        }
    }

    public void newFunct(@NotNull Person person) {
        Log.d("Estimate Keypoint", "person" + String.valueOf(person));
        List<KeyPoint> kplist = person.getKeyPoints();
        int kpsize = kplist.size();
        Log.d("Estimate Keypoint", "KP Size" + String.valueOf(kpsize));

        for (int i =0;i<kplist.size();i++) {
            KeyPoint kp_one = kplist.get(i);
            Position position = kp_one.getPosition();
            int x = position.getX();
            int y = position.getY();
            float confidence = kp_one.getScore();
            //Log.d("Estimate Keypoint", " KP index: "+String.valueOf(i)+ " Score: " + String.valueOf(confidence)+ " X Y: " + String.valueOf(x) + " " + String.valueOf(y));
        }

    }
    public void uploadImagetoFirebase(HumanActivity currentActivity, Bitmap bitmap){

        if (currentActivity == HumanActivity.BOXING){
            FirebaseStorage storage = FirebaseStorage.getInstance();

            StorageReference storageRef = storage.getReference("har");
            StorageReference imagesRef = storageRef.child("image"+ "_" + currentActivity.toString() + "_"+ String.valueOf(FrameCount));

// Create a reference to 'images/mountains.jpg'


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });




        }// Create a reference to "mountains.jpg"



    }

    public void uploadVideotoFirebase(HumanActivity currentActivity, Bitmap bitmap){




            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            savedImagesForDataCollectionVideo.add(data);
            if(savedImagesForDataCollectionVideo.size() == numFramesDataCollection){
                //Construct Video and upload


                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageRef = storage.getReference("har");
                StorageReference imagesRef = storageRef.child("video"+ "_" + currentActivity.toString() + "_"+ String.valueOf(FrameCount));
                UploadTask uploadTask = imagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });


// Create a reference to 'images/mountains.jpg'
            }



    }



    public HumanActivity processFrame(@NotNull Person person){


        if( (FrameCount%6 ==4)  || (FrameCount%6==5)){
              updateFrameKeyPoints(person);
              return currentActivity;
         }
         if(FrameCount%6 ==0){
             PersonKeyPoints onePersonKps = extractOneTimeStepKeypoints(person);
             Log.d("processFrame", "Timestamp: FrameNum "+ String.valueOf(FrameCount));

             if(onePersonKps.valid) {
                 arrayListForModel.add(onePersonKps);
                 InvalidCount=0;
             }else{
                 InvalidCount+=1;
                 arrayListForModel.clear();
                 if (InvalidCount >0){
                     currentActivity = HumanActivity.UNKNOWN;
                 }
                 return currentActivity;
             }
             Log.d("processFrame", "Size of arrayListForModel"+ String.valueOf(arrayListForModel.size()));

             if(arrayListForModel.size()==10){
                 float[][][][][] lstmBuffer;
                 char[] data = new char[6000];
                 char[] data2 = new char[6000];
                 String tmpString = new String(data);
                 String tmpString2 = new String(data2);
                 tmpString ="";
                 tmpString2 ="";
                 lstmBuffer=lstmModel.getLstmInputBuff();
                 for(int i=0;i<10;i++) {
                     PersonKeyPoints tmp = arrayListForModel.get(i);

                     for (int j = 0; j < 18; j++) {
                         lstmBuffer[0][i][0][j][0]= tmp.keypoints[j].y;
                         lstmBuffer[0][i][0][j][1]= tmp.keypoints[j].x;
                         //Log.d("processFrame", "ModelBuff x "+ String.valueOf(i)+ " "+ String.valueOf(j)+" "+ String.valueOf(lstmBuffer[0][i][0][j][0]));
                         //Log.d("processFrame", "ModelBuff y "+ String.valueOf(i)+ " "+ String.valueOf(j)+" "+  String.valueOf(lstmBuffer[0][i][0][j][1]));
                         if(j!=17){
                             tmpString = tmpString + String.valueOf(tmp.keypoints[j].y_debug)+",";
                             tmpString = tmpString + String.valueOf(tmp.keypoints[j].x_debug)+",";
                         }
                         tmpString2 = tmpString2 + String.valueOf(tmp.keypoints[j].y)+",";
                         tmpString2 = tmpString2 + String.valueOf(tmp.keypoints[j].x)+",";
                     }
                     //tmpString2 = tmpString2 + "/n";

                     Log.d("processFrame", "Stored FrameNum "+ String.valueOf(tmp.keypoints[i].FrameNum_debug));
                 }
                 //Log.d("processFrame Raw KP", tmpString);
                 //Log.d("processFrame Model KP", tmpString2);
                 //Log.d("processFrame", "modelBuffer "+ String.valueOf(lstmBuffer));
                 currentActivity =lstmModel.inferHumanActivity();
                 Log.d("processFrame", "Inferred Activity "+ String.valueOf(currentActivity));
                 arrayListForModel.remove(0);
                 if (currentActivity == HumanActivity.BOXING){
                     FirebaseStorage storage = FirebaseStorage.getInstance();

                     StorageReference storageRef = storage.getReference();
                     StorageReference mountainsRef = storageRef.child("mountains.jpg");

// Create a reference to 'images/mountains.jpg'
                     StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

// While the file names are the same, the references point to different files
                     mountainsRef.getName().equals(mountainImagesRef.getName());    // true
                     mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

                 }// Create a reference to "mountains.jpg"

                 return currentActivity;
             } else{

                 //arrayListForModel.remove(0);
                 return currentActivity;

             }
         }
         return currentActivity;
    }

    public void updateFrameKeyPoints(@NotNull Person person) {

        Log.d("updateFrameKeyPoints", "FrameCount%6" + String.valueOf(FrameCount));
        List<KeyPoint> kplist = person.getKeyPoints();
        int kpsize = kplist.size();
        Log.d("updateFrameKeyPoints", "KP Size" + String.valueOf(kpsize));

        float x = 0, y = 0;

        for (int i = 0; i < kplist.size(); i++) {
            KeyPoint kp_one = kplist.get(i);
            Position position = kp_one.getPosition();
            x = (float) position.getX();
            y = (float) position.getY();
            float confidence = kp_one.getScore();
            if (FrameCount % 6 == 4) {
                lastTwoKeypoints[0].keypoints[i].x = x;
                lastTwoKeypoints[0].keypoints[i].y = y;
                lastTwoKeypoints[0].keypoints[i].confidence = confidence;
                if (confidence < 0.4) {
                    lastTwoKeypoints[0].keypoints[i].valid = false;
                } else {
                    lastTwoKeypoints[0].keypoints[i].valid = true;

                }
            }
            if (FrameCount % 6 == 5) {
                lastTwoKeypoints[1].keypoints[i].x = x;
                lastTwoKeypoints[1].keypoints[i].y = y;
                lastTwoKeypoints[1].keypoints[i].confidence = confidence;
                if (confidence < 0.4) {
                    lastTwoKeypoints[1].keypoints[i].valid = false;
                } else {
                    lastTwoKeypoints[1].keypoints[i].valid = true;

                }
            }
        }

    }


//Extract 18X2 Every 6th Frame... That is one timestep. We have 10 timesteps
    public PersonKeyPoints extractOneTimeStepKeypoints(@NotNull Person person) {

        PersonKeyPoints thisPerson = new PersonKeyPoints();

        Log.d("EstimateActivity extr", "person" + String.valueOf(person));
        List<KeyPoint> kplist = person.getKeyPoints();
        int kpsize = kplist.size();
        Log.d("EstimateActivity extr ", "KP Size" + String.valueOf(kpsize));
        int numInvalidKps=0;
        int numInvalidKpsforAverage=0;
        float [][] kpArray = new float[18][2];
        float x=0,y=0,x_debug=0,y_debug=0;
        float sumX=0;
        float sumY=0;
        float centerX=0;
        float centerY=0;
        float maxX=0,maxY=0,minX=257.0f,minY=257.0f,allMin=257.0f, allMax=0;
        for (int i =0;i<kplist.size();i++) {
            KeyPoint kp_one = kplist.get(i);
            Position position = kp_one.getPosition();
            x = (float)position.getX();
            y = (float)position.getY();
            x_debug =x;
            y_debug =y;

            float confidence = kp_one.getScore();
            thisPerson.keypoints[i] = new OneKeyPoint();
            // Below is the case of FrameCount ==0
            if (confidence <0.4) {
                numInvalidKps += 1;
                if (lastTwoKeypoints[1].keypoints[i].valid) {
                    x = lastTwoKeypoints[1].keypoints[i].x;
                    y = lastTwoKeypoints[1].keypoints[i].y;
                } else if ( lastTwoKeypoints[0].keypoints[i].valid ) {

                    x = lastTwoKeypoints[0].keypoints[i].x;
                    y = lastTwoKeypoints[0].keypoints[i].y;
                } else {
                    numInvalidKpsforAverage += 1;
                }

                thisPerson.keypoints[i].valid=false; //Not Valid, It may have valid borrowed values as above

            } else{
                thisPerson.keypoints[i].valid=true;
            }
            thisPerson.keypoints[i].x=x;
            thisPerson.keypoints[i].y=y;
            thisPerson.keypoints[i].confidence=confidence;
            thisPerson.keypoints[i].x_debug = x_debug;
            thisPerson.keypoints[i].y_debug = y_debug;
            thisPerson.keypoints[i].FrameNum_debug = FrameCount;

            sumX+=x;
            sumY+=y;
            if(x>maxX){
                maxX=x;
            }
            if (x <minX){
                minX = x;
            }
            if(y>maxY){
                maxY=y;
            }
            if (y <minY){
                minY = y;
            }
            allMin = minX;

            if (minY<allMin){
                allMin = minY;
            }
            allMax = maxX;

            if (minY<allMin){
                allMin = minY;
            }


            //Log.d("EstimateActivity extr", " KP index: "+String.valueOf(i)+ " Score: " + String.valueOf(confidence)+ " X Y: " + String.valueOf(x) + " " + String.valueOf(y));
        }

        centerX = sumX/(17);
        centerY = sumY/(17);
        //Log.d("EstimateActivity extr", " Centroid: " +  " X Y: " + String.valueOf(centerX) + " " + String.valueOf(centerY));


        //Log.d("EstimateActivity extr", " Min : " +  " X Y: " + String.valueOf(minX) + " " + String.valueOf(minY));

        //Log.d("EstimateActivity extr", " Max: " +  " X Y: " + String.valueOf(maxX) + " " + String.valueOf(maxY));
        minX = minX - centerX;
        maxX = maxX - centerX;
        minY = minY -centerY;
        maxY = maxY - centerY;
        Log.d("EstimateActivity extr", " Min - centroid: " +  " X Y: " + String.valueOf(minX) + " " + String.valueOf(minY));

        Log.d("EstimateActivity extr", " Max - controid" +  " X Y: " + String.valueOf(maxX) + " " + String.valueOf(maxY));
        thisPerson.keypoints[17]= new OneKeyPoint();
        thisPerson.keypoints[17].x = centerX;
        thisPerson.keypoints[17].y = centerY;


        if(numInvalidKpsforAverage >10){
            thisPerson.valid = false;

        } else{
            thisPerson.valid = true;

        }
        for (int i=0;i <18;i++){
            x=thisPerson.keypoints[i].x;
            y=thisPerson.keypoints[i].y;
            //Log.d("EstimateActivity extr", " Before Norm "+String.valueOf(i)+ " X Y: " + String.valueOf(x) + " " + String.valueOf(y));

            x = x - centerX;
            y = y - centerY;


            x = (x - minX)/(maxX-minX+1);
            thisPerson.keypoints[i].x=x;

            y = (y - minY)/(maxY-minY+1);
            thisPerson.keypoints[i].y=y;
            //Log.d("EstimateActivity extr", " After Norm "+String.valueOf(i)+ " X Y: " + String.valueOf(x) + " " + String.valueOf(y));

        }

        return thisPerson;
    }

    private void initializeLstm(int i){
        lstmModel.create(activityContext);
        Log.d("EstimateActivity Inilst", "initializing lstm");
    }

    public int getAndIncrementFrameNum() {
        FrameCount = (FrameCount+1)%12024;
        return FrameCount;
    }

    public HumanActivity  getCurrentActivity() {
        return currentActivity;
    }
}