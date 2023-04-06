package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.adpater.AdaptadorReciclerdatosdiarios;
import com.example.frigoasistencias2.bd.Managerbd;
import com.example.frigoasistencias2.clases.datosdiarios;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class datosDiarios extends AppCompatActivity {

    RecyclerView recicler;
    ArrayList<datosdiarios> datos;
    TextView total;
    AdaptadorReciclerdatosdiarios adapter;
    Managerbd bd;
    SQLiteDatabase bdcache;
    int totalt;
    String api_areas,fechadia;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_diarios);

        datos = new ArrayList<datosdiarios>();

        bd = new Managerbd(this, "Registro", null, 1);
        bdcache = bd.getReadableDatabase();
        recicler = (RecyclerView) findViewById(R.id.recicler_diarios);
        total = findViewById(R.id.txt_dd_total);
        api_areas = getString(R.string.api_areas);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        SimpleDateFormat dateFormat = null;


        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        fechadia = dateFormat.format(date);

        buscardata();


        adapter = new AdaptadorReciclerdatosdiarios(datos);
        recicler.setAdapter(adapter);
        recicler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void buscardata() {
        final int[] contador = {1};
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_areas +"?v_fecha="+fechadia+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&bandera=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        contador[0]++;
                    }

                }catch (JSONException e)
                {
                    Log.d("LISTADODIARIO","entro3"+e.toString());
                    Toast.makeText(datosDiarios.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(datosDiarios.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
}