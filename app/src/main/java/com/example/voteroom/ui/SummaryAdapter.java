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
        public String questionTitle;
        public List<String> options;
        public List<Long> votes;
        public long totalVotes;

        public SummaryItem(String questionTitle, List<String> options, List<Long> votes, long totalVotes) {
            this.questionTitle = questionTitle;
            this.options = options;
            this.votes = votes;
            this.totalVotes = totalVotes;
        }
    }

    private final List<SummaryItem> items;

    public SummaryAdapter(List<SummaryItem> items) {
        this.items = items;
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
        StringBuilder sb = new StringBuilder();
        sb.append(item.questionTitle).append("\n");
        for (int i = 0; i < item.options.size(); i++) {
            String opt = item.options.get(i);
            long count = item.votes.get(i);
            int percent = (item.totalVotes > 0) ? (int) ((count * 100) / item.totalVotes) : 0;
            sb.append(opt).append(" – ").append(percent).append("% (").append(count).append(" głosów)\n");
        }
        holder.title.setText(sb.toString().trim());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.questionTitle);
        }
    }
}