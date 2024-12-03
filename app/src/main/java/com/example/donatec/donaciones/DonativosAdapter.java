package com.example.donatec.donaciones;

import static android.provider.Settings.System.getString;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;
import com.example.donatec.inicio.Donacion;
import com.example.donatec.inicio.DonacionAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonativosAdapter extends RecyclerView.Adapter<DonativosAdapter.ViewHolder> {
    // atributos
    private List<Donacion> donativos;
    private SessionManager sessionManager;
    private Activity activity;
    private RequestQueue listaSolicitudes;
    // vista y campos del formulario para agregar un donativo nuevo (layout fragment_formulario_donativo.xml)
    private View vistaFormulario;
    private EditText titulo, descripcion, cantidad;
    private ImageView imagen;
    private Bitmap imagenBitmap;
    // campos de autocompletado para las categorias y medidas
    private AutoCompleteTextView categoria, medida;
    // hashmap para las medidas y categorias (nombre, id)
    private HashMap<String, Integer> medidas, categorias;
    private AlertDialog alertDialog;
    private final IOnDonativoUpdateListener updateListener;
    private View dialogoSolicitudesVista;


    // constructor
    public DonativosAdapter(List<Donacion> donativos, Activity activity, View vistaFormulario, AlertDialog alertDialog, HashMap<String, Integer> medidas, HashMap<String, Integer> categorias, IOnDonativoUpdateListener updateListener) {
        this.donativos = donativos;
        this.activity = activity;
        this.sessionManager = new SessionManager(activity);
        this.listaSolicitudes = Volley.newRequestQueue(activity);
        this.medidas = medidas;
        this.categorias = categorias;

        // Inflar la vista del formulario para agregar un donativo nuevo, guardandola en el atributo vistaFormulario
        this.vistaFormulario = vistaFormulario;
        // Obtener los campos del formulario inflado
        this.titulo = vistaFormulario.findViewById(R.id.titulo);
        this.descripcion = vistaFormulario.findViewById(R.id.descripcion);
        this.cantidad = vistaFormulario.findViewById(R.id.cantidad);
        this.imagen = vistaFormulario.findViewById(R.id.image);
        this.categoria = vistaFormulario.findViewById(R.id.categoria);
        this.medida = vistaFormulario.findViewById(R.id.medida);

        this.alertDialog = alertDialog;
        this.updateListener = updateListener;
    }

    // metodo para setear la bitmap de la imagen
    public void setImagenBitmap(Bitmap imagenBitmap) {
        this.imagenBitmap = imagenBitmap;
    }

    // metodo para convertir una imagen de BITMAP a STRING
    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // metodo que se ejecuta al crear un nuevo ítem (ViewHolder) para el RecyclerView
    @NonNull
    @Override
    public DonativosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla cada ítem (item_list.xml) para generar la vista de cada Pokémon
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_donativo_item, parent, false);

        // Crea un nuevo ViewHolder con la vista inflada
        return new ViewHolder(view);
    }

    // metodo que se ejecuta al mostrar un ítem en el RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtiene el Pokémon en la posición 'position'
        Donacion donativo = donativos.get(position);

        // Actualiza los datos de la vista con el Pokémon
        holder.titulo.setText(donativo.getTitle());
        holder.nombre_usuario.setText(donativo.getUsername());
        Picasso.get().load(this.donativos.get(position).getImageUrl()).into(holder.imagen);
        holder.descripcion.setText(donativo.getDescription());
        holder.categoria.setText(donativo.getCategory());
        holder.ubicacion.setText(donativo.getMunicipality() + ", " + donativo.getState());
        holder.fecha_publicacion.setText(donativo.getPublished_at());
        holder.cantidad.setText(donativo.getMeasurement());

        // si el donativo no está disponible, oculta el botón "postuarme" y "editar" ya que no tiene caso
        if (!donativo.isAvailable()) {
            holder.btn_no_disponible.setVisibility(View.GONE);
            holder.btn_editar.setVisibility(View.GONE);
        } else {
            // Asigna un click listener al botón "No disponible"
            holder.btn_no_disponible.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // mostrar un diálogo de confirmación
                    AlertDialog AlertDialog = new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialog)
                            .setTitle("Marcar NO disponible")
                            .setMessage("¿Estás seguro de que deseas marcar como no disponible tu donativo?. Después no podras cambiarlo.")
                            .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                                String url = v.getContext().getString(R.string.api_base_url) + "donations_update_status/" + donativo.getDonationId();

                                // peticion para marcar como no disponible el donativo
                                JsonObjectRequest request = new JsonObjectRequest(url, null,
                                        response -> {
                                            try {
                                                // mostrar un mensaje de éxito
                                                Toast.makeText(v.getContext(), "Donativo marcado como no disponible", Toast.LENGTH_SHORT).show();
                                                // ocultar el botón "postularme"
                                                holder.btn_no_disponible.setVisibility(View.GONE);
                                            } catch (Exception e) {
                                                Toast.makeText(v.getContext(), "Error al marcar el donativo como no disponible", Toast.LENGTH_SHORT).show();
                                            }
                                        },
                                        error -> {
                                            // mostrar un mensaje de error
                                            Toast.makeText(v.getContext(), "Error al marcar el donativo como no disponible", Toast.LENGTH_SHORT).show();
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

                                // añadir la petición a la cola
                                listaSolicitudes.add(request);
                            })
                            .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                                // cerrar el diálogo
                                dialogInterface.dismiss();
                            }).create();

                    // mostrar el dialogo
                    AlertDialog.show();
                }
            });
        }

        // Asigna un click listener al botón "Editar"
        holder.btn_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // verificar si el AlertDialog ya tiene un padre y removerlo
                if (vistaFormulario.getParent() != null) {
                    ((ViewGroup) vistaFormulario.getParent()).removeView(vistaFormulario);
                }

                // mostrar los datos del donativo en el formulario
                titulo.setText(donativo.getTitle());
                descripcion.setText(donativo.getDescription());
                cantidad.setText(String.valueOf(donativo.getAmount()));
                Picasso.get().load(donativo.getImageUrl()).into(imagen);
                categoria.setText(donativo.getCategory());
                medida.setText(donativo.getMeasurement());
                categoria.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(categorias.keySet())));
                medida.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(medidas.keySet())));

                // mostrar un diálogo de edición
                alertDialog = new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialog)
                        .setTitle("Editar Donativo")
                        .setView(vistaFormulario)
                        .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                            resetFormulario();
                            setImagenBitmap(null);
                            // cerrar el diálogo
                            dialogInterface.dismiss();
                        }).setNeutralButton("Aceptar", null).create();

                // mostrar el dialogo
                alertDialog.show();

                // Configurar el botón "Aceptar" manualmente
                Button aceptarBtn = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                // evento para el boton aceptar
                aceptarBtn.setOnClickListener(View -> {
                    // Verificar si los datos son válidos
                    if (titulo.getText().toString().isEmpty() ||
                            descripcion.getText().toString().isEmpty() ||
                            cantidad.getText().toString().isEmpty() ||
                            categoria.getText().toString().isEmpty() ||
                            medida.getText().toString().isEmpty()) {
                        Toast.makeText(activity, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return; // No cerrar el diálogo
                    }

                    // Validar el formato de cantidad
                    try {
                        Byte.parseByte(cantidad.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(activity, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
                        return; // No cerrar el diálogo
                    }

                    try {
                        // Crear la donación
                        Donativo donativonuevo = new Donativo(
                                titulo.getText().toString(),
                                descripcion.getText().toString(),
                                bitmapToString(imagenBitmap),
                                categorias.get(categoria.getText().toString()),
                                Byte.parseByte(cantidad.getText().toString()),
                                medidas.get(medida.getText().toString()),
                                sessionManager
                        );

                        // Verificar si la donación es válida
                        if (!donativonuevo.isValidDonation()) {
                            Toast.makeText(activity, "Los datos del donativo no son válidos", Toast.LENGTH_SHORT).show();
                            return; // No cerrar el diálogo
                        }

                        Log.d("donativo enviado al servidor: ", donativonuevo.toString());

                        // crear un objeto para la petición
                        JSONObject donativoRequest = new JSONObject();

                        try {
                            donativoRequest.put("image", donativonuevo.getImage());
                            donativoRequest.put("title", donativonuevo.getTitle());
                            donativoRequest.put("description", donativonuevo.getDescription());
                            donativoRequest.put("category_id", donativonuevo.getIdCategory());
                            donativoRequest.put("amount", Byte.parseByte(cantidad.getText().toString()));
                            donativoRequest.put("measurement_id", medidas.get(medida.getText().toString()));

                            // impirmir en consola el objeto donativo
//                            Log.d("Donativo", donativoRequest.toString());

                            // enivar la petición al servidor
                            enviarDonacionServidor(donativoRequest, donativo.getDonationId());
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }

                    } catch (Exception e) {
                        // crear un objeto para la petición
                        JSONObject donativoRequest = new JSONObject();
                        try {
                            donativoRequest.put("title", titulo.getText().toString());
                            donativoRequest.put("description", descripcion.getText().toString());
                            donativoRequest.put("category_id", categorias.get(categoria.getText().toString()));
                            donativoRequest.put("amount", Byte.parseByte(cantidad.getText().toString()));
                            donativoRequest.put("measurement_id", medidas.get(medida.getText().toString()));

                            // impirmir en consola el objeto donativo
                            Log.d("Donativo", donativoRequest.toString());

                            // enivar la petición al servidor
                            enviarDonacionServidor(donativoRequest, donativo.getDonationId());
                        } catch (JSONException jsonException) {
                            Toast.makeText(activity, "Error al enviar la petición", Toast.LENGTH_SHORT).show();
                        }
                    }

                    resetFormulario();
                    setImagenBitmap(null);
                    alertDialog.dismiss(); // Cierra el diálogo manualmente
                });
            }
        });

        // Asigna un click listener al botón "Solicitudes"
        holder.btn_solicitudes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // obtener las solicitudes de la donacion con id x
                getSolicitudes(donativo.getDonationId(), v.getContext());
            }
        });
    }


    @Override
    public int getItemCount() {
        return donativos.size();
    }

    // metodo para resetear los campos del formulario
    public void resetFormulario() {
        this.titulo.setText("");
        this.descripcion.setText("");
        this.cantidad.setText("");
        this.imagen.setImageResource(R.drawable.image);
        this.categoria.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(categorias.keySet())));
        this.medida.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(medidas.keySet())));
    }

    // metodo para enviar la donacion al servidor
    public void enviarDonacionServidor(JSONObject donativoRequest, int donationId) {
        // endpoint para enviar la donación
        String endpoint = activity.getString(R.string.api_base_url) + "donations_update/" + donationId;

        // crear una petición POST
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, endpoint, donativoRequest,
                response -> {
                    // actualizar la lista de donativos
                    if (updateListener != null) {
                        updateListener.onDonativoUpdated();
                        // mostrar un mensaje de éxito
                        Toast.makeText(activity, "Donativo actualizado", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Obtener el mensaje de error
                    String errorMessage;
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode; // Código de respuesta HTTP
                        String responseBody = new String(error.networkResponse.data); // Respuesta del servidor
                        errorMessage = "Error HTTP " + statusCode + ": " + responseBody;
                    } else if (error.getCause() != null) {
                        errorMessage = "Error: " + error.getCause().getMessage();
                    } else {
                        errorMessage = "Error desconocido: " + error.toString();
                    }

                    // Mostrar un mensaje o registrar el error
                    Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("VolleyError", errorMessage, error);
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

        // añadir la petición a la cola
        listaSolicitudes.add(request);
    }

    // metoso para obtener las solicitudes del donativo
//    public void getSolicitudes(int idDonativo) {
//        String url = activity.getString(R.string.api_base_url) + "request/" + idDonativo;
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        JSONArray data = response.getJSONArray("data");
//
//                        // Inflar la vista personalizada del diálogo
//                        View dialogView = LayoutInflater.from(activity).inflate(R.layout.fragment_item_request, null);
//                        TextView nombreUsuarioTextView = dialogView.findViewById(R.id.nombre_usuario);
//                        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup);
//
//                        for (int i = 0; i < data.length(); i++) {
//                            JSONObject donacion = data.getJSONObject(i);
//
//                            String numeroTelefonitoAplicante = donacion.getString("phone_applicant");
//                            String nombreUusarioAplicante = donacion.getString("applicant_username");
//                            // necesito guardar numeroTelefonitoAplicante, nombreUusarioAplicante en una vista
//                            nombreUsuarioTextView.setText(nombreUusarioAplicante);
//                            JSONArray mensajes = donacion.getJSONArray("messages");
//                            for (int j = 0; j < mensajes.length(); j++) {
//                                String mensaje = (String) mensajes.get(j);
//
//                                Chip chip = new Chip(activity);
//                                // Configurar estilos
//                                chip.setChipBackgroundColorResource(R.color.violeta_ultra_bajo); // Color de fondo
//                                chip.setTextColor(activity.getResources().getColor(R.color.white)); // Color del texto
//                                chip.setChipCornerRadius(20); // Esquinas redondeadas (en píxeles)
//                                chip.setChipIconResource(R.drawable.message); // Ícono
//                                chip.setChipIconTintResource(R.color.white); // Color del ícono
//                                chip.setChipStrokeWidth(0); // Ancho del borde (0 si no hay borde)
//                                chip.setText(mensaje); // Texto
//
//                                // necesito agregarlos en un chipgroup
//                                chipGroup.addView(chip);
//                            }
//
//                        }
//
//                        // mostrar un diálogo con las solicitudes
//                        AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialog)
//                                .setTitle("Solicitudes")
//                                .setMessage("No hay solicitudes")
//                                .setView(dialogView)
//                                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
//                                    // cerrar el diálogo
//                                    dialogInterface.dismiss();
//                                }).create();
//
//                        alertDialog.show();
//
//                    } catch (JSONException e) {
//                        Toast.makeText(activity, "algo ha fallado", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    Toast.makeText(activity, "Error de conexión...", Toast.LENGTH_SHORT).show();
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                // Añadir la cabecera Authorization
//                headers.put("Authorization", "Bearer " + sessionManager.getAuthToken());
//                return headers;
//            }
//
//            ;
//        };
//
//        listaSolicitudes.add(request);
//    }


    public void getSolicitudes(int idDonativo, Context context) {
        // endpoint para obtener las solicitudes de la donacion
        String url = activity.getString(R.string.api_base_url) + "request/" + idDonativo;

        // peticion
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // obtener el data
                        JSONArray data = response.getJSONArray("data");

                        // Inflar la vista principal del diálogo
                        View dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_item_request_container, null);
                        // obtiene el LinearLayout
                        LinearLayout container = dialogView.findViewById(R.id.container);

                        // Iterar sobre los objetos del array "data"
                        for (int i = 0; i < data.length(); i++) {
                            // obtener la donacion
                            JSONObject donacion = data.getJSONObject(i);

                            // Inflar una vista personalizada para cada donación
                            View solicitudView = LayoutInflater.from(activity).inflate(R.layout.fragment_item_request, container, false);

                            // Obtener referencias de los elementos en la vista inflada
                            TextView nombreUsuarioTextView = solicitudView.findViewById(R.id.nombre_usuario);
                            ChipGroup chipGroup = solicitudView.findViewById(R.id.chipGroup);
                            Button botonContacto = solicitudView.findViewById(R.id.contacto);

                            // Establecer el nombre del usuario
                            String nombreUusarioAplicante = donacion.getString("applicant_username");
                            nombreUsuarioTextView.setText(nombreUusarioAplicante);

                            // Añadir mensajes al ChipGroup
                            JSONArray mensajes = donacion.getJSONArray("messages");
                            for (int j = 0; j < mensajes.length(); j++) {
                                // obtiene el mensaje
                                String mensaje = mensajes.getString(j);

                                // nuevo chip
                                Chip chip = new Chip(context);
                                chip.setText(mensaje);
                                chip.setChipBackgroundColorResource(R.color.violeta_ultra_bajo);
                                chip.setTextColor(context.getResources().getColor(R.color.white));
                                chip.setChipCornerRadius(20);
                                chip.setChipIconResource(R.drawable.message);
                                chip.setChipIconTintResource(R.color.white);
                                // Configurar el LayoutParams
                                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, // Ancho
                                        ViewGroup.LayoutParams.WRAP_CONTENT  // Alto
                                );
                                chip.setLayoutParams(layoutParams);
                                chipGroup.addView(chip);
                            }

                            // define el telefono
                            String numeroTelefono = "tel:" + donacion.getString("phone_applicant");
                            // texto de boton
                            botonContacto.setText("Contactar a " + nombreUusarioAplicante);

                            // click al boton
                            botonContacto.setOnClickListener(v -> {
                                // Crear un Intent para abrir el marcador telefónico
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                // intento para abrir el telefono
                                intent.setData(Uri.parse(numeroTelefono));

                                // Verificar que exista una app para manejar el Intent antes de ejecutarlo
                                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                                    activity.startActivity(intent);
                                }
                            });


                            // Agregar la vista inflada al contenedor principal
                            container.addView(solicitudView);
                        }

                        // Mostrar el diálogo con las vistas dinámicas
                        AlertDialog alertDialog = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog)
                                .setTitle("Solicitudes")
                                .setMessage("Aquí podrás ver los usuarios que han solicitado tu donativo, además, podrás contactarlos.")
                                .setView(dialogView)
                                .setPositiveButton("Aceptar", (dialogInterface, which) -> dialogInterface.dismiss())
                                .create();

                        alertDialog.show();

                    } catch (JSONException e) {
                        Toast.makeText(activity, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(activity, "Aún no hay solicitudes...", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + sessionManager.getAuthToken());
                return headers;
            }
        };

        listaSolicitudes.add(request);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // atributos
        private TextView titulo;
        private TextView nombre_usuario;
        private ImageView imagen;
        private TextView descripcion;
        private Chip categoria;
        private Chip ubicacion;
        private TextView fecha_publicacion;
        private TextView cantidad;
        private Button btn_no_disponible, btn_editar, btn_solicitudes;


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
            btn_no_disponible = itemView.findViewById(R.id.btn_no_disponible);
            btn_editar = itemView.findViewById(R.id.btn_editar);
            btn_solicitudes = itemView.findViewById(R.id.btn_solicitudes);

        }
    }
}
