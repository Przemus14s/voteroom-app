package com.example.voteroom.data;

import java.util.List;

public class Room {

    private int id;
    private boolean active;
    private List<User> participants;
    private List<Question> questions;
    private String currentQuestionId;

    public Room() {
    }

    public Room(int roomId, boolean active, List<User> participants, List<Question> questions, String currentQuestionId) {
        this.id = roomId;
        this.active = active;
        this.participants = participants;
        this.questions = questions;
        this.currentQuestionId = currentQuestionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(String currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }
}
