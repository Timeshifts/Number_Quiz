package com.example.number_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            startActivity(intent);
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        //makeRequest();
    }

    public void makeRequest() {
        String url = "https://numbersapi.p.rapidapi.com/random/trivia?min=10&max=20&fragment=true&json=true";

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> System.out.println(response),
                error -> System.err.println(error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                final String API_KEY = getResources().getString(R.string.number_api_key);
                final String API_HOST = getResources().getString(R.string.number_api_host);
                params.put("X-RapidAPI-Key", API_KEY);
                params.put("X-RapidAPI-Host", API_HOST);
                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

}