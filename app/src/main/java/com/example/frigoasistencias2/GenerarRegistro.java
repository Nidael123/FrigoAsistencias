package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class GenerarRegistro extends AppCompatActivity {

    ListView faltantes;
    ArrayList<String> listadofaltantes;
    ArrayAdapter adpter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_registro);

        faltantes =findViewById(R.id.listview_m_faltantes);
    }
}