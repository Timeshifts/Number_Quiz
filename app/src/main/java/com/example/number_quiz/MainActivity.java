package com.example.number_quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            startActivity(intent);
        });

        final Button questionBankButton = findViewById(R.id.questionBankButton);
        questionBankButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), QuestionBankActivity.class);
            startActivity(intent);
        });

        final Button langButton = findViewById(R.id.langButton);
        langButton.setOnClickListener(v -> {
            LocaleListCompat oldLocale, newLocale;
            oldLocale = AppCompatDelegate.getApplicationLocales();
            if (oldLocale.isEmpty() ) {
                newLocale = LocaleListCompat.forLanguageTags("en-US");
            } else {
                if (Objects.requireNonNull(oldLocale.get(0)).getLanguage().equals(new Locale("en").getLanguage())) {
                    newLocale = LocaleListCompat.forLanguageTags("ko-KR");
                } else {
                    newLocale = LocaleListCompat.forLanguageTags("en-US");
                }
            }
            AppCompatDelegate.setApplicationLocales(newLocale);
        });
    }

}