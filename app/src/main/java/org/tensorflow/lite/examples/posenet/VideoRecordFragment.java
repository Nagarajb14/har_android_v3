package org.tensorflow.lite.examples.posenet;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;



import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.app.PendingIntent.getActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoRecordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    View rootview;
    Context thiscontext;
    Uri videoPathUri;
    ProgressBar videoProgressBar;
    String recordActivity="tmpActivity";
    public VideoRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoRecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoRecordFragment newInstance(String param1, String param2) {
        VideoRecordFragment fragment = new VideoRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void uploadData(Uri videoUri, StorageReference videoRef) {
        if (videoUri != null) {
            //UploadTask uploadTask = videoRef.putFile(videoUri);
            videoProgressBar.setVisibility(View.VISIBLE);

            videoRef.putFile(videoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.d("Upload", "upload complete");
                    if (task.isSuccessful())
                        Toast.makeText(getContext(), "Upload Complete", Toast.LENGTH_SHORT).show();

                    videoProgressBar.setVisibility(View.INVISIBLE);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (double)100.0 * ((float)(taskSnapshot.getBytesTransferred() ))/ ((float)taskSnapshot.getTotalByteCount()) ;

                    int currentprogress = (int) progress;
                    Toast.makeText(getContext(), "Upload%: "+String.valueOf(currentprogress), Toast.LENGTH_SHORT).show();

                    videoProgressBar.setProgress((int)currentprogress);
                }
            });
        } else {
            Toast.makeText(getContext(), "Nothing to upload", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thiscontext = container.getContext();
        rootview = inflater.inflate(R.layout.activity_video_record, container, false);

        Button start_button = (Button) rootview.findViewById(R.id.video_button_start);
        Button stop_button = (Button) rootview.findViewById(R.id.video_button_stop);
        Button upload_button = (Button) rootview.findViewById(R.id.video_upload_button);
        Button play_button = (Button) rootview.findViewById(R.id.video_button_play);
        videoProgressBar = (ProgressBar)rootview.findViewById(R.id.progressBar);
        videoProgressBar.setVisibility(View.INVISIBLE);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCameraPresent()) {


                    if (thiscontext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                    } else {

                        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);

                    }

                }


            }
        });

        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageRef = storage.getReference("har");
                Log.d("Upload", "upload starting");
                StorageReference videoFileRef = storageRef.child("Video"+ "_" + recordActivity+ System.currentTimeMillis());
                uploadData(videoPathUri, videoFileRef);
// Create a reference to 'images/mountains.jpg'

            }// Create a reference to "mountains.jpg"

        });

        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VideoView mVideoView = (VideoView)rootview.findViewById(R.id.videoView);
                mVideoView.setVideoURI(videoPathUri);
                MediaController mediaController = new MediaController(thiscontext);
                mediaController.setAnchorView(mVideoView);
                mVideoView.setMediaController(mediaController);
                mVideoView.requestFocus();
                mVideoView.start();

            }
        });


        Spinner spinner1 = (Spinner)rootview.findViewById(R.id.spinner1);
        final String[] groups = new String[] {"ON_PHONECALL", "EATING_DRINKING",  "ON_MOBILE", "FALLING", "WORKING_ON_DESKTOP", "SITTING"};
        ArrayAdapter<CharSequence> featuresAdapter = new ArrayAdapter<CharSequence>(thiscontext, android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
        featuresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(featuresAdapter);
        for (String s : groups) featuresAdapter.add(s);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // Here go your instructions when the user chose something
                Toast.makeText(thiscontext, groups[position], 0).show();
                recordActivity = groups[position];
            }
            public void onNothingSelected(AdapterView<?> arg0) {

                recordActivity ="None";
            }
        });





        return rootview;
    }

    private boolean isCameraPresent() {
        PackageManager pm = thiscontext.getPackageManager();
        if (pm.hasSystemFeature(pm.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }

    }

    public void onActivityResult(int requestCode, int resultCode,  Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        Log.d("Video Capture", "On Activity result");

        if (resultCode == RESULT_CANCELED) {

            Log.d("Video Capture", "Video Captured cancelled");

            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                Uri uri;
                if (data == null || data.getData() == null) {
                    Bitmap bitmap= (Bitmap)data.getExtras().get("data");
                    // TODO:Get uri from bitmap for image
                    //uri = getImageUri(context, bitmap);
                } else {
                    //Get uri for video
                    videoPathUri = data.getData();
                    Log.d("Video Capture", "Video Captured file stored  "+videoPathUri );
                }


            }
        }
    }



}