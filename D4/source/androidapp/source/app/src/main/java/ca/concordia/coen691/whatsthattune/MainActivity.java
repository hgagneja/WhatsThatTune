package ca.concordia.coen691.whatsthattune;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import ca.concordia.coen691.Item;
import ca.concordia.coen691.SpotifyApiRs;


public class MainActivity extends AppCompatActivity {

    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording,buttonWav ;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // Great! User has recorded and saved the audio file
            } else if (resultCode == RESULT_CANCELED) {
                // Oops! User has canceled the recording
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = (Button) findViewById(R.id.button);
        buttonStop = (Button) findViewById(R.id.button2);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.button3);
        buttonStopPlayingRecording = (Button)findViewById(R.id.button4);

        buttonStart.setEnabled(true);
        buttonStop.setEnabled(true);
        buttonPlayLastRecordAudio.setEnabled(true);
        buttonStopPlayingRecording.setEnabled(true);

        random = new Random();


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    CreateRandomAudioFileName(5) + "AudioRecording.m4a";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Toast.makeText(MainActivity.this, "Recording started",
                            Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();

                Toast.makeText(MainActivity.this, "Recording Completed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {


                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }

                try {
                    AsyncTask<String,Void,String> t = new doPost().execute();

                    String response = t.get();

                    String[] respArray =  response.substring(2,response.length()-1).split(",");

                    if(respArray.length==4){

                        String album = respArray[0].substring(0,respArray[0].length()-1).replaceAll("-"," ");
                        String title = respArray[1].substring(1,respArray[1].length()-1).replaceAll("-"," ");
                        String artist = respArray[2].substring(1,respArray[2].length()-1).replaceAll("-"," ");
                        String confidence = respArray[3];


                        AsyncTask<String,Void,String> tSpotify = new doGet().execute(title,artist);

                        String respSpotify = tSpotify.get();

                        Gson g = new Gson();

                        SpotifyApiRs spotifyRs = g.fromJson(respSpotify,SpotifyApiRs.class);

                        List<Item> items = spotifyRs.getTracks().getItems();

                        TextView artistLabel = (TextView) findViewById(R.id.textView);
                        TextView artistVal = (TextView) findViewById(R.id.textView2);
                        TextView trackLabel = (TextView) findViewById(R.id.textView3);
                        TextView trackVal = (TextView) findViewById(R.id.textView4);
                        TextView albumLabel = (TextView) findViewById(R.id.textView5);
                        TextView albumVal = (TextView) findViewById(R.id.textView6);
                        TextView spotifyVal = (TextView) findViewById(R.id.textView7);


                        if(items.isEmpty()){

                            artistLabel.setVisibility(View.VISIBLE);
                            artistVal.setVisibility(View.VISIBLE);
                            artistVal.setText(artist);

                            trackLabel.setVisibility(View.VISIBLE);
                            trackVal.setVisibility(View.VISIBLE);
                            trackVal.setText(title);

                            albumLabel.setVisibility(View.VISIBLE);
                            albumVal.setVisibility(View.VISIBLE);
                            albumVal.setText(album);

                        }
                        else{
                            Item i = items.get(0);
                            String ab = i.getAlbum().getName();
                            String ar = i.getArtists().get(0).getName();
                            String titl = i.getName();
                            String link = i.getExternalUrls()!=null?i.getExternalUrls().getSpotify():"";


                            artistLabel.setVisibility(View.VISIBLE);
                            artistVal.setVisibility(View.VISIBLE);
                            artistVal.setText(ar);

                            trackLabel.setVisibility(View.VISIBLE);
                            trackVal.setVisibility(View.VISIBLE);
                            trackVal.setText(titl);

                            albumLabel.setVisibility(View.VISIBLE);
                            albumVal.setVisibility(View.VISIBLE);
                            albumVal.setText(ab);

                            spotifyVal.setVisibility(View.VISIBLE);
                            spotifyVal.setText(link);

                        }

                    }


                    Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();



                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }



            }
        });

    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    class doGet extends AsyncTask<String,Void,String>{


        protected String doInBackground(String... params){

            String urlString = "https://api.spotify.com/v1/search/?q=track:"+params[0]+" artist:"+params[1]+"&type=track&time="+System.currentTimeMillis();

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();


                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }

                br.close();

                return responseOutput.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "";
        }

    }

    class doPost extends AsyncTask<String,Void,String> {

        protected String doInBackground(String... urls){

            String fileName=AudioSavePathInDevice;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "------------------------afb19f4aeefb356c";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(fileName);

            if (!sourceFile.isFile()) {

            }
            else{
                try{
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL("http://35.163.12.69:81/result");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy            s
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("file", sourceFile.getName());
                    conn.setRequestProperty("connection", "close");


                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFile.getName() + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    conn.connect();
                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();

                    String serverResponseMessage = conn.getResponseMessage().toString();


                    DataInputStream inStream;
                    String str="";
                    String response="";
                    try {
                        inStream = new DataInputStream(conn.getInputStream());

                        while ((str = inStream.readLine()) != null) {
                            response=str;
                        }
                        inStream.close();
                    }
                    catch (IOException ioex) {
                        Log.e("joshtag", "SOF error: " + ioex.getMessage(), ioex);
                    }
                    conn.disconnect();
                    conn=null;
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                    if(serverResponseCode == 201){
                        Log.e("SERVER RESPONSE: 201",response);
                    }//END IF Response code 201
                    // conn.disconnect();

                    return response;
                }//END TRY - FILE READ
                catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Log.e("joshtag", "UL error: " + ex.getMessage(), ex);
                } //CATCH - URL Exception

                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Upload file Exception", "Exception : "+ e.getMessage(), e);
                }

            }//END ELSE, if file exists.

        return null;
        }

    }

}