package com.example.voteroom.ui.moderator;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.voteroom.R;
import com.example.voteroom.data.FirebaseDataSource;

import java.util.HashMap;
import java.util.Map;

public class EditQuestionActivity extends AppCompatActivity {
    private EditText questionTitleField;
    private LinearLayout optionsLayout;
    private Button saveButton;
    private Button addOptionButton;
    private Button removeOptionButton;


    private String roomCode, questionId;
    private final Map<String, EditText> optionFields = new HashMap<>();

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

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        questionId = getIntent().getStringExtra("QUESTION_ID");

        FirebaseDataSource.getInstance().getQuestion(roomCode, questionId, (title, options) -> {
            questionTitleField.setText(title);

            for (Map.Entry<String, String> entry : options.entrySet()) {
                EditText optionField = new EditText(this);
                optionField.setText(entry.getValue());
                optionsLayout.addView(optionField);
                optionFields.put(entry.getKey(), optionField);
            }
        });

        saveButton.setOnClickListener(v -> saveUpdatedQuestion());

        addOptionButton.setOnClickListener(v -> {
            if (optionFields.size() >= 4) {
                Toast.makeText(this, "Maksymalnie 4 opcje", Toast.LENGTH_SHORT).show();
                return;
            }

            String newOptionKey = "option" + (optionFields.size() + 1);
            EditText optionField = new EditText(this);
            optionField.setHint("Opcja odpowiedzi");
            optionField.setTextColor(Color.WHITE);
            optionField.setHintTextColor(Color.WHITE);
            optionField.setBackgroundResource(android.R.drawable.editbox_background);
            optionField.setPadding(12, 12, 12, 12);

            optionsLayout.addView(optionField);
            optionFields.put(newOptionKey, optionField);
        });

        removeOptionButton.setOnClickListener(v -> {
            if (optionFields.size() <= 2) {
                Toast.makeText(this, "Minimum 2 opcje", Toast.LENGTH_SHORT).show();
                return;
            }

            String lastKey = "option" + optionFields.size();
            EditText lastField = optionFields.get(lastKey);
            optionsLayout.removeView(lastField);
            optionFields.remove(lastKey);
        });
    }

    private void saveUpdatedQuestion() {
        String newTitle = questionTitleField.getText().toString().trim();
        Map<String, String> updatedOptions = new HashMap<>();

        for (Map.Entry<String, EditText> entry : optionFields.entrySet()) {
            String text = entry.getValue().getText().toString().trim();
            if (!text.isEmpty()) {
                updatedOptions.put(entry.getKey(), text);
            }
        }

        FirebaseDataSource.getInstance().updateQuestion(roomCode, questionId, newTitle, updatedOptions,
                () -> {
                    Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                    finish();
                },
                () -> Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show());
    }
}