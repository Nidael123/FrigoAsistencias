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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistroAsistencia extends AppCompatActivity implements View.OnClickListener {

    Button btn_escanear,btn_asistencia,btn_anadir,btn_guardar,btn_nuevo,btn_listado;
    TextView txt_fecha,txt_error,txt_turno,txt_cantidad;
    String api_asistencias, fechadia,fechaturno,jornada,api_descanso;
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Spinner departamentos;
    ArrayList<String> listadepartamentos,listanombres;
    ArrayList<String> cedulas,cedulaserror;
    Managerbd bd;
    SQLiteDatabase bdcache;
    RequestQueue n_requerimiento;
    JSONObject jsonObject;
    boolean avanzartransaccion = false,bandera = true;  //true avanza  bandera f = hay error en las alguna cedula
    ListView listacedulas;
    ArrayAdapter adapter;
    Integer turno;
    Boolean bandera1;//true no presento  -- false  presento

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_asistencia);
        btn_escanear =  findViewById(R.id.btn_escanear);
        btn_asistencia =  findViewById(R.id.btn_asistencias);
        btn_anadir =  findViewById(R.id.btn_salir);
        btn_guardar =  findViewById(R.id.btn_guardarregistroerror);
        btn_listado = findViewById(R.id.btn_listado);
        btn_nuevo =  findViewById(R.id.btn_nuevo);
        txt_fecha =  findViewById(R.id.txt_fecha);
        txt_error =  findViewById(R.id.txt_error_cedula2);
        txt_turno = findViewById(R.id.txt_turno);
        txt_cantidad = findViewById(R.id.txt_cantidad);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        editor = preferences.edit();
        txt_cantidad.setText("0");
        listadepartamentos = new ArrayList<String>();
        cedulas = new ArrayList<String>();
        listanombres = new ArrayList<String>();
        cedulaserror = new ArrayList<String>();
        departamentos = findViewById(R.id.spi_departamentos);
        btn_asistencia.setEnabled(false);
        btn_nuevo.setEnabled(false);
        bandera1 = false;
        bd = new Managerbd(this, "Registro", null, R.string.versionbase);
        api_asistencias = getString(R.string.api_aistencias);
        api_descanso = getString(R.string.api_descansos);
        Log.d("registros", "" + preferences.getInt("cant_depart", 0));
        listadepartamentos.add("Escoja una opcion");
        for (int i = 0; i <= preferences.getInt("cant_depart", 0); i++) {
            Log.d("registros", preferences.getString("depa" + i, "mal"));
            listadepartamentos.add(preferences.getString("depa" + i, "mal"));
        }
        departamentos.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, listadepartamentos));
        listacedulas = (ListView) findViewById(R.id.list_itemcedulaserror);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        txt_fecha.setText(preferences.getString("nombre_usuario", "mal"));

        final int OPEN_HOUR = 07; /* 0 - 23*/
        final int OPEN_MINUTE = 0; /* 0 - 59*/
        final int OPEN_SECOND = 0; /* 0 - 59*/

        /* 07:00 PM */
        final int CLOSED_HOUR = 19;
        final int CLOSED_MINUTE = 0;
        final int CLOSED_SECOND = 0;

        Calendar openHour = Calendar.getInstance();
        openHour.set(Calendar.HOUR_OF_DAY, OPEN_HOUR);
        openHour.set(Calendar.MINUTE, OPEN_MINUTE);
        openHour.set(Calendar.SECOND, OPEN_SECOND);

        Calendar closedHour = Calendar.getInstance();
        closedHour.set(Calendar.HOUR_OF_DAY, CLOSED_HOUR);
        closedHour.set(Calendar.MINUTE, CLOSED_MINUTE);
        closedHour.set(Calendar.SECOND, CLOSED_SECOND);

        Calendar now = Calendar.getInstance();

        if(now.after(openHour) && now.before(closedHour))
        {
            txt_turno.setText("Turno Dia");
            Log.d("tturno","dia");
            turno = 1;
        }else{
            txt_turno.setText("Turno Noche");
            turno = 2;
        }

        listacedulas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                final int posicion=i;
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(RegistroAsistencia.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Elimina este Poducto ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                {

                    cedulas.remove(posicion);
                    listanombres.remove(posicion);
                    adapter.notifyDataSetChanged();
                    txt_cantidad.setText(cedulas.size()+"");
                    actualizar(cedulas.get(posicion),"C");
                }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) { } });
                dialogo1.show();
                return false;
            }
        });
        btn_escanear.setOnClickListener(this);
        btn_asistencia.setOnClickListener(this);
        btn_anadir.setOnClickListener(this);
        btn_guardar.setOnClickListener(this);
        btn_nuevo.setOnClickListener(this);
        btn_listado.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_escanear:
                escanear();
                break;
            case R.id.btn_asistencias:
                startActivity(new Intent(RegistroAsistencia.this, CedulaError.class));
                finish();
                break;
            case R.id.btn_salir:
                escanear();
                break;
            case R.id.btn_guardarregistroerror:
                if(departamentos.getSelectedItemPosition() != 0) {
                    btn_guardar.setEnabled(true);
                    if(cedulas.size() > 0)
                    {
                        subirsistema();
                    }
                    else
                        Log.d("Boton guardar","si termina en le boton: "+cedulas.size() );
                }
                break;
            case R.id.btn_nuevo:
                startActivity(new Intent(RegistroAsistencia.this,RegistroAsistencia.class));
                finish();
                break;
            case R.id.btn_listado:
                if(departamentos.getSelectedItemPosition() != 0)
                {
                    editor.putString("departamento",departamentos.getSelectedItem().toString());
                    editor.commit();
                    startActivity(new Intent(RegistroAsistencia.this,ListadoDiario.class));
                }else{
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(RegistroAsistencia.this);
                    dialogo1.setTitle("Importante"); dialogo1.setMessage("Escoja una opcion primero");
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    { public void onClick(DialogInterface dialogo1, int id)
                    {  }
                    });
                    dialogo1.show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(cedulas.size() > 0 || cedulaserror.size() > 0)
        {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(RegistroAsistencia.this);
            dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea salir sin procesar los datos ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id)
            {
                cancelar_cedulas();
                finish();   }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id) { } });
            dialogo1.show();
        }
        else
            finish();
    }
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(RegistroAsistencia.this);
        dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea borrar todas las cedulas con errores ?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id)
        {        }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id) { } });
        dialogo1.show();
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            subirbase(intentResult.getContents());
            if (intentResult.getContents() == null) {
                llenarlist_view();
                Log.d("cantidad de cedulas:",""+cedulas.size());
                txt_cantidad.setText(cedulas.size()+"");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                for (int i = 0;i<=listanombres.size()-1;i++)
                {
                    Log.d("recorrido",listanombres.get(i)+"");
                }
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
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(RegistroAsistencia.this);
            dialogo1.setTitle("Importante"); dialogo1.setMessage("Escoja una opcion primero");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id)
            {  }
            });
            dialogo1.show();
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
                Toast.makeText(getBaseContext(), "Usuario ingresado", Toast.LENGTH_SHORT).show();
                llenarusuario(v_cedula);
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
        Cursor cursor = bdcache.rawQuery("Select cedula from t_registro where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'"+"and estadoeliminar in ('A') and  estadosubido <> 'C'", null);
        Log.d("estadoeliminar23",cursor.getCount()+"");
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
        btn_guardar.setEnabled(false);
        btn_escanear.setEnabled(false);
        btn_anadir.setEnabled(false);
        btn_nuevo.setEnabled(true);
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
                //buscar la cabecera
                buscarcabecera();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error al guardar cabecera",""+error.toString());
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
                parametros.put("v_turno",String.valueOf(turno));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(requerimiento);
    }
    public void buscarcabecera()
    {
        //AppController.getInstance().getRequestQueue().getCache().get(url).serverDate
        Log.d("buscarcabecera",""+preferences.getInt("id_usuario",0));
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+preferences.getInt("id_usuario",0)+"&v_fecha="+fechadia+"&v_estado=1&v_departamento="+departamentos.getSelectedItem().toString(), null, new Response.Listener<JSONObject>() {
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
                    Toast.makeText(RegistroAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(RegistroAsistencia.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public  void guardardetalle(Integer id_cabecera1,String cedula1,String fecha1){
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
                    Log.d("detalleerror",error.toString());
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
            requerimiento.setShouldCache(true);
            n_requerimiento.add(requerimiento);
    }

    public void validarcedula(String cedula,Integer id_cabecera,String fecha)
    {
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
                            cedulaserror.add(cedula);
                            //Toast.makeText(RegistroAsistencia.this,"Este usuario esta asignado en otra Area",Toast.LENGTH_SHORT).show();
                            //actualizar(cedula,"C");
                            actualizar(cedula,"N");
                            //eliminarcedula(cedula);
                            btn_asistencia.setEnabled(true);
                        }
                    }else {
                        guardar_error(cedula);
                        cedulaserror.add(cedula);
                        txt_error.setText("Error en una o varias cedulas por favor verifique en asistencia");
                        btn_asistencia.setEnabled(true);
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(RegistroAsistencia.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
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
        Log.d("Registro Asistencia","si carga");
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,listanombres);
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

    public void llenarusuario(String cedula)
    {
        final String[] estado = new String[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+cedula+"&v_fecha="+fechadia+"&v_estado=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    jsonObject = new JSONObject(jsonArray.get(0).toString());

                    estado[0] = jsonObject.getString("nombre");
                    Log.d("Lista nombre",jsonObject.getString("nombre"));
                    listanombres.add(cedula +"  :   "+jsonObject.getString("nombre"));
                    Log.d("Lista nombre",cedula +"  :   "+jsonObject.getString("nombre"));

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

    public void cancelar_cedulas()
    {
        for (int i =0;i<=cedulas.size()-1;i++)
        {
            actualizar(cedulas.get(i),"C");
            actualizar2(cedulas.get(i),"C");
        }
    }
    public void actualizar2(String v_cedula,String estado)
    {
        bdcache = bd.getWritableDatabase();
        bdcache.execSQL("update t_registro set estadoeliminar = '"+estado+"' where cedula ='"+v_cedula+"'" );
    }
/*
    public void eliminarcedula(String v_cedula)
    {
        for(int i = 0;i<=cedulaserror.size()-1;i++)
        {
            if(cedulaserror.get(i)==v_cedula)
            {
                cedulaserror.remove(i);
            }
        }
        for(int i = 0;i<=cedulas.size()-1;i++)
        {
            if(cedulas.get(i)==v_cedula)
            {
                cedulas.remove(i);
            }
            adapter.notifyDataSetChanged();
        }
    }*/
}