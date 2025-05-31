package com.example.voteroom.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voteroom.R;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionItem> questions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QuestionItem question);
    }

    public QuestionAdapter(List<QuestionItem> questions, OnItemClickListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_tile, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionItem question = questions.get(position);
        holder.questionTitle.setText(question.title);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(question));
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTitle;
        QuestionViewHolder(View itemView) {
            super(itemView);
            questionTitle = itemView.findViewById(R.id.questionTitle);
        }
    }
}