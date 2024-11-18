package com.example.donatec.inicio;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsernameProfileFragment extends Fragment {
    private TextView nombre, nombre_usuario, descripcion;
    private Chip ubicacion, genero;
    private ArrayList<Donacion> donaciones;
    DonacionAdapter adapter;
    private RecyclerView recycler;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;
    String token;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_username_profile, container, false);

        // relacionando los elementos de la vista con las variables
        nombre = view.findViewById(R.id.nombre);
        nombre_usuario = view.findViewById(R.id.nombre_usuario);
        descripcion = view.findViewById(R.id.descripcion);
        ubicacion = view.findViewById(R.id.ubicacion);
        genero = view.findViewById(R.id.genero);
        recycler = view.findViewById(R.id.recycler);
        // Definir el layout manager como lista vertical
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // inicializar la lista de donaciones
        donaciones = new ArrayList<>();

        // inicializar el adaptador del recycler con la lista de donaciones
        adapter = new DonacionAdapter(donaciones, getActivity());

        // Asignar el adaptador al RecyclerView
        recycler.setAdapter(adapter);

        // inicializar la cola de peticiones
        requestQueue = Volley.newRequestQueue(getActivity());

        // inicializar el SessionManager
        sessionManager = new SessionManager(getActivity());

        // obtener el token de la sesion
        token = sessionManager.getAuthToken();

        // obtener el username desde los argumentos
        Bundle args = getArguments();

        // verificar si los argumentos no son nulos
        if (args != null) {
            // obtener el username desde los argumentos
            String usernameText = args.getString("username");
            // asignar el username al TextView
            getUserData(usernameText);
            getUserDonations(usernameText);
        }

        return view;
    }


    // metodo para obtener la data del usuario seleccionado
    private void getUserData(String username) {

        // endpoint para obtener la data del usuario
        String endpointDataUser = getString(R.string.api_base_url) + "show/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpointDataUser, null,
                response -> {
                    try {
                        // obtener el objeto "data" del JSON de la respuesta
                        JSONObject data = response.getJSONObject("user");

                        // obtener la data del usurio
                        String name = data.getString("name") + " " + data.getString("paternal_surname");
                        String usernameRequest = data.getString("username");
                        String description = data.getString("description");
                        String location = data.getString("municipality") + ", " + data.getString("state");
                        String gender = data.getString("gender");

                        // asignar la data del usuario a los TextViews
                        this.nombre.setText(name);
                        this.nombre_usuario.setText(usernameRequest);
                        this.descripcion.setText(description);
                        this.ubicacion.setText(location);
                        this.genero.setText(gender);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getActivity(), "Error al obtener la data del usuario", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir la cabecera Authorization
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // agregar la solicitud a la cola de peticiones
        requestQueue.add(request);
    }

    // metodo para obtener las donaciones del usuario
    private void getUserDonations(String username) {
        // endpoint para obtener las donaciones del usuario
        String endpointDonations = getString(R.string.api_base_url) + "donations_user/" + username;

        donaciones.clear();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpointDonations, null,
                response -> {
                    try {
                        // obtener el array "donations" del JSON de la respuesta
                        JSONArray donations = response.getJSONArray("data");

                        // recorrer el array de donaciones
                        for (int i = 0; i < donations.length(); i++) {
                            JSONObject donation = donations.getJSONObject(i);
                            // obtener la data de la donacion
                            int donationId = donation.getInt("donation_id");
                            String title = donation.getString("title");
                            String category = donation.getString("category");
                            String description = donation.getString("description");
                            String imageUrl = getString(R.string.api_resource) + donation.getString("image_url");
                            int amount = donation.getInt("amount");
                            String measurement = donation.getString("measurement");
                            String published_at = donation.getString("published_at");
                            boolean available = donation.getBoolean("available");
                            String state = donation.getString("state");
                            String municipality = donation.getString("municipality");
                            String usernameDonation = donation.getString("username");

                            // crear una instancia de Donacion
                            Donacion donacion = new Donacion(donationId, title, category, description, imageUrl, (byte) amount, measurement, published_at, available, state, municipality, usernameDonation);

                            // agregar la donacion a la lista de donaciones
                            donaciones.add(donacion);
                        }

                        // notificar al adaptador que los datos han cambiado
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // manejar el error
                    adapter.notifyDataSetChanged();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir la cabecera Authorization
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // agregar la solicitud a la cola de peticiones
        requestQueue.add(request);
    }
}