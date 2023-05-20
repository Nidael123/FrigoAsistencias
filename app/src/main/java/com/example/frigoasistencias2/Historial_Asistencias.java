package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import com.example.frigoasistencias2.adpater.AdaptadorListadoPdf;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class Historial_Asistencias extends AppCompatActivity {

    RecyclerView listapdf;
    AdaptadorListadoPdf adapter;
    ArrayList<String> rutas;
    File file;
    File[] arrayfiles ;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_asistencias);
        listapdf = findViewById(R.id.recycler_h_listadopdf);
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Asistencias/");
        rutas=new ArrayList<>();

        FileFilter filtro = new FileFilter() {
            @Override
            public boolean accept(File arch) {
                return arch.isFile();
            }
        };
        arrayfiles = file.listFiles(filtro);

        Log.d("ficheros1",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Asistencias/");
        Log.d("ficheros3",file.canRead()+"-:"+arrayfiles.length+file.getName());
        for (int i = 0; i < arrayfiles.length; i++)
        {
            //Sacamos del array files un fichero
            File file = arrayfiles[i];
            Log.d("ficheros2",file.getName()+"");
            //Si es directorio...
            if (file.isDirectory())
                rutas.add(file.getName() + "/");
                //Si es fichero...
            else
                rutas.add(file.getName());
        }
        rutas.add("rutas 1");
        rutas.add("rutas 2");
        adapter =  new AdaptadorListadoPdf(rutas);
        listapdf.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        listapdf.setAdapter(adapter);
    }
}