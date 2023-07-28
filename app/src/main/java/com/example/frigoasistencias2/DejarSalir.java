package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class DejarSalir extends AppCompatActivity {

    Button btn_escanear, btn_ingresomanual;
    TextView txt_cantidad;
    RequestQueue n_requerimiento;
    JSONObject jsonObject;
    String api_descanso,api_areas,fechadia,fechamomento,horamomento;
    SharedPreferences preferences;
    int id_usuario,contador;
    Dialog alerta;
    ListView listViewdiaria;
    ArrayAdapter adapter;
    ArrayList<String> listanombres,listacedulas,listacedulasbanio;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dejar_salir);
        btn_escanear = findViewById(R.id.btn_s_escanear);
        btn_ingresomanual = findViewById(R.id.btn_s_ingresomanual);
        txt_cantidad = findViewById(R.id.txt_s_cantidad);
        api_descanso = getString(R.string.api_descansos);
        api_areas = getString(R.string.api_areas);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        id_usuario = preferences.getInt("id_usuario",0);
        listViewdiaria = findViewById(R.id.list_s_nombres);
        listanombres = new ArrayList<String>();
        listacedulas = new ArrayList<String>();
        contador=0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        fechamomento = dateFormat2.format(date);
        horamomento = dateFormat1.format(date);
        cargardatos();
        btn_escanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escanear();
            }
        });
        btn_ingresomanual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerta = new Dialog(DejarSalir.this);
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
                                Toast.makeText(DejarSalir.this, "Numeros Incompletos", Toast.LENGTH_LONG).show();

                            //subirbase(cedulamanual.getText().toString());
                            //Toast.makeText(RegistroAsistencia.this, "probando"+cedulamanual.getText(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                alerta.show();
            }
        });
    }

    public void cargardatos()
    {
        Log.d("cargalistado",preferences.getInt("turno",0)+"v");
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_areas +"?v_fecha="+fechamomento+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&bandera=1&v_turno="+preferences.getInt("turno",0)+"&v_hora="+horamomento, null, new Response.Listener<JSONObject>() {
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
                                Toast.makeText(DejarSalir.this,"Listado Vacio",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Personas help = new Personas();
                                help.setNombre(jsonObject.getString("nombre"));
                                help.setCedulas(jsonObject.getString("cedula"));

                                Log.d("lISTADO",jsonObject.getString("nombre"));
                                listanombres.add(jsonObject.getString("nombre"));
                                listacedulas.add(jsonObject.getString("cedula"));
                                contador ++;
                            }
                        }
                        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,listanombres);
                        listViewdiaria.setAdapter(adapter);
                        txt_cantidad.setText(""+contador);
                    }else
                        Toast.makeText(DejarSalir.this,"Listado Vacio",Toast.LENGTH_SHORT).show();

                }catch (JSONException e)
                {
                    Log.d("123456","entro3"+e.toString());
                    Toast.makeText(DejarSalir.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerrorlistadiario","dd"+error.toString());
                Toast.makeText(DejarSalir.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void escanear()
    {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
        Log.d("registros","escaneando");
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
        String fechadiacabe,horamomento;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        horamomento =dateFormat1.format(date);
        Log.d("subira base",""+api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+id_usuario+"&bandera=1&v_turno="+preferences.getInt("turno",0)+"&v_hora="+horamomento);
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+id_usuario+"&bandera=1&v_turno="+preferences.getInt("turno",0)+"&v_hora="+horamomento,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("subira base",""+jsonArray.toString());
                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Log.d("123456789","dale"+jsonObject.toString());
                        Toast.makeText(DejarSalir.this,jsonObject.getString("mensaje"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e)
                {
                    Log.d("DANIEL","entro3"+e.toString());
                    Toast.makeText(DejarSalir.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
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
}