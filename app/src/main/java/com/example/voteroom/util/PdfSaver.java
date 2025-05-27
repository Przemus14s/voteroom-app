package com.example.voteroom.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PdfSaver {

    public static boolean savePdf(Context context, byte[] pdfData) {
        String fileName = "nazwa_pliku.pdf";
        OutputStream out = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = context.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    out = resolver.openOutputStream(uri);
                } else {
                    Toast.makeText(context, "Błąd zapisu PDF (brak URI)", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                out = new FileOutputStream(file);
            }

            if (out != null) {
                out.write(pdfData);
                out.flush();
                out.close();
                Toast.makeText(context, "PDF zapisany w katalogu Pobrane", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(context, "Błąd zapisu PDF (brak OutputStream)", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(context, "Błąd zapisu PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }
}