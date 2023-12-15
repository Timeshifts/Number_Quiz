package com.example.number_quiz;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.room.Room;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QuestionBankActivity extends AppCompatActivity {

    Handler handler = new Handler();
    LinearLayout questionLayout;
    AppDatabase db;
    QuestionDao questionDao;
    String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        LocaleListCompat localeListCompat = AppCompatDelegate.getApplicationLocales();
        if (!localeListCompat.isEmpty() &&
                requireNonNull(localeListCompat.get(0)).getLanguage()
                        .equals(new Locale("ko").getLanguage())) {
            language = "ko";
        } else {
            language = "en";
        }

        Context context = getApplicationContext();
        questionLayout = findViewById(R.id.questionLayout);
        db = Room.databaseBuilder(context, AppDatabase.class, "QuestionDB").build();
        questionDao = db.questionDao();
        GetQuestionThread thread = new GetQuestionThread();
        thread.start();
    }

    public void setQuestionView(@NonNull List<QuestionRecord> questionRecordList) {

        int textViewCount = 0;

        for (int i = questionRecordList.size()-1; i > -1; i--) {

            if (textViewCount == 10) break;
            QuestionRecord questionRecord = questionRecordList.get(i);
            // 언어에 맞는 문제 기록들만 출력
            if (!Objects.equals(language, questionRecord.lang)) continue;

            textViewCount++;
            TextView textView = new TextView(this);
            textView.setText(getString(R.string.question_view_entry,
                    questionRecord.question, questionRecord.answer));

            if (questionRecord.wasRight) {
                textView.setBackgroundColor(Color.parseColor("#ccffcc"));
            } else {
                textView.setBackgroundColor(Color.parseColor("#ffcccc"));
            }

            questionLayout.addView(textView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 48);

            textView.setLayoutParams(layoutParams);
            textView.setTextSize(26);
            textView.setBottom(32);
        }
    }

    class GetQuestionThread extends Thread {

        @Override
        public void run() {
            List<QuestionRecord> questionRecordList = questionDao.getAll();
            handler.post(() -> setQuestionView(questionRecordList));
        }
    }
}

