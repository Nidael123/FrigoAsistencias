package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.bd.Managerbd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CedulaError extends AppCompatActivity {

    Managerbd bd;
    SQLiteDatabase bdcache;
    ArrayList<String> cedulas;
    ListView listacedulas;
    Adapter adapter;
    Button btn_guardar;
    String api_asistencias,api_usuario;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    EditText txt_usuario,txt_pass;
    //TextView txt_error;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cedula_error);

        bd = new Managerbd(this,"Registro",null,1);
        cedulas = new ArrayList<String>();
        listacedulas = (ListView)findViewById(R.id.list_itemcedulaserror);
        //txt_error = (TextView) findViewById(R.id.txt_error_cedula2);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line,cedulas);
        btn_guardar = (Button) findViewById(R.id.btn_guardarregistroerror);
        txt_usuario = (EditText)findViewById(R.id.txt_usuario_error);
        txt_pass = (EditText)findViewById(R.id.txt_pass_error);
        listacedulas.setAdapter((ListAdapter) adapter);
        preferences = getSharedPreferences("infousuario",MODE_PRIVATE);
        api_asistencias = getString(R.string.api_aistencias);
        api_usuario = getString(R.string.api_usuario);
        llenarlista();

        Log.d("Cedula",preferences.getInt("id_cabecera",1)+"");
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txt_usuario.getText().toString() != "" && txt_pass.getText().toString() != "" )
                {
                    Log.d("Cedula","Guardando:"+cedulas.size());
                    for(int i =0;i<=cedulas.size()-1;i++)
                    {
                        Log.d("Cedula","Guardando");
                        guardar(cedulas.get(i));
                    }
                    //logear(apisusario + "?usuario=" + txt_user.getText().toString()+"&contrasena="+txt_password.getText().toString());
                }
                else
                    Toast.makeText(CedulaError.this,"No se admiten campos en blanco",Toast.LENGTH_LONG);


            }
        });
    }

    public void llenarlista() {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        bdcache = bd.getReadableDatabase();
        Cursor cursor = bdcache.rawQuery("Select cedula from t_registro where fechaingreso like " + "'%" + fechadia + "%' and estadosubido ='E'", null);
        cursor.moveToFirst();
        do{
            cedulas.add(cursor.getString(0));
        }while(cursor.moveToNext());
    }
    public void guardar(String cedula)
    {
        Log.d("Cedula","guardar funcion");
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_asistencias, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(CedulaError.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
                actualizar(cedula,"S");
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
        Log.d("buscarusuarioxhora",cursor.getCount()+"");
        if (cursor.moveToFirst()) {
            hora = cursor.getString(0);
        } else {
            hora = "error";
        }
        return hora;
    }
    public void validar_pass(String urlusuario)
    {

    }
}