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

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.service.ModeratorService;

public class ModeratorRoomActivity extends AppCompatActivity {
    private String roomCode;
    private String roomName;

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