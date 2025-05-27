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
import java.util.Random;

public class CreateRoomActivity extends AppCompatActivity {
    private EditText roomNameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        roomNameField = findViewById(R.id.roomNameField);
        Button createRoomButton = findViewById(R.id.createRoomButton);
        Button backButton = findViewById(R.id.backButton);

        createRoomButton.setOnClickListener(v -> createRoom());
        backButton.setOnClickListener(v -> finish());
    }

    private String generateRoomCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void createRoom() {
        String roomName = roomNameField.getText().toString().trim();
        if (roomName.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę pokoju", Toast.LENGTH_SHORT).show();
            return;
        }
        String roomCode = generateRoomCode();
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", roomName);
        roomData.put("code", roomCode);
        roomData.put("active", true);

        dbRef.setValue(roomData).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(this, ModeratorRoomActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("ROOM_NAME", roomName);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd tworzenia pokoju", Toast.LENGTH_SHORT).show()
        );
    }
}