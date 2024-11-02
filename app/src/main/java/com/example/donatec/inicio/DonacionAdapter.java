package com.example.donatec.inicio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donatec.R;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DonacionAdapter extends RecyclerView.Adapter<DonacionAdapter.ViewHolder> {
    // atributo: Lista de donaciones (fuente de datos) que se van a mostrar en el RecyclerView
    ArrayList<Donacion> listaDonaciones;

    // constructor parametrizado
    public DonacionAdapter(ArrayList<Donacion> listaDonaciones) {
        this.listaDonaciones = listaDonaciones;
    }

    // Método que se llama cuando se necesita crear un nuevo ViewHolder (la "caja" que contendrá cada ítem de la lista)
    @NonNull
    @Override
    public DonacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla cada ítem (item_list.xml) para generar la vista de cada Pokémon
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item_list_donacion, parent, false);
        // Crea un nuevo ViewHolder con la vista inflada
        return new ViewHolder(view);
    }

    // mérodo que se llama cuando se enlazan los datos del pokemon con la vista ya inflada
    @Override
    public void onBindViewHolder(@NonNull DonacionAdapter.ViewHolder holder, int position) {
        // asigna los datos del donativo a la vista
        holder.titulo.setText(this.listaDonaciones.get(position).getTitle());
        holder.nombre_usuario.setText(this.listaDonaciones.get(position).getUsername());
        holder.fecha_publicacion.setText(this.listaDonaciones.get(position).getPublished_at());
        Picasso.get().load(this.listaDonaciones.get(position).getImageUrl()).into(holder.imagen);
        holder.descripcion.setText(this.listaDonaciones.get(position).getDescription());
        holder.categoria.setText(this.listaDonaciones.get(position).getCategory());
        holder.ubicacion.setText(this.listaDonaciones.get(position).getMunicipality() + ", " + this.listaDonaciones.get(position).getState());
        holder.cantidad.setText(this.listaDonaciones.get(position).getAmount() + " " + this.listaDonaciones.get(position).getMeasurement());

        // Listener para el usuario de la donación
        holder.nombre_usuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Toast.makeText(v.getContext(), "Usuario: " + listaDonaciones.get(position).getUsername(), Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el botón "postularme"
        holder.btn_postularme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toast que muestre el id del donativo
                Toast.makeText(v.getContext(), "Donativo: " + listaDonaciones.get(position).getDonationId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Devuelve el tamaño de la lista (cuántos Pokémon hay)
    @Override
    public int getItemCount() {
        return this.listaDonaciones.size();
    }

    /**
     * El ViewHolder es una clase interna del adaptador que se usa para almacenar referencias a las vistas que contienen los datos,
     * como los TextView y ImageView. Este patrón ayuda a optimizar el rendimiento del RecyclerView,
     * ya que evita inflar y encontrar las vistas cada vez que se desplaza la lista, lo que podría hacerla lenta.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titulo;
        private TextView nombre_usuario;
        private ImageView imagen;
        private TextView descripcion;
        private Chip categoria;
        private Chip ubicacion;
        private TextView fecha_publicacion;
        private TextView cantidad;
        private Button btn_postularme;


        // Constructor del ViewHolder, se ejecuta al crear cada ítem
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // inicializa las vistas del ítem
            titulo = itemView.findViewById(R.id.titulo);
            nombre_usuario = itemView.findViewById(R.id.nombre_usuario);
            imagen = itemView.findViewById(R.id.imagen);
            descripcion = itemView.findViewById(R.id.descripcion);
            categoria = itemView.findViewById(R.id.categoria);
            ubicacion = itemView.findViewById(R.id.ubicacion);
            fecha_publicacion = itemView.findViewById(R.id.fecha_publicacion);
            cantidad = itemView.findViewById(R.id.cantidad);
            btn_postularme = itemView.findViewById(R.id.btn_postularme);

        }
    }
}
