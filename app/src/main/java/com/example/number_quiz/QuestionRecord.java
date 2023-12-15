package com.example.number_quiz;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class QuestionRecord {

    QuestionRecord(String question, int answer, boolean wasRight, String lang) {
        this.question = question;
        this.answer = answer;
        this.wasRight = wasRight;
        this.lang = lang;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "answer")
    public int answer;

    @ColumnInfo(name = "wasRight")
    public boolean wasRight;

    @ColumnInfo(name = "lang")
    public String lang;

}