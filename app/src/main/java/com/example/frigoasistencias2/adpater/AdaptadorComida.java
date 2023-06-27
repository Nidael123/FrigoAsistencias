package com.example.frigoasistencias2.adpater;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.frigoasistencias2.ListadoDiario;
import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.clases.Personas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdaptadorComida extends RecyclerView.Adapter<AdaptadorComida.ViewHolder> {

        ArrayList<Personas> persona;
        ArrayList<Personas> personacopia;
        String api_descanso;
        RequestQueue n_requerimiento;
        int id_usuario,turno;

        public AdaptadorComida (ArrayList<Personas> v_persona,String v_api_descanso,int v_id_usuario,int v_turno)
        {
                persona = v_persona;
                api_descanso = v_api_descanso;
                id_usuario = v_id_usuario;
                turno = v_turno;
                Log.d("cantidad",api_descanso+id_usuario+turno+"");
                personacopia = new ArrayList<>();
                personacopia.addAll(persona);
        }
        public void filtrado(final String txtBuscar) {
                int longitud = txtBuscar.length();
                if (longitud == 0) {
                        persona.clear();
                        persona.addAll(personacopia);
                } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                List<Personas> collecion = persona.stream()
                                        .filter(i -> i.getNombre().toLowerCase().contains(txtBuscar.toLowerCase()))
                                        .collect(Collectors.toList());
                                persona.clear();
                                persona.addAll(collecion);
                        } else {
                                for (Personas c : personacopia) {
                                        if (c.getNombre().toLowerCase().contains(txtBuscar.toLowerCase())) {
                                                persona.add(c);
                                        }
                                }
                        }
                }
                notifyDataSetChanged();
        }
        @NonNull
        @Override
        public AdaptadorComida.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comida,null,false);
            return new ViewHolder(vista);
        }
        @Override
        public void onBindViewHolder(@NonNull AdaptadorComida.ViewHolder holder, int position) {
                holder.asignar_datos(persona.get(position));
        }
        @Override
        public int getItemCount() {
                return persona.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

                TextView txt_persona;
                Button btn_comer;
                JSONObject jsonObject;
                Personas persona;
                public ViewHolder(@NonNull View itemView) {
                        super(itemView);
                        txt_persona = itemView.findViewById(R.id.item_c_usuario);
                        btn_comer = itemView.findViewById(R.id.item_c_btncomer);

                        btn_comer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        Log.d("dadapter",persona.getCedulas());
                                        banio(persona.getCedulas(),9);
                                }
                        });
                }

                public void asignar_datos(Personas p)
                {
                        persona=p;
                        txt_persona.setText(p.getNombre());
                }

                public void banio(String v_cedula , int v_estado) {
                        String fechadiacabe;

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());//seteo la fecha actual
                        Date date = new Date();
                        fechadiacabe = dateFormat.format(date);

                        JsonObjectRequest json = new JsonObjectRequest(Request.Method.GET, api_descanso +"?v_cedula="+v_cedula+"&v_fecha="+fechadiacabe+"&v_estado="+v_estado+"&v_usuario="+id_usuario+"&bandera=1",null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                        try {
                                                btn_comer.setEnabled(false);
                                                JSONArray jsonArray = response.getJSONArray("data");
                                                for(int i = 0;i<=jsonArray.length()-1;i++)
                                                {
                                                        jsonObject = new JSONObject(jsonArray.get(i).toString());
                                                        Log.d("123456789","dale"+jsonObject.getString("mensaje"));
                                                        //Toast.makeText(ListadoDiario.this,jsonObject.getString("mensaje"),Toast.LENGTH_SHORT).show();
                                                }
                                                Toast.makeText(itemView.getContext(),"Usuario enviado a comer",Toast.LENGTH_LONG).show();
                                        }catch (JSONException e)
                                        {
                                                Log.d("DANIEL","entro3"+e.toString());
                                                //Toast.makeText(ListadoDiario.this,"Error de base consulte con sistemas",Toast.LENGTH_SHORT).show();
                                        }
                                }
                        }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                        Log.d("buscarerror","dd"+error.toString());
                                        //Toast.makeText(.this,"Error de coneccion consulte con sistemas"+error.toString(),Toast.LENGTH_SHORT).show;
                                }
                        });
                        n_requerimiento = Volley.newRequestQueue(itemView.getContext());
                        json.setShouldCache(true);
                        n_requerimiento.add(json);
                }
        }
}
