package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TYMCZASOWE TESTOWANIE POŁĄCZENIA Z BAZĄ
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference dbRef = database.getReference();
        String logId = dbRef.child("logs").push().getKey();
        if (logId != null) {
            dbRef.child("logs").child(logId).setValue("test log :D")
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Log zapisany!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Błąd zapisu: " + e.getMessage()));
        }

        Button moderatorButton = findViewById(R.id.moderatorButton);
        Button userButton = findViewById(R.id.userButton);

        moderatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ModeratorLoginActivity.class));
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JoinRoomActivity.class));
            }
        });
    }
}