package com.example.voteroom.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voteroom.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {
    private String roomCode, questionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        questionId = getIntent().getStringExtra("QUESTION_ID");

        if (roomCode == null || questionId == null) {
            Toast.makeText(this, "Brak danych do wyświetlenia wyników", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadResults();
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadResults() {
        DatabaseReference questionRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions").child(questionId);

        questionRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Nie znaleziono pytania", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Wyświetl tytuł pytania
            TextView titleView = findViewById(R.id.resultsTitle);
            String questionTitle = snapshot.child("title").getValue(String.class);
            if (questionTitle != null && !questionTitle.isEmpty()) {
                titleView.setText("Wyniki: " + questionTitle);
            }

            Map<String, String> options = new HashMap<>();
            if (snapshot.child("options").exists()) {
                for (DataSnapshot opt : snapshot.child("options").getChildren()) {
                    options.put(opt.getKey(), opt.getValue(String.class));
                }
            }

            Map<String, Long> votes = new HashMap<>();
            long totalVotes = 0;
            if (snapshot.child("votes").exists()) {
                for (DataSnapshot vote : snapshot.child("votes").getChildren()) {
                    long count = vote.getValue(Long.class) != null ? vote.getValue(Long.class) : 0;
                    votes.put(vote.getKey(), count);
                    totalVotes += count;
                }
            }

            for (int i = 1; i <= 4; i++) {
                String key = String.valueOf(i);
                String optionText = options.get(key);
                TextView resultView = getResultTextView(i);
                if (optionText != null && resultView != null) {
                    long count = votes.getOrDefault(key, 0L);
                    int percent = (totalVotes > 0) ? (int) ((count * 100) / totalVotes) : 0;
                    String voteWord = (count == 1) ? "głos" : (count >= 2 && count <= 4) ? "głosy" : "głosów";
                    resultView.setText(optionText + " – " + percent + "% (" + count + " " + voteWord + ")");
                    resultView.setVisibility(TextView.VISIBLE);
                } else if (resultView != null) {
                    resultView.setVisibility(TextView.GONE);
                }
            }

            BarChart barChart = findViewById(R.id.barChart);
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            int idx = 0;
            for (int i = 1; i <= 4; i++) {
                String key = String.valueOf(i);
                String optionText = options.get(key);
                if (optionText != null) {
                    long count = votes.getOrDefault(key, 0L);
                    entries.add(new BarEntry(idx, count));
                    labels.add(optionText);
                    idx++;
                }
            }
            if (!entries.isEmpty()) {
                BarDataSet dataSet = new BarDataSet(entries, "Głosy");
                dataSet.setColor(getResources().getColor(R.color.teal_200, null));
                BarData data = new BarData(dataSet);
                data.setBarWidth(0.7f);
                barChart.setData(data);
                barChart.getDescription().setEnabled(false);
                barChart.getLegend().setEnabled(false);
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                barChart.getAxisLeft().setDrawGridLines(false);
                barChart.getAxisRight().setDrawGridLines(false);
                barChart.getAxisRight().setEnabled(false);
                barChart.invalidate();
            } else {
                barChart.clear();
                barChart.setNoDataText("Brak danych do wyświetlenia");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Błąd pobierania wyników", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private TextView getResultTextView(int idx) {
        switch (idx) {
            case 1: return findViewById(R.id.resultOption1);
            case 2: return findViewById(R.id.resultOption2);
            case 3: return findViewById(R.id.resultOption3);
            case 4: return findViewById(R.id.resultOption4);
            default: return null;
        }
    }
}