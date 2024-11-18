package com.example.donatec.inicio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DonacionAdapter extends RecyclerView.Adapter<DonacionAdapter.ViewHolder> {
    // atributo: Lista de donaciones (fuente de datos) que se van a mostrar en el RecyclerView
    ArrayList<Donacion> listaDonaciones;
    FragmentActivity activity;
    SessionManager sessionManager;
    RequestQueue queue;

    // constructor parametrizado
    public DonacionAdapter(ArrayList<Donacion> listaDonaciones, Activity activity) {
        this.listaDonaciones = listaDonaciones;
        this.activity = (FragmentActivity) activity;
        this.sessionManager = new SessionManager(activity);
        this.queue = Volley.newRequestQueue(activity);
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
    public void onBindViewHolder(@NonNull DonacionAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
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
                // iniciar el fragmento de perfil de usuario

                Toast.makeText(v.getContext(), "Usuario: " + listaDonaciones.get(position).getUsername(), Toast.LENGTH_SHORT).show();

                // instancia de UsernameProfileFragment
                UsernameProfileFragment usernameProfileFragment = new UsernameProfileFragment();

                // pasar el username del usuario a UsernameProfileFragment
                Bundle bundle = new Bundle();

                // pasar el username del usuario a UsernameProfileFragment
                bundle.putString("username", listaDonaciones.get(position).getUsername());

                // asignar los argumentos al fragmento
                usernameProfileFragment.setArguments(bundle);

                // Iniciar la transacción de fragmentos
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.principalFragment, usernameProfileFragment) // Reemplaza R.id.principalFragment con el ID de tu contenedor de fragmentos
                        .addToBackStack(null) // Agrega a la pila para navegar hacia atrás
                        .commit();
            }
        });

        // si el usuario es el mismo que el de la sesión, oculta el botón "postularme"
        if (this.sessionManager.getUsername().equals(this.listaDonaciones.get(position).getUsername())) {
            holder.btn_postularme.setVisibility(View.GONE);
        }

        // Listener para el botón "postularme"
        holder.btn_postularme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflar la vista del layout personalizado
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.fragment_postulacion, null);
                TextInputEditText messageInput = dialogView.findViewById(R.id.message);


                // mostrar un diálogo de confirmación
                AlertDialog dialog = new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialog)
                        .setTitle("Postulación a donativo")
                        .setMessage("¿Estás seguro de que deseas postularte para recibir este donativo?")
                        .setView(dialogView)
                        .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                            // cerrar el diálogo
                            dialogInterface.dismiss();
                        })
                        .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                            try {
                                postularse(listaDonaciones.get(position).getDonationId(), messageInput.getText().toString());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .create();

                // mostrar el dialogo
                dialog.show();

            }
        });
    }

    private void postularse(int idDonativo, String message) throws JSONException {
        // Validar que el mensaje no esté vacío
        if (message.isBlank() || message.length() < 10) {
            Toast.makeText(activity, "El mensaje no puede estar vacío o muy corto", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL de la API
        String endpointPostulacion = activity.getString(R.string.api_base_url) + "donationpostulation/" + idDonativo;

        // Crear un objeto JSON
        // Crear un objeto JSON con los datos de la postulación
        JSONObject postulacion = new JSONObject();
        postulacion.put("message", message);
        postulacion.put("applicant_id", sessionManager.getIdAccount());


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, endpointPostulacion, postulacion, response -> {
            // Mostrar un mensaje de éxito
            Toast.makeText(activity, "Postulación exitosa", Toast.LENGTH_SHORT).show();
        }, error -> {
            // Mostrar el mensaje de error detallado en un Toast
            Toast.makeText(activity, "No se ha podido registrar la postulación.", Toast.LENGTH_LONG).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir la cabecera Authorization
                headers.put("Authorization", "Bearer " + sessionManager.getAuthToken());
                return headers;
            }

            ;
        };

        // Añadir la solicitud a la cola
        queue.add(request);
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
