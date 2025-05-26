package com.example.voteroom.data;

public class User {

    private int id;
    private String name;
    private int roomId;
    private boolean hasVoted;

    public User() {
    }

    public User(int id, String name, int roomId, boolean hasVoted) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
        this.hasVoted = hasVoted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
}
