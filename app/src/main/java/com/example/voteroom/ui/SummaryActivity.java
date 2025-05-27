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
    private static final int REQUEST_CODE_WRITE_EXTERNAL = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        roomCode = getIntent().getStringExtra("ROOM_CODE");

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

        Button backToMainButton = findViewById(R.id.backToMainButton);
        backToMainButton.setOnClickListener(v -> finish());

        loadSummary();
    }

    private void loadSummary() {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.get().addOnSuccessListener(snapshot -> {
            List<SummaryAdapter.SummaryItem> items = new ArrayList<>();
            for (DataSnapshot qSnap : snapshot.getChildren()) {
                String title = qSnap.child("title").getValue(String.class);
                List<String> options = new ArrayList<>();
                List<Long> votes = new ArrayList<>();
                long totalVotes = 0;

                DataSnapshot optionsSnap = qSnap.child("options");
                DataSnapshot votesSnap = qSnap.child("votes");

                for (DataSnapshot opt : optionsSnap.getChildren()) {
                    String optKey = opt.getKey();
                    String optText = opt.getValue(String.class);
                    options.add(optText);
                    long count = 0;
                    if (votesSnap.hasChild(optKey)) {
                        Long v = votesSnap.child(optKey).getValue(Long.class);
                        if (v != null) count = v;
                    }
                    votes.add(count);
                    totalVotes += count;
                }
                items.add(new SummaryAdapter.SummaryItem(title, options, votes, totalVotes));
            }
            RecyclerView recyclerView = findViewById(R.id.summaryRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SummaryAdapter(items));
        });
    }

    private void generateAndSavePdf() {
        if (roomCode == null) {
            Toast.makeText(this, "Brak kodu pokoju", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference questionsRef = FirebaseDatabase.getInstance("https://voteroom-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("rooms").child(roomCode).child("questions");

        questionsRef.get().addOnSuccessListener(snapshot -> {
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
                canvas.drawText("Pytanie: " + title, 40, y, paint);
                y += 25;

                DataSnapshot options = qSnap.child("options");
                DataSnapshot votes = qSnap.child("votes");
                long totalVotes = 0;
                for (DataSnapshot v : votes.getChildren()) {
                    Long count = v.getValue(Long.class);
                    if (count != null) totalVotes += count;
                }

                for (int i = 1; i <= 4; i++) {
                    String key = String.valueOf(i);
                    if (options.hasChild(key)) {
                        String optText = options.child(key).getValue(String.class);
                        long count = votes.hasChild(key) ? votes.child(key).getValue(Long.class) : 0;
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
            pdfDocument.finishPage(page);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                    } catch (IOException e) {
                        Toast.makeText(this, "Błąd zapisu PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                try {
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    File file = new File(dir, "wyniki_glosowania_" + roomCode + ".pdf");
                    FileOutputStream fos = new FileOutputStream(file);
                    pdfDocument.writeTo(fos);
                    fos.close();
                    Toast.makeText(this, "PDF zapisany w: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Błąd zapisu PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            pdfDocument.close();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Błąd pobierania danych", Toast.LENGTH_SHORT).show()
        );
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