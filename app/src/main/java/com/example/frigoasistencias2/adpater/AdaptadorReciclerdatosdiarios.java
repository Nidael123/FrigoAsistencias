package com.example.frigoasistencias2.adpater;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frigoasistencias2.ListadoAsistencia;
import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.clases.datosdiarios;

import java.util.ArrayList;

public class AdaptadorReciclerdatosdiarios extends RecyclerView.Adapter<AdaptadorReciclerdatosdiarios.ViewHolder>{
    String fechatomada;
    ArrayList<datosdiarios> datos;
    Context context;
    public AdaptadorReciclerdatosdiarios(ArrayList<datosdiarios>  v_datos) {
        this.datos = v_datos;
    }


    @NonNull
    @Override
    public AdaptadorReciclerdatosdiarios.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_datosdiarios,null,false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorReciclerdatosdiarios.ViewHolder holder, int position) {
        holder.asignar(datos.get(position));
        Integer help = new Integer(position);

        holder.btn_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListadoAsistencia.class);
                intent.putExtra("fecha",fechatomada);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fecha;
        Button btn_datos;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            fecha = itemView.findViewById(R.id.txt_fechaitem);
            btn_datos = (Button) itemView.findViewById(R.id.btn_datos);
        }
        public void asignar(datosdiarios  data){
            fecha.setText(data.getFecha());
            fechatomada = data.getFecha();
        }
    }
}
