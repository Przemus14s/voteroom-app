// app/src/main/java/com/example/voteroom/ui/user/VotingNotStartedActivity.java
package com.example.voteroom.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VotingNotStartedActivity extends AppCompatActivity {
    private String roomCode;
    private DatabaseReference roomRef;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_not_started);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (roomCode != null) {
            roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("active");
            activeListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Boolean isActive = snapshot.getValue(Boolean.class);
                    if (isActive != null && isActive) {
                        Intent intent = new Intent(VotingNotStartedActivity.this, SelectVoteActivity.class);
                        intent.putExtra("ROOM_CODE", roomCode);
                        startActivity(intent);
                        finish();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(VotingNotStartedActivity.this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show();
                }
            };
            roomRef.addValueEventListener(activeListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomRef != null && activeListener != null) {
            roomRef.removeEventListener(activeListener);
        }
    }
}