package com.example.donatec;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    // atributos
    private RequestQueue queue;
    private EditText nombre, apellidoPaterno, apellidoMaterno, fechaNacimiento, nombreUsuario, correoElectronico, contrasena, numeroTelefonico, descripcion;
    private AutoCompleteTextView sexo;
    private Button obtenerUbicacionButton, btnRegister;
    private HashMap<String, Integer> genderMap = new HashMap<String, Integer>();
    private String fechaNacimientoString;
    private Ubicacion ubicacionObj;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar atributos y elementos de la vista
        nombre = findViewById(R.id.nombre);
        apellidoPaterno = findViewById(R.id.apellidoPaterno);
        apellidoMaterno = findViewById(R.id.apellidoMaterno);
        nombreUsuario = findViewById(R.id.nombreUsuario);
        correoElectronico = findViewById(R.id.correoElectronico);
        contrasena = findViewById(R.id.contrasena);
        numeroTelefonico = findViewById(R.id.numeroTelefonico);
        btnRegister = findViewById(R.id.registrarButton);
        fechaNacimiento = findViewById(R.id.fechaNacimiento);
        sexo = findViewById(R.id.sexo);
        obtenerUbicacionButton = findViewById(R.id.obtenerUbicacionButton);
        descripcion = findViewById(R.id.descripcion);
        ubicacionObj = new Ubicacion(this);
        queue = Volley.newRequestQueue(this);


        // click para datepicker
        fechaNacimiento.setOnClickListener(v -> mostrarDatePickerDialog());

        setSexo();

        // Configurar el botón de registro
        btnRegister.setOnClickListener(v -> register());

        // Configurar el botón para obtener la ubicación
        obtenerUbicacionButton.setOnClickListener(v -> {
            ubicacionObj.obtenerUbicacion();
        });
    }

    // metodo para mostrar el datepicker
    private void mostrarDatePickerDialog() {
        // Obtén la fecha actual
        final Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        // Crea el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    // Muestra la fecha seleccionada en el EditText
                    fechaNacimientoString = year + "/" + (month + 1) + "/" + dayOfMonth;
                    fechaNacimiento.setText(fechaNacimientoString);
                },
                anio, mes, dia);

        // Opcional: Establece un límite máximo (por ejemplo, la fecha actual)
        datePickerDialog.getDatePicker().setMaxDate(calendario.getTimeInMillis());

        // Muestra el DatePickerDialog
        datePickerDialog.show();
    }

    // metodo para obtener los sexos de la api y cargarlos en el dropdown
    private void setSexo() {
        //  llamada a la api para obtener los datos de los sexos
        String url = getString(R.string.api_base_url) + "genders";

        // realizar la peticion con volley de los datos de los sexos
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // lista para guardar los sexos y mostrarlos en el dropdown
                        List<String> genderList = new ArrayList<String>();
                        try {
                            // obtener los datos de la respuesta
                            JSONArray gendersArray = response.getJSONArray("data");

                            // recorrer el array y agregar los datos al map y a la lista
                            for (int i = 0; i < gendersArray.length(); i++) {
                                JSONObject genderObject = gendersArray.getJSONObject(i);

                                // obtener el nombre y id del genero
                                String genderName = genderObject.getString("name");
                                int genderId = genderObject.getInt("id");

                                // agregar el nombre del genero a la lista
                                genderList.add(genderName);

                                // guardar el id del genero y nombre en el hashmap
                                genderMap.put(genderName, genderId);
                            }

                            // agregar los datos al dropdown
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_dropdown_item_1line, genderList);

                            // setear el adapter al dropdown
                            sexo.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Error al obtener los datos de los sexos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, "Error en la conexión", Toast.LENGTH_SHORT).show();

                    }
                });

        queue.add(jsonObjectRequest);
    }


    // metodo para verificar si google play services esta disponible
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void register() {
        // Aquí se debe realizar la solicitud a la API
        String url = getString(R.string.api_base_url) + "users";

        int genderId = genderMap.get(sexo.getText().toString()) != null ? genderMap.get(sexo.getText().toString()) : 0;

        // Crear un objeto con los datos del usuario
        UsuarioRegistro usuario = new UsuarioRegistro(
                nombre.getText().toString().trim(),
                apellidoPaterno.getText().toString().trim(),
                apellidoMaterno.getText().toString().trim(),
                fechaNacimientoString,
                ubicacionObj.getMunicipio(),
                ubicacionObj.getEstado(),
                genderId,
                nombreUsuario.getText().toString().trim(),
                descripcion.getText().toString().trim(),
                correoElectronico.getText().toString().trim(),
                contrasena.getText().toString().trim(),
                numeroTelefonico.getText().toString().trim()
        );

        // Validar que todos los campos estén llenos
        if (!usuario.validarCampos()) {
            Toast.makeText(this, "Por favor, llena todos los campos debidamente", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, usuario.getUsuarioAsJSON(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int codigo = 0;
                        String mensaje = "";
                        // Procesar la respuesta
                        try {
                            codigo = response.getInt("code");
                            mensaje = response.getString("message");
                            // si el codigo de la respuesta es 200
                            if (codigo == 200) {
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error en el registro: " + mensaje, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "error en la respuesta: " + mensaje, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
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
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

}
