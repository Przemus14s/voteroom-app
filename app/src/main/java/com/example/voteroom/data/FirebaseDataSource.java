package com.example.voteroom.data;


import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataSource {

    private static FirebaseDataSource instance;

    public FirebaseDataSource() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/");
    }

    public static FirebaseDataSource getInstance() {
        if (instance == null) {
            instance = new FirebaseDataSource();
        }
        return instance;
    }

    public void createRoom() {

    }
}
