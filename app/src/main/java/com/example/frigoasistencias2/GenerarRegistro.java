package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.adpater.AdaptadorRecyclerFaltas;
import com.example.frigoasistencias2.clases.Personas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GenerarRegistro extends AppCompatActivity {

    ArrayList<Personas> persona;
    RecyclerView faltantes;
    ArrayList<String> listadofaltantes,listadofaltantecedulas;
    String api_faltas,api_areas,api_descanso;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    AdaptadorRecyclerFaltas adapter;
    JSONObject jsonObject;
    Button btn_guardarfaltas;
    TextView total;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_registro);

        listadofaltantes = new ArrayList<>();
        listadofaltantecedulas = new ArrayList<>();
        persona = new ArrayList<>();
        faltantes =findViewById(R.id.recycler_g_cedulas);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        api_faltas = getString(R.string.api_faltas);
        api_descanso = getString(R.string.api_descansos);
        api_areas =getString(R.string.api_areas);
        btn_guardarfaltas = findViewById(R.id.btn_g_guardar);
        total = findViewById(R.id.txt_g_total);
        faltantes.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));



        adapter = new AdaptadorRecyclerFaltas(listadofaltantecedulas,listadofaltantes);



        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        cargardatos();

        btn_guardarfaltas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //estado = adapter.guardarcambios();
                persona = adapter.retornarcedulas();
                if(persona.size() > 0)
                {
                    for (int i = 0;i<= persona.size()-1;i++)
                    {
                        Log.d("botonguardar",persona.get(i).getNombre()+"-"+persona.get(i).getEstado());
                        //guardarfaltas(estado.get(i).toString(),cedula.get(i));
                        //guardarfaltas(persona.get(i).getEstado(),persona.get(i).getNombre());
                    }
                    Toast.makeText(GenerarRegistro.this,"Asistencia guardada",Toast.LENGTH_SHORT).show();
                }
                startActivity(new Intent(GenerarRegistro.this,RegistroAsistencia.class));
                finish();
            }
        });
    }

    public void cargardatos()
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("Fecha",fechadia+preferences.getInt("id_usuario",0)+preferences.getString("departamento","mal"));
        final String[] help = new String[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_faltas +"?id_usuario="+preferences.getInt("id_usuario",0)+"&fechaingreso="+fechadia+"&departamento="+preferences.getString("departamento","mal"), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("12345689",jsonArray.toString());

                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        if(jsonObject.getInt("estado") != 0)
                        {
                            listadofaltantes.add(jsonObject.getString("nombres")+":"+jsonObject.getString("cedula"));
                            Log.d("generar",jsonObject.getString("nombres"));
                            listadofaltantecedulas.add(jsonObject.getString("cedula"));
                        }
                        else {
                            Toast.makeText(GenerarRegistro.this,"No hay datos",Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter = new AdaptadorRecyclerFaltas(listadofaltantes,listadofaltantecedulas);
                    faltantes.setAdapter(adapter);
                    total.setText(listadofaltantecedulas.size()+"");
                }catch (JSONException e)
                {
                    Log.d("LISTADODIARIO","entro3"+e.toString());
                    Toast.makeText(GenerarRegistro.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(GenerarRegistro.this,"Error de coneccion consulte con sistemas",Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void guardarfaltas(String v_estado,String v_cedula)
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        int numeroestado=0;
        switch (v_estado)
        {
            case "FALTA":
                numeroestado = 4;
                break;

            case "LIBRE":
                numeroestado = 5;
                break;

            case "VACACIONES":
                numeroestado = 6;
                break;

            case "PERMISO MEDICO":
                numeroestado = 10;
                break;
        }
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+numeroestado,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                }catch (JSONException e)
                {
                    Log.d("LISTADODIARIO","entro3"+e.toString());
                    Toast.makeText(GenerarRegistro.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(GenerarRegistro.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
}