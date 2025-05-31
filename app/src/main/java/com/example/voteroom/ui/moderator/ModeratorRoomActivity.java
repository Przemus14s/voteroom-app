package com.example.voteroom.ui.moderator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;
import com.example.voteroom.service.ModeratorService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ModeratorRoomActivity extends AppCompatActivity {
    private String roomCode;
    private String roomName;

    private QuestionAdapter questionAdapter;
    private List<QuestionItem> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_room);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomName = getIntent().getStringExtra("ROOM_NAME");

        if (roomCode == null || roomName == null) {
            Toast.makeText(this, "Brak danych pokoju", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView roomTitleText = findViewById(R.id.roomTitleText);
        TextView roomCodeText = findViewById(R.id.roomCodeText);
        ImageView copyCodeIcon = findViewById(R.id.copyCodeIcon);

        roomTitleText.setText("Pokój: " + roomName);
        roomCodeText.setText("Kod: " + roomCode);

        copyCodeIcon.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Kod pokoju", roomCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Kod skopiowany do schowka", Toast.LENGTH_SHORT).show();
        });

        RecyclerView questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        questionAdapter = new QuestionAdapter(questionList, question -> {
            Intent intent = new Intent(this, PreviewVoteActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("QUESTION_ID", question.id);
            startActivity(intent);
        });
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(questionAdapter);

        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for (DataSnapshot questionSnap : snapshot.getChildren()) {
                    String id = questionSnap.getKey();
                    String title = questionSnap.child("title").getValue(String.class);
                    if (id != null && title != null) questionList.add(new QuestionItem(id, title));
                }
                questionAdapter.setQuestions(questionList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ModeratorRoomActivity.this, "Błąd pobierania pytań", Toast.LENGTH_SHORT).show();
            }
        });

        Button addQuestionButton = findViewById(R.id.addQuestionButton);
        Button closeRoomButton = findViewById(R.id.closeRoomButton);
        Button backButton = findViewById(R.id.backButton);

        addQuestionButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModeratorRoomActivity.this, AddQuestionActivity.class);
            intent.putExtra("ROOM_CODE", roomCode);
            intent.putExtra("ROOM_NAME", roomName);
            startActivity(intent);
        });

        closeRoomButton.setOnClickListener(v -> ModeratorService.closeRoom(this, roomCode));

        backButton.setOnClickListener(v -> finish());
    }
}