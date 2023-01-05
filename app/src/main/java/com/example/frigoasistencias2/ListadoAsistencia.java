package com.example.frigoasistencias2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import com.example.frigoasistencias2.clases.datoscedula;
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

public class ListadoAsistencia extends AppCompatActivity {

    Managerbd bd;
    SQLiteDatabase bdcache;
    ListView listacedulas;
    //RecyclerView recycler;
    public ArrayList<datoscedula> cedulas;
    ArrayList<String> datoscedulas;
    //AdaptadorRecycler adapter;
    ArrayAdapter adapter;
    Button btn_anadir,btn_guardar;
    TextView txt_estadoguardar,txt_nombreusuario;
    private SharedPreferences preferences;
    boolean bandera; //  en el caso de asistencia 0 guardo   1 mando error y no guardo
    boolean bandera2; //en el caso de que el usuario este registrado en algun otro lugar
    Bundle parametros;
    String api_aistencia;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_asistencia);

        datoscedulas = new ArrayList<>();
        preferences = getSharedPreferences("datosapp",MODE_PRIVATE);
        bandera=bandera2=false;
        txt_estadoguardar = (TextView) findViewById(R.id.txt_estadoguardar);
        txt_nombreusuario = (TextView) findViewById(R.id.txt_fecha);
        btn_anadir = (Button) findViewById(R.id.btn_anadir);
        btn_guardar = (Button)findViewById(R.id.btn_guardar);
        bd = new Managerbd(this,"Registro",null,1);
        bdcache = bd.getReadableDatabase();
        listacedulas = (ListView) findViewById(R.id.lista_cedulas);
        //recycler = (RecyclerView)findViewById(R.id.recicler_cedulas);
        cedulas = new ArrayList<datoscedula>();
        //instaciar el adaptador y enviar la lista
        //el recicler .setadapter(adaptador)
        api_aistencia = getString(R.string.api_aistencias);
        parametros = this.getIntent().getExtras();
        txt_nombreusuario.setText(preferences.getString("nombre_usuario","mal"));
        Log.d("listado",parametros.getString("fecha"));
        llenararray();

        //adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,datoscedulas);
        Log.d("datoscedulamain",datoscedulas.size()+"");
        //adapter = new AdaptadorRecycler(cedulas);
        //listacedulas.setAdapter(adapter);
        //recycler.setAdapter(adapter);
        //recycler.setLayoutManager(new LinearLayoutManager(this));

        //validar_guardado();

        btn_anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escanear();
            }
        });

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //guardar en la base de datos
                if(bandera = true)
                {
                    txt_estadoguardar.setText("Sin Novedad");
                    guardar_base();
                    Toast.makeText(getBaseContext(), "Guardando........", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    txt_estadoguardar.setText("Error el usuario no ha marcado valide si lo acepta o elimine el usuario");
                    Toast.makeText(getBaseContext(), "Error al Guardar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listacedulas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int posicion = i;
                Log.d("datoscedulamaininicio",datoscedulas.size()+"");
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ListadoAsistencia.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Elimina este Usuario ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                { datoscedulas.remove(posicion);
                    adapter.notifyDataSetChanged(); }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) { } });
                dialogo1.show();
                Log.d("datoscedulamainfin",datoscedulas.size()+"");
                return false;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {

            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
            else
            {
                subirbase(intentResult.getContents());
                llenararray();
                adapter.notifyDataSetChanged();
                Toast.makeText(getBaseContext(), ""+intentResult.getContents(), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void subirbase(String v_cedula)
    {
        boolean ingresar;
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
                //BUSAQUEDA DE LOS DATOS DEL USUARIO
                content.put("estado", "OK");
                bdcache.insert("t_registro", null, content);
                Toast.makeText(getBaseContext(), "Insertado", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getBaseContext(), "Usuario ya ingresado", Toast.LENGTH_SHORT).show();
        }
    }
    public void escanear()
    {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }
    public void llenararray()
    {
        Cursor cursor = bdcache.rawQuery("Select * from t_registro where fechaingreso like "+"'%"+parametros.getString("fecha")+"%'",null);
        if(cursor.moveToFirst())
        {
            do {
                Log.d("estado",cursor.getString(6)+"");
                datosbase(cursor.getString(2));
            }while(cursor.moveToNext());
        }
        Log.d("datoscedulamain",datoscedulas.size()+"");
    }
    public void datosbase(String v_cedula)
    {

        String fechadia;
        //datoscedula help1 = new datoscedula();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_aistencia+"?v_usuario="+v_cedula+"&v_fecha"+fechadia, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.d("datosbase",jsonObject.toString());

                    datoscedulas.add(v_cedula+""+ jsonObject.getString("nombre")+jsonObject.getString("areatrabajo"));
                    /*help1.setMarcado(jsonObject.getString("areatrabajo"));
                    help1.setNombre(jsonObject.getString("nombre"));
                    help1.setCedula(cedula);
                    help1.setFecha(fecha);
                    help1.setEstado(estado);
                    Log.d("retorno",help1.getCedula());*/
                    /*switch (posicion)
                    {
                        case 1:
                            estado = jsonObject.getString("nombre").toString();
                            Log.d("Listado",estado[0]);
                            break;
                        case 2:
                            estado[0] = jsonObject.getString("estado");
                            Log.d("Listado",estado[0]);
                            break;
                        case 3:
                            estado[0] = jsonObject.getString("areatrabajo");
                            Log.d("Listado",estado[0]);
                            break;
                    }


                    if(jsonObject.getInt("id_usuario") == 0)
                    {
                        Toast.makeText(Login.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","usuario no valido" );
                    }
                    else
                    {
                        for (int i = 0;i<=jsonArray.length()-1;i++ ) {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            editor.putInt("id_usuario",jsonObject.getInt("id_usuario"));
                            editor.putString("depa"+i,jsonObject.getString("departamento"));
                            Log.d("login",""+jsonObject.getString("departamento"));
                        }
                        editor.putInt("cant_depart", jsonArray.length()-1);
                        editor.commit();
                        Toast.makeText(Login.this,"Ingreso con Exitoso",Toast.LENGTH_SHORT);
                        Log.d("logeo",""+help.length);
                        startActivity(new Intent(Login.this,RegistroAsistencia.class));
                    }*/
                    //Log.d("retorno1",help1.getCedula());
                    //datoscedulas.add(help1.getNombre());

                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,datoscedulas);
                    listacedulas.setAdapter(adapter);
                }catch (JSONException e)
                {
                    Log.d("Listado","entro3"+e.toString());
                    Toast.makeText(ListadoAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(ListadoAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
    public void llenar()
    {
        datoscedula help ;
        Cursor cursor = bdcache.rawQuery("Select * from t_registro where fechaingreso like "+"'%"+parametros.getString("fecha")+"%'",null);
        if(cursor.moveToFirst())
        {
            do {
                try {
                    help = usuarioocupado(cursor.getString(2),cursor.getString(3),cursor.getString(5));
                    Log.d("retorno antes",help.getCedula());
                    //datoscedulas.add(cursor.getString(3).toString() + help.getNombre() + help.getEstado());
                    cedulas.add(help);
                    Log.d("datoscedulastamaño",cedulas.size()+"");
                    if (help.getEstado() != "OK") {
                        bandera = false;
                    }
                }catch (Exception e)
                {
                    Log.d("logeo","entro4  "+e.toString());
                    Toast.makeText(ListadoAsistencia.this,"Error de coneccion consulte con sistemas"+e.toString(),Toast.LENGTH_SHORT);
                }
                Log.d("retorno cedula",cedulas.size()+"");
            }while(cursor.moveToNext());
        }
    }
    public boolean buscarusuario(String v_cedula)
    {
        boolean ingresar;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        bdcache = bd.getReadableDatabase();
        Log.d("ID_usuario existente",""+v_cedula);
        Log.d("ID_usuario existente",""+fechadia);
        Cursor cursor = bdcache.rawQuery("Select cedula from t_registro where cedula like "+"'%"+v_cedula+"%'"+" and fechaingreso like "+"'%"+fechadia+"%' and id_libro = "+preferences.getInt("id_libro", 0),null);
        Log.d("ID_usuario existente",""+cursor.getCount());

        if (cursor.getCount()>0)
        {
            ingresar = false;
            Log.d("ID_usuario existente", "error");
        }
        else
        {
            ingresar = true;
            Log.d("ID_usuario existente", "continuo");
        }
        return ingresar;
    }
    public datoscedula usuarioocupado(String cedula,String fecha,String estado)
    {
        //1 saco el nombre 2 saco el es estado si = ok es decir marco ,3 el area libre en caso de no ser asignado
        //llamar a la funcion que valida si el usuario esta en otra area llama tambien para lenar los datos del usuario
        String fechadia;
        datoscedula help1 = new datoscedula();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_aistencia+"?v_usuario="+cedula+"&v_fecha"+fechadia+"&v_estado=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());

                    help1.setMarcado(jsonObject.getString("areatrabajo"));
                    help1.setNombre(jsonObject.getString("nombre"));
                    help1.setCedula(cedula);
                    help1.setFecha(fecha);
                    help1.setEstado(estado);
                    Log.d("retorno",help1.getCedula());
                    /*switch (posicion)
                    {
                        case 1:
                            estado = jsonObject.getString("nombre").toString();
                            Log.d("Listado",estado[0]);
                            break;
                        case 2:
                            estado[0] = jsonObject.getString("estado");
                            Log.d("Listado",estado[0]);
                            break;
                        case 3:
                            estado[0] = jsonObject.getString("areatrabajo");
                            Log.d("Listado",estado[0]);
                            break;
                    }


                    if(jsonObject.getInt("id_usuario") == 0)
                    {
                        Toast.makeText(Login.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","usuario no valido" );
                    }
                    else
                    {
                        for (int i = 0;i<=jsonArray.length()-1;i++ ) {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            editor.putInt("id_usuario",jsonObject.getInt("id_usuario"));
                            editor.putString("depa"+i,jsonObject.getString("departamento"));
                            Log.d("login",""+jsonObject.getString("departamento"));
                        }
                        editor.putInt("cant_depart", jsonArray.length()-1);
                        editor.commit();
                        Toast.makeText(Login.this,"Ingreso con Exitoso",Toast.LENGTH_SHORT);
                        Log.d("logeo",""+help.length);
                        startActivity(new Intent(Login.this,RegistroAsistencia.class));
                    }*/
                    Log.d("retorno1",help1.getCedula());
                    datoscedulas.add(help1.getNombre());
                    Log.d("datoscedula2",datoscedulas.size()+"");
                }catch (JSONException e)
                {
                    Log.d("Listado","entro3"+e.toString());
                    Toast.makeText(ListadoAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(ListadoAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
        Log.d("retorno",help1.getCedula());
        return help1;
    }
    public void guardar_base()
    {
        boolean estado = true;
        if (estado )
        {
            //guardar cabecera
            guardarcabecera();
            estado = false;
        }
    }
    public void guardarcabecera()
    {
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_aistencia, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ListadoAsistencia.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
                //buscar la cabecera
                buscarcabecera();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoAsistencia.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();

                parametros.put("v_usuario", String.valueOf(preferences.getInt("id_usuario",0)));
                parametros.put("v_departamento","DESCABEZADO");
                parametros.put("v_fechaingreso", String.valueOf(201));
                parametros.put("v_estado", String.valueOf(0));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(requerimiento);
    }

    public void buscarcabecera()
    {
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_aistencia +"?v_usuario=6"+"&v_fecha=201"+"&v_estado=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    if(jsonObject.getInt("id_cabecera") == 0)
                    {
                        Toast.makeText(ListadoAsistencia.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","usuario no valido" );
                    }
                    else
                    {
                        for (int i = 0 ;i<= datoscedulas.size()-1;i++)
                        {
                            Log.d("guardar detalle","usuario no valido" );
                            guardardetalle(69,"099999999","2022-12-21");
                        }
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(ListadoAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(ListadoAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
    public  void guardardetalle(Integer id_cabecera1,String cedula1,String fecha1){
        Log.d("Gurdardetalle",""+cedula1+":"+id_cabecera1);

        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_aistencia, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ListadoAsistencia.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoAsistencia.this,"error guardado",Toast.LENGTH_LONG).show();
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
    }

/*
    public void guardadetalle(String id_producto,int cantidad)
    {
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_usuario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(agregarproductos.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("v_fechaingreso", fechadia);
                parametros.put("v_id_producto", id_producto);
                parametros.put("v_cantidad", Integer.toString(cantidad));
                parametros.put("v_fecgaingresovalidar", fechadia);
                parametros.put("v_estado", Integer.toString(estado));

                return parametros;
            }
        };
        n_requeriminto = Volley.newRequestQueue(this);
        n_requeriminto.add(requerimiento);
    }*/
    }
/*
    public void guardarcabecera()
    {
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_usuario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(RegistroAsistencia.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
                //revisar que arroja el string
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

                parametros.put("v_fechacabecera", fechadia);
                parametros.put("v_id_solicitante", preferences.getString("id_usuario","1"));
                parametros.put("v_id_area",preferences.getString("id_area","Error"));//cambiar junto con los datos del usuario
                parametros.put("v_fechaingreso", fechadia);
                parametros.put("v_observaciones", observaciones.getText().toString());
                parametros.put("v_fecgaingresovalidar", fechadia);
                parametros.put("v_estado", Integer.toString(estado));
                return parametros;
            }
        };
        n_requeriminto = Volley.newRequestQueue(this);
        n_requeriminto.add(requerimiento);
    }

    public void guardarusuarios(String id_producto,int cantidad)
    {
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_usuario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(agregarproductos.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("v_fechaingreso", fechadia);
                parametros.put("v_id_producto", id_producto);
                parametros.put("v_cantidad", Integer.toString(cantidad));
                parametros.put("v_fecgaingresovalidar", fechadia);
                parametros.put("v_estado", Integer.toString(estado));

                return parametros;
            }
        };
        n_requeriminto = Volley.newRequestQueue(this);
        n_requeriminto.add(requerimiento);
    }
  */

