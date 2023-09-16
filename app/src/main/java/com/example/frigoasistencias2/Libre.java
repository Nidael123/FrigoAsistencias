package com.example.frigoasistencias2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.contentcapture.DataRemovalRequest;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Libre extends AppCompatActivity {

    TextView txt_fechainicio,txt_fechafin,txt_nombre;
    int ultimoAnio, ultimoMes, ultimoDiaDelMes,ultimoAnio1, ultimoMes1, ultimoDiaDelMes1;
    EditText edit_cedula;
    Button btn_guardar,btn_historial;
    JSONObject jsonObject;
    RequestQueue n_requerimiento;
    String api_libre;
    private SharedPreferences preferences;

    public DatePickerDialog.OnDateSetListener listenerDeDatePicker  = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int anio, int mes, int diaDelMes) {
            ultimoAnio = anio;
            ultimoMes = mes;
            ultimoDiaDelMes = diaDelMes;
            String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", ultimoAnio, ultimoMes+1, ultimoDiaDelMes);
            txt_fechainicio.setText(fecha);
            txt_fechafin.setText(fecha);
        }
    };
    public DatePickerDialog.OnDateSetListener listenerDeDatePicker1  = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int anio, int mes, int diaDelMes) {
            ultimoAnio1 = anio;
            ultimoMes1 = mes;
            ultimoDiaDelMes1 = diaDelMes;
            String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", ultimoAnio1, ultimoMes1+1, ultimoDiaDelMes1);
            txt_fechafin.setText(fecha);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libre);

        txt_fechainicio = findViewById(R.id.txt_l_fechainicio);
        txt_fechafin = findViewById(R.id.txt_l_fechafin);
        txt_nombre = findViewById(R.id.txt_l_nombre);
        api_libre = getString(R.string.api_libre);
        edit_cedula = findViewById(R.id.edit_l_cedula);
        btn_guardar = findViewById(R.id.btn_l_guardar);
        preferences = getSharedPreferences("infousuario", MODE_PRIVATE);
        btn_historial = findViewById(R.id.btn_l_historial);

        long ahora = System.currentTimeMillis();
        Date date = new Date(ahora);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ahora);


        txt_fechainicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialogoFecha = new DatePickerDialog(Libre.this, listenerDeDatePicker, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //dialogoFecha.getDatePicker().setMinDate(calendar.get(Calendar.DAY_OF_YEAR));
                dialogoFecha.show();
            }
        });

        txt_fechafin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialogoFecha = new DatePickerDialog(Libre.this, listenerDeDatePicker1, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //dialogoFecha.getDatePicker().setMaxDate(calendar.get(Calendar.DAY_OF_YEAR));
                Log.d("fecha",calendar.get(Calendar.DAY_OF_YEAR)+"");
                dialogoFecha.show();
            }
        });

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                Date dateOjb ;
                Date dateOjb2;

                    try {
                        dateOjb = format1.parse(txt_fechainicio.getText().toString());
                        dateOjb2 = format1.parse(txt_fechafin.getText().toString());
                        if (dateOjb2.after(dateOjb) || dateOjb2.equals(dateOjb)) {
                            if (edit_cedula.length() == 10) {
                                desplegar_alerta();
                            }
                        } else {
                            Toast.makeText(Libre.this, "Fecha final debe ser mayor a fecha inicial", Toast.LENGTH_LONG).show();
                        }
                    } catch (ParseException e) {
                        Toast.makeText(Libre.this, "No deje valores en blanco", Toast.LENGTH_LONG).show();
                    }

                /*if(ultimoAnio <= ultimoAnio1)
                {
                    if(ultimoMes <= ultimoMes1)
                    {
                        if(ultimoDiaDelMes <= ultimoDiaDelMes1)
                        {
                            if(txt_fechainicio.getText()== "FECHA INICIO" )
                            {
                                Toast.makeText(Libre.this, "Debe haber fecha de inicio y fecha final de libre", Toast.LENGTH_LONG).show();
                            }
                            else{
                                if(edit_cedula.length() == 10)
                                {
                                    buscar_usuario();
                                    Log.d("estoy en buscar","buscar");
                                }
                                else
                                    Toast.makeText(Libre.this, "Deben ser 10 Numeros", Toast.LENGTH_LONG).show();
                            }
                        }else
                            Toast.makeText(Libre.this, "LA FECHA FINAL PUEDE SER IGUAL O MAYOR A LA FECHA INICIO", Toast.LENGTH_LONG).show();
                    }else
                        Toast.makeText(Libre.this, "LA FECHA FINAL PUEDE SER IGUAL O MAYOR A LA FECHA INICIO", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(Libre.this, "LA FECHA FINAL PUEDE SER IGUAL O MAYOR A LA FECHA INICIO", Toast.LENGTH_LONG).show();
*/
            }
        });
        btn_historial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Libre.this,HistorialLibres.class);
                intent.putExtra("estado",5);
                startActivity(intent);
            }
        });
    }
    public void desplegar_alerta()
    {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(Libre.this);
        dialogo1.setTitle("Importante"); dialogo1.setMessage("¿ Desea guardar el libre de:"+edit_cedula.getText()+" desde la Fecha:  "+txt_fechainicio.getText().toString()+"\n"+
            "hasta la fecha: "+txt_fechafin.getText().toString()+"?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id)
        {
            buscar_usuario();
        }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        { public void onClick(DialogInterface dialogo1, int id) { } });
        dialogo1.show();
    }

    public void buscar_usuario()
    {
        int estado = 5;
        String fechadia;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
        Date date = new Date();
        fechadia = dateFormat.format(date);
        Log.d("buscarusuario",api_libre+"?v_cedula="+String.valueOf(edit_cedula.getText())+"&v_fecha_inicio="+txt_fechainicio.getText()+"&v_fecha_fin="+txt_fechafin.getText()+"&v_fecha_ingreso="+fechadia+"&bandera=0"+"&v_id_supervisor="+preferences.getInt("id_usuario",0));
        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_libre+"?v_cedula="+String.valueOf(edit_cedula.getText())+"&v_fecha_inicio="+txt_fechainicio.getText()+"&v_fecha_fin="+txt_fechafin.getText()+"&v_fecha_ingreso="+fechadia+"&bandera=0"+"&v_id_supervisor="+preferences.getInt("id_usuario",0)+"&v_estado="+estado, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());

                    Log.d("data",jsonObject.toString());
                    txt_nombre.setText(jsonObject.getString("nombre"));
                    Toast.makeText(Libre.this,jsonObject.getString("mensaje"),Toast.LENGTH_SHORT).show();
                }catch (JSONException e)
                {
                    Log.d("logeo","entro3"+e.toString());
                    Toast.makeText(Libre.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("logeo","entro4"+error.toString());
                Toast.makeText(Libre.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT);
            }
        });
        n_requerimiento = Volley.newRequestQueue(this);
        n_requerimiento.add(json);
    }
}