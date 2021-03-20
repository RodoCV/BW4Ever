package com.example.bw4ever.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bw4ever.R;
import com.example.bw4ever.modelo.Rutina;

import java.util.List;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaHolder> {
    List<Rutina> lista;
    int item_layout;
    Activity activity;

    public RutinaAdapter(List<Rutina> lista, int item_layout, Activity activity) {
        this.lista = lista;
        this.item_layout = item_layout;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RutinaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(item_layout, parent,false);
        return new RutinaHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaHolder holder, int position) {
        Rutina rutina = lista.get(position);
        holder.txtnombre.setText(rutina.getNombre());
        holder.txtdescripcion.setText(rutina.getDescripcion());
        holder.txtdificultad.setText(rutina.getDificultad());
        Glide.with(activity).load(rutina.getUrl_foto()).into(holder.imgrutina);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class RutinaHolder extends RecyclerView.ViewHolder{
        private final Object ListView;
        TextView txtnombre, txtdescripcion, txtdificultad;
        ImageView imgrutina;
        ListView listejercicios;
        public RutinaHolder(@NonNull View itemView) {
            super(itemView);
            txtnombre = itemView.findViewById(R.id.item_nombre);
            txtdificultad = itemView.findViewById(R.id.item_dificultad);
            txtdescripcion = itemView.findViewById(R.id.item_descripcion);
            imgrutina = itemView.findViewById(R.id.item_imagen);
            ListView = itemView.findViewById(R.id.item_listaejercicios);
        }
    }
}