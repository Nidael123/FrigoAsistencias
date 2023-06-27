package com.example.frigoasistencias2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.bd.Managerbd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Login extends AppCompatActivity {

    Button btn_login;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    EditText txt_user,txt_password;
    String apisusario;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    ArrayList<String> departamentos;
    boolean isWifiConn = false;
    boolean isMobileConn = false;
    Managerbd bd;
    SQLiteDatabase bdcache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        apisusario = getString(R.string.api_usuario);
        btn_login = (Button) findViewById(R.id.btn_login1);
        txt_password = (EditText) findViewById(R.id.txt_password);
        txt_user = (EditText) findViewById(R.id.txt_user);

        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        editor = preferences.edit();
        bd = new Managerbd(this, "Registro", null, R.string.versionbase);

        verificarversion();

        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(Login.this);
        dialogo1.setTitle("Importante"); dialogo1.setMessage("Conectarse a la red de Frigopesca");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id)
        {  }
        });



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validar_red();
                if(isWifiConn)
                {
                    if(txt_user.getText().toString() != "" && txt_password.getText().toString() != "" )
                    {
                        Toast.makeText(Login.this, "Ingreso", Toast.LENGTH_LONG);
                        String fechadia;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
                        Date date = new Date();
                        fechadia = dateFormat.format(date);


                        bdcache = bd.getWritableDatabase();
                        bdcache.execSQL("delete from t_registro where fechaingreso not like"+"'%"+fechadia+"%'");

                        logear(apisusario + "?usuario=" + txt_user.getText().toString()+"&contrasena="+txt_password.getText().toString());
                    }
                    else
                        Toast.makeText(Login.this,"No se admiten campos en blanco",Toast.LENGTH_LONG);
                }
                else
                {
                    Toast.makeText(Login.this, "Acceda a la red de Frigopesca e Intente de nuevo", Toast.LENGTH_SHORT);
                }

                if(isMobileConn && isWifiConn == false)
                {
                    dialogo1.show();
                }
            }
        });
    }
    public void logear(String url)
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
                        Toast.makeText(Login.this,"Error en usuario o contraseña",Toast.LENGTH_SHORT).show();
                        Log.d("error","usuario no valido" );
                    }
                    else
                    {
                        for (int i = 0;i<=jsonArray.length()-1;i++ ) {
                            jsonObject = new JSONObject(jsonArray.get(i).toString());
                            editor.putInt("id_usuario",jsonObject.getInt("id_usuario"));
                            editor.putString("depa"+i,jsonObject.getString("departamento"));
                            editor.putString("nombre_usuario",jsonObject.getString("nombre"));
                            editor.putString("supervisor",jsonObject.getString("supervisor"));
                            Log.d("login",""+jsonObject.getString("supervisor"));
                        }
                        editor.putInt("cant_depart", jsonArray.length()-1);
                        editor.commit();
                        Toast.makeText(Login.this,"Ingreso con Exitoso",Toast.LENGTH_SHORT);
                        Log.d("logeo",""+help.length);
                        startActivity(new Intent(Login.this,RegistroAsistencia.class));
                    }
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(Login.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(Login.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
    public void validar_red()
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
    }
    public void verificarversion()
    {
        String version;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            Log.d("VERSION",""+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}