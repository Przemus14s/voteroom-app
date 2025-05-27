package com.example.voteroom.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;

import java.util.List;

public class QuestionTileAdapter extends RecyclerView.Adapter<QuestionTileAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(String questionId, String title);
    }

    private final List<QuestionItem> questions;
    private final OnItemClickListener listener;

    public QuestionTileAdapter(List<QuestionItem> questions, OnItemClickListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_tile, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionItem item = questions.get(position);
        holder.title.setText(item.title);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item.id, item.title));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.questionTitle);
        }
    }

    public static class QuestionItem {
        public String id;
        public String title;

        public QuestionItem(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}