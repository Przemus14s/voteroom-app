package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinRoomActivity extends AppCompatActivity {
    private EditText roomCodeField;
    private Button joinRoomButton, backButton;
    private String roomCode;
    private ValueEventListener activeListener;
    private DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        roomCodeField = findViewById(R.id.roomCodeField);
        joinRoomButton = findViewById(R.id.joinRoomButton);
        backButton = findViewById(R.id.backButton);

        joinRoomButton.setOnClickListener(v -> joinRoom());
        backButton.setOnClickListener(v -> finish());
    }

    private void joinRoom() {
        roomCode = roomCodeField.getText().toString().trim();
        if (roomCode.isEmpty()) {
            Toast.makeText(this, "Podaj kod pokoju", Toast.LENGTH_SHORT).show();
            return;
        }

        roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);

        roomRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Nie znaleziono pokoju", Toast.LENGTH_SHORT).show();
                return;
            }
            Boolean isActive = snapshot.child("active").getValue(Boolean.class);
            if (isActive == null || !isActive) {

                Intent intent = new Intent(this, SummaryActivity.class);
                intent.putExtra("ROOM_CODE", roomCode);
                startActivity(intent);
                finish();
                return;
            }
            listenForRoomActive();
            Intent intent = new Intent(this, SelectVoteActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
        );
    }

    private void listenForRoomActive() {
        if (roomRef == null) return;
        activeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    Intent intent = new Intent(JoinRoomActivity.this, SummaryActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        roomRef.child("active").addValueEventListener(activeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomRef != null && activeListener != null) {
            roomRef.child("active").removeEventListener(activeListener);
        }
    }
}