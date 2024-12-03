package com.example.donatec;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilFragment extends Fragment {
    // atributos
    private EditText nombre, apellidoPaterno, apellidoMaterno, fechaNacimiento, correoElectronico, contrasena, numeroTelefonico, descripcion;
    private Chip ubicacion;
    private TextView nombreUsuario;
    private AutoCompleteTextView sexo;
    private Button button;
    private HashMap<String, Integer> genderMap;
    private String fechaNacimientoString;
    private SessionManager sessionManager;
    private RequestQueue listaPeticiones;
    private FloatingActionButton buttonFloating;
    private AlertDialog alertDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_perfil, container, false);

        // obtener los elementos de la vista
        nombre = vista.findViewById(R.id.nombre);
        apellidoPaterno = vista.findViewById(R.id.apellidoPaterno);
        apellidoMaterno = vista.findViewById(R.id.apellidoMaterno);
        fechaNacimiento = vista.findViewById(R.id.fechaNacimiento);
        correoElectronico = vista.findViewById(R.id.correoElectronico);
        contrasena = vista.findViewById(R.id.contrasena);
        numeroTelefonico = vista.findViewById(R.id.numeroTelefonico);
        descripcion = vista.findViewById(R.id.descripcion);
        ubicacion = vista.findViewById(R.id.ubicacion);
        nombreUsuario = vista.findViewById(R.id.nombre_usuario);
        sexo = vista.findViewById(R.id.sexo);
        button = vista.findViewById(R.id.button);
        buttonFloating = vista.findViewById(R.id.logout);

        genderMap = new HashMap<>();
        sessionManager = new SessionManager(getActivity());
        listaPeticiones = Volley.newRequestQueue(getActivity());

        isEnableInputs(false);

        contrasena.setOnClickListener(v -> {
            contrasena.setText("");
        });

        button.setOnClickListener(v -> {
            if (button.getText().toString().equals("Editar")) {
                isEnableInputs(true);
                button.setText("Guardar");
            } else {
                isEnableInputs(false);
                button.setText("Editar");
                updateUserInfo();
            }
        });

        // click para datepicker
        fechaNacimiento.setOnClickListener(v -> mostrarDatePickerDialog());

        // muestra el boton flotante
        buttonFloating.show();

        buttonFloating.setOnClickListener(v -> {
            // Navegar a la pantalla de crear donativo
            alertDialog = new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialog)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estas seguro de que quieres cerrar sesión de tu cuenta?")
                    .setCancelable(false)
                    .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                        // cerrar el diálogo
                        dialogInterface.dismiss();
                    })
                    .setNeutralButton("Aceptar", null)
                    .create();

            // mostrar el dialogo
            alertDialog.show();

            // Configurar el botón "Aceptar" manualmente
            Button aceptarBtn = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            // evento para el boton aceptar
            aceptarBtn.setOnClickListener(View -> {
                Toast.makeText(getActivity(), "Cerrando sesión...", Toast.LENGTH_SHORT).show();

                // crear una peticion al api para cerrar sesion
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_base_url) + "logout", null,
                        response -> {
                            // elimina la sesion del usuario
                            sessionManager.logout();
                            // inicia la actividad de login
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            // cierra la actividad
                            getActivity().finish();
                            return;
                        },
                        error -> {
                            Toast.makeText(getActivity(), "Error en la conexión", Toast.LENGTH_SHORT).show();
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
                alertDialog.dismiss(); // Cierra el diálogo manualmente
            });
        });

        getGenders();
        getUserInformation();

        return vista;
    }

    // metodo para obtener los sexos de la api y cargarlos en el dropdown
    private void getGenders() {
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, genderList);

                            // setear el adapter al dropdown
                            sexo.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Error al obtener los datos de los sexos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Error en la conexión", Toast.LENGTH_SHORT).show();

                    }
                });

        listaPeticiones.add(jsonObjectRequest);
    }

    private void getUserInformation() {
        // endpoint
        String url = getString(R.string.api_base_url) + "getuser/" + sessionManager.getIdAccount();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // obtener los datos del usuario
                        JSONObject userObject = response.getJSONObject("data");
                        Log.d("Volley", userObject.toString());
                        String username = userObject.getString("username");
                        String name = userObject.getString("name");
                        String paternalSurname = userObject.getString("paternal_surname");
                        String maternalSurname = userObject.getString("maternal_surname");
                        String birthdate = userObject.getString("birthdate");
                        String gender = userObject.getString("gender");
                        String email = userObject.getString("email");
                        String password = userObject.getString("password");
                        String phoneNumber = userObject.getString("phone_number");
                        String description = userObject.getString("description");
                        String location = userObject.getString("location");


                        // Formateador para el formato original
                        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

                        // Analizar la fecha al formato ISO 8601
                        ZonedDateTime fecha = ZonedDateTime.parse(birthdate, formatoEntrada);

                        // Formateador para el formato deseado
                        DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                        // Convertir y formatear la fecha
                        fechaNacimientoString = fecha.format(formatoSalida);
                        fechaNacimiento.setText(fechaNacimientoString);

                        // setear los datos en los campos correspondientes
                        nombreUsuario.setText(username);
                        nombre.setText(name);
                        apellidoPaterno.setText(paternalSurname);
                        apellidoMaterno.setText(maternalSurname);
                        sexo.setText(gender);
                        correoElectronico.setText(email);
                        contrasena.setText(password);
                        numeroTelefonico.setText(phoneNumber);
                        descripcion.setText(description);
                        ubicacion.setText(location);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                },
                errror -> {
                    Toast.makeText(getActivity(), "Error en la conexión", Toast.LENGTH_SHORT).show();
                    if (errror.networkResponse != null) {
                        Log.e("Volley", new String(errror.networkResponse.data));
                    }
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

    private void updateUserInfo() {
        int genderId = genderMap.get(sexo.getText().toString()) != null ? genderMap.get(sexo.getText().toString()) : 0;

        UsuarioRegistro usuario = new UsuarioRegistro(
                nombre.getText().toString(),
                apellidoPaterno.getText().toString(),
                apellidoMaterno.getText().toString(),
                fechaNacimientoString,
                genderId,
                descripcion.getText().toString(),
                correoElectronico.getText().toString(),
                contrasena.getText().toString(),
                numeroTelefonico.getText().toString());

        if (!usuario.validarCamposActualizacion()) {
            Toast.makeText(getActivity(), "Por favor, llena todos los campos debidamente", Toast.LENGTH_SHORT).show();
            Log.d("hey", usuario.toString());
            return;
        }

        Toast.makeText(getActivity(), "Actualizando...", Toast.LENGTH_SHORT).show();
        Log.d("hey", usuario.toString());

        String url = getString(R.string.api_base_url) + "update/" + sessionManager.getUsername();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, usuario.getUsuarioActJSON(),
                response -> {
                    getUserInformation();
                    Toast.makeText(getActivity(), "Actualización exitosa", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(getActivity(), "Error en la conexión", Toast.LENGTH_SHORT).show();

                    if (error.networkResponse != null) {
                        Log.e("Volley", new String(error.networkResponse.data));
                    }
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

    // metodo para mostrar el datepicker
    private void mostrarDatePickerDialog() {
        // Obtén la fecha actual
        final Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        // Crea el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
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

    public void isEnableInputs(boolean enable) {
        //bloquear los campos de la vista
        nombre.setEnabled(enable);
        apellidoPaterno.setEnabled(enable);
        apellidoMaterno.setEnabled(enable);
        fechaNacimiento.setEnabled(enable);
        correoElectronico.setEnabled(enable);
        contrasena.setEnabled(enable);
        numeroTelefonico.setEnabled(enable);
        descripcion.setEnabled(enable);
        sexo.setEnabled(enable);
    }
}