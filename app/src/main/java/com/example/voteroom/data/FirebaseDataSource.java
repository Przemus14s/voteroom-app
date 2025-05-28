package com.example.voteroom.data;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
}
