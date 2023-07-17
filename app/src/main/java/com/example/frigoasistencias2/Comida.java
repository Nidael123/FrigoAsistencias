package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Comida extends AppCompatActivity implements SearchView.OnQueryTextListener{

    boolean estadobuscador;
    RecyclerView recicler;
    AdaptadorComida adapter;
    String api_areas,fechadia,api_descanso,fechamomento,horamomento;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    JSONObject jsonObject;
    ArrayList<Personas> personas;
    SearchView search_buscar;
    Button btn_manual,btn_escanar;
    int id_usuario;
    Dialog alerta;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comida);
        estadobuscador = false;
        api_areas = getString(R.string.api_areas);
        api_descanso = getString(R.string.api_descansos);
        recicler = findViewById(R.id.recy_comida);
        search_buscar = findViewById(R.id.search_c_buscar);
        btn_escanar = findViewById(R.id.btn_c_regresar);
        btn_manual = findViewById(R.id.btn_c_manual);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recicler.setLayoutManager(manager);
        recicler.setHasFixedSize(true);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        fechadia = dateFormat.format(date);
        fechamomento = dateFormat2.format(date);
        horamomento = dateFormat1.format(date);
        personas=new ArrayList<>();
        cargardatos();
        search_buscar.setOnQueryTextListener(this);
        id_usuario = preferences.getInt("id_usuario",0);

        btn_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerta = new Dialog(Comida
                        .this);
                alerta.setContentView(R.layout.alertdialog_cedula_manual);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    alerta.requireViewById(R.id.btn_alert_guardar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditText cedulamanual= alerta.requireViewById(R.id.edittext_alert_cedula);
                            if(cedulamanual.length() == 10)
                            {
                                subirbase(cedulamanual.getText().toString());
                            }
                            else
                                Toast.makeText(Comida.this, "Numeros Incompletos", Toast.LENGTH_LONG).show();

                            //subirbase(cedulamanual.getText().toString());
                            //Toast.makeText(RegistroAsistencia.this, "probando"+cedulamanual.getText(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                alerta.show();
            }
        });

        btn_escanar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                estadobuscador = false;
                Log.d("estadocomer3",estadobuscador+"");
                escanear();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            Log.d("subira base",""+intentResult.getContents());
            subirbase(intentResult.getContents());
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                escanear();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void subirbase(String v_cedula ) {
        int v_estado = 12;
        String fechadiacabe;
        Log.d("estadocomer5",estadobuscador+"");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        Log.d("subira base",""+api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+id_usuario+"&bandera=1");
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+id_usuario+"&bandera=1",null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("subira base",""+jsonArray.toString());
                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Log.d("123456789","dale"+jsonObject.toString());
                        Toast.makeText(Comida.this,jsonObject.getString("mensaje"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e)
                {
                    Log.d("DANIEL","entro3"+e.toString());
                    Toast.makeText(Comida.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                //Toast.makeText(.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show;
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void cargardatos()
    {
        Log.d("cargalistado",api_descanso +"?v_fecha="+fechadia+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&bandera=0&v_turno="+preferences.getInt("turno",0)+"&v_hora="+horamomento);
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_fecha="+fechadia+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&bandera=0&v_turno="+preferences.getInt("turno",0)+"&v_hora="+horamomento, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("revicioncomida",jsonArray.toString());
                    if(jsonArray.length() -1 > 0)
                    {
                        for(int i = 0;i<=jsonArray.length()-1;i++)
                        {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            Personas help = new Personas();
                            help.setNombre(jsonObject.getString("nombre"));
                            help.setCedulas(jsonObject.getString("cedula"));
                            //Log.d("lISTADO 123",help.get);
                            personas.add(help);
                            Log.d("lISTADO 12",personas.get(i).getCedulas());
                        }
                        Log.d("lISTADO 1",personas.get(0).getCedulas());
                        adapter = new AdaptadorComida(personas,api_descanso,preferences.getInt("id_usuario",0),preferences.getInt("turno",0));
                        recicler.setAdapter(adapter);
                    }else
                        Toast.makeText(Comida.this,"Listado Vacio",Toast.LENGTH_SHORT).show();

                }catch (JSONException e)
                {
                    Log.d("123456","entro3"+e.toString());
                    Toast.makeText(Comida.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerrorcomida",""+error.toString());
                Toast.makeText(Comida.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }

    public void escanear()
    {
        Log.d("estadocomer4",estadobuscador+"");
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
        Log.d("registros","escaneando");
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