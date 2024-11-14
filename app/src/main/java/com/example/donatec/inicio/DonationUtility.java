package com.example.donatec.inicio;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.donatec.R;
import com.example.donatec.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DonationUtility {
    // atributos
    private Activity activity;
    private ArrayList<Donacion> ListaDonaciones;
    private DonacionAdapter adapter;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;
    private String token;
    private boolean isRequestAlreadyMade;

    // Constructor
    public DonationUtility(Activity activity, ArrayList<Donacion> ListaDonaciones, DonacionAdapter adapter, RequestQueue requestQueue, SessionManager sessionManager) {
        this.activity = activity;
        this.ListaDonaciones = ListaDonaciones;
        this.adapter = adapter;
        this.requestQueue = requestQueue;
        this.sessionManager = sessionManager;
        this.token = sessionManager.getAuthToken();
        this.isRequestAlreadyMade = false;
    }


    public void getDonations(String endpoint) {
        Log.d("endpoint: ", endpoint);

        // obtener informacion para validar si ya se ha hecho la petición al endpoint recibido
        isRequestAlreadyMade = sessionManager.isEndpointVisitado(endpoint);

        Log.d("Ya ha sido visitado: ", String.valueOf(isRequestAlreadyMade));

        // Crear una lista para almacenar las donaciones de un endpoint
        List<Donacion> donacionPorEndpoint = new ArrayList<>();

        // limpiar la lista de donaciones global
        ListaDonaciones.clear();

        // Si no se ha hecho la petición anteriormente a ese endpoint
        if (!isRequestAlreadyMade) {

            // Crear una solicitud GET
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Obtener el array "data" del JSON de la respuesta
                                JSONArray dataArray = response.getJSONArray("data");

                                // Recorrer el array y extraer cada donación
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject donacionObj = dataArray.getJSONObject(i);
                                    int donationId = donacionObj.getInt("donation_id");
                                    String title = donacionObj.getString("title");
                                    String category = donacionObj.getString("category");
                                    String description = donacionObj.getString("description");
                                    String imageUrl = activity.getString(R.string.api_resource) + donacionObj.getString("image_url");
                                    byte amount = (byte) donacionObj.getInt("amount");
                                    String measurement = donacionObj.getString("measurement");
                                    String published_at = donacionObj.getString("published_at");
                                    boolean available = donacionObj.getBoolean("available");
                                    String state = donacionObj.getString("state");
                                    String municipality = donacionObj.getString("municipality");
                                    String username = donacionObj.getString("username");
                                    Donacion donacion = new Donacion(donationId, title, category, description, imageUrl, amount, measurement, published_at, available, state, municipality, username);
                                    // Agregar la donación a la lista de enspoint individual
                                    donacionPorEndpoint.add(donacion);
                                }

                                // Agregar las donaciones obtenidas del enpoint a la lista global
                                ListaDonaciones.addAll(donacionPorEndpoint);

                                // Guardar en las preferencias compartidas el endpoint y la informacion obtenida de este
                                sessionManager.setEndpointLlamado(endpoint, donacionPorEndpoint);

                                // Notificar al adaptador que los datos han cambiado
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                Toast.makeText(activity, "Error cargando los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(activity, "No se han encontrado donaciones", Toast.LENGTH_SHORT).show();
                            // si existe un error en la peticion, guarda el endpoint con la lista vacia
                            sessionManager.setEndpointLlamado(endpoint, donacionPorEndpoint);
                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Añadir la cabecera Authorization
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            // Agregar la solicitud a la cola de solicitudes de Volley
            requestQueue.add(request);
        } else {
            // Si ya se ha hecho la petición anteriormente
            // Obtener las donaciones de las preferencias compartidas
            ListaDonaciones.addAll(sessionManager.getDonaciones(endpoint));
            // Notificar al adaptador que los datos han cambiado
            adapter.notifyDataSetChanged();
        }
    }

}
