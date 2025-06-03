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
        if (item.options != null && !item.options.isEmpty()) {
            holder.options.setText("Opcje: " + String.join(", ", item.options));
            holder.options.setVisibility(View.VISIBLE);
        } else {
            holder.options.setText("");
            holder.options.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item.id, item.title));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, options;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.questionTitle);
            options = v.findViewById(R.id.questionSubtitle);
        }
    }

    public static class QuestionItem {
        public String id;
        public String title;
        public List<String> options;

        public QuestionItem(String id, String title, List<String> options) {
            this.id = id;
            this.title = title;
            this.options = options;
        }
    }
}