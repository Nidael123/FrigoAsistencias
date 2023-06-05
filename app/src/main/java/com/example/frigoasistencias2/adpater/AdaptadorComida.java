package com.example.frigoasistencias2.adpater;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.clases.Personas;

import java.util.ArrayList;

public class AdaptadorComida extends RecyclerView.Adapter<AdaptadorComida.ViewHolder> {

        ArrayList<Personas> persona;

        public AdaptadorComida (ArrayList<Personas> v_persona)
        {
                persona = v_persona;
                Log.d("cantidad",v_persona.size()+":"+persona.size());
        }
        @NonNull
        @Override
        public AdaptadorComida.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comida,parent,false);
            return new ViewHolder(vista);
        }

        @Override
        public void onBindViewHolder(@NonNull AdaptadorComida.ViewHolder holder, int position) {
                holder.asignar_datos(persona.get(position));

                holder.btn_comer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                Log.d("prueba",""+holder.txt_persona.getText());
                        }
                });
        }

        @Override
        public int getItemCount() {
                return persona.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

                TextView txt_persona;
                Button btn_comer;
                public ViewHolder(@NonNull View itemView) {
                        super(itemView);
                        txt_persona = itemView.findViewById(R.id.item_c_usuario);
                        btn_comer = itemView.findViewById(R.id.item_c_btncomer);
                }

                public void asignar_datos(Personas p)
                {
                        txt_persona.setText(p.getNombre());
                }
        }


}
