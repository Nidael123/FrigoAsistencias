package com.example.frigoasistencias2.adpater;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frigoasistencias2.R;
import com.example.frigoasistencias2.clases.Personas;

import java.util.ArrayList;

public class AdaptadorRecyclerFaltas extends RecyclerView.Adapter<AdaptadorRecyclerFaltas.ViewHolder> {

    ArrayList<String> cedulas,nombres,estados;

    ArrayList<Personas> persona;
    public AdaptadorRecyclerFaltas(ArrayList<String>v_cedulas,ArrayList<String>v_nombres)
    {
        persona = new ArrayList<>();
        cedulas = v_cedulas;
        nombres=v_nombres;
        estados = new ArrayList<>();
        for (int i = 0;i<=nombres.size()-1;i++)
        {
            Personas helppersonas = new Personas();
            helppersonas.setCedulas(v_cedulas.get(i));
            helppersonas.setNombre(v_nombres.get(i));
            helppersonas.setEstado("FALTA");
            persona.add(helppersonas);
            estados.add("FALTA");
            Log.d("principal",nombres.get(i));
        }
    }

    @NonNull
    @Override
    public AdaptadorRecyclerFaltas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemfaltas,null,false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorRecyclerFaltas.ViewHolder holder, int position) {
        holder.asignar_datos(cedulas.get(position));
    }
    @Override
    public int getItemCount() {
        return cedulas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_cedula;
        Spinner spin_estado;
        ArrayList<String> listaestdados;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_cedula = itemView.findViewById(R.id.txt_if_cedula);
            spin_estado = itemView.findViewById(R.id.sp_estado);
            listaestdados = new ArrayList<>();
            listaestdados.add("FALTA");
            listaestdados.add("LIBRE");
            listaestdados.add("VACACIONES");
            listaestdados.add("PERMISO MEDICO");

            spin_estado.setAdapter(new ArrayAdapter<String>(itemView.getContext(), android.R.layout.simple_spinner_dropdown_item, listaestdados));
            Log.d("recyclerestados",estados.size()+"");

            Log.d("estadosrecycler",cedulas.size()+"");

            spin_estado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    for (int y = 0; y <= persona.size()-1;y++ )
                    {
                        if(persona.get(y).getCedulas() == txt_cedula.getText() );
                        {
                            Log.d("saldra?",persona.get(y).getCedulas() + txt_cedula.getText()+spin_estado.getSelectedItem().toString());
                            estados.set(y,spin_estado.getSelectedItem().toString());
                        }
                    }
                    Log.d("itembueno",txt_cedula.getText()+  spin_estado.getSelectedItem().toString()+estados.size());
                    //estados.add(spin_estado.getSelectedItem().toString());
                    //procesarfaltantes(i,spin_estado.getSelectedItem().toString());
                    //Log.d("itembueno2",estados.get(i));
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        public void asignar_datos(String cedula)
        {
            txt_cedula.setText(cedula);
        }
    }
    public ArrayList<String> guardarcambios()
    {
        for (int i = 0;i<=estados.size()-1;i++)
        {
            Log.d("estadofinal",estados.get(i).toString());
        }
        return estados;
    }
    public ArrayList<String>retornarcedulas()
    {
        return nombres;
    }
}
