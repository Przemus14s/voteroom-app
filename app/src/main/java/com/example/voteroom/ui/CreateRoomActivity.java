package com.example.voteroom.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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

        createRoomButton.setOnClickListener(v -> createRoom());
        backButton.setOnClickListener(v -> finish());
    }

    private void createRoom() {
        String roomName = roomNameField.getText().toString().trim();
        if (roomName.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę pokoju", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms");
        String roomCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", roomName);
        roomData.put("active", true);

        roomsRef.child(roomCode).setValue(roomData)
                .addOnSuccessListener(aVoid -> {

                    SharedPreferences prefs = getSharedPreferences("moderator_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("ROOM_CODE", roomCode)
                            .putString("ROOM_NAME", roomName)
                            .apply();

                    Intent intent = new Intent(this, ModeratorRoomActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.putExtra("ROOM_NAME", roomName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Błąd tworzenia pokoju", Toast.LENGTH_SHORT).show()
                );
    }
}