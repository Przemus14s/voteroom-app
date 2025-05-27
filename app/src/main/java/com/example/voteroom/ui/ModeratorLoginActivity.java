package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
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

        loginButton.setOnClickListener(v -> loginModerator());
        registerButton.setOnClickListener(v -> registerModerator());
        backButton.setOnClickListener(v -> finish());
    }

    private void registerModerator() {
        String name = moderatorNameField.getText().toString().trim();
        String password = moderatorPasswordField.getText().toString().trim();

        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę i hasło", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference moderatorsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("moderators").child(name);

        moderatorsRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, "Moderator już istnieje", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("password", password);
                moderatorsRef.setValue(data).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Zarejestrowano moderatora", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
        );
    }

    private void loginModerator() {
        String name = moderatorNameField.getText().toString().trim();
        String password = moderatorPasswordField.getText().toString().trim();

        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę i hasło", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference moderatorsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("moderators").child(name);

        moderatorsRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Brak takiego moderatora", Toast.LENGTH_SHORT).show();
            } else {
                String savedPassword = snapshot.child("password").getValue(String.class);
                if (password.equals(savedPassword)) {
                    Toast.makeText(this, "Zalogowano jako moderator", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, CreateRoomActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Błędne hasło", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
        );
    }
}