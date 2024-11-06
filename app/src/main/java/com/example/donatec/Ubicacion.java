package com.example.donatec;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Ubicacion {
    // atributos
    private FusedLocationProviderClient fusedLocationClient;
    private String municipio;
    private String estado;
    //    private Context context;
    private Activity activity;

    // Constructor parametrizado
    public Ubicacion(Activity context) {
        this.activity = context;
    }

    // metodo para obtener la ubicacion del dispositivo
    public void obtenerUbicacion() {
        if (isGooglePlayServicesAvailable()) {
            setupGoogleLocationClient();
        } else {
            setupLocationManager();
        }
    }

    // metodo para verificar si google play services esta disponible
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.activity);
        return resultCode == ConnectionResult.SUCCESS;
    }


    // metodo para obtener el nombre de municipio y estado de las coordenadas de la ubicacion
    private void obtenerMunicipioYEstado(double latitude, double longitude) {
        Toast.makeText(this.activity, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();
        // Crear un objeto Geocoder para obtener la dirección a partir de las coordenadas
        Geocoder geocoder = new Geocoder(this.activity, new Locale("es", "MX"));
        try {
            // Obtener la dirección a partir de las coordenadas
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            // Si se encontraron direcciones
            if (addresses != null && !addresses.isEmpty()) {
                // Obtener la primera dirección
                Address address = addresses.get(0);
                // Obtener el municipio
                this.municipio = address.getSubAdminArea();
                // Obtener el estado
                this.estado = address.getAdminArea();

                Toast.makeText(this.activity, "Ubicación obtenida: " + this.municipio + ", " + this.estado, Toast.LENGTH_SHORT).show();

                // Mostrar la ubicación en los TextView correspondientes
                TextView estadotxt = activity.findViewById(R.id.estado);
                TextView municipiotxt = activity.findViewById(R.id.municipio);
                estadotxt.setText(this.estado);
                municipiotxt.setText(this.municipio);
            } else {
                Toast.makeText(this.activity, "No se encontraron datos de ubicación", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.activity, "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // metodo para configurar el cliente de ubicacion de google
    private void setupGoogleLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity);

        if (ActivityCompat.checkSelfPermission(this.activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos
            ActivityCompat.requestPermissions((Activity) this.activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        obtenerMunicipioYEstado(location.getLatitude(), location.getLongitude());
                    } else {

                        Toast.makeText(this.activity, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this.activity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // metodo para configurar el location manager para obtener la ubicacion del dispositivo si no se tiene google play services
    private void setupLocationManager() {
        LocationManager locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);

        // verifica si los permisos de ubicacion estan concedidos si no, solicita los permisos
        if (ActivityCompat.checkSelfPermission(this.activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            Toast.makeText(this.activity, "Por favor activa el GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    obtenerMunicipioYEstado(location.getLatitude(), location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        }, null);
    }

    public String getMunicipio() {
        return municipio;
    }


    public String getEstado() {
        return estado;
    }
}
