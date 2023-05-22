package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.pdfview.PDFView;

import java.io.File;

public class VisorPdf extends AppCompatActivity {

    PDFView vistaPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visor_pdf);


        vistaPdf = findViewById(R.id.vistaPdf);
        String carpeta = "/Asistencias";
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + carpeta;

        File file = new File(path,getIntent().getStringExtra("ruta"));
        Log.d("pdf",getIntent().getStringExtra("ruta"));

        vistaPdf.fromFile(file);
        vistaPdf.isZoomEnabled();

        vistaPdf.show();
    }
}