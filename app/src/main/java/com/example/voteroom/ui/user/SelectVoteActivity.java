package com.example.voteroom.ui.user;

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
import com.example.voteroom.ui.moderator.ModeratorRoomActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectVoteActivity extends AppCompatActivity {
    private String roomCode;
    private RecyclerView recyclerView;
    private QuestionTileAdapter adapter;
    private List<QuestionTileAdapter.QuestionItem> questions = new ArrayList<>();

    private DatabaseReference roomRef;
    private ValueEventListener questionsListener;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vote);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        recyclerView = findViewById(R.id.questionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionTileAdapter(questions, (questionId, title) -> {
            Intent intent = new Intent(this, VoteActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("QUESTION_ID", questionId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        Button moderatorPanelButton = findViewById(R.id.moderatorPanelButton);
        moderatorPanelButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModeratorRoomActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("ROOM_NAME", "");
            startActivity(intent);
        });

        roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);

        listenForRoomActive();
        listenForQuestions();
    }

    private void listenForQuestions() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                questions.clear();
                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    String id = qSnap.getKey();
                    String title = qSnap.child("title").getValue(String.class);
                    if (id != null && title != null) {
                        questions.add(new QuestionTileAdapter.QuestionItem(id, title));
                    }
                }
                adapter.notifyDataSetChanged();
                if (questions.isEmpty()) {
                    Toast.makeText(SelectVoteActivity.this, "Brak głosowań w tym pokoju", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SelectVoteActivity.this, "Błąd pobierania pytań", Toast.LENGTH_SHORT).show();
            }
        };
        questionsRef.addValueEventListener(questionsListener);
    }

    private void listenForRoomActive() {
        activeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    Toast.makeText(SelectVoteActivity.this, "Pokój został zamknięty", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SelectVoteActivity.this, SummaryActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        if (questionsListener != null) {
            DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("questions");
            questionsRef.removeEventListener(questionsListener);
        }
    }
}