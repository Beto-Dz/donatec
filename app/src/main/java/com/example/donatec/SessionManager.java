package com.example.donatec;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.donatec.inicio.Donacion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "login_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ENDPOINTS_CALLED = "endpoints_called";
    private static final String KEY_CATEGORIES = "categories";
    private Gson gson;
    Map<String, List<Donacion>> mapEndpointsCalled;

    public SessionManager(Context context) {
        // Inicializa las preferencias compartidas y el editor
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Inicializa Gson
        gson = new Gson();

        // obtiene el JSON de los endpoints llamados previamente en las preferencias compartidas y si no hay regresa un null
        String json = sharedPreferences.getString(SessionManager.KEY_ENDPOINTS_CALLED, null);
        String categoriesJson = sharedPreferences.getString(SessionManager.KEY_CATEGORIES, null);

        // Si el JSON es nulo, asigna un objeto vacío
        if (json == null) {
            mapEndpointsCalled = new HashMap<>();
        } else {
            // Obtiene el tipo de datos de un Map<String, List<Donation>> que maneja Gson para deserializar el JSON
            Type type = new TypeToken<Map<String, List<Donacion>>>() {}.getType();

            // Inicializa el Map con los endpoints llamados previamente (deserializa el JSON)
            mapEndpointsCalled = gson.fromJson(json, type);
        }
    }

    public void createLoginSession(boolean isLoggedIn, int id_person, int id_account, String name, String paternal_surname, String maternal_surname, String state_user, int municipality_id, String username, String email, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putInt("id_person", id_person);
        editor.putInt("id_account", id_account);
        editor.putString("name", name);
        editor.putString("paternal_surname", paternal_surname);
        editor.putString("maternal_surname", maternal_surname);
        editor.putString("state_user", state_user);
        editor.putInt("municipality_id", municipality_id);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("authToken", token);

        // serializa el Map a JSON y lo guarda en la cadena endpointsJson
        String endpointsJson = gson.toJson(mapEndpointsCalled);

        // Guarda la cadena JSON en las preferencias compartidas
        editor.putString(KEY_ENDPOINTS_CALLED, endpointsJson);

        // Aplica los cambios
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getIdPerson() {
        return sharedPreferences.getInt("id_person", 0);
    }

    public int getIdAccount() {
        return sharedPreferences.getInt("id_account", 0);
    }

    public String getName() {
        return sharedPreferences.getString("name", null);
    }

    public String getPaternalSurname() {
        return sharedPreferences.getString("paternal_surname", null);
    }

    public String getMaternalSurname() {
        return sharedPreferences.getString("maternal_surname", null);
    }

    public String getUsername() {
        return sharedPreferences.getString("username", null);
    }

    public String getStateUser() {
        return sharedPreferences.getString("state_user", null);
    }

    public int getMunicipalityId() {
        return sharedPreferences.getInt("municipality_id", 0);
    }

    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    public String getAuthToken() {
        return sharedPreferences.getString("authToken", null);
    }

    public boolean isEndpointVisitado(String endpoint) {
        return mapEndpointsCalled.containsKey(endpoint);
    }

    public void setEndpointLlamado(String endpointLlamado, List<Donacion> donaciones) {
        // agregar el endpoint llamado al Map con las donaciones
        mapEndpointsCalled.put(endpointLlamado, donaciones);

        // serializa el Map a JSON y lo guarda en la cadena endpointsJson
        String endpointsJson = gson.toJson(mapEndpointsCalled);

        // Guarda la cadena JSON en las preferencias compartidas
        editor.putString(KEY_ENDPOINTS_CALLED, endpointsJson);

        // Aplica los cambios
        editor.apply();
    }

    public void clearEndpoint(String endpoint) {
        // Eliminar el endpoint del mapa de endpoints llamados
        mapEndpointsCalled.remove(endpoint);
        String endpointsJson = gson.toJson(mapEndpointsCalled);
        editor.putString(KEY_ENDPOINTS_CALLED, endpointsJson);
        editor.apply();
    }

    public List<Donacion> getDonaciones(String endpoint) {
        return mapEndpointsCalled.get(endpoint);
    }

    public void clearEndpoints() {
        // Limpiar el mapa de endpoints llamados
        mapEndpointsCalled.clear();
        String endpointsJson = gson.toJson(mapEndpointsCalled);
        editor.putString(KEY_ENDPOINTS_CALLED, endpointsJson);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}


