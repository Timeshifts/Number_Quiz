package com.example.number_quiz;

import static java.util.Objects.requireNonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class GameActivity extends AppCompatActivity {

    static RequestQueue requestQueue;
    RxDataStore<Preferences> dataStore;

    TextView questionView;
    TextView bestView;
    Button nextButton;
    Button answerButton1;
    Button answerButton2;
    Button answerButton3;

    public int answer;
    public int answerNo;
    public int number1, number2, number3;
    public int hearts = 3;
    public int score = 0;
    public int highScore = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dataStore = new RxPreferenceDataStoreBuilder(
                getApplicationContext(), "highScore").build();
        questionView = findViewById(R.id.questionView);
        bestView = findViewById(R.id.bestView);
        nextButton = findViewById(R.id.nextButton);
        answerButton1 = findViewById(R.id.answer1);
        answerButton2 = findViewById(R.id.answer2);
        answerButton3 = findViewById(R.id.answer3);

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        updateScoreView();
        makeQuestion();
    }

    public int makeRandomNumber(int baseNumber, float range) {
        Random random = new Random();
        final int bound = (int) (baseNumber * range);
        return random.nextInt(bound + 1);
    }

    public void updateHeartView() {
        final ImageView heart1 = findViewById(R.id.heartView1);
        final ImageView heart2 = findViewById(R.id.heartView2);
        final ImageView heart3 = findViewById(R.id.heartView3);

        heart1.setImageResource((hearts >= 1) ? R.drawable.heart : R.drawable.heart_empty);
        heart2.setImageResource((hearts >= 2) ? R.drawable.heart : R.drawable.heart_empty);
        heart3.setImageResource((hearts >= 3) ? R.drawable.heart : R.drawable.heart_empty);
    }

    public void updateScoreView() {
        Preferences.Key<Integer> highScoreKey;
        try {
            highScoreKey = PreferencesKeys.intKey("highScore");
            Flowable<Integer> highScoreFlow = dataStore.data().map(
                    prefs -> prefs.get(highScoreKey));
            highScore = highScoreFlow.blockingFirst();
        } catch (Exception e) {
            highScore = 0;
        }
        if (highScore < score) {
            highScore = score;
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(PreferencesKeys.intKey("highScore"), highScore);
                return Single.just(mutablePreferences);
            });
        }
        final TextView scoreView = findViewById(R.id.scoreView);
        scoreView.setText(getString(R.string.score_view, score));
        bestView.setText(getString(R.string.best_view, highScore));
    }

    public void handleUserAnswer(int userAnswerNo) {
        if (userAnswerNo == answerNo) {
            questionView.setText(R.string.correct_answer);
            score++;
            updateScoreView();
        } else {
            questionView.setText(getString(R.string.incorrect_answer, answer));
            hearts--;
            updateHeartView();
        }

        answerButton1.setOnClickListener(null);
        answerButton2.setOnClickListener(null);
        answerButton3.setOnClickListener(null);
        nextButton.setVisibility(View.VISIBLE);
        if (hearts == 0) {
            questionView.append('\n' + getString(R.string.game_over));
            nextButton.setText(R.string.title_button);
            nextButton.setOnClickListener(v -> returnToTitle());
        } else {
            nextButton.setOnClickListener(v -> makeQuestion());
        }
    }

    private void returnToTitle() {
        dataStore.dispose();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void makeQuestionView(String response) {

        Gson gson = new Gson();
        NumberResponse numberResponse = gson.fromJson(response, NumberResponse.class);
        String question = String.format("Which number is %s?", numberResponse.text);

        LocaleListCompat localeListCompat = AppCompatDelegate.getApplicationLocales();

        if (!localeListCompat.isEmpty() &&
                requireNonNull(localeListCompat.get(0)).getLanguage()
                        .equals(new Locale("ko").getLanguage())) {
            translateQuestion(question);
        } else {
            questionView.setText(question);
        }

        Random random = new Random();
        answer = numberResponse.number;
        answerNo = random.nextInt(3) + 1;
        number1 = answer; number2 = answer; number3 = answer;

        switch (answerNo) {
            case 1:
                number2 += makeRandomNumber(answer, 0.2f) + 1;
                number3 = number2 + makeRandomNumber(number2, 0.2f) + 1;
                break;
            case 2:
                number1 -= makeRandomNumber(answer, 0.2f) + 1;
                number3 += makeRandomNumber(answer, 0.2f) + 1;
                break;
            case 3:
                number2 -= makeRandomNumber(answer, 0.2f) + 1;
                number1 = number2 - makeRandomNumber(number2, 0.2f) - 1;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + answerNo);
        }

        answerButton1.setText(String.valueOf(number1));
        answerButton1.setOnClickListener(v -> handleUserAnswer(1));
        answerButton2.setText(String.valueOf(number2));
        answerButton2.setOnClickListener(v -> handleUserAnswer(2));
        answerButton3.setText(String.valueOf(number3));
        answerButton3.setOnClickListener(v -> handleUserAnswer(3));
    }

    public void makeTranslatedQuestionView(String response) {
        Gson gson = new Gson();
        System.out.println(response);
        TranslateResponse translateResponse = gson.fromJson(response, TranslateResponse.class);
        String translatedQuestion = translateResponse.trans;
        questionView.setText(translatedQuestion);
    }

    public void translateQuestion(String question) {
        String url = "https://google-translate113.p.rapidapi.com/api/v1/translator/text";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                this::makeTranslatedQuestionView,
                error -> System.err.println(error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("from", "en");
                params.put("to", "ko");
                params.put("text", question);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                final String API_KEY = getResources().getString(R.string.translate_api_key);
                final String API_HOST = getResources().getString(R.string.translate_api_host);
                params.put("X-RapidAPI-Key", API_KEY);
                params.put("X-RapidAPI-Host", API_HOST);
                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    public void makeQuestion() {
        if (nextButton.getVisibility() == View.VISIBLE) nextButton.setVisibility(View.INVISIBLE);

        String url = "https://numbersapi.p.rapidapi.com/random/trivia?min=0&max=9999&fragment=true&json=true";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                this::makeQuestionView,
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
