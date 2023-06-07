package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.adpater.AdaptadorComida;
import com.example.frigoasistencias2.clases.Personas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Comida extends AppCompatActivity {

    RecyclerView recicler;
    AdaptadorComida adapter;
    String api_areas,fechadia,api_descanso;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    JSONObject jsonObject;
    ArrayList<Personas> personas;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comida);
        api_areas = getString(R.string.api_areas);
        api_descanso = getString(R.string.api_descansos);
        recicler = findViewById(R.id.recy_comida);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recicler.setLayoutManager(manager);
        recicler.setHasFixedSize(true);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        personas=new ArrayList<>();
        cargardatos();
    }

    public void cargardatos()
    {
        Log.d("cargalistado",preferences.getString("departamento","mal"));
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_fecha="+fechadia+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&v_turno="+preferences.getInt("turno",0)+"&bandera=0", null, new Response.Listener<JSONObject>() {
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
                            Personas help = new Personas();
                            help.setNombre(jsonObject.getString("nombre"));
                            help.setCedulas(jsonObject.getString("cedula"));

                            Log.d("lISTADO 123",jsonObject.getString("nombre"));
                            personas.add(help);
                        }
                        adapter = new AdaptadorComida(personas,api_descanso,preferences.getInt("id_usuario",0),+preferences.getInt("turno",0));
                        recicler.setAdapter(adapter);
                    }else
                        Toast.makeText(Comida.this,"Listado Vacio",Toast.LENGTH_SHORT).show();

                }catch (JSONException e)
                {
                    Log.d("123456","entro3"+e.toString());
                    //Toast.makeText(Comida.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(Comida.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
}