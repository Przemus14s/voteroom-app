package com.example.voteroom.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.example.voteroom.service.UserService;
import com.example.voteroom.util.AppLifecycleListener;
import com.example.voteroom.util.NotificationHelper;
import com.google.firebase.database.ValueEventListener;

public class VotingNotStartedActivity extends AppCompatActivity {
    private String roomCode;
    private String roomName;
    private ValueEventListener activeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_not_started);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        roomName = getIntent().getStringExtra("ROOM_NAME");

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (roomCode != null) {
            activeListener = UserService.listenForRoomActive(roomCode, new UserService.RoomActiveCallback() {
                @Override
                public void onRoomActiveChanged(boolean isActive) {
                    if (isActive) {
                        if (!AppLifecycleListener.isAppInForeground()) {
                            NotificationHelper.showVotingStartedNotification(
                                    VotingNotStartedActivity.this,
                                    roomCode,
                                    roomName != null ? roomName : roomCode
                            );
                        }
                        Intent intent = new Intent(VotingNotStartedActivity.this, SelectVoteActivity.class);
                        intent.putExtra("ROOM_CODE", roomCode);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(VotingNotStartedActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeListener != null) {
            UserService.removeRoomActiveListener(roomCode, activeListener);
        }
    }
}