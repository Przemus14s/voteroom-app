package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.service.ModeratorService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateRoomActivity extends AppCompatActivity {
    private EditText roomNameField;
    private Button createRoomButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        SharedPreferences prefs = getSharedPreferences("moderator_prefs", MODE_PRIVATE);
        String roomCode = prefs.getString("ROOM_CODE", null);
        String roomName = prefs.getString("ROOM_NAME", null);

        if (roomCode != null && roomName != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("active");
            dbRef.get().addOnSuccessListener(snapshot -> {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && isActive) {
                    Intent intent = new Intent(this, ModeratorRoomActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.putExtra("ROOM_NAME", roomName);
                    startActivity(intent);
                    finish();
                }
            });
        }

        roomNameField = findViewById(R.id.roomNameField);
        createRoomButton = findViewById(R.id.createRoomButton);
        backButton = findViewById(R.id.backButton);

        createRoomButton.setOnClickListener(v -> ModeratorService.createRoom(this, roomNameField.getText().toString().trim()));
        backButton.setOnClickListener(v -> finish());
    }

}