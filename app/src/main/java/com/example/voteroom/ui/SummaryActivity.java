package com.example.voteroom.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voteroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    private String roomCode;
    private RecyclerView summaryRecyclerView;
    private SummaryAdapter summaryAdapter;
    private final List<SummaryAdapter.SummaryItem> summaryItems = new ArrayList<>();
    private static final int REQUEST_CODE_WRITE_EXTERNAL = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        summaryRecyclerView = findViewById(R.id.summaryRecyclerView);
        summaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        summaryAdapter = new SummaryAdapter(summaryItems);
        summaryRecyclerView.setAdapter(summaryAdapter);

        Button backToMainButton = findViewById(R.id.backToMainButton);
        backToMainButton.setOnClickListener(v -> finish());

        Button generatePdfButton = findViewById(R.id.generatePdfButton);
        generatePdfButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_EXTERNAL);
            } else {
                generateAndSavePdf();
            }
        });

        if (roomCode == null) {
            Toast.makeText(this, "Brak kodu pokoju", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSummary();
    }

    private void loadSummary() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.get().addOnSuccessListener(snapshot -> {
            summaryItems.clear();
            if (!snapshot.hasChildren()) {
                Toast.makeText(this, "Brak zakończonych głosowań", Toast.LENGTH_SHORT).show();
                summaryAdapter.notifyDataSetChanged();
                return;
            }
            for (DataSnapshot qSnap : snapshot.getChildren()) {
                String title = qSnap.child("title").getValue(String.class);
                List<String> options = new ArrayList<>();
                List<Long> votes = new ArrayList<>();
                long totalVotes = 0;

                DataSnapshot optionsSnap = qSnap.child("options");
                DataSnapshot votesSnap = qSnap.child("votes");

                for (int i = 1; i <= 4; i++) {
                    String key = String.valueOf(i);
                    String opt = optionsSnap.child(key).getValue(String.class);
                    if (opt != null) {
                        options.add(opt);
                        long count = 0;
                        if (votesSnap.exists()) {
                            Long v = votesSnap.child(key).getValue(Long.class);
                            count = v != null ? v : 0;
                        }
                        votes.add(count);
                        totalVotes += count;
                    }
                }

                if (title != null && !options.isEmpty()) {
                    summaryItems.add(new SummaryAdapter.SummaryItem(title, options, votes, totalVotes));
                }
            }
            summaryAdapter.notifyDataSetChanged();
            if (summaryItems.isEmpty()) {
                Toast.makeText(this, "Brak zakończonych głosowań", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd pobierania podsumowania", Toast.LENGTH_SHORT).show()
        );
    }

    private void generateAndSavePdf() {
        if (roomCode == null) {
            Toast.makeText(this, "Brak kodu pokoju", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.hasChildren()) {
                Toast.makeText(this, "Brak danych do zapisania", Toast.LENGTH_SHORT).show();
                return;
            }
            PdfDocument pdfDocument = createSummaryPdf(snapshot);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    savePdfToDownloadsQ(pdfDocument);
                } else {
                    savePdfToDownloadsLegacy(pdfDocument);
                }
            } catch (IOException e) {
                Toast.makeText(this, "Błąd zapisu PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                pdfDocument.close();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd pobierania danych", Toast.LENGTH_SHORT).show()
        );
    }

    private PdfDocument createSummaryPdf(DataSnapshot snapshot) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        int y = 60;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(20);
        canvas.drawText("Podsumowanie głosowania - Pokój: " + roomCode, 40, y, paint);
        y += 40;
        paint.setTextSize(16);

        for (DataSnapshot qSnap : snapshot.getChildren()) {
            String title = qSnap.child("title").getValue(String.class);
            DataSnapshot options = qSnap.child("options");
            DataSnapshot votes = qSnap.child("votes");
            long totalVotes = 0;
            if (votes.exists()) {
                for (int i = 1; i <= 4; i++) {
                    String key = String.valueOf(i);
                    Long count = votes.child(key).getValue(Long.class);
                    if (count != null) totalVotes += count;
                }
            }

            if (title != null && options.exists()) {
                canvas.drawText("Pytanie: " + title, 40, y, paint);
                y += 25;

                for (int i = 1; i <= 4; i++) {
                    String key = String.valueOf(i);
                    String optText = options.child(key).getValue(String.class);
                    if (optText != null) {
                        long count = 0;
                        if (votes.exists()) {
                            Long v = votes.child(key).getValue(Long.class);
                            count = v != null ? v : 0;
                        }
                        int percent = (totalVotes > 0) ? (int) ((count * 100) / totalVotes) : 0;
                        canvas.drawText(optText + " – " + percent + "% (" + count + " głosów)", 60, y, paint);
                        y += 20;
                    }
                }
                y += 20;
                if (y > 800) {
                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 60;
                }
            }
        }
        pdfDocument.finishPage(page);
        return pdfDocument;
    }

    private void savePdfToDownloadsQ(PdfDocument pdfDocument) throws IOException {
        ContentValues values = new ContentValues();
        values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "wyniki_glosowania_" + roomCode + ".pdf");
        values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(android.provider.MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                pdfDocument.writeTo(out);
                Toast.makeText(this, "PDF zapisany w katalogu Pobrane", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void savePdfToDownloadsLegacy(PdfDocument pdfDocument) throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "wyniki_glosowania_" + roomCode + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdfDocument.writeTo(fos);
            Toast.makeText(this, "PDF zapisany w: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSavePdf();
            } else {
                Toast.makeText(this, "Brak uprawnień do zapisu pliku", Toast.LENGTH_SHORT).show();
            }
        }
    }
}