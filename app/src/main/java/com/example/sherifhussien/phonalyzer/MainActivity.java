package com.example.sherifhussien.phonalyzer;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PHONALIZER_INITIATE_URL = "http://phonalyzer.herokuapp.com/welcome";

    private static final String PHONALIZER_CHAT_URL = "http://phonalyzer.herokuapp.com/chat";

    private static String uuid="";

    private static MessageAdapter adapter;

    private static ArrayList<Message> messages;

    private static ListView messagesListView;

    private static final String number="2001003835879";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);

        messages = new ArrayList<>();

        messagesListView = (ListView) findViewById(R.id.message_list_view);
        adapter = new MessageAdapter(this, messages);
        messagesListView.setAdapter(adapter);

        // Class that answers queries about the state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            //executing a thread
            new PhonalyzerTask().execute(PHONALIZER_INITIATE_URL);


            ImageButton fab = (ImageButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "FAB clicked",Toast.LENGTH_LONG).show();

                    messages.add(new Message(number,true));
                    adapter.notifyDataSetChanged();
                    messagesListView.setSelection(messagesListView.getCount() - 1);

                    new PhonalyzerTask().execute(PHONALIZER_CHAT_URL,number);
                }
            });

            Button send=(Button) findViewById(R.id.send_button);
            send.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Send clicked",Toast.LENGTH_LONG).show();
                    EditText editText=(EditText) MainActivity.this.findViewById(R.id.edit_text);
                    String number=editText.getText().toString();

                    if(!TextUtils.isEmpty(number)){
                        messages.add(new Message(number,true));
                        adapter.notifyDataSetChanged();
                        messagesListView.setSelection(messagesListView.getCount() - 1);
                        editText.setText("");

                        new PhonalyzerTask().execute(PHONALIZER_CHAT_URL,number);
                    }
                }
            });


        } else {

            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            TextView internet=(TextView)findViewById(R.id.internet_text_view);
            internet.setText("No internet connection.");
        }


    }


    /**
     * Perform background operations and publish results on the UI threads
     * Params refers to the parameters that would be parsed to your Task
     * Progress refers to the progress indicator/counter type
     */


    private class PhonalyzerTask extends AsyncTask<String,Void,Message> { // <Params,Progress,Result>

        @Override
        protected Message doInBackground(String... params) {

            //Create URL object
            URL url = createUrl(params[0]);

            String number=null;
            if(!TextUtils.isEmpty(uuid)){
                number=params[1];
//                Log.v(LOG_TAG, "do in back number"+number);
            }


            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url,number);
            } catch (IOException e) {
                Log.e(LOG_TAG,"error in makeHttpRequest",e);
            }

            // Extract relevant fields from the JSON response and create a Message object
            Message message = extractFromJson(jsonResponse);

//            Log.v(LOG_TAG, "do in back "+message.toString());


            // Return the Message object as the result of the PhonalyzerTask
            return message;
        }


        @Override
        protected void onPostExecute(Message message) {  //runs on main thread
            if(message==null){
                return;
            }
            if(TextUtils.isEmpty(uuid)){
                uuid=message.getUuid();
            }
            //Log.v(LOG_TAG,"Session id is "+MainActivity.this.uuid);
            //Log.v(LOG_TAG, "on post "+message.toString());

            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            updateUi(message);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url,String number) throws IOException {
            String jsonResponse = "";
            if(url == null){
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            OutputStream os=null;
            BufferedWriter writer=null;
            JSONObject output = new JSONObject();


            try {
                urlConnection = (HttpURLConnection) url.openConnection(); //HTTP client
                urlConnection.setDoInput(true);


                if(TextUtils.isEmpty(uuid)){
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect(); //establishing the http connection with the server
                    if(urlConnection.getResponseCode() == 200){
                        inputStream = urlConnection.getInputStream();
                        jsonResponse = readFromStream(inputStream);
                    }
                    else{
                        Log.e(LOG_TAG,"error response code: "+urlConnection.getResponseCode());
                    }

                }else{

                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.addRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty ("Authorization", uuid);

                    os = urlConnection.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    try {
                        output.put("message",number);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG,"can not add the key value pait to the jsonobject",e);
                    }

                    writer.write(output.toString());
                    writer.flush();
                    urlConnection.connect();
                    if(urlConnection.getResponseCode() == 200){
                        inputStream = urlConnection.getInputStream();
                        jsonResponse = readFromStream(inputStream);
                    }else{
                        Log.e(LOG_TAG,"error response code: "+urlConnection.getResponseCode());
                    }

                }

            } catch (IOException e) {
                Log.e(LOG_TAG,"problem retrieving JSON results",e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if(writer!=null){
                    writer.close();
                }
                if(os!=null){
                    os.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the InputStream into a String which contains the whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();    //mutable and don't waste memory
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8")); //from raw binary data to human reading characters,that allows to read one character et a time.
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }


        /**
         * Return an Message object
         */
        private Message extractFromJson(String messageJSON) {

//            Log.v(LOG_TAG, "Extract: "+messageJSON);

            //true if str is null or zero length
            if(TextUtils.isEmpty(messageJSON)){
                return null;
            }

            try {
                JSONObject jsonResponse = new JSONObject(messageJSON);
                String message = jsonResponse.getString("message");
                //Log.v(LOG_TAG, "Extract: "+message);


                if(TextUtils.isEmpty(uuid)){
                    uuid = jsonResponse.getString("uuid");

                    return new Message(message,uuid,false);
                }

                return  new Message(message,false);


            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the message JSON results", e);
            }
            return null;
        }

        private void updateUi(Message message) {

            messages.add(message);
            adapter.notifyDataSetChanged();
            messagesListView.setSelection(messagesListView.getCount() - 1);

        }
    }
}

