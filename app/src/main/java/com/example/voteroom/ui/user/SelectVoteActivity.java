package com.example.voteroom.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;
import com.example.voteroom.service.UserService;
import com.example.voteroom.ui.QuestionTileAdapter;
import com.example.voteroom.ui.SummaryActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectVoteActivity extends AppCompatActivity {
    private String roomCode;
    private RecyclerView recyclerView;
    private QuestionTileAdapter adapter;
    private List<QuestionTileAdapter.QuestionItem> questions = new ArrayList<>();

    private ValueEventListener questionsListener;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vote);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        Log.d("SelectVoteActivity", "Odebrany ROOM_CODE: " + roomCode);

        if (roomCode == null || roomCode.isEmpty()) {
            Toast.makeText(this, "Brak kodu pokoju!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.questionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionTileAdapter(questions, (questionId, title) -> {
            String key = "voted_" + roomCode + "_" + questionId;
            boolean hasVoted = getSharedPreferences("votes", MODE_PRIVATE).getBoolean(key, false);
            if (hasVoted) {
                Toast.makeText(this, "Już głosowałeś na to pytanie. Czekaj na zamknięcie pokoju.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, VoteActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("QUESTION_ID", questionId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        MaterialButton backToMainButton = findViewById(R.id.backToMainButton);
        backToMainButton.setOnClickListener(v -> finish());

        listenForRoomActive();
        listenForQuestions();
    }

    private void listenForQuestions() {
        questionsListener = UserService.listenForQuestions(roomCode, new UserService.QuestionsCallback() {
            @Override
            public void onQuestionsLoaded(List<QuestionTileAdapter.QuestionItem> loadedQuestions) {
                questions.clear();
                questions.addAll(loadedQuestions);
                adapter.notifyDataSetChanged();
                if (questions.isEmpty()) {
                    Toast.makeText(SelectVoteActivity.this, "Brak głosowań w tym pokoju", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectVoteActivity.this, "Liczba pytań: " + questions.size(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SelectVoteActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForRoomActive() {
        activeListener = UserService.listenForRoomActive(roomCode, new UserService.RoomActiveCallback() {
            @Override
            public void onRoomActiveChanged(boolean isActive) {
                if (!isActive) {
                    Toast.makeText(SelectVoteActivity.this, "Pokój został zamknięty", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SelectVoteActivity.this, SummaryActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeListener != null) {
            UserService.removeRoomActiveListener(roomCode, activeListener);
        }
    }
}