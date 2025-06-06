package com.example.voteroom.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.voteroom.R;
import com.example.voteroom.ui.moderator.CreateRoomActivity;
import com.example.voteroom.ui.user.JoinRoomActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {

            } else {
            }
        });

        checkNotificationPermission();

        Button moderatorButton = findViewById(R.id.moderatorButton);
        Button userButton = findViewById(R.id.userButton);

        moderatorButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateRoomActivity.class));
        });

        userButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JoinRoomActivity.class));
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}