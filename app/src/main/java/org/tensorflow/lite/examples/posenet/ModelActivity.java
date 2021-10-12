package org.tensorflow.lite.examples.posenet;

import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
class KeyPoint {
    public float x;
    public float y;
    boolean valid;
}
public class ModelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //String data = getIntent().getStringExtra("JSON");
        //postString(data);
        //Log.d("DATA_HERE",data);
        //largeLog("largelog",data);
        JSONObject jobj = null;
//        postData(jobj);

//        try {
//            Log.d("TRYING_HERE","HERE");
//            jobj = new JSONObject(data);
//            Log.d("PRE-SUCESS","TRUE");
//            //postData(jobj);
//            Log.d("SUCESS","TRUE");
//            Toast.makeText(this,"Done",Toast.LENGTH_SHORT).show();
//        } catch (JSONException e) {
//           e.printStackTrace();
//            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
//            Log.d("ERROR",e.getMessage());
//        }

        //postData(jobj);


    }
    public static void largeLog(String tag, String content) {
        if (content.length() > 1000) {
            Log.d(tag, content.substring(0, 1000));
            largeLog(tag, content.substring(1000));
        } else {
            Log.d(tag, content);
        }
    }

    public void postData(JSONObject object) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

//        JSONObject object = new JSONObject();
//        try {
//            //input your API parameters
//            object.put("parameter","value");
//            object.put("parameter","value");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        String url = "http://5cc4d01eca9d.ngrok.io/submit";
        // Enter the correct url for your api service site
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY","DONE!");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY","Error!");
                Log.d("VOLLEY-ERROR",error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void postString(final String mRequestBody){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//            String URL = "http://...";
//            JSONObject jsonBody = new JSONObject();
//            jsonBody.put("firstkey", "firstvalue");
//            jsonBody.put("secondkey", "secondobject");
//            final String mRequestBody = jsonBody.toString();

        String url = "http://5cc4d01eca9d.ngrok.io/submit";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOG_VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {

                    responseString = String.valueOf(response.statusCode);

                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(stringRequest);
    }
    
}