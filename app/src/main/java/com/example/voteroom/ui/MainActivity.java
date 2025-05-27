package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button moderatorButton = findViewById(R.id.moderatorButton);
        Button userButton = findViewById(R.id.userButton);

        moderatorButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateRoomActivity.class));
        });

        userButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JoinRoomActivity.class));
        });
    }
}