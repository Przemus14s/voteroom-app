package com.example.voteroom;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Button generatePdfButton = findViewById(R.id.generatePdfButton);
        generatePdfButton.setOnClickListener(v -> finish());
    }
}