package com.example.voteroom.ui.moderator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {
    private String roomCode, roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomName = getIntent().getStringExtra("ROOM_NAME");

        EditText questionField = findViewById(R.id.questionField);
        EditText option1Field = findViewById(R.id.option1Field);
        EditText option2Field = findViewById(R.id.option2Field);
        EditText option3Field = findViewById(R.id.option3Field);
        EditText option4Field = findViewById(R.id.option4Field);

        Button saveQuestionButton = findViewById(R.id.saveQuestionButton);
        saveQuestionButton.setOnClickListener(v -> {
            String question = questionField.getText().toString().trim();
            String opt1 = option1Field.getText().toString().trim();
            String opt2 = option2Field.getText().toString().trim();
            String opt3 = option3Field.getText().toString().trim();
            String opt4 = option4Field.getText().toString().trim();

            if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty()) {
                Toast.makeText(this, "Wypełnij pytanie i co najmniej 2 opcje", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("rooms").child(roomCode).child("questions");
            String questionId = questionsRef.push().getKey();

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("title", question);

            Map<String, Object> options = new HashMap<>();
            options.put("1", opt1);
            options.put("2", opt2);
            if (!opt3.isEmpty()) options.put("3", opt3);
            if (!opt4.isEmpty()) options.put("4", opt4);

            questionData.put("options", options);

            questionsRef.child(questionId).setValue(questionData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Pytanie zapisane", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Błąd zapisu pytania", Toast.LENGTH_SHORT).show()
                    );
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}