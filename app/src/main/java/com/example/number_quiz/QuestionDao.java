package com.example.number_quiz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuestionDao {
    @Query("SELECT * FROM questionrecord")
    List<QuestionRecord> getAll();

    @Insert
    void insertAll(QuestionRecord... questionRecords);

}
