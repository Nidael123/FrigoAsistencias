package com.example.frigoasistencias2;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CedulaError extends AppCompatActivity {

    Managerbd bd;
    SQLiteDatabase bdcache;
    ArrayList<String> cedulas,lista_nombres;
    ListView listacedulas;
    ArrayAdapter adapter;
    Button btn_guardar,btn_salir;
    String api_asistencias,api_usuario,fechadia123;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    EditText txt_usuario,txt_pass;
    JSONObject jsonObject;
    TextView txt_error;
    boolean guardarcedulas;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cedula_error);

        bd = new Managerbd(this,"Registro",null,R.string.versionbase);
        cedulas = new ArrayList<String>();
        listacedulas = (ListView)findViewById(R.id.list_itemcedulaserror);
        lista_nombres = new ArrayList<>();
        txt_error = (TextView) findViewById(R.id.txt_error_cedula2);
        guardarcedulas = false ;//true las guardo false muestro que no se puede

        btn_guardar = (Button) findViewById(R.id.btn_r_ingresomanual);
        btn_salir = (Button)findViewById(R.id.btn_salir);
        txt_usuario = (EditText)findViewById(R.id.txt_usuario_error);
        txt_pass = (EditText)findViewById(R.id.txt_pass_error);

        preferences = getSharedPreferences("infousuario",MODE_PRIVATE);
        api_asistencias = getString(R.string.api_aistencias);
        api_usuario = getString(R.string.api_usuario);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,lista_nombres);
        llenarlista();
        listacedulas.setAdapter(adapter);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia123 = dateFormat.format(date);
        Log.d("horrores1",preferences.getInt("id_cabecera",1)+":"+fechadia123);
        btn_salir.setEnabled(false);
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txt_usuario.getText().toString() != "" && txt_pass.getText().toString() != "" )
                {
                    Log.d("antes",guardarcedulas+"");
                    Log.d("horrores2","Guardando:"+cedulas.size());
                    for (int y=0; y<=cedulas.size()-1;y++)
                    {
                        validarcedula(cedulas.get(y));
                    }
                    Log.d("despues",guardarcedulas+"");
                    if(guardarcedulas=false){
                        for(int i =0;i<=cedulas.size()-1;i++)
                        {
                            Log.d("horrorescedula","Guardando");
                            validar_pass(api_usuario+ "?usuario=" + txt_usuario.getText().toString()+"&contrasena="+txt_pass.getText().toString(),cedulas.get(i));
                            btn_guardar.setEnabled(false);
                            btn_salir.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
                                    finish();
                                }
                            });

                        }
                    }else
                    {
                        txt_error.setText("pida a los usuarios que los suelten de sus areas anteriores para continuar");
                        btn_salir.setEnabled(true);
                    }
                }
                else
                    Toast.makeText(CedulaError.this,"No se admiten campos en blanco",Toast.LENGTH_LONG);
            }
        });
        btn_salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cedulas.size()<= 0)
                {
                    startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
                    finish();
                }
                else if(txt_error.getText() == "Datos guardados con exito!")
                {
                    startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
                    finish();
                }
                else{
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(CedulaError.this);
                    dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea borrar todas las cedulas con errores ?");
                    dialogo1.setCancelable(false);
                    dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
                    { public void onClick(DialogInterface dialogo1, int id)
                    {
                        for(int i = 0; i<= cedulas.size()-1 ;i++)
                        {
                            actualizar(cedulas.get(i),"C");
                        }
                        startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
                    }
                    });
                    dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                    { public void onClick(DialogInterface dialogo1, int id) { } });
                    dialogo1.show();
                }

            }
        });
        listacedulas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int posicion=i;
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(CedulaError.this);
                dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Elimina este Poducto ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id)
                {
                    actualizar(cedulas.get(posicion),"C");
                    cedulas.remove(posicion);
                    lista_nombres.remove(posicion);
                    adapter.notifyDataSetChanged();
                }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                { public void onClick(DialogInterface dialogo1, int id) { } });
                dialogo1.show();
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(cedulas.size() > 0)
        {
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(CedulaError.this);
            dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea salir sin procesar los datos ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id)
            {
                cancelar_cedulas();
                startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
                finish();
            }
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
    public void onBackPressed() {
        Log.d("banck1","presiona el back");
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(CedulaError.this);
        dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea borrar todas las cedulas con errores ?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id)
        {
            for(int i = 0; i<= cedulas.size()-1 ;i++)
            {
                actualizar(cedulas.get(i),"EA");
            }
            startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
        }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id) { } });
        dialogo1.show();






        if(cedulas.size()<= 0)
        {
            Log.d("banck3","presiona el back");
            startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
            finish();
        }
        else if(txt_error.getText() == "Datos guardados con exito!")
        {
            Log.d("banck2","presiona el back");
            startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
        }
        else{
            Log.d("banck1","presiona el back");
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(CedulaError.this);
            dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea borrar todas las cedulas con errores ?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id)
            {
                for(int i = 0; i<= cedulas.size()-1 ;i++)
                {
                    actualizar(cedulas.get(i),"EA");
                }
                startActivity(new Intent(CedulaError.this, RegistroAsistencia.class));
            }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            { public void onClick(DialogInterface dialogo1, int id) { } });
            dialogo1.show();
        }
        Log.d("banck","presiona el back");
        super.onBackPressed();
    }*/

    public void llenarlista() {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select cedula from t_personaserror where fecha like " + "'%" + fechadia + "%' group by cedula", null);
        if(cursor.getCount() >0)
        {
            cursor.moveToFirst();
            do{
                cedulas.add(cursor.getString(0));
                llenarusuario(cursor.getString(0));
                adapter.notifyDataSetChanged();
                Log.d("horrores4",cursor.getString(0));
            }while(cursor.moveToNext());
        }
        else {
            txt_error.setText("Los usuarios con error ya estan registrados");
            Log.d("no hay datos ", "no hay datos ");
        }
    }
    public void guardar(String cedula)
    {
        Log.d("horrores5","guardar funcion");
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_asistencias, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                actualizar(cedula,"S");
                Toast.makeText(CedulaError.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CedulaError.this,"error guardado",Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("v_numerocedula", cedula);
                parametros.put("v_fechaingreso", buscarusuarioxhora(cedula));
                parametros.put("v_id_cabecera", String.valueOf(preferences.getInt("id_cabecera",1)));
                parametros.put("v_estado", String.valueOf(1));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(requerimiento);
    }
    public void actualizar(String v_cedula,String estado)
    {
        bdcache = bd.getWritableDatabase();
        bdcache.execSQL("update t_registro set estadosubido = '"+estado+"' where cedula ='"+v_cedula+"'" );
    }
    public String buscarusuarioxhora(String v_cedula) {
        String hora;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select fechaingreso from t_registro where cedula like " + "'%" + v_cedula + "%'" + " and fechaingreso like " + "'%" + fechadia + "%'", null);
        Log.d("horrores6",cursor.getCount()+"");
        if (cursor.moveToFirst()) {
            hora = cursor.getString(0);
        } else {
            hora = "error";
        }
        return hora;
    }
    public void validar_pass(String url,String cedula1)
    {
        final int[] help = {-1};
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    if(jsonObject.getInt("id_usuario") == 0)
                    {
                        Toast.makeText( CedulaError.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","usuario no valido" );
                        btn_guardar.setEnabled(true);
                        txt_error.setText("Error de usuario o contraseña! Vuelva a intentar :v");
                    }
                    else
                    {
                        txt_error.setText("Datos guardados con exito!");
                        guardar(cedula1);

                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(CedulaError.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(CedulaError.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
    public void  llenarusuario(String cedula)
    {
        String fechadia12;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia12 = dateFormat.format(date);
        final String[] estado = new String[1];
        Log.d("api_asistencia",api_asistencias +"?v_usuario="+cedula+"&v_fecha="+fechadia12+"&v_estado=0");
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+cedula+"&v_fecha="+fechadia12+"&v_estado=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.d("erroren",jsonArray.get(0).toString());
                    estado[0] = jsonObject.getString("nombre");
                    lista_nombres.add(jsonObject.getString("areatrabajo") +":"+jsonObject.getString("nombre"));
                    Log.d("cedulaerror11",jsonObject.getString("areatrabajo")+":   "+jsonObject.getString("nombre"));
                    adapter.notifyDataSetChanged();
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(CedulaError.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(CedulaError.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
    public void cancelar_cedulas()
    {
        Log.d("cancelar error","cedula:"+cedulas.size());
        for (int i =0;i<=cedulas.size()-1;i++)
        {
            Log.d("cancelar error","cedula:"+cedulas.get(i));
            actualizar(cedulas.get(i),"E");
        }
    }

    public void validarcedula(String cedula)
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_asistencias +"?v_usuario="+cedula+"&v_fecha="+fechadia+"&v_estado=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");

                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    Log.d("cedulaerror",jsonArray.get(0).toString());
                    Log.d("VALIDAR USUARIOced","SI MARCO" );
                    if (jsonObject.getString("areatrabajo").contains("LIBRE"))
                      {
                         Log.d("VALIDAR USUARIOced","ESTA LIBRE" );

                      }else{
                         Log.d("VALIDAR USUARIOced","no ESTA LIBRE" );
                         guardarcedulas=true;
                         Log.d("guardar",guardarcedulas+"");
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(CedulaError.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
                Log.d("antesguardar",guardarcedulas+"");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(CedulaError.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }

}