package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.telephony.mbms.StreamingServiceInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.frigoasistencias2.clases.Personas;
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
    String api_areas,fechadia,api_descanso,api_faltas,horamomento,fechamomento;
    ArrayList<Personas> personas;
    ArrayList <String> listanombres,listacedulas,listacedulasbanio;
    ArrayAdapter adapter;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    int contador;
    Button btn_regresarbanio,btn_ircomer,btn_faltas,btn_ingresomanual;
    EditText edit_buscar;
    Dialog alerta;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_diario);

        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        personas = new ArrayList<>();
        listViewdiaria = findViewById(R.id.list_diaria);
        txt_fecha = findViewById(R.id.txt_fechadiaria);
        api_areas = getString(R.string.api_areas);
        api_descanso = getString(R.string.api_descansos);
        api_faltas = getString(R.string.api_faltas);
        listanombres = new ArrayList<String>();
        listacedulas = new ArrayList<String>();
        listacedulasbanio = new ArrayList<>();
        edit_buscar = findViewById(R.id.edit_l_ingresos);
        txt_total = findViewById(R.id.txt_l_total);
        btn_regresarbanio = findViewById(R.id.btn_a_regresar);
        btn_ircomer = findViewById(R.id.btn_a_comer);
        btn_faltas = findViewById(R.id.btn_a_falta);
        btn_ingresomanual = findViewById(R.id.btn_a_manual);
        bd = new Managerbd(this, "Registro", null, R.string.versionbase);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        fechamomento = dateFormat2.format(date);
        horamomento = dateFormat1.format(date);
        Log.d("fechahora",fechamomento+":"+horamomento);
        cargardatos();
        txt_fecha.setText(fechadia);
        contador = 0;
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
                    for(int i =0;i<=listacedulas.size()-1;i++)
                    {
                        if(personas.get(i).getNombre().equals(listViewdiaria.getAdapter().getItem(posicion)))
                        {
                            Log.d("palabra",listacedulas.get(i));
                            Log.d("palabra3",personas.get(i).getNombre());
                            soltarusuario(personas.get(i).getCedulas());
                            //contador--;
                            //txt_total.setText(""+contador);
                            personas.remove(i);
                            adapter.notifyDataSetChanged();
                            return;
                        }else{
                            Log.d("palabra2","mal");
                        }
                    }

                    //soltarusuario(listacedulas.get(posicion));
                }
                });
                dialogo1.setNegativeButton("IR AL BAÑO", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) {
                    boolean bandera;
                    Log.d("soltar usuario",listViewdiaria.getAdapter().getItem(posicion)+"");
                    //banio(listacedulas.get(posicion),7);/*7 BAÑO IN 8 BAÑO OUT 9 CAOMIDA IN*/
                    //subirbase(listacedulas.get(posicion));
                    for(int i =0;i<=listacedulas.size()-1;i++)
                    {
                        if(personas.get(i).getNombre().equals(listViewdiaria.getAdapter().getItem(posicion)))
                        {
                           Log.d("palabra",listacedulas.get(i));
                           banio(personas.get(i).getCedulas(),7);
                        }else{
                            Log.d("palabra2","mal");
                        }
                    }
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

        btn_ircomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListadoDiario.this,Comida.class));
                /*AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ListadoDiario.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿Seguro va a enviar a comer?");
                dialogo1.setCancelable(false);
                dialogo1.setNeutralButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialogo1.setPositiveButton("SI", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                {
                    mandar_comer();
                }
                });
                dialogo1.show();*/
            }
        });

        btn_faltas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //revisar lo de super usuario para ingresar y habilitarlo al inicio del activity
                //pendiente
                startActivity(new Intent(ListadoDiario.this,GenerarRegistro.class));
            }
        });

        edit_buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0)
                    adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_ingresomanual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerta = new Dialog(ListadoDiario.this);
                alerta.setContentView(R.layout.alertdialog_cedula_manual);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    alerta.requireViewById(R.id.btn_alert_guardar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditText cedulamanual= alerta.requireViewById(R.id.edittext_alert_cedula);
                            if(cedulamanual.length() == 10)
                            {
                                procesarbanio(cedulamanual.getText().toString());
                            }
                            else
                                Toast.makeText(ListadoDiario.this, "Numeros Incompletos", Toast.LENGTH_LONG).show();

                            //subirbase(cedulamanual.getText().toString());
                            //Toast.makeText(RegistroAsistencia.this, "probando"+cedulamanual.getText(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                alerta.show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(ListadoDiario.this,RegistroAsistencia.class));
        finish();
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
            /*entradabanio(intentResult.getContents());
            llenarcedulasbanio(intentResult.getContents());*/
            procesarbanio(intentResult.getContents());
            if (intentResult.getContents() == null) {

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                    if(jsonArray.length() -1 > 0)
                    {
                        for(int i = 0;i<=jsonArray.length()-1;i++)
                        {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            Personas help = new Personas();
                            help.setNombre(jsonObject.getString("nombre"));
                            help.setCedulas(jsonObject.getString("cedula"));

                            Log.d("lISTADO",jsonObject.getString("nombre"));
                            listanombres.add(jsonObject.getString("nombre"));
                            listacedulas.add(jsonObject.getString("cedula"));
                            contador ++;
                            personas.add(help);
                        }
                        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,listanombres);
                        listViewdiaria.setAdapter(adapter);
                        txt_total.setText(""+contador);
                    }else
                        Toast.makeText(ListadoDiario.this,"Listado Vacio",Toast.LENGTH_SHORT).show();

                }catch (JSONException e)
                {
                    Log.d("123456","entro3"+e.toString());
                    Toast.makeText(ListadoDiario.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerrorlistadiario","dd"+error.toString());
                Toast.makeText(ListadoDiario.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void soltarusuario(String cedula)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        String fecha = dateFormat.format(date);
        Log.d("sacar",api_areas +"?fecha="+fecha+"&cedulas="+cedula+"&bandera=0");
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_areas +"?fecha="+fecha+"&cedula="+cedula+"&bandera=0", null, new Response.Listener<JSONObject>() {
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
    public void banio(String v_cedula , int v_estado) {
        String fechadiacabe;

        /*if(v_estado == 7 || v_estado == 9 )
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
            Date date = new Date();
            fechadiacabe = dateFormat.format(date);
        }
        else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
            Date date = new Date();
            fechadiacabe = dateFormat.format(date);
        }*/
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);

        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+preferences.getInt("id_usuario",0)+"&bandera=1",null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Log.d("123456789","dale"+jsonObject.getString("mensaje"));
                        Toast.makeText(ListadoDiario.this,jsonObject.getString("mensaje"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e)
                {
                    Log.d("DANIEL","entro3"+e.toString());
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
                    Toast.makeText(getBaseContext(), "Usuario ingresado de regreso", Toast.LENGTH_SHORT).show();

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
    public  void procesarbanio(String v_cedula)
    {
        banio(v_cedula,8);
        //ingresarusuariobanio(v_cedula);
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
        Cursor cursor = bdcache.rawQuery("Select cedula from t_descansos where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'"+"and estadobanio in ('S') ", null);
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
                banio(v_cedula,7);
            }
            else
                Toast.makeText(getBaseContext(), "Usuario ya salio", Toast.LENGTH_SHORT).show();
        }
    }
    public void ingresarusuariobanio(String v_cedula)
    {

        /*boolean ingresar; //true si false no
        ContentValues content = new ContentValues();
        if(v_cedula != null)
        {
            String fechadia;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
            Date date = new Date();
            fechadia = dateFormat.format(date);

            ingresar =buscarusuario(v_cedula);
            if(ingresar) {
                bdcache = bd.getWritableDatabase();
                bdcache.execSQL("update t_descansos set estadobanio = 'N' where cedula ='"+v_cedula+"' AND fechaingreso LIKE  " + "'%" + fechadia + "%'" + "   " );
                banio(v_cedula,8);
                Toast.makeText(getBaseContext(), "Usuario ingresado de regreso", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getBaseContext(), "Usuario no ha salido, dar primero permiso al baño", Toast.LENGTH_SHORT).show();
        }*/
    }
    public void mandar_comer()
    {
        Log.d("listadodiario123",listacedulas.size()+"");
        for (int z =0; z <= listacedulas.size()-1;z++)
        {
            Log.d("listadodiario123",listacedulas.get(z));
            banio(listacedulas.get(z),9);
        }
        Toast.makeText(ListadoDiario.this,"USUARIOS ENVIADOS A COMER",Toast.LENGTH_SHORT).show();
    }
}