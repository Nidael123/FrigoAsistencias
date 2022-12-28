package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.example.frigoasistencias2.adpater.AdaptadorReciclerdatosdiarios;
import com.example.frigoasistencias2.bd.Managerbd;
import com.example.frigoasistencias2.clases.datosdiarios;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class datosDiarios extends AppCompatActivity {

    RecyclerView recicler;
    ArrayList<datosdiarios> datos;

    AdaptadorReciclerdatosdiarios adapter;
    Managerbd bd;
    SQLiteDatabase bdcache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_diarios);

        datos = new ArrayList<datosdiarios>();

        bd = new Managerbd(this,"Registro",null,1);
        bdcache = bd.getReadableDatabase();
        recicler = (RecyclerView) findViewById(R.id.recicler_diarios);
        buscardata();



        adapter = new AdaptadorReciclerdatosdiarios(datos);
        recicler.setAdapter(adapter);
        recicler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void buscardata()
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        bdcache = bd.getReadableDatabase();

        //Cursor cursor = bdcache.rawQuery("Select fechaingreso from t_registro where "+" fechaingreso like "+"'%"+fechadia+"%'",null);
        Cursor cursor = bdcache.rawQuery("Select fechaingreso,id_libro from t_registro where "+" fechaingreso like "+"'%"+fechadia+"%' group by id_libro",null);


        if(cursor.moveToFirst())
        {
            do {
                try {
                    datosdiarios help;
                    help = new datosdiarios();
                    Log.d("ID_usuario existente",""+fechadia);
                    help.setFecha(fechadia);
                    datos.add(help);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }while(cursor.moveToNext());
        }
    }
}