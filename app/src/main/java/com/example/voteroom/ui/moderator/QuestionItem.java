package com.example.voteroom.ui.moderator;

import java.util.List;

public class QuestionItem {
    public String id;
    public String title;
    public List<String> options;

    public QuestionItem(String id, String title, List<String> options) {
        this.id = id;
        this.title = title;
        this.options = options;
    }
}