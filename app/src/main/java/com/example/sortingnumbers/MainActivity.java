package com.example.sortingnumbers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    String result, url;

    Button retrieveButton, clearAllButton;

    TextView randomNum1, randomNum2, randomNum3, randomNum4;

    private TextView multipleOf2, multipleOf3, multipleOf5, multipleOf10;

    int[] numberArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveButton = findViewById(R.id.retrieveButton);
        clearAllButton = findViewById(R.id.clearAllButton);

        randomNum1 = findViewById(R.id.randomNum1);
        randomNum2 = findViewById(R.id.randomNum2);
        randomNum3 = findViewById(R.id.randomNum3);
        randomNum4 = findViewById(R.id.randomNum4);

        randomNum1.setOnTouchListener(this);
        randomNum2.setOnTouchListener(this);
        randomNum3.setOnTouchListener(this);
        randomNum4.setOnTouchListener(this);

        multipleOf2 = findViewById(R.id.multipleOf2);
        multipleOf2.setOnDragListener(this);

        multipleOf3 = findViewById(R.id.multipleOf3);
        multipleOf3.setOnDragListener(this);

        multipleOf5 = findViewById(R.id.multipleOf5);
        multipleOf5.setOnDragListener(this);

        multipleOf10 = findViewById(R.id.multipleOf10);
        multipleOf10.setOnDragListener(this);

    }

    // this method resets all the values on screen
    public void clearAllNumbers(View view) {
        randomNum1.setText("_____");
        randomNum2.setText("_____");
        randomNum3.setText("_____");
        randomNum4.setText("_____");

        multipleOf2.setText("Multiples of 2");
        multipleOf3.setText("Multiples of 3");
        multipleOf5.setText("Multiples of 5");
        multipleOf10.setText("Multiples of 10");
    }

    //
    public void buttonGetNumbers(View view) {
        url = "https://qrng.anu.edu.au/API/jsonI.php?length=4&type=uint8";

        Thread myThread = new Thread(new GetNumberThread());
        myThread.start();
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                TextView textView = (TextView) event.getLocalState();

                int valueToUse = -1;

                if (textView.getId() == R.id.randomNum1) {
                    // value of the first array
                    valueToUse = numberArr[0];
                    randomNum1.setText("_____");
                }

                if (textView.getId() == R.id.randomNum2) {
                    valueToUse = numberArr[1];
                    randomNum2.setText("_____");
                }

                if (textView.getId() == R.id.randomNum3) {
                    valueToUse = numberArr[2];
                    randomNum3.setText("_____");
                }

                if (textView.getId() == R.id.randomNum4) {
                    valueToUse = numberArr[3];
                    randomNum4.setText("_____");
                }


                if (v.getId() == R.id.multipleOf2) {
                    multipleOf2.append("\n" + valueToUse);
                }

                if (v.getId() == R.id.multipleOf3) {
                    multipleOf3.append("\n" + valueToUse);
                }

                if (v.getId() == R.id.multipleOf5) {
                    multipleOf5.append("\n" + valueToUse);
                }

                if (v.getId() == R.id.multipleOf10) {
                    multipleOf10.append("\n" + valueToUse);
                }

                break;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            for (int i = 3; i > -1; i--) {
                // checking if at least 1 of the 4 random numbers is a multiple of at least 1 of 2, 3, 5, or 10
                if (numberArr[i] % 2 == 0 || numberArr[i] % 3 == 0 || numberArr[i] % 5 == 0 || numberArr[i] % 10 == 0) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(data, dragShadowBuilder, v, 0);
                    return true;
                }

            }
            return false;
        }
        return false;
    }

    private class GetNumberThread implements Runnable {
        @Override
        public void run() {
            Exception exception = null;
            try {
                result = readJSONData(url);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String str = jsonObject.getString("data");

                            String[] string = str.replaceAll("\\[", "")
                                    .replaceAll("]", "")
                                    .split(",");

                            Toast.makeText(MainActivity.this, "Numbers: " + string[0] + ", " + string[1] + ", " + string[2] + ", " + string[3], Toast.LENGTH_LONG).show();

                            numberArr = new int[string.length];

                            for (int i = 0; i < string.length; i++) {
                                numberArr[i] = Integer.valueOf(string[i]);
                            }

                            randomNum1.setText(string[0]);
                            randomNum2.setText(string[1]);
                            randomNum3.setText(string[2]);
                            randomNum4.setText(string[3]);

                        } catch (Exception e) {
                            Log.d("ReadWeatherJSONDataTask", e.getLocalizedMessage());
                        }
                    }
                });
            } catch (IOException e) {
                exception = e;
            }
        }
    }

    public void checkConnection() {
        ConnectivityManager connectMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //fetch data

            String networkType = networkInfo.getTypeName().toString();
            Toast.makeText(this, "connected to " + networkType, Toast.LENGTH_LONG).show();
        } else {
            //display error
            Toast.makeText(this, "no network connection", Toast.LENGTH_LONG).show();
        }
    }

    private String readJSONData(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 2500;

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("tag", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
                conn.disconnect();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}