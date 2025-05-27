package com.example.voteroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ModeratorRoomActivity extends AppCompatActivity {
    private String roomCode;
    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_room);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomName = getIntent().getStringExtra("ROOM_NAME");

        if (roomCode == null || roomName == null) {
            Toast.makeText(this, "Brak danych pokoju", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView roomTitleText = findViewById(R.id.roomTitleText);
        TextView roomCodeText = findViewById(R.id.roomCodeText);

        roomTitleText.setText("Pokój: " + roomName);
        roomCodeText.setText("Kod: " + roomCode);

        Button addQuestionButton = findViewById(R.id.addQuestionButton);
        Button closeRoomButton = findViewById(R.id.closeRoomButton);
        Button backButton = findViewById(R.id.backButton);

        addQuestionButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModeratorRoomActivity.this, AddQuestionActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("ROOM_NAME", roomName);
            startActivity(intent);
        });

        closeRoomButton.setOnClickListener(v -> closeRoom());

        backButton.setOnClickListener(v -> finish());
    }

    private void closeRoom() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("active");
        dbRef.setValue(false).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Pokój zamknięty", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd zamykania pokoju", Toast.LENGTH_SHORT).show()
        );
    }
}