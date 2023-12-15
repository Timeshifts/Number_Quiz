package com.example.number_quiz;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    ImageButton hintButton;

    public int answer;
    public int answerNo;
    public int number1, number2, number3;
    public int hearts = 3;
    public int score = 0;
    public int highScore = 0;
    public boolean isHintUsed = false;

    public String language;

    QuestionRecord insertingQuestionRecord;
    QuestionDao questionDao;

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
        hintButton = findViewById(R.id.hintView);
        hintButton.setOnClickListener(v -> useHint());

        LocaleListCompat localeListCompat = AppCompatDelegate.getApplicationLocales();
        if (!localeListCompat.isEmpty() &&
                requireNonNull(localeListCompat.get(0)).getLanguage()
                        .equals(new Locale("ko").getLanguage())) {
            language = "ko";
        } else {
            language = "en";
        }

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "QuestionDB").build();
        questionDao = db.questionDao();

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

    public void useHint() {
        if (isHintUsed) return;
        isHintUsed = true;
        hintButton.setImageResource(R.drawable.bulb_empty);
        Random random = new Random();
        CharSequence charSequence;

        int hintNo = random.nextInt(3) + 1;

        // 정답 지우기 방지
        if (hintNo == answerNo) {
            hintNo += 1;
            if (hintNo == 4) hintNo = 1;
        }

        Button strikeButton;

        switch (hintNo) {
            case 1:
                strikeButton = answerButton1;
                break;
            case 2:
                strikeButton = answerButton2;
                break;
            default: // 3
                strikeButton = answerButton3;
                break;
        }

        charSequence = strikeButton.getText();
        SpannableString spannableString = new SpannableString(charSequence);

        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        spannableString.setSpan(strikethroughSpan, 0, charSequence.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        strikeButton.setText(spannableString);

        // 리스너 제거로 답에서 제외된 버튼 클릭하는 불상사 방지
        strikeButton.setOnClickListener(null);
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
            recordQuestion(true);
            questionView.setText(R.string.correct_answer);
            score++;
            updateScoreView();
        } else {
            recordQuestion(false);
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

    public void recordQuestion(boolean correct) {

        insertingQuestionRecord = new QuestionRecord(
                questionView.getText().toString(), answer, correct, language);

        InsertQuestionThread thread = new InsertQuestionThread();
        thread.start();

    }

    private void returnToTitle() {
        dataStore.dispose();
        finish();
    }

    public void enableAnswerButton() {
        answerButton1.setOnClickListener(v -> handleUserAnswer(1));
        answerButton2.setOnClickListener(v -> handleUserAnswer(2));
        answerButton3.setOnClickListener(v -> handleUserAnswer(3));
    }

    public void makeQuestionView(String response) {

        Gson gson = new Gson();
        NumberResponse numberResponse = gson.fromJson(response, NumberResponse.class);
        String question = String.format("Which number is %s?", numberResponse.text);

        boolean waitForTranslate;

        if (Objects.equals(language, "ko")) {
            translateQuestion(question);
            waitForTranslate = true;
        } else {
            questionView.setText(question);
            waitForTranslate = false;
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
        answerButton2.setText(String.valueOf(number2));
        answerButton3.setText(String.valueOf(number3));
        if (!waitForTranslate) enableAnswerButton();
    }

    public void makeTranslatedQuestionView(String response) {
        Gson gson = new Gson();
        System.out.println(response);
        TranslateResponse translateResponse = gson.fromJson(response, TranslateResponse.class);
        String translatedQuestion = translateResponse.trans;
        questionView.setText(translatedQuestion);
        enableAnswerButton();
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

    class InsertQuestionThread extends Thread {

        @Override
        public void run() { questionDao.insertAll(insertingQuestionRecord); }
    }
}
