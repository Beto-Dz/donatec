package com.example.donatec.donaciones;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;
import com.example.donatec.inicio.Donacion;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonativosFragment extends Fragment implements  IOnDonativoUpdateListener {
    // atributos
    private RecyclerView recyclerView;
    private List<Donacion> donativos;
    private DonativosAdapter donativosAdapter;
    private SessionManager sessionManager;
    private RequestQueue requestQueue;
    private FloatingActionButton buttonFloating;
    // hashmap para las medidas y categorias
    private HashMap<String, Integer> medidas, categorias;
    // vista y campos del formulario
    private View vistaFormulario;
    private EditText titulo, descripcion, cantidad;
    private ImageView imagen;
    private Bitmap imagenBitmap;
    private AutoCompleteTextView categoria, medida;
    private RequestQueue listaPeticiones;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;
    private AlertDialog AlertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_donativos, container, false);

        // Inicializar la lista de peticiones
        listaPeticiones = Volley.newRequestQueue(getActivity());

        // Inicializar los hashmap
        medidas = new HashMap<>();
        categorias = new HashMap<>();

        // Inicializar el session manager
        sessionManager = new SessionManager(getActivity());

        // Inflar la vista del formulario
        this.vistaFormulario = inflater.inflate(R.layout.fragment_formulario_donativo, container, false);
        // Obtener los campos del formulario
        this.titulo = vistaFormulario.findViewById(R.id.titulo);
        this.descripcion = vistaFormulario.findViewById(R.id.descripcion);
        this.cantidad = vistaFormulario.findViewById(R.id.cantidad);
        this.imagen = vistaFormulario.findViewById(R.id.image);
        this.categoria = vistaFormulario.findViewById(R.id.categoria);
        this.medida = vistaFormulario.findViewById(R.id.medida);


        // evento al clickear la imagen
        imagen.setOnClickListener(v -> {
            // abrir la camara del celular
            openCamera();
        });

        // obtener las medidas y categorias
        getMedidas();
        getCategorias();

        // Inicializa el RecyclerView
        recyclerView = view.findViewById(R.id.recycler_postulaciones);

        // Inicializar el boton flotante
        buttonFloating = view.findViewById(R.id.fab);

        // muestra el boton flotante
        buttonFloating.show();

        // evento para el boton flotante
        buttonFloating.setOnClickListener(v -> {
            // Antes de configurar el AlertDialog se debe verificar si la vista ya tiene un padre
            // Si tiene un padre, se debe remover para evitar un error
            if (vistaFormulario.getParent() != null) {
                ((ViewGroup) vistaFormulario.getParent()).removeView(vistaFormulario);
            }

            // Navegar a la pantalla de crear donativo
            AlertDialog = new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialog)
                    .setTitle("Agregar donativo")
                    .setMessage("logica para agregar un donativo")
                    .setView(vistaFormulario)
                    .setCancelable(false)
                    .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                        resetFormulario();
                        donativosAdapter.setImagenBitmap(null);
                        // cerrar el diálogo
                        dialogInterface.dismiss();
                    })
                    .setNeutralButton("Aceptar", null)
                    .create();

            // mostrar el dialogo
            AlertDialog.show();

            // Configurar el botón "Aceptar" manualmente
            Button aceptarBtn = AlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            // evento para el boton aceptar
            aceptarBtn.setOnClickListener(View -> {
                // Verificar si se ha seleccionado una imagen
                if (this.imagenBitmap == null) {
                    Toast.makeText(getActivity(), "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show();
                    return; // No cerrar el diálogo
                }

                // Verificar si los datos son válidos
                if (titulo.getText().toString().isEmpty() ||
                        descripcion.getText().toString().isEmpty() ||
                        cantidad.getText().toString().isEmpty() ||
                        categoria.getText().toString().isEmpty() ||
                        medida.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return; // No cerrar el diálogo
                }

                // Validar el formato de cantidad
                try {
                    Byte.parseByte(cantidad.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
                    return; // No cerrar el diálogo
                }

                // Crear la donación
                Donativo donativo = new Donativo(
                        titulo.getText().toString(),
                        descripcion.getText().toString(),
                        bitmapToString(imagenBitmap),
                        categorias.get(categoria.getText().toString()),
                        Byte.parseByte(cantidad.getText().toString()),
                        medidas.get(medida.getText().toString()),
                        sessionManager
                );

                // Verificar si la donación es válida
                if (!donativo.isValidDonation()) {
                    Toast.makeText(getActivity(), "Los datos del donativo no son válidos", Toast.LENGTH_SHORT).show();
                    return; // No cerrar el diálogo
                }

                // Si todos los campos son válidos, envía la donación al servidor
                enviarDonacionServidor(donativo);
                resetFormulario();
                donativosAdapter.setImagenBitmap(null);
                AlertDialog.dismiss(); // Cierra el diálogo manualmente
            });


        });

        // Definir el layout manager como lista vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // inicializar la lista
        donativos = new ArrayList<>();


        // Crea un nuevo adaptador
        donativosAdapter = new DonativosAdapter(donativos, getActivity(), vistaFormulario, AlertDialog, medidas, categorias, this);

        // Asignar el adaptador al RecyclerView
        recyclerView.setAdapter(donativosAdapter);

        // Asignar el adaptador al RecyclerView
        sessionManager = new SessionManager(getActivity());

        // Obtener la cola de solicitudes de Volley
        this.requestQueue = Volley.newRequestQueue(getActivity());

        // metodo para hacer la petificación de los donativos
        getDonativos();

        return view;
    }

    // metodo para obtener los donativos del usuario logueado
    public void getDonativos() {
        String endpoint = getString(R.string.api_base_url) + "donations_user/" + sessionManager.getUsername();

        // Crear una petición GET
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                response -> {
                    try {
                        // Obtener el array "data" del JSON de la respuesta
                        JSONArray dataArray = response.getJSONArray("data");

                        // limpiar la lista de donativos
                        donativos.clear();

                        // Recorrer el array y extraer cada donación
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject donacionObj = dataArray.getJSONObject(i);
                            int donationId = donacionObj.getInt("donation_id");
                            String title = donacionObj.getString("title");
                            String category = donacionObj.getString("category");
                            String description = donacionObj.getString("description");
                            String imageUrl = getString(R.string.api_resource) + donacionObj.getString("image_url");
                            byte amount = (byte) donacionObj.getInt("amount");
                            String measurement = donacionObj.getString("measurement");
                            String published_at = donacionObj.getString("published_at");
                            boolean available = donacionObj.getBoolean("available");
                            String state = donacionObj.getString("state");
                            String municipality = donacionObj.getString("municipality");
                            String username = donacionObj.getString("username");
                            Donacion donacion = new Donacion(donationId, title, category, description, imageUrl, amount, measurement, published_at, available, state, municipality, username);
                            // Agregar la donación a la lista de enspoint individual
                            donativos.add(donacion);
                        }

                        // Notificar al adaptador que se ha actualizado la lista
                        donativosAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Error al obtener los donativos", Toast.LENGTH_SHORT).show();

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


        requestQueue.add(request);
    }

    // metodo para obtener las medidas y categorias
    public void getMedidas() {
        String endpoint = getString(R.string.api_base_url) + "measurements";

        // Crear una petición GET
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                response -> {
                    try {
                        // Obtener el array "data" del JSON de la respuesta
                        JSONArray dataArray = response.getJSONArray("data");

                        // Recorrer el array y extraer cada donación
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject donacionObj = dataArray.getJSONObject(i);
                            int measurementId = donacionObj.getInt("id");
                            String measurement = donacionObj.getString("name");
                            // agregar a al hashmap de medidas
                            medidas.put(measurement, measurementId);
                        }

                        // crear un adaptador para las medidas
                        ArrayAdapter<String> adapterMedidas = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>(medidas.keySet()));
                        // asignar el adaptador a la medida
                        medida.setAdapter(adapterMedidas);
                        // impedir que el usuario escriba en el campo de medida
                        medida.setKeyListener(null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Error al obtener las medidas", Toast.LENGTH_SHORT).show();

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

        listaPeticiones.add(request);
    }

    // metodo para obtener las categorias
    public void getCategorias() {
        String endpoint = getString(R.string.api_base_url) + "categories";

        // Crear una petición GET
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                response -> {
                    try {
                        // Obtener el array "data" del JSON de la respuesta
                        JSONArray dataArray = response.getJSONArray("data");

                        // Recorrer el array y extraer cada donación
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject donacionObj = dataArray.getJSONObject(i);
                            int categoryId = donacionObj.getInt("id");
                            String category = donacionObj.getString("name");
                            // agregar a la lista de categorias
//                            listaCategorias.add(category);
                            // agregar a al hashmap de categorias
                            categorias.put(category, categoryId);
                        }

                        // crear un adaptador para las categorias
                        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>(categorias.keySet()));
                        // asignar el adaptador a la categoria
                        categoria.setAdapter(adapterCategorias);

                        // impedir que el usuario escriba en el campo de categoria
                        categoria.setKeyListener(null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Error al obtener las categorias", Toast.LENGTH_SHORT).show();
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

        listaPeticiones.add(request);
    }

    // metood para abrir la camara
    public void openCamera() {
        // Verificar si la aplicación tiene permisos de cámara
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            // Crear un intent para abrir la cámara
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Iniciar la actividad de la cámara
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);


        } else {
            // Solicitar permisos de cámara
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
        }
    }

    @Override
    // metodo para obtener la imagen de la camara
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Verificar si la solicitud fue exitosa
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            // Obtener la imagen de la cámara
            this.imagenBitmap = (Bitmap) data.getExtras().get("data");
            // Mostrar la imagen en el ImageView
            imagen.setImageBitmap(imagenBitmap);
            donativosAdapter.setImagenBitmap(imagenBitmap);
        }
    }

    // metodo para convertir una imagen de BITMAP a STRING
    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // metodo para resetear los campos del formulario
    public void resetFormulario() {
        this.titulo.setText("");
        this.descripcion.setText("");
        this.cantidad.setText("");
        this.imagen.setImageResource(R.drawable.image);
        this.categoria.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>(categorias.keySet())));
        this.medida.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>(medidas.keySet())));
    }


    public void enviarDonacionServidor(Donativo donativo) {
        // Crear un objeto JSON con los datos del donativo
        JSONObject jsonDonativo = new JSONObject();
        try {
            jsonDonativo.put("title", donativo.getTitle());
            jsonDonativo.put("description", donativo.getDescription());
            jsonDonativo.put("image", donativo.getImage());
            jsonDonativo.put("amount", donativo.getAmount());
            jsonDonativo.put("donor_account_id", donativo.getIdDonor());
            jsonDonativo.put("category_id", donativo.getIdCategory());
            jsonDonativo.put("municipality_id", donativo.getIdmunicipality());
            jsonDonativo.put("type_measurement_id", donativo.getIdMeasurement());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear una petición POST
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_base_url) + "donations", jsonDonativo,
                response -> {
                    // Mostrar un mensaje de éxito
                    Toast.makeText(getActivity(), "Donativo agregado correctamente", Toast.LENGTH_SHORT).show();
                    getDonativos();
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
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("VolleyError", errorMessage, error);

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir la cabecera Authorization
                headers.put("Authorization", "Bearer " + sessionManager.getAuthToken());
                return headers;
            }
        };

        // Agregar la petición a la cola de solicitudes
        requestQueue.add(request);
    }


    @Override
    public void onDonativoUpdated() {
        getDonativos();
    }
}