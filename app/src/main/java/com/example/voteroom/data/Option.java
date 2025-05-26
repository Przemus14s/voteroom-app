package com.example.voteroom.data;

public class Option {

    private int id;
    private String text;
    private int votes;

    public Option() {
    }

    public Option(int id, String text, int votes) {
        this.id = id;
        this.text = text;
        this.votes = votes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
