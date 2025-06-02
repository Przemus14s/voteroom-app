package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.ui.ResultsActivity;
import com.example.voteroom.ui.SummaryActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModeratorVotingActivity extends AppCompatActivity {
    private String roomCode;
    private DatabaseReference roomRef;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_voting);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);

        Button endVotingButton = findViewById(R.id.endVotingButton);

        endVotingButton.setOnClickListener(v -> {
            if (roomCode != null) {
                roomRef.child("active").setValue(false)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Głosowanie zakończone", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Błąd zakończenia głosowania", Toast.LENGTH_SHORT).show());
            }
        });

        // Nasłuch na zakończenie głosowania
        activeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    Intent intent = new Intent(ModeratorVotingActivity.this, SummaryActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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