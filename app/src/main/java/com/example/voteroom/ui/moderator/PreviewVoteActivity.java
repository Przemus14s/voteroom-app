package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.*;

public class PreviewVoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_vote);

        String roomCode = getIntent().getStringExtra("ROOM_CODE");
        String questionId = getIntent().getStringExtra("QUESTION_ID");

        TextView questionTitle = findViewById(R.id.questionTitle);
        TextView optionsText = findViewById(R.id.optionsText);
        Button editButton = findViewById(R.id.editButton);
        Button backButton = findViewById(R.id.backButton);

        DatabaseReference ref = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions").child(questionId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String title = snapshot.child("title").getValue(String.class);
                questionTitle.setText(title);
                StringBuilder sb = new StringBuilder();
                DataSnapshot options = snapshot.child("options");
                for (DataSnapshot opt : options.getChildren()) {
                    sb.append(opt.getValue(String.class)).append("\n");
                }
                optionsText.setText(sb.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreviewVoteActivity.this, EditQuestionActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("QUESTION_ID", questionId);
            startActivity(intent);
        });
    }
}