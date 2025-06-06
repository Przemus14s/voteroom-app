package com.example.voteroom.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.ui.ResultsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class VoteActivity extends AppCompatActivity {
    private String roomCode;
    private String questionId;
    private DatabaseReference roomRef;
    private ValueEventListener activeListener;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        questionId = getIntent().getStringExtra("QUESTION_ID");

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getString("USER_ID", null);
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            prefs.edit().putString("USER_ID", userId).apply();
        }

        TextView questionText = findViewById(R.id.questionText);
        RadioGroup optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        RadioButton option1 = findViewById(R.id.option1);
        RadioButton option2 = findViewById(R.id.option2);
        RadioButton option3 = findViewById(R.id.option3);
        RadioButton option4 = findViewById(R.id.option4);

        if (roomCode == null || questionId == null) {
            Toast.makeText(this, "Brak pytania", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference questionRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions").child(questionId);

        questionRef.get().addOnSuccessListener(qSnap -> {
            if (!qSnap.exists()) {
                Toast.makeText(this, "Nie znaleziono pytania", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String title = qSnap.child("title").getValue(String.class);
            questionText.setText(title);

            DataSnapshot options = qSnap.child("options");
            option1.setText(options.child("1").getValue(String.class));
            option2.setText(options.child("2").getValue(String.class));
            if (options.hasChild("3")) {
                option3.setText(options.child("3").getValue(String.class));
                option3.setVisibility(RadioButton.VISIBLE);
            } else {
                option3.setVisibility(RadioButton.GONE);
            }
            if (options.hasChild("4")) {
                option4.setText(options.child("4").getValue(String.class));
                option4.setVisibility(RadioButton.VISIBLE);
            } else {
                option4.setVisibility(RadioButton.GONE);
            }
        });

        Button voteButton = findViewById(R.id.voteButton);
        voteButton.setOnClickListener(v -> {

            int checkedId = optionsRadioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, "Wybierz opcję", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedOption = null;
            if (checkedId == R.id.option1) selectedOption = "1";
            else if (checkedId == R.id.option2) selectedOption = "2";
            else if (checkedId == R.id.option3) selectedOption = "3";
            else if (checkedId == R.id.option4) selectedOption = "4";

            if (selectedOption == null || questionId == null) {
                Toast.makeText(this, "Błąd głosowania", Toast.LENGTH_SHORT).show();
                return;
            }

            setVoted(roomCode, questionId);

            DatabaseReference voteRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("questions").child(questionId).child("votes").child(selectedOption);


            DatabaseReference votersRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("questions").child(questionId).child("voters").child(userId);

            voteRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                @Override
                public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData currentData) {
                    Integer currentVotes = currentData.getValue(Integer.class);
                    if (currentVotes == null) {
                        currentData.setValue(1);
                    } else {
                        currentData.setValue(currentVotes + 1);
                    }
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    if (committed) {

                        votersRef.setValue(true).addOnCompleteListener(task -> {
                            Toast.makeText(VoteActivity.this, "Głos oddany!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VoteActivity.this, SelectVoteActivity.class);
                            intent.putExtra("ROOM_CODE", roomCode);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        Toast.makeText(VoteActivity.this, "Błąd oddawania głosu", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void listenForRoomClosed() {
        roomRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode);
        activeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean isActive = snapshot.child("active").getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    Intent intent = new Intent(VoteActivity.this, ResultsActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.putExtra("QUESTION_ID", questionId);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        roomRef.addValueEventListener(activeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomRef != null && activeListener != null) {
            roomRef.removeEventListener(activeListener);
        }
    }


    private void setVoted(String roomCode, String questionId) {
        String key = "voted_" + roomCode + "_" + questionId;
        getSharedPreferences("votes", MODE_PRIVATE).edit().putBoolean(key, true).apply();
    }
}