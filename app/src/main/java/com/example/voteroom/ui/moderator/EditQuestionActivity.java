package com.example.voteroom.ui.moderator;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.data.FirebaseDataSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditQuestionActivity extends AppCompatActivity {
    private EditText questionTitleField;
    private LinearLayout optionsLayout;
    private Button saveButton, addOptionButton, removeOptionButton, backButton;

    private String roomCode, questionId;
    private final List<EditText> optionFields = new ArrayList<>();

    private DatabaseReference optionsRef;
    private ValueEventListener optionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_question);

        questionTitleField = findViewById(R.id.editQuestionTitle);
        optionsLayout = findViewById(R.id.optionsLayout);
        saveButton = findViewById(R.id.saveButton);
        addOptionButton = findViewById(R.id.addOptionButton);
        removeOptionButton = findViewById(R.id.removeOptionButton);
        backButton = findViewById(R.id.backButton);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        questionId = getIntent().getStringExtra("QUESTION_ID");

        if (roomCode == null || questionId == null) {
            Toast.makeText(this, "Brak wymaganych danych.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        optionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions").child(questionId).child("options");

        optionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                optionsLayout.removeAllViews();
                optionFields.clear();
                for (DataSnapshot optSnap : snapshot.getChildren()) {
                    String optionText = optSnap.getValue(String.class);
                    if (optionText != null) {
                        EditText optionEdit = createOptionEditText();
                        optionEdit.setText(optionText);
                        optionEdit.setHint("Opcja odpowiedzi");
                        optionsLayout.addView(optionEdit);
                        optionFields.add(optionEdit);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        optionsRef.addValueEventListener(optionsListener);

        FirebaseDataSource.getInstance().getQuestion(roomCode, questionId, (title, options) -> {
            questionTitleField.setText(title != null ? title : "");
        });

        saveButton.setOnClickListener(v -> saveUpdatedQuestion());

        addOptionButton.setOnClickListener(v -> {
            if (optionFields.size() >= 4) {
                Toast.makeText(this, "Maksymalnie 4 opcje", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText optionField = createOptionEditText();
            optionField.setHint("Opcja odpowiedzi");
            optionsLayout.addView(optionField);
            optionFields.add(optionField);
            // Zapisz nową pustą opcję do Firebase pod kolejnym numerem
            optionsRef.child(String.valueOf(optionFields.size())).setValue("");
        });

        removeOptionButton.setOnClickListener(v -> {
            if (optionFields.size() <= 2) {
                Toast.makeText(this, "Minimum 2 opcje", Toast.LENGTH_SHORT).show();
                return;
            }
            int lastIdx = optionFields.size() - 1;
            optionsLayout.removeView(optionFields.get(lastIdx));
            optionsRef.child(String.valueOf(optionFields.size())).removeValue();
            optionFields.remove(lastIdx);
        });

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private EditText createOptionEditText() {
        EditText optionField = new EditText(this);
        optionField.setTextColor(Color.parseColor("#22306D"));
        optionField.setHintTextColor(Color.parseColor("#E3EAF4"));
        optionField.setBackgroundResource(R.drawable.edittext_border);
        optionField.setBackgroundColor(Color.WHITE);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        optionField.setPadding(pad, pad, pad, pad);
        return optionField;
    }

    private void saveUpdatedQuestion() {
        String newTitle = questionTitleField.getText() != null ? questionTitleField.getText().toString().trim() : "";
        if (newTitle.isEmpty()) {
            Toast.makeText(this, "Tytuł pytania nie może być pusty", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> updatedOptions = new LinkedHashMap<>();
        int idx = 1;
        for (EditText field : optionFields) {
            String text = field.getText() != null ? field.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                updatedOptions.put(String.valueOf(idx), text);
                idx++;
            }
        }
        if (updatedOptions.size() < 2) {
            Toast.makeText(this, "Minimum 2 opcje", Toast.LENGTH_SHORT).show();
            return;
        }

        optionsRef.setValue(updatedOptions).addOnSuccessListener(aVoid -> {
            FirebaseDataSource.getInstance().updateQuestion(roomCode, questionId, newTitle, updatedOptions,
                    () -> {
                        Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    () -> Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Błąd zapisu opcji", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (optionsRef != null && optionsListener != null) {
            optionsRef.removeEventListener(optionsListener);
        }
    }
}