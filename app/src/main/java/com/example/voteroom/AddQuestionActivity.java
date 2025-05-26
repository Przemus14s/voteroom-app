package com.example.voteroom;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AddQuestionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        Button saveQuestionButton = findViewById(R.id.saveQuestionButton);
        saveQuestionButton.setOnClickListener(v -> finish());
    }
}