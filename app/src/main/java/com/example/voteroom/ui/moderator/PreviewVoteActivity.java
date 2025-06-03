package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PreviewVoteActivity extends AppCompatActivity {
    private String roomCode;
    private String questionId;
    private ValueEventListener questionListener;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_vote);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        questionId = getIntent().getStringExtra("QUESTION_ID");

        TextView questionTitle = findViewById(R.id.questionTitle);
        TextView optionsText = findViewById(R.id.optionsText);
        Button editButton = findViewById(R.id.editButton);
        Button backButton = findViewById(R.id.backButton);

        if (roomCode == null || questionId == null) {
            Toast.makeText(this, "Brak wymaganych danych pytania.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ref = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions").child(questionId);

        questionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String title = snapshot.child("title").getValue(String.class);
                questionTitle.setText(title != null ? title : "Brak tytułu");

                StringBuilder sb = new StringBuilder();
                DataSnapshot options = snapshot.child("options");
                for (DataSnapshot opt : options.getChildren()) {
                    String optVal = opt.getValue(String.class);
                    if (optVal != null && !optVal.trim().isEmpty()) {
                        sb.append("• ").append(optVal).append("\n");
                    }
                }
                optionsText.setText(sb.length() > 0 ? sb.toString().trim() : "Brak opcji");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PreviewVoteActivity.this, "Błąd pobierania pytania", Toast.LENGTH_SHORT).show();
            }
        };
        ref.addValueEventListener(questionListener);

        editButton.setOnClickListener(v -> {
            if (roomCode != null && questionId != null) {
                Intent intent = new Intent(PreviewVoteActivity.this, EditQuestionActivity.class);
                intent.putExtra("ROOM_CODE", roomCode);
                intent.putExtra("QUESTION_ID", questionId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Brak danych do edycji.", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref != null && questionListener != null) {
            ref.removeEventListener(questionListener);
        }
    }
}