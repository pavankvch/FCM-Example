package com.example.sundar.cloudmessaging;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sundar on 27/7/16.
 */
public class RegistrationActivity extends AppCompatActivity {

    Button registerButton;
    EditText editName;
    EditText editPassword;
    EditText editEmail;
    ProgressDialog dialog;
    int statusCode;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        editName=(EditText)findViewById(R.id.editName);
        editPassword=(EditText)findViewById(R.id.editPassword);
        editEmail=(EditText)findViewById(R.id.editEmail);
        registerButton=(Button)findViewById(R.id.btnRegister);

        String fcmToken=FirebaseInstanceId.getInstance().getToken();

        if(fcmToken!=null)
        Log.d("Token:", fcmToken);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Log.d("REGISTERACTIVITY:", "Button CLICKED");
                sendJsonDatatoServer();

            }
        });
    }

    private void sendJsonDatatoServer() {
        String uname = editName.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        String android_id = Settings.Secure.getString(getBaseContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();

        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        if(!uname.isEmpty() && !password.isEmpty() && !email.isEmpty()) {

            JSONObject jsonObject=new JSONObject();


            try{
//                jsonObject.put("",uname);
//                jsonObject.put("", password);
                jsonObject.put("regId", fcmToken);
                jsonObject.put("appUserName", email);
                jsonObject.put("mobileDeviceId", android_id);
                jsonObject.put("appName", "GCM");
//                jsonObject.put("", uuid);
                jsonObject.put("appGroupName", "general");

            }catch (JSONException ex){
                ex.printStackTrace();
            }

            if (jsonObject.length() > 0) {
                new RegisterAsync().execute(String.valueOf(jsonObject));
            }

        }else{
            Toast.makeText(getApplicationContext(), "Please fill all Fields", Toast.LENGTH_LONG).show();
        }
    }

    private class RegisterAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegistrationActivity.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage("Loading. Please wait...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String jsonResponse = "";
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {

                URL url = new URL("http://192.168.2.15:5050/PushPlatFormAPI/deviceReg/createDeviceReg");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");//set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);// json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();//input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                jsonResponse = buffer.toString();//response data

                try {
                    JSONObject jObj = new JSONObject(jsonResponse.toString());
                    String statusMessage=jObj.getString("statusMessage");
                    statusCode=jObj.getInt("statusCode");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("JSONRESPONSE:",jsonResponse);
                //send to post execute
                return jsonResponse;

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ERROR:", "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.cancel();

            Log.d("Response is:", s);

            if(statusCode==200) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
