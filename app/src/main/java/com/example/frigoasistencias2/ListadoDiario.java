package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.mbms.StreamingServiceInfo;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
    TextView txt_fecha,txt_total;
    String api_areas,fechadia,api_descanso;
    ArrayList <String> listanombres,listacedulas,listacedulasbanio;
    ArrayAdapter adapter;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    int contador;
    Button btn_regresarbanio;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_diario);

        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        listViewdiaria = findViewById(R.id.list_diaria);
        txt_fecha = findViewById(R.id.txt_fechadiaria);
        api_areas = getString(R.string.api_areas);
        api_descanso = getString(R.string.api_descansos);
        listanombres = new ArrayList<String>();
        listacedulas = new ArrayList<String>();
        listacedulasbanio = new ArrayList<>();
        txt_total = findViewById(R.id.txt_l_total);
        btn_regresarbanio = findViewById(R.id.btn_a_regresar);
        bd = new Managerbd(this, "Registro", null, R.string.versionbase);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        cargardatos();
        txt_fecha.setText(fechadia);
        contador = 1;
        listViewdiaria.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                final int posicion=i;
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ListadoDiario.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿Que Deseea hacer con este usuario?");
                dialogo1.setCancelable(false);
                dialogo1.setNeutralButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialogo1.setPositiveButton("SOLTAR", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                {
                    Log.d("soltar usuario",listacedulas.get(posicion));
                    //soltarusuario(listacedulas.get(posicion));
                    contador--;
                    txt_total.setText(""+contador);
                }
                });
                dialogo1.setNegativeButton("IR AL BAÑO", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) {
                    boolean bandera;

                    //banio(listacedulas.get(posicion),7);/*7 BAÑO IN 8 BAÑO OUT 9 CAOMIDA IN*/
                    subirbase(listacedulas.get(posicion));

                } });
                dialogo1.show();
                return false;
            }
        });

        btn_regresarbanio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escanear();
            }
        });
    }
    public void  escanear(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            /*entradabanio(intentResult.getContents());*/
            llenarcedulasbanio(intentResult.getContents());
            if (intentResult.getContents() == null) {
                procesarbanio();
            } else {
                escanear();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                        contador ++;
                    }
                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,listanombres);
                    listViewdiaria.setAdapter(adapter);
                    txt_total.setText(""+contador);
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
    /*public void banio(String v_cedula , int v_estado)
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_descanso, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ListadoDiario.this,"Permiso Concedido",Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error al guardar cabecera",""+error.toString());
                Toast.makeText(ListadoDiario.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();

                parametros.put("v_cedula", v_cedula);
                parametros.put("v_fecha", fechadiacabe);
                parametros.put("v_estado", String.valueOf(v_estado));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(requerimiento);
    }*/
    public void banio(String v_cedula , int v_estado)
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);

        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

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

    public void entradabanio(String v_cedula){
        Log.d("banio",v_cedula);
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado=8",null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

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
    public void llenarcedulasbanio(String cedula)
    {
        Boolean bandera ; //true avanza false no guarda
        bandera = buscarusuario(cedula);

        if(bandera = true)
        {
            listacedulasbanio.add(cedula);
        }
        else{
            Toast.makeText(ListadoDiario.this,"Este usuario ya Entro",Toast.LENGTH_LONG).show();
        }

    }
    public  void procesarbanio()
    {
        for(int i =0;i<=listacedulasbanio.size()-1;i++)
        {
            if(listacedulasbanio.get(i)!=null)
            {
                subirbase(listacedulasbanio.get(i));
            }
        }
    }
    public void actualizarbanio(String v_cedula,String estado)
    {
        bdcache = bd.getWritableDatabase();
        bdcache.execSQL("update t_registro set estadobanio = '"+estado+"' where cedula ='"+v_cedula+"'" );
    }

    public boolean buscarusuario(String v_cedula) {
        boolean ingresar;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select cedula from t_descansos where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'"+"and estadobanio in ('N') ", null);
        Log.d("estadoeliminar23",cursor.getCount()+"");
        if (cursor.getCount() > 0) {
            ingresar = true;
        } else {
            ingresar = false;
        }
        return ingresar;
    }
    public void subirbase(String v_cedula)
    {
        boolean ingresar; //true si false no
        ContentValues content = new ContentValues();
        if(v_cedula != null)
        {
            String fechadia;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
            Date date = new Date();
            fechadia = dateFormat.format(date);

            ingresar =buscarusuario(v_cedula);
            if(ingresar == false) {
                content.put("cedula", v_cedula);
                content.put("fechaingreso", fechadia);
                content.put("estadobanio", "S");
                bdcache.insert("t_descansos", null, content);
                Toast.makeText(getBaseContext(), "Permiso de salida", Toast.LENGTH_SHORT).show();
                //entradabanio(v_cedula); si vale
            }
            else
                Toast.makeText(getBaseContext(), "Usuario ya salio", Toast.LENGTH_SHORT).show();
        }
    }
}