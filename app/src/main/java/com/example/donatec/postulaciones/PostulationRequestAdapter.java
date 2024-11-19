package com.example.donatec.postulaciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donatec.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostulationRequestAdapter extends RecyclerView.Adapter<PostulationRequestAdapter.ViewHolder> {
    // atributos
    ArrayList<PostulationRequest> postulationRequests;

    // constructor
    public PostulationRequestAdapter(ArrayList<PostulationRequest> postulationRequests) {
        this.postulationRequests = postulationRequests;
    }

    @NonNull
    @Override
    public PostulationRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_postulacion_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostulationRequestAdapter.ViewHolder holder, int position) {
        // obtener la postulación en la posición indicada
        PostulationRequest postulationRequest = postulationRequests.get(position);
        // asignar los valores de la postulación a los elementos del layout
        holder.titulo.setText(postulationRequest.getTitle());
        holder.nombre_usuario.setText(postulationRequest.getDonorUsername());
        Picasso.get().load(postulationRequest.getImageURL()).into(holder.imagen);
        holder.chipGroup.removeAllViews();

        // Configurar el LayoutParams
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // Ancho
                ViewGroup.LayoutParams.WRAP_CONTENT  // Alto
        );

        for (String tag : postulationRequest.getMessages()) {
            Chip chip = new Chip(holder.chipGroup.getContext());
            // Configurar estilos
            chip.setChipBackgroundColorResource(R.color.violeta_ultra_bajo); // Color de fondo
            chip.setTextColor(holder.chipGroup.getContext().getResources().getColor(R.color.white)); // Color del texto
            chip.setLayoutParams(layoutParams);
            chip.setChipCornerRadius(20); // Esquinas redondeadas (en píxeles)
            chip.setChipIconResource(R.drawable.message); // Ícono
            chip.setChipIconTintResource(R.color.white); // Color del ícono
            chip.setChipStrokeWidth(0); // Ancho del borde (0 si no hay borde)
            chip.setText(tag); // Texto

            // agregar el chip al chipGroup
            holder.chipGroup.addView(chip);
        }

    }

    @Override
    public int getItemCount() {
        return postulationRequests.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // atributos
        private TextView titulo, nombre_usuario;
        private ImageView imagen;
        private ChipGroup chipGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // inicializar atributos con los elementos del layout
            titulo = itemView.findViewById(R.id.titulo);
            nombre_usuario = itemView.findViewById(R.id.nombre_usuario);
            imagen = itemView.findViewById(R.id.imagen);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }
    }
}
