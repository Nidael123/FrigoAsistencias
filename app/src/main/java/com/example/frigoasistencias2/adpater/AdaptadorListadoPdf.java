package com.example.frigoasistencias2.adpater;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.VisorPdf;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdaptadorListadoPdf  extends RecyclerView.Adapter<AdaptadorListadoPdf.ViewHolder> {

    ArrayList<String> namefiles;
    Context context;

    public AdaptadorListadoPdf(ArrayList<String> v_namefiles){
        namefiles = v_namefiles;
    }

    @NonNull
    @Override
    public AdaptadorListadoPdf.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listado_pdf,null,false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorListadoPdf.ViewHolder holder, int position) {
        holder.asignar(namefiles.get(position));

        holder.btn_ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cargo la vista
                Intent intent= new Intent(context, VisorPdf.class);
                intent.putExtra("ruta",holder.ruta.getText());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return namefiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView ruta;
        Button btn_ver;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ruta = itemView.findViewById(R.id.txt_il_ruta);
            btn_ver = itemView.findViewById(R.id.btn_il_ver);
            context = itemView.getContext();
        }
        public void asignar(String direccion)
        {
            ruta.setText(direccion);
        }
    }
}
