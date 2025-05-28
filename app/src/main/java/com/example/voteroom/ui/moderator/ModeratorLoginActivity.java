package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.service.ModeratorService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ModeratorLoginActivity extends AppCompatActivity {
    private EditText moderatorNameField, moderatorPasswordField;
    private Button loginButton, registerButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_login);

        moderatorNameField = findViewById(R.id.usernameField);
        moderatorPasswordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        loginButton.setOnClickListener(v -> ModeratorService.login(this, moderatorNameField.getText().toString(), moderatorPasswordField.getText().toString()));
        registerButton.setOnClickListener(v -> ModeratorService.register(this, moderatorNameField.getText().toString(), moderatorPasswordField.getText().toString()));
        backButton.setOnClickListener(v -> finish());
    }
}