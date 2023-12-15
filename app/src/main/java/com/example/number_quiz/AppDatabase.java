package com.example.number_quiz;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {QuestionRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuestionDao questionDao();
}
