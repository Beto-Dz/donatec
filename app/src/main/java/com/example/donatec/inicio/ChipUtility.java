package com.example.donatec.inicio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.donatec.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.donatec.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChipUtility {
    // atributos
    private Activity activity;
    private ChipGroup chipGroup;
    private String endpointCategories;
    private RequestQueue requestQueue;
    private SessionManager sessionManager;
    private DonationUtility donationUtility;
    private Chip chipSelected;
    private String token;


    // constructor parametrizado
    public ChipUtility(Activity activity, ChipGroup chipGroup, RequestQueue requestQueue, SessionManager sessionManager, DonationUtility donationUtility) {
        this.activity = activity;
        this.chipGroup = chipGroup;
        this.endpointCategories = activity.getString(R.string.api_base_url) + "categories";
        this.requestQueue = requestQueue;
        this.sessionManager = sessionManager;
        this.donationUtility = donationUtility;
        this.token = sessionManager.getAuthToken();

        if (chipGroup.getChildCount() == 0) {
            //limpia el chipGroup
            chipGroup.removeAllViews();

            // crear el primer chip para la categoria "Todas"
            getChipTodas();
            getChipsCategories();
        }
    }

    // metodo para agregar los chips al grupo de chips
    private void addChipToGroup(Chip chip) {
        this.chipGroup.addView(chip);
    }

    // crear chip de categoria "todas"
    @SuppressLint("ResourceType")
    private void getChipTodas() {
        // Crear un chip para la categoria "Todas"
        Chip chip = new Chip(new ContextThemeWrapper(activity, com.google.android.material.R.style.Widget_Material3_Chip_Filter));
        chip.setId(100);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setCheckedIconVisible(true);
        chip.setChipCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, activity.getResources().getDisplayMetrics()));
        chip.setChipBackgroundColorResource(R.color.violeta_fuerte);
        chip.setChipStrokeWidth(0);
        chip.setTextColor(activity.getResources().getColor(R.color.white));
        chip.setText("Todas");
        addChipToGroup(chip);

        this.chipSelected = chip;
    }

    // Método para obtener la lista de categorias y crear los chips para agegar al chipGroup
    public void getChipsCategories() {

        //crear una solicitud de Volley para obtener las categorias
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpointCategories, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el array "data" del JSON de la respuesta
                            JSONArray dataArray = response.getJSONArray("data");

                            // crear un chip por cada categoria
                            for (int i = 0; i < dataArray.length(); i++) {
                                // Obtener el objeto JSON de la posición actual
                                JSONObject categoriaObj = dataArray.getJSONObject(i);
                                // Obtener el nombre y el id de la categoria
                                String category = categoriaObj.getString("name");
                                int id = categoriaObj.getInt("id");

                                // Crear un chip por cada categoria
                                Chip chip = new Chip(new ContextThemeWrapper(activity, com.google.android.material.R.style.Widget_Material3_Chip_Filter));
                                chip.setId(id);
                                chip.setCheckable(true);
                                chip.setCheckedIconVisible(true);
                                chip.setChipCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, activity.getResources().getDisplayMetrics()));
                                chip.setChipBackgroundColorResource(R.color.violeta_fuerte);
                                chip.setChipStrokeWidth(0);
                                chip.setTextColor(activity.getResources().getColor(R.color.white));
                                chip.setText(category);
                                addChipToGroup(chip);
                            }

                            agregarChipListener();
                        } catch (JSONException e) {
                            Toast.makeText(activity, "Error cargando los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "error en la conexion: ";

                        if (error.networkResponse != null) {
                            // Agregar código de estado HTTP a la descripción del error
                            errorMessage += "Código HTTP " + error.networkResponse.statusCode + ". ";

                            // Obtener el cuerpo de la respuesta de error, si está disponible
                            if (error.networkResponse.data != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, "UTF-8");
                                    errorMessage += "Respuesta del servidor: " + responseBody;
                                } catch (Exception e) {
                                    errorMessage += "Error al leer el cuerpo de la respuesta.";
                                }
                            }
                        } else if (error.getCause() != null) {
                            // Imprimir la excepción subyacente
                            errorMessage += error.getCause().getMessage();
                        } else {
                            errorMessage += "Error desconocido.";
                        }

                        // Mostrar el mensaje de error detallado en un Toast
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
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
    }

    // metodo para agregar evento de seleccion de un chip
    public void agregarChipListener() {
        // Agregar un listener para el evento de clic en el chip
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chipSelected = (Chip) v;
                    // Obtener el texto del chip seleccionado
                    String category = ((Chip) v).getText().toString();
                    // definir el endpoint para obtener las donaciones por categoria
                    String categoryEnpoint = category.equals("Todas") ? "" : "/" + category;
                    String endpoint = activity.getString(R.string.api_base_url) + "donations/" + sessionManager.getStateUser() + categoryEnpoint;
                    // Llamar al método para obtener la lista de donaciones por categoria
                    donationUtility.getDonations(endpoint, false);
                    // Desckeckear todos los chips excepto el seleccionado
                    uncheckChips(chip.getId());
                }
            });
        }
    }

    // metodo para desckeckear todos los chips del chipGroup excepto el seleccionado
    public void uncheckChips(int id) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getId() != id) {
                // Desckeckear el chip, quitar el click y fondo mas bajo
                chip.setChecked(false);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.violeta_bajo);
                // reactivar el chip por 5000ms
                chip.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chip.setClickable(true);
                        chip.setChipBackgroundColorResource(R.color.violeta_fuerte);
                    }
                }, 1000);
            }
        }
    }

    public String getTextChipSelected() {
        return chipSelected.getText().toString();
    }
}
