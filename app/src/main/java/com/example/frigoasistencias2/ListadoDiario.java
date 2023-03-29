package com.example.frigoasistencias2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.bd.Managerbd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ListadoDiario extends AppCompatActivity {

    Managerbd bd;
    SQLiteDatabase bdcache;
    ListView listViewdiaria;
    TextView txt_fecha;
    String api_areas,fechadia;
    ArrayList <String> listanombres,listacedulas;
    ArrayAdapter adapter;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_diario);

        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        listViewdiaria = findViewById(R.id.list_diaria);
        txt_fecha = findViewById(R.id.txt_fechadiaria);
        api_areas = getString(R.string.api_areas);
        listanombres = new ArrayList<String>();
        listacedulas = new ArrayList<String>();
        bd = new Managerbd(this, "Registro", null, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        cargardatos();
        txt_fecha.setText(fechadia);

        listViewdiaria.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                final int posicion=i;
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ListadoDiario.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea soltar este usuario ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                {
                    Log.d("soltar usuario",listacedulas.get(posicion));
                    soltarusuario(listacedulas.get(posicion));
                }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) {
                    /*  */
                } });
                dialogo1.show();
                return false;
            }
        });

    }

    public void cargardatos()
    {
        Log.d("listado",preferences.getString("departamento","mal"));
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_areas +"?v_fecha="+fechadia+"&v_id_usuario="+preferences.getInt("id_usuario",0)+"&v_departamento="+preferences.getString("departamento","mal")+"&bandera=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Log.d("lISTADO DIARIO",jsonObject.getString("departamento"));
                        listanombres.add(jsonObject.getString("nombre")+":"+jsonObject.getString("departamento"));
                        listacedulas.add(jsonObject.getString("cedula"));
                    }
                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,listanombres);
                    listViewdiaria.setAdapter(adapter);
                }catch (JSONException e)
                {
                    Log.d("LISTADODIARIO","entro3"+e.toString());
                    Toast.makeText(ListadoDiario.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(ListadoDiario.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void soltarusuario(String cedula)
    {
        Log.d("sacar",api_areas +"?fecha="+fechadia+"&cedulas="+cedula+"&bandera=0");
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_areas +"?fecha="+fechadia+"&cedula="+cedula+"&bandera=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.d("estado",jsonObject.getString("estado" ));
                    if(jsonObject.getString("estado" ) != "bien")
                    {
                        actualizar(cedula,"C");
                        Toast.makeText(ListadoDiario.this,"usuario liberado",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ListadoDiario.this,"No se solto al usuario",Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e)
                {
                    Log.d("LISTADODIARIO","entro3"+e.toString());
                    Toast.makeText(ListadoDiario.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(ListadoDiario.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void actualizar(String v_cedula,String estado)
    {
        bdcache = bd.getWritableDatabase();
        bdcache.execSQL("update t_registro set estadosubido = '"+estado+"' where cedula ='"+v_cedula+"'" );
    }
}