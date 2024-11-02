package com.example.donatec.inicio;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class InicioFragment extends Fragment {
    private ArrayList<Donacion> ListaDonaciones;
    private RecyclerView recyclerView;
    private DonacionAdapter adapter;
    private RequestQueue requestQueue;
    private ChipGroup chipGroupInicio;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        // Obtener la cola de solicitudes de Volley
        requestQueue = Volley.newRequestQueue(getActivity());

        // Obtener la referencia al RecyclerView
        recyclerView = view.findViewById(R.id.recycler_inicio);

        // Obtener la referencia al ChipGroup
        chipGroupInicio = view.findViewById(R.id.chipGroupInicio);

        getChipGroup();

        // Definir el layout manager como lista vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // inicializar la lista de donaciones
        ListaDonaciones = new ArrayList<>();
        adapter = new DonacionAdapter(ListaDonaciones);
        // Asignar el adaptador al RecyclerView
        recyclerView.setAdapter(adapter);

        // Llamar al método para obtener la lista de Pokémon
        getPokemonList();

        return view;
    }

    // Método para obtener la lista de donaciones
    private void getPokemonList() {
        // estado del pais donde se encuentra el usuario
        String estado = "Puebla";
        String url = getString(R.string.api_base_url) + "donations/" + estado;

        // Crear una solicitud de Volley para obtener los datos de las donaciones
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el array "data" del JSON de la respuesta
                            JSONArray dataArray = response.getJSONArray("data");

                            Log.d("Response", response.toString());

                            // Limpiar la lista de donaciones
                            ListaDonaciones.clear();

                            // Recorrer el array y extraer cada donación
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject donacionObj = dataArray.getJSONObject(i);

                                // Obtener los datos de la donación
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

                                // Crear un objeto Donacion y agregarlo a la lista
                                Donacion donacion = new Donacion(donationId, title, category, description, imageUrl, amount, measurement, published_at, available, state, municipality, username);
                                ListaDonaciones.add(donacion);
                            }

                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Error cargando los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("Error", error.toString());
                    }
                });

        // Agregar la solicitud a la cola de solicitudes de Volley
        requestQueue.add(request);
    }

    // Método para obtener la lista de categorias y crear los chips para agegar al chipGroup
    public void getChipGroup() {
        String url = getString(R.string.api_base_url) + "categories";

        //crear una solicitud de Volley para obtener las categorias
        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el array "data" del JSON de la respuesta
                            JSONArray dataArray = response.getJSONArray("data");
                            Log.d("Response", response.toString());

                            // crear un chip por cada categoria
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject categoriaObj = dataArray.getJSONObject(i);
                                String category = categoriaObj.getString("name");
                                int id = categoriaObj.getInt("id");

                                Chip chip = new Chip(new ContextThemeWrapper(getActivity(), com.google.android.material.R.style.Widget_Material3_Chip_Filter));
                                chip.setId(id);
                                chip.setCheckable(true);
                                chip.setCheckedIconVisible(true);
                                chip.setChipCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
                                chip.setChipBackgroundColorResource(R.color.violeta_fuerte);
                                chip.setChipStrokeWidth(0);
                                chip.setTextColor(getResources().getColor(R.color.white));
                                chip.setText(category);
                                chipGroupInicio.addView(chip);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Error cargando los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("Error", error.toString());
                    }
                });

        // Agregar la solicitud a la cola de solicitudes de Volley
        requestQueue.add(request2);
    }


}