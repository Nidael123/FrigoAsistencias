package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

public class RegistroAsistencia extends AppCompatActivity implements View.OnClickListener {

    Button btn_escanear,btn_asistencia,btn_anadir,btn_guardar;
    TextView txt_fecha,txt_error;
    String api_asistencias, fechadia;;
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Spinner departamentos;
    ArrayList<String> listadepartamentos;
    ArrayList<String> cedulas,cedulaserror;
    Managerbd bd;
    SQLiteDatabase bdcache;
    RequestQueue n_requerimiento;
    JSONObject jsonObject;
    boolean avanzartransaccion = false,bandera = true;  //true avanza  bandera f = hay error en las alguna cedula
    ListView listacedulas;
    ArrayAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_asistencia);
        btn_escanear = (Button) findViewById(R.id.btn_escanear);
        btn_asistencia =(Button) findViewById(R.id.btn_asistencias);
        btn_anadir = (Button)findViewById(R.id.txt_cancelar);
        btn_guardar = (Button)findViewById(R.id.btn_guardarregistroerror);
        txt_fecha = (TextView)findViewById(R.id.txt_fecha);
        txt_error = (TextView)findViewById(R.id.txt_error_cedula2);
        preferences = getSharedPreferences("infousuario",MODE_PRIVATE);
        editor=preferences.edit();
        listadepartamentos = new ArrayList<String>();
        cedulas = new ArrayList<String>();
        cedulaserror = new ArrayList<String>();
        departamentos = findViewById(R.id.spi_departamentos);
        bd = new Managerbd(this,"Registro",null,1);
        api_asistencias = getString(R.string.api_aistencias);
        Log.d("registros",""+preferences.getInt("cant_depart",0));
        listadepartamentos.add("Escoja una opcion");
        for(int i = 0;i<=preferences.getInt("cant_depart",0);i++)
        {
            Log.d("registros",preferences.getString("depa"+i,"mal"));
            listadepartamentos.add(preferences.getString("depa"+i,"mal"));
        }
        departamentos.setAdapter(new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_dropdown_item,listadepartamentos));
        listacedulas = (ListView) findViewById(R.id.list_itemcedulaserror);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        txt_fecha.setText(preferences.getString("nombre_usuario","mal"));

        btn_escanear.setOnClickListener(this);
        btn_asistencia.setOnClickListener(this);
        btn_anadir.setOnClickListener(this);
        btn_guardar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_escanear:
                escanear();
                break;
            case R.id.btn_asistencias:
                startActivity(new Intent(RegistroAsistencia.this, CedulaError.class));
                break;
            case R.id.txt_cancelar:
                escanear();
                break;
            case R.id.btn_guardarregistroerror:
                if(departamentos.getSelectedItemPosition() != 0) {
                    Log.d("spiner",""+departamentos.getSelectedItem().toString());
                    subirsistema();
                    Log.d("Boton guardar","si termina en le boton: "+cedulaserror.size() );
                    /*
                    if(cedulaserror.size() > 0)
                    {
                        startActivity(new Intent(RegistroAsistencia.this,CedulaError.class));
                    }
                    else{
                        //finalizo
                        Toast.makeText(this,"Guardado Completo",Toast.LENGTH_LONG);
                        Log.d("TERMINO","ALFIN");
                    }*/
                }
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            subirbase(intentResult.getContents());
            if (intentResult.getContents() == null) {
                llenarlist_view();
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                escanear();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void escanear()
    {
        if(departamentos.getSelectedItemPosition() != 0)
        {
            departamentos.setEnabled(false);
            Log.d("registros","escaneando");
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Scan a barcode or QR Code");
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();
            Log.d("registros","escaneando");
        }else{
            Toast.makeText(RegistroAsistencia.this,"Por favor escoja un departamento",Toast.LENGTH_SHORT);
        }
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
            if(ingresar) {
                content.put("id_libro", preferences.getInt("id_libro", 0));
                content.put("cedula", v_cedula);
                content.put("fechaingreso", fechadia);
                content.put("estado", "OK");
                bdcache.insert("t_registro", null, content);
                Toast.makeText(getBaseContext(), "Insertado", Toast.LENGTH_SHORT).show();
                cedulas.add(v_cedula);
            }
            else
                Toast.makeText(getBaseContext(), "Usuario ya ingresado", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean buscarusuario(String v_cedula) {
        boolean ingresar;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select cedula from t_registro where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'", null);
        if (cursor.getCount() > 0) {
            ingresar = false;
        } else {
            ingresar = true;
        }
        return ingresar;
    }
    public String buscarusuarioxhora(String v_cedula) {
        String hora;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select fechaingreso from t_registro where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'", null);
        Log.d("buscarusuarioxhora",cursor.getCount()+"");
        if (cursor.moveToFirst()) {
            hora = cursor.getString(0);
        } else {
            hora = "error";
        }
        return hora;
    }
    public void subirsistema()
    {
        guardarcabecera();
        for (int i =0;i<=cedulaserror.size()-1;i++)
        {
            Log.d("cedulas",cedulaserror.get(i));
        }
        Log.d("bandera",""+bandera);
    }
    public void guardarcabecera()
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);

        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_asistencias, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(RegistroAsistencia.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
                //buscar la cabecera
                buscarcabecera();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegistroAsistencia.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();

                parametros.put("v_usuario", String.valueOf(preferences.getInt("id_usuario",0)));
                parametros.put("v_departamento",departamentos.getSelectedItem().toString());
                parametros.put("v_fechaingreso", fechadiacabe);
                parametros.put("v_estado", String.valueOf(0));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        requerimiento.setShouldCache(false);
        n_requerimiento.add(requerimiento);
    }
    public void buscarcabecera()
    {
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        Log.d("buscarcabecera",""+preferences.getInt("id_usuario",0));
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+preferences.getInt("id_usuario",0)+"&v_fecha="+fechadia+"&v_estado=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.d("id_cabecera",""+jsonObject.getInt("id_cabecera"));
                    editor.putInt("id_cabecera",jsonObject.getInt("id_cabecera"));
                    editor.commit();
                    if(jsonObject.getInt("id_cabecera") != 0)
                    {
                        Log.d("guardar detalle","usuario no valido" );
                        Log.d("guardar detalle",cedulas.size()-1+"" );

                        for (int i = 0 ;i<= cedulas.size()-1;i++)
                        {
                            Log.d("guardar detalle","usuario no valido" );
                            validarcedula(cedulas.get(i),jsonObject.getInt("id_cabecera"),buscarusuarioxhora(cedulas.get(i)));
                            //guardardetalle(jsonObject.getInt("id_cabecera"),cedulas.get(i),buscarusuarioxhora(cedulas.get(i)));
                            Log.d("guardar detalle","numero cedula" );
                        }
                    }
                    else
                    {
                        Toast.makeText(RegistroAsistencia.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","no hay cabecera" );
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(RegistroAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(RegistroAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(false);
        n_requerimiento.add(json);
    }
    public  void guardardetalle(Integer id_cabecera1,String cedula1,String fecha1){
        Log.d("Gurdardetalle",""+cedula1+":"+id_cabecera1);
            StringRequest requerimiento = new StringRequest(Request.Method.POST, api_asistencias, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(RegistroAsistencia.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
                    actualizar(cedula1,"S");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RegistroAsistencia.this,"error guardado",Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parametros = new HashMap<>();
                    parametros.put("v_numerocedula", cedula1);
                    parametros.put("v_fechaingreso", fecha1);
                    parametros.put("v_id_cabecera",String.valueOf(id_cabecera1) );
                    parametros.put("v_estado", String.valueOf(1));
                    return parametros;
                }
            };
            n_requerimiento = Volley.newRequestQueue(this);
            n_requerimiento.add(requerimiento);
        /*}
        else
        {
            cedulaserror.add(cedula1);
            Log.d("cedulas error",""+cedulaserror.size());
            //for(int i = 0;i<=cedulaserror.size()-1)
        }*/
    }

    public void validarcedula(String cedula,Integer id_cabecera,String fecha)
    {
        //validarcedula(cedulas.get(i),jsonObject.getInt("id_cabecera"),buscarusuarioxhora(cedulas.get(i)));
        Log.d("",avanzartransaccion+"");
        final int[] estado = new int[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+cedula+"&v_fecha="+fechadia+"&v_estado=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    estado[0] = jsonObject.getInt("estado");
                    Log.d("revisar", estado[0]+"dato");
                    if(estado[0] == 1)
                    {
                        Log.d("VALIDAR USUARIO","SI MARCO" );
                        if (jsonObject.getString("areatrabajo").contains("LIBRE"))
                        {
                            Log.d("VALIDAR USUARIO","ESTA LIBRE" );
                            guardardetalle(id_cabecera,cedula,fecha);
                        }else{
                            Log.d("VALIDAR USUARIO","no ESTA LIBRE" );
                            guardar_error(cedula);
                            txt_error.setText("Error en una o varias cedulas por favor verifique en asistencia");
                        }
                    }else {
                        guardar_error(cedula);
                        txt_error.setText("Error en una o varias cedulas por favor verifique en asistencia");
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(RegistroAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(RegistroAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }

    public void llenarlist_view()
    {
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,cedulas);
        listacedulas.setAdapter(adapter);
    }

    public void actualizar(String v_cedula,String estado)
    {
        bdcache = bd.getWritableDatabase();
        bdcache.execSQL("update t_registro set estadosubido = '"+estado+"' where cedula ='"+v_cedula+"'" );
    }

    public void guardar_error(String cedulas)
    {
        Log.d("cedulas error", "estoy en la funcion");
        actualizar(cedulas,"E");
        bandera = false;
        Log.d("bandera",""+bandera);
    }
}