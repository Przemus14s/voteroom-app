package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;

public class
VoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        Button voteButton = findViewById(R.id.voteButton);
        voteButton.setOnClickListener(v -> startActivity(new Intent(VoteActivity.this, ResultsActivity.class)));
    }
}