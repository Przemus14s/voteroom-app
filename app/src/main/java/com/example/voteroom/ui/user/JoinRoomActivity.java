package com.example.voteroom.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.service.UserService;
import com.example.voteroom.ui.SummaryActivity;
import com.google.firebase.database.ValueEventListener;

public class JoinRoomActivity extends AppCompatActivity {
    private EditText roomCodeField;
    private Button joinRoomButton, backButton;
    private String roomCode;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        roomCodeField = findViewById(R.id.roomCodeField);
        joinRoomButton = findViewById(R.id.joinRoomButton);
        backButton = findViewById(R.id.backButton);

        joinRoomButton.setOnClickListener(v -> joinRoom());
        backButton.setOnClickListener(v -> finish());
    }

    private void joinRoom() {
        roomCode = roomCodeField.getText().toString().trim();
        if (roomCode.isEmpty()) {
            Toast.makeText(this, "Podaj kod pokoju", Toast.LENGTH_SHORT).show();
            return;
        }

        UserService.checkRoom(roomCode, new UserService.CheckRoomCallback() {
            @Override
            public void onRoomChecked(boolean exists, boolean isActive, String roomName) {
                if (!exists) {
                    Toast.makeText(JoinRoomActivity.this, "Niepoprawny kod pokoju", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent;
                if (isActive) {
                    intent = new Intent(JoinRoomActivity.this, SelectVoteActivity.class);
                } else {
                    intent = new Intent(JoinRoomActivity.this, VotingNotStartedActivity.class);
                }
                intent.putExtra("ROOM_CODE", roomCode);
                intent.putExtra("ROOM_NAME", roomName);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(JoinRoomActivity.this, error, Toast.LENGTH_SHORT).show();
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