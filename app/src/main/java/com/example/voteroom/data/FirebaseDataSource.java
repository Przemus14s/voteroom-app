package com.example.voteroom.data;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDataSource {

    private static FirebaseDataSource instance;
    private final FirebaseDatabase database;

    public FirebaseDataSource() {
        database = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/");
    }

    public static FirebaseDataSource getInstance() {
        if (instance == null) {
            instance = new FirebaseDataSource();
        }
        return instance;
    }

    public DatabaseReference getModeratorReference(String name) {
        return database.getReference("moderators").child(name);
    }

    public DatabaseReference getRoomsReference() {
        return database.getReference("rooms");
    }

    public void getQuestion(String roomCode, String questionId, QuestionCallback callback) {
        DatabaseReference ref = database.getReference("rooms").child(roomCode).child("questions").child(questionId);

        ref.get().addOnSuccessListener(snapshot -> {
            String title = snapshot.child("title").getValue(String.class);
            Map<String, String> options = new HashMap<>();

            for (DataSnapshot opt : snapshot.child("options").getChildren()) {
                options.put(opt.getKey(), opt.getValue(String.class));
            }

            callback.onData(title, options);
        });
    }

    public void updateQuestion(String roomCode, String questionId, String title, Map<String, String> options,
                               Runnable onSuccess, Runnable onFailure) {
        DatabaseReference ref = database.getReference("rooms").child(roomCode).child("questions").child(questionId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("options", options);

        ref.updateChildren(updates)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    public interface QuestionCallback {
        void onData(String title, Map<String, String> options);
    }

}
