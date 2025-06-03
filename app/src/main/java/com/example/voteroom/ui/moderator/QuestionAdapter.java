package com.example.voteroom.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voteroom.R;
import java.util.List;
import java.util.Map;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionItem> questions;
    private Map<String, String> optionsMap;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QuestionItem question);
    }


    public QuestionAdapter(List<QuestionItem> questions, Map<String, String> optionsMap, OnItemClickListener listener) {
        this.questions = questions;
        this.optionsMap = optionsMap;
        this.listener = listener;
    }


    public QuestionAdapter(List<QuestionItem> questions, OnItemClickListener listener) {
        this(questions, null, listener);
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

        String options = (optionsMap != null) ? optionsMap.get(question.id) : null;
        if (options != null && !options.isEmpty()) {
            holder.questionSubtitle.setText("Opcje: " + options);
            holder.questionSubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.questionSubtitle.setText("");
            holder.questionSubtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(question);
        });
    }

    @Override
    public int getItemCount() {
        return (questions != null) ? questions.size() : 0;
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTitle, questionSubtitle;
        QuestionViewHolder(View itemView) {
            super(itemView);
            questionTitle = itemView.findViewById(R.id.questionTitle);
            questionSubtitle = itemView.findViewById(R.id.questionSubtitle);
        }
    }
}