package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.adpater.AdapterLibres;
import com.example.frigoasistencias2.clases.Personas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistorialLibres extends AppCompatActivity implements  SearchView.OnQueryTextListener{

    RecyclerView recicler;
    String api_libres,estado;
    SharedPreferences preferences;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    ArrayList<Personas> personas;
    AdapterLibres adapter;
    SearchView buscar_libre;
    boolean estadobuscador;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_libres);

        recicler = findViewById(R.id.recycler_hl_personas);
        api_libres = getString(R.string.api_libre);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recicler.setLayoutManager(manager);
        recicler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        estado = String.valueOf(getIntent().getIntExtra("estado",0));
        personas=new ArrayList<>();
        buscar_libre = findViewById(R.id.search_hl_libre);
        estadobuscador = false;
        buscar_libre.setOnQueryTextListener(this);
        cargardatos();
    }
    public void cargardatos()
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("Antes de revicion",api_libres +"?v_fecha_ingreso="+fechadia+"&v_id_supervisor="+preferences.getInt("id_usuario",0)+"&v_estado="+estado+"&bandera=1");
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_libres +"?v_fecha_ingreso="+fechadia+"&v_id_supervisor="+preferences.getInt("id_usuario",0)+"&v_estado="+estado+"&bandera=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("revicion",jsonArray.toString());
                    if(jsonArray.length() -1 >= 0)
                    {
                        for(int i = 0;i<=jsonArray.length()-1;i++)
                        {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            if(jsonObject.getString("cedula").equals("0"))
                            {
                                Toast.makeText(HistorialLibres.this,"Listado Vacio",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Personas help = new Personas();
                                help.setNombre(jsonObject.getString("nombre"));
                                help.setCedulas(jsonObject.getString("cedula"));
                                help.setFechainicio(jsonObject.getString("fecha_inicio"));
                                help.setFechafin(jsonObject.getString("fecha_fin"));
                                Log.d("lISTADO",jsonObject.getString("nombre"));
                                personas.add(help);
                            }
                        }
                        adapter = new AdapterLibres(personas);
                        recicler.setAdapter(adapter);
                    }else
                        Toast.makeText(HistorialLibres.this,"Listado Vacio",Toast.LENGTH_SHORT).show();

                }catch (JSONException e)
                {
                    Log.d("123456","entro3"+e.toString());
                    Toast.makeText(HistorialLibres.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerrorlistadiario","dd"+error.toString());
                Toast.makeText(HistorialLibres.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        estadobuscador=true;
        return true;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("estadocomer1",estadobuscador+"");
        if(estadobuscador)
        {
            Log.d("estadocomer2",estadobuscador+"");
            adapter.filtrado(newText);
        }
        return false;
    }
}