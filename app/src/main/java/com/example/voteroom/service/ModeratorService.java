package com.example.voteroom.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.voteroom.data.FirebaseDataSource;
import com.example.voteroom.ui.moderator.CreateRoomActivity;
import com.example.voteroom.ui.moderator.ModeratorRoomActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ModeratorService {
    private static final FirebaseDataSource firebaseDataSource = FirebaseDataSource.getInstance();

    public static void login(Context context, String name, String password) {
        if (name.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Podaj nazwę i hasło", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference moderatorsRef = firebaseDataSource.getModeratorReference(name);

        moderatorsRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(context, "Brak takiego moderatora", Toast.LENGTH_SHORT).show();
            } else {
                String savedPassword = snapshot.child("password").getValue(String.class);
                if (password.equals(savedPassword)) {
                    Toast.makeText(context, "Zalogowano jako moderator", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, CreateRoomActivity.class);
                    context.startActivity(intent);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).finish();
                    }
                } else {
                    Toast.makeText(context, "Błędne hasło", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
        );
    }

    public static void register(Context context, String name, String password) {
        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Podaj nazwę i hasło", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference moderatorsRef = firebaseDataSource.getModeratorReference(name);

        moderatorsRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(context, "Moderator już istnieje", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("password", password);
                moderatorsRef.setValue(data).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Zarejestrowano moderatora", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
        );
    }

    public static void createRoom(Context context, String name) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Podaj nazwę pokoju", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomsRef = firebaseDataSource.getRoomsReference();
        String roomCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", name);
        roomData.put("active", true);

        roomsRef.child(roomCode).setValue(roomData)
                .addOnSuccessListener(aVoid -> {

                    SharedPreferences prefs = context.getSharedPreferences("moderator_prefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("ROOM_CODE", roomCode)
                            .putString("ROOM_NAME", name)
                            .apply();

                    Intent intent = new Intent(context, ModeratorRoomActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    intent.putExtra("ROOM_NAME", name);
                    context.startActivity(intent);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Błąd tworzenia pokoju", Toast.LENGTH_SHORT).show()
                );
    }

    public static void closeRoom(Context context, String code) {
        DatabaseReference roomRef = firebaseDataSource.getRoomsReference().child(code);

        roomRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Pokój zamknięty", Toast.LENGTH_SHORT).show();
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }

        }).addOnFailureListener(e ->
                Toast.makeText(context, "Błąd zamykania pokoju", Toast.LENGTH_SHORT).show()
        );
    }
}
