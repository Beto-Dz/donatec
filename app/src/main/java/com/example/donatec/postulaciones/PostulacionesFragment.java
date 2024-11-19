package com.example.donatec.postulaciones;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostulacionesFragment extends Fragment {
    //Lista de postulaciones
    private ArrayList<PostulationRequest> postulationRequests;
    private RecyclerView recyclerView;
    private PostulationRequestAdapter adapter;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_postulaciones, container, false);

        // Obtener el RecyclerView
        recyclerView = view.findViewById(R.id.recycler_postulaciones);

        // definir una orientación vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Crear la lista de postulaciones
        postulationRequests = new ArrayList<>();

        // Crear el adaptador
        adapter = new PostulationRequestAdapter(postulationRequests);

        // Asignar el adaptador al RecyclerView
        recyclerView.setAdapter(adapter);

        // iniciar la cola de peticiones
        requestQueue = Volley.newRequestQueue(getActivity());

        // Obtener la instancia de SessionManager
        sessionManager = new SessionManager(getActivity());

        // Obtener las postulaciones del servidor
        getPostulaciones();


        return view;
    }

    // Método para obtener las postulaciones del servidor por el nombre de usuario
    public void getPostulaciones() {
        // defnir la URL del endpoint
        String endpoint = getString(R.string.api_base_url) + "getmypostulations/" + sessionManager.getUsername();

        // Crear la petición GET
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                response -> {
                    try {
                        // Obtener el array de postulaciones
                        JSONArray postulationRequestsArray = response.getJSONArray("data");

                        // Limpiar la lista de postulaciones
                        postulationRequests.clear();

                        // Recorrer el array de postulaciones
                        for (int i = 0; i < postulationRequestsArray.length(); i++) {
                            // Obtener el objeto JSON de la postulación
                            JSONObject postulationRequestObject = postulationRequestsArray.getJSONObject(i);
                            int id = postulationRequestObject.getInt("id_donation");
                            String title = postulationRequestObject.getString("title_donation");
                            String imageUrl = getString(R.string.api_resource) + postulationRequestObject.getString("image_url");
                            // Obtener el array de mensajes
                            JSONArray messagesArray = postulationRequestObject.getJSONArray("messages");
                            // Crear un array de strings para los mensajes
                            String messages[] = new String[messagesArray.length()];
                            for (int j = 0; j < messagesArray.length(); j++) {
                                messages[j] = messagesArray.getString(j);
                            }
                            boolean acepted = postulationRequestObject.getBoolean("accepted");
                            String applicant_username = postulationRequestObject.getString("applicant_username");
                            String donorUsername = postulationRequestObject.getString("donor_username");


                            // Crear una nueva postulación
                            // Crear un objeto PostulationRequest
                            PostulationRequest postulationRequest = new PostulationRequest(id, title, imageUrl, messages, acepted, applicant_username, donorUsername);

                            Log.d("PostulacionesFragment", postulationRequest.toString());
                            // Añadir la postulación a la lista
                            postulationRequests.add(postulationRequest);
                        }

                        // Notificar al adaptador que los datos han cambiado
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();

                        // Notificar al adaptador que los datos han cambiado
                        adapter.notifyDataSetChanged();
                    }
                },
                error -> {
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

        // Añadir la petición a la cola
        requestQueue.add(request);
    }
}