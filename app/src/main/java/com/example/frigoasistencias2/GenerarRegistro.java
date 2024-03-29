package com.example.frigoasistencias2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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
import com.example.frigoasistencias2.adpater.AdaptadorRecyclerFaltas;
import com.example.frigoasistencias2.clases.Personas;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GenerarRegistro extends AppCompatActivity {

    ArrayList<Personas> persona;
    RecyclerView faltantes;
    ArrayList<String> listadofaltantes,listadofaltantecedulas;
    String api_faltas,api_areas,api_descanso;
    RequestQueue n_requerimiento;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    AdaptadorRecyclerFaltas adapter;
    JSONObject jsonObject;
    Button btn_guardarfaltas,btn_historial;
    TextView total;
    String fechadia;
    int id_cabeceraasis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_registro);

        listadofaltantes = new ArrayList<>();
        listadofaltantecedulas = new ArrayList<>();

        persona = new ArrayList<>();
        faltantes =findViewById(R.id.recycler_g_cedulas);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        editor =preferences.edit();
        api_faltas = getString(R.string.api_faltas);
        api_descanso = getString(R.string.api_descansos);
        api_areas =getString(R.string.api_areas);
        btn_guardarfaltas = findViewById(R.id.btn_g_guardar);
        btn_historial = findViewById(R.id.btn_g_registros);
        //btn_guardarfaltas.setEnabled(false);
        total = findViewById(R.id.txt_g_total);
        faltantes.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));



        adapter = new AdaptadorRecyclerFaltas(listadofaltantecedulas,listadofaltantes);




        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);

        cargardatos();

        btn_guardarfaltas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //estado = adapter.guardarcambios();
                persona = adapter.retornarcedulas();
                //insertar cabecera
                //guardarfaltascabecera();
                verificarcabecera();
            }
        });
        btn_historial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerarRegistro.this,Historial_Asistencias.class));
            }
        });
    }

    public void cargardatos()
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("Fecha",fechadia+preferences.getInt("id_usuario",0)+preferences.getString("departamento","mal"));
        final String[] help = new String[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_faltas +"?id_usuario="+preferences.getInt("id_usuario",0)+"&fechaingreso="+fechadia+"&departamento="+preferences.getString("departamento","mal")+"&bandera=0", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("12345689",jsonArray.toString());

                    for(int i = 0;i<=jsonArray.length()-1;i++)
                    {
                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                        if(jsonObject.getInt("estado") != 0)
                        {
                            listadofaltantes.add(jsonObject.getString("nombres")+":"+jsonObject.getString("cedula"));
                            Log.d("generar",jsonObject.getString("nombres"));
                            listadofaltantecedulas.add(jsonObject.getString("cedula"));
                        }
                        else {
                            Toast.makeText(GenerarRegistro.this,"No hay datos",Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter = new AdaptadorRecyclerFaltas(listadofaltantes,listadofaltantecedulas);
                    faltantes.setAdapter(adapter);
                    total.setText(listadofaltantecedulas.size()+"");
                }catch (JSONException e)
                {
                    Log.d("CARGARDATOS","entro3"+e.toString());
                    Toast.makeText(GenerarRegistro.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(GenerarRegistro.this,"Error de coneccion consulte con sistemas",Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }
    public void guardarfaltasdetalle(String v_estado,String v_cedula, int v_id_cabecerasas)
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);
        int numeroestado=0;
        switch (v_estado)
        {
            case "FALTA":
                numeroestado = 4;
                break;

            case "LIBRE":
                numeroestado = 5;
                break;

            case "VACACIONES":
                numeroestado = 6;
                break;

            case "PERMISO MEDICO":
                numeroestado = 10;
                break;
        }
        int finalNumeroestado = numeroestado;
        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_faltas, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(GenerarRegistro.this,"Todo bien Todo bonito",Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GenerarRegistro.this,"Verifique que este conectado A la red e intente de nuevo",Toast.LENGTH_LONG).show();
                Log.d("detalleerror",error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("cedula", v_cedula);
                parametros.put("fecha", fechadiacabe);
                parametros.put("estado",String.valueOf(finalNumeroestado));
                parametros.put("bandera", String.valueOf(1));
                parametros.put("id_cabecera", String.valueOf(v_id_cabecerasas));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        requerimiento.setShouldCache(true);
        n_requerimiento.add(requerimiento);
    }

    public void guardarfaltascabecera()
    {
        String fechadiacabe;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadiacabe = dateFormat.format(date);

        StringRequest requerimiento = new StringRequest(Request.Method.POST, api_faltas, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response1",response.toString());
                verificarcabeceraid();
                //startActivity(new Intent(GenerarRegistro.this,RegistroAsistencia.class));
                //finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GenerarRegistro.this,"Verifique que este conectado A la red e intente de nuevo",Toast.LENGTH_LONG).show();
                Log.d("detalleerror",error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("fecha", fechadiacabe);

                parametros.put("id_supervisor", String.valueOf(preferences.getInt("id_usuario",0)));
                parametros.put("bandera", String.valueOf(0));
                return parametros;
            }
        };
        n_requerimiento = Volley.newRequestQueue(this);
        requerimiento.setShouldCache(true);
        n_requerimiento.add(requerimiento);
    }

    public void verificarcabecera()
    {
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("Fecha",fechadia+preferences.getInt("id_usuario",0)+preferences.getString("departamento","mal"));
        final String[] help = new String[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_faltas +"?id_usuario="+preferences.getInt("id_usuario",0)+"&fechaingreso="+fechadia+"&bandera=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("verificar",jsonArray.toString());
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    if(jsonObject.getInt("cabecera") == 0)
                    {
                        guardarfaltascabecera();
                    }
                    else{
                        Toast.makeText(GenerarRegistro.this,"Ya esta registrado la asistencia",Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e)
                {
                    Log.d("VERIFICARCABECERA","entro3"+e.toString());
                    Toast.makeText(GenerarRegistro.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(GenerarRegistro.this,"Error de coneccion consulte con sistemas",Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }

    public void verificarcabeceraid()
    {

        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("Fecha",fechadia+preferences.getInt("id_usuario",0)+preferences.getString("departamento","mal"));
        final String[] help = new String[1];
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_faltas +"?id_usuario="+preferences.getInt("id_usuario",0)+"&fechaingreso="+fechadia+"&bandera=1", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    Log.d("verificar",jsonArray.toString());
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    id_cabeceraasis = jsonObject.getInt("cabecera");


                    if(id_cabeceraasis == 0)
                    {
                        Log.d("error al guardar",preferences.getInt("id_cabeceraasistencia",0)+"");
                        Toast.makeText(GenerarRegistro.this,"Error al generar el reporte consulte con sistemas",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(persona.size() > 0)
                        {
                            for (int i = 0;i<= persona.size()-1;i++)
                            {
                                Log.d("botonguardar",persona.get(i).getNombre()+"-"+persona.get(i).getEstado());
                                guardarfaltasdetalle(persona.get(i).getEstado(),persona.get(i).getNombre(),id_cabeceraasis);
                            }
                            crearPDF();
                            //Toast.makeText(GenerarRegistro.this,"Asistencia guardada",Toast.LENGTH_SHORT).show();
                        }
                    }

                }catch (JSONException e)
                {
                    Log.d("CABECERAID","entro3"+e.toString());
                    Toast.makeText(GenerarRegistro.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("buscarerror","dd"+error.toString());
                Toast.makeText(GenerarRegistro.this,"Error de coneccion consulte con sistemas",Toast.LENGTH_SHORT).show();
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        json.setShouldCache(true);
        n_requerimiento.add(json);
    }

    private void crearPDF() {
        try {
            String carpeta = "/Asistencias";
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + carpeta;

            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
                Toast.makeText(this, "CARPETA CREADA", Toast.LENGTH_SHORT).show();
            }

            File archivo = new File(dir,   preferences.getString("nombre_usuario","mal")+"_"+fechadia+".pdf");
            FileOutputStream fos = new FileOutputStream(archivo);

            Document documento = new Document();
            PdfWriter.getInstance(documento, fos);

            documento.open();

            Paragraph titulo = new Paragraph(
                    "Asistencia \n\n\n"+fechadia,
                    FontFactory.getFont("arial", 22, Font.BOLD, BaseColor.BLUE)
            );
            documento.add(titulo);

            PdfPTable tabla = new PdfPTable(3);
            tabla.addCell("NOMBRE");
            tabla.addCell("CEDULA");
            tabla.addCell("ESTADO");

            for (int i = 0 ; i < persona.size() ; i++) {
                tabla.addCell(persona.get(i).getCedulas());
                tabla.addCell(persona.get(i).getNombre());
                tabla.addCell(persona.get(i).getEstado());
            }

            documento.add(tabla);

            documento.close();

        } catch (FileNotFoundException e) {
            Log.d("pdferror",e.toString());
        } catch ( DocumentException e) {
            Log.d("pdferror2",e.toString());
        }
    }
}