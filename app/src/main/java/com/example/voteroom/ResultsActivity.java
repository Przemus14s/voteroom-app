package com.example.voteroom;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}