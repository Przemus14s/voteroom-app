package com.example.voteroom.ui.moderator;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;
import com.example.voteroom.ui.SummaryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ModeratorResultsActivity extends AppCompatActivity {
    private String roomCode;
    private RecyclerView resultsRecyclerView;
    private SummaryAdapter summaryAdapter;
    private final List<SummaryAdapter.SummaryItem> summaryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_results);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        summaryAdapter = new SummaryAdapter(summaryItems);
        resultsRecyclerView.setAdapter(summaryAdapter);

        if (roomCode == null) {
            Toast.makeText(this, "Brak kodu pokoju", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadResults();
    }

    private void loadResults() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.get().addOnSuccessListener(snapshot -> {
            summaryItems.clear();
            if (!snapshot.hasChildren()) {
                Toast.makeText(this, "Brak zakończonych głosowań", Toast.LENGTH_SHORT).show();
                summaryAdapter.notifyDataSetChanged();
                return;
            }
            for (DataSnapshot qSnap : snapshot.getChildren()) {
                String title = qSnap.child("title").getValue(String.class);
                List<String> options = new ArrayList<>();
                List<Long> votes = new ArrayList<>();
                long totalVotes = 0;

                DataSnapshot optionsSnap = qSnap.child("options");
                DataSnapshot votesSnap = qSnap.child("votes");

                for (int i = 1; i <= 4; i++) {
                    String key = String.valueOf(i);
                    String opt = optionsSnap.child(key).getValue(String.class);
                    if (opt != null) {
                        options.add(opt);
                        long count = 0;
                        if (votesSnap.exists()) {
                            Long v = votesSnap.child(key).getValue(Long.class);
                            count = v != null ? v : 0;
                        }
                        votes.add(count);
                        totalVotes += count;
                    }
                }

                if (title != null && !options.isEmpty()) {
                    summaryItems.add(new SummaryAdapter.SummaryItem(title, options, votes, totalVotes));
                }
            }
            summaryAdapter.notifyDataSetChanged();
            if (summaryItems.isEmpty()) {
                Toast.makeText(this, "Brak zakończonych głosowań", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd pobierania wyników", Toast.LENGTH_SHORT).show()
        );
    }
}