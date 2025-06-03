package com.example.voteroom.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;

import java.util.List;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
    public static class SummaryItem {
        public String questionId;
        public String questionTitle;
        public List<String> options;
        public List<Long> votes;
        public long totalVotes;

        public SummaryItem(String questionId, String questionTitle, List<String> options, List<Long> votes, long totalVotes) {
            this.questionId = questionId;
            this.questionTitle = questionTitle;
            this.options = options;
            this.votes = votes;
            this.totalVotes = totalVotes;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String questionId, String questionTitle);
    }

    private final List<SummaryItem> items;
    private final OnItemClickListener listener;

    public SummaryAdapter(List<SummaryItem> items, OnItemClickListener listener) {
        this.items = items;
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
        SummaryItem item = items.get(position);
        holder.title.setText(item.questionTitle);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < item.options.size(); i++) {
            String opt = item.options.get(i);
            long count = item.votes.get(i);
            int percent = (item.totalVotes > 0) ? (int) ((count * 100) / item.totalVotes) : 0;
            sb.append(opt)
                    .append(" – ")
                    .append(percent)
                    .append("% (")
                    .append(count)
                    .append(" głos")
                    .append(count == 1 ? "" : "ów")
                    .append(")\n");
        }
        holder.subtitle.setText(sb.toString().trim());
        holder.subtitle.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item.questionId, item.questionTitle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.questionTitle);
            subtitle = v.findViewById(R.id.questionSubtitle);
        }
    }
}