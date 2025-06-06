package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;
import com.example.voteroom.ui.QuestionTileAdapter;
import com.example.voteroom.ui.SummaryActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ModeratorVotingActivity extends AppCompatActivity {
    private String roomCode;
    private DatabaseReference roomRef;
    private ValueEventListener activeListener;
    private ValueEventListener questionsListener;
    private QuestionTileAdapter adapter;
    private List<QuestionTileAdapter.QuestionItem> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_voting);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);

        Button endVotingButton = findViewById(R.id.endVotingButton);

        RecyclerView votingRecyclerView = findViewById(R.id.votingRecyclerView);
        adapter = new QuestionTileAdapter(questions, (questionId, title) -> {

        });
        votingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        votingRecyclerView.setAdapter(adapter);

        DatabaseReference questionsRef = roomRef.child("questions");
        questionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questions.clear();
                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    String id = qSnap.getKey();
                    String title = qSnap.child("title").getValue(String.class);
                    List<String> options = new ArrayList<>();
                    DataSnapshot optionsSnap = qSnap.child("options");
                    DataSnapshot votesSnap = qSnap.child("votes");
                    long totalVotes = 0;
                    for (DataSnapshot opt : optionsSnap.getChildren()) {
                        String optVal = opt.getValue(String.class);
                        String key = opt.getKey();
                        long votes = 0;
                        if (votesSnap.exists() && key != null) {
                            Long v = votesSnap.child(key).getValue(Long.class);
                            votes = v != null ? v : 0;
                            totalVotes += votes;
                        }
                        if (optVal != null) {
                            options.add(optVal + " (" + votes + " głosów)");
                        }
                    }
                    if (id != null && title != null) {

                        questions.add(new QuestionTileAdapter.QuestionItem(id, title, options));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        questionsRef.addValueEventListener(questionsListener);

        endVotingButton.setOnClickListener(v -> {
            if (roomCode != null) {
                roomRef.child("active").setValue(false)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Głosowanie zakończone", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Błąd zakończenia głosowania", Toast.LENGTH_SHORT).show());
            }
        });

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
            public void onCancelled(@NonNull DatabaseError error) {
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
        if (roomRef != null && questionsListener != null) {
            roomRef.child("questions").removeEventListener(questionsListener);
        }
    }
}