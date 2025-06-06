package com.example.voteroom.service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.voteroom.ui.QuestionTileAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String DB_URL = "https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/";

    public interface QuestionsCallback {
        void onQuestionsLoaded(List<QuestionTileAdapter.QuestionItem> questions);
        void onError(String error);
    }

    public static ValueEventListener listenForQuestions(String roomCode, QuestionsCallback callback) {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("rooms").child(roomCode).child("questions");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuestionTileAdapter.QuestionItem> questions = new ArrayList<>();
                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    String id = qSnap.getKey();
                    String title = qSnap.child("title").getValue(String.class);
                    List<String> options = new ArrayList<>();
                    DataSnapshot optionsSnap = qSnap.child("options");
                    for (DataSnapshot opt : optionsSnap.getChildren()) {
                        String optVal = opt.getValue(String.class);
                        if (optVal != null) options.add(optVal);
                    }
                    if (id != null && title != null) {
                        questions.add(new QuestionTileAdapter.QuestionItem(id, title, options));
                    }
                }
                callback.onQuestionsLoaded(questions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Błąd pobierania pytań");
            }
        };
        questionsRef.addValueEventListener(listener);
        return listener;
    }

    public interface RoomActiveCallback {
        void onRoomActiveChanged(boolean isActive);
        void onError(String error);
    }

    public static ValueEventListener listenForRoomActive(String roomCode, RoomActiveCallback callback) {
        DatabaseReference activeRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("rooms").child(roomCode).child("active");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                callback.onRoomActiveChanged(isActive != null && isActive);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Błąd połączenia z bazą");
            }
        };
        activeRef.addValueEventListener(listener);
        return listener;
    }

    public interface VoteCallback {
        void onVoteSuccess();
        void onVoteError(String error);
    }

    public static void vote(Context context, String roomCode, String questionId, String option, String userId, VoteCallback callback) {
        DatabaseReference voteRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("rooms").child(roomCode).child("questions").child(questionId).child("votes").child(option);
        DatabaseReference votersRef = FirebaseDatabase.getInstance(DB_URL)
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
                        setVoted(context, roomCode, questionId);
                        callback.onVoteSuccess();
                    });
                } else {
                    callback.onVoteError("Błąd oddawania głosu");
                }
            }
        });
    }

    private static void setVoted(Context context, String roomCode, String questionId) {
        String key = "voted_" + roomCode + "_" + questionId;
        context.getSharedPreferences("votes", Context.MODE_PRIVATE).edit().putBoolean(key, true).apply();
    }

    public interface CheckRoomCallback {
        void onRoomChecked(boolean exists, boolean isActive, String roomName);
        void onError(String error);
    }

    public static void checkRoom(String roomCode, CheckRoomCallback callback) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("rooms").child(roomCode);
        roomRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                callback.onRoomChecked(false, false, null);
                return;
            }
            Boolean isActive = snapshot.child("active").getValue(Boolean.class);
            String roomName = snapshot.child("name").getValue(String.class);
            callback.onRoomChecked(true, isActive != null && isActive, roomName);
        }).addOnFailureListener(e -> callback.onError("Błąd połączenia z bazą"));
    }

    public static void removeRoomActiveListener(String roomCode, ValueEventListener listener) {
        if (roomCode == null || listener == null) return;
        DatabaseReference activeRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("rooms").child(roomCode).child("active");
        activeRef.removeEventListener(listener);
    }
}