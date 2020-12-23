package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    Button searchButton;
    TextView details;
    String APIkey;
    Vibrator vibe;

    class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Establish connection with address
                connection.connect();

                //retrieve data from url
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                //Retrieve data and return it as String
                int data = isr.read();
                String content = "";
                char ch;
                while (data != -1){
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                return content;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void search(View view){

        details.setVisibility(View.INVISIBLE);

        vibe.vibrate(80);

        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.searchButton);
        details = findViewById(R.id.details);

        String cName = cityName.getText().toString();

        if(cName.isEmpty()) {
            Toast.makeText(this, "Enter a city name!", Toast.LENGTH_SHORT).show();
            hideKeyboard();
            return;
        }

        String content;
        DownloadTask task = new DownloadTask();
        try {
            content = task.execute("https://api.openweathermap.org/data/2.5/weather?q=" +
                    cName+"&appid=" + APIkey).get();

            if(content == null) {
                Toast.makeText(this, "Could not find weather!", Toast.LENGTH_SHORT).show();
                hideKeyboard();
                return;
            }

//            Log.i("contentData",content);

            //JSON
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mainTemperature = jsonObject.getString("main");
            String windPart = jsonObject.getString("wind");

            JSONArray array = new JSONArray(weatherData);

            String main = "";
            String description = "";
            String temp = "";
            String humidity = "";
            String wind = "";

            for(int i=0; i<array.length(); i++){
                JSONObject jsonPart = array.getJSONObject(i);
                main = jsonPart.getString("main");
                description = jsonPart.getString("description");
            }

            JSONObject mainPart = new JSONObject(mainTemperature);
            temp = mainPart.getString("temp");
            humidity = mainPart.getString("humidity");

            JSONObject windSpeed = new JSONObject(windPart);
            wind = windSpeed.getString("speed");

            double temperature = Double.parseDouble(temp) - 273;
            double roundOff = (double) Math.round(temperature * 100) / 100;

            String resultText = "Main :     "+main+
                    "\n\nDescription :    "+description +
                    "\n\nTemperature :   "+ String.valueOf(roundOff) +" *C"+
                    "\n\nHumidity :   "+humidity +" %"+
                    "\n\nWind :   "+wind +" Km/hr";

            details.setText(resultText);
            details.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Could not find weather", Toast.LENGTH_LONG);
        }

        hideKeyboard();

    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.searchButton);
        details = findViewById(R.id.details);
        APIkey = "bce38abd11542f820714804fb49a5599" ;

        vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    }
}
