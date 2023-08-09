package com.example.frigoasistencias2.adpater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.clases.Personas;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdapterLibres extends RecyclerView.Adapter<AdapterLibres.ViewHolder> {

    ArrayList<Personas> listadoperosnas,listadopersonascopia;

    public AdapterLibres (ArrayList<Personas> P)
    {
        listadoperosnas = new ArrayList<>();
        listadoperosnas = P;
        listadopersonascopia = new ArrayList<>();
        listadopersonascopia.addAll(listadoperosnas);
    }

    public void filtrado(final String txtBuscar) {
        int longitud = txtBuscar.length();
        if (longitud == 0) {
            listadoperosnas.clear();
            listadoperosnas.addAll(listadopersonascopia);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Personas> collecion = listadoperosnas.stream()
                        .filter(i -> i.getNombre().toLowerCase().contains(txtBuscar.toLowerCase()))
                        .collect(Collectors.toList());
                listadoperosnas.clear();
                listadoperosnas.addAll(collecion);
            } else {
                for (Personas c : listadopersonascopia) {
                    if (c.getNombre().toLowerCase().contains(txtBuscar.toLowerCase())) {
                        listadoperosnas.add(c);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public AdapterLibres.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libres,null,false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterLibres.ViewHolder holder, int position) {
        holder.asignardatos(listadoperosnas.get(position));
    }

    @Override
    public int getItemCount() {
        return listadoperosnas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_cedula,txt_nombre,txt_fechainicio,txt_fechafin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_cedula = itemView.findViewById(R.id.txt_ifal_cedula);
            txt_nombre = itemView.findViewById(R.id.txt_ifal_nombre);
            txt_fechainicio = itemView.findViewById(R.id.txt_ifal_fechainicio);
            txt_fechafin = itemView.findViewById(R.id.txt_ifal_fechafin);
        }
        public void asignardatos(Personas p){
            txt_cedula.setText(p.getCedulas());
            txt_nombre.setText(p.getNombre());
            txt_fechainicio.setText(p.getFechainicio());
            txt_fechafin.setText(p.getFechafin());
        }
    }
}
