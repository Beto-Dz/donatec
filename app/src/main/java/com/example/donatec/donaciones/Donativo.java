package com.example.donatec.donaciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donatec.R;
import com.example.donatec.SessionManager;

import java.util.Objects;

/**
 * Clase que representa un donativo para la aplicación.
 * Esta clase es utilizada para crear un objeto de donativo a partir del formulario para agregar donaciones.
 * y para validar si los campos del formulario son correctos.
 */
public class Donativo {
    // atributos
    private String title;
    private String description;
    private String image;
    private int idCategory;
    private int idDonor;
    private byte amount;
    private int idMeasurement;
    private int idmunicipality;
    private SessionManager sessionManager;


    // constructor parametrizado
    public Donativo(String title, String description, String image, int idCategory, byte amount, int idMeasurement, SessionManager sessionManager) {
        this.title = validarCampo(title, 5, 20);
        this.description = validarCampo(description, 5, 255);
        this.image = validarCampo(image, 5, 2147483647);
        this.idCategory = idCategory;
        this.amount = amount;
        this.idMeasurement = idMeasurement;
        this.sessionManager = sessionManager;
        this.idDonor = this.sessionManager.getIdAccount();
        this.idmunicipality = this.sessionManager.getMunicipalityId();
    }

    /**
     * Método para validar los campos del formulario de donaciones.
     *
     * @param campo     campo a validar
     * @param minLength longitud mínima de caracteres del campo
     * @param maxLength longitud máxima de caracteres del campo
     * @return campo si es válido, null si no es válido
     */
    private String validarCampo(String campo, int minLength, int maxLength) {
        return (campo != null && !campo.isBlank() && campo.length() >= minLength && campo.length() <= maxLength) ? campo : null;
    }

    /**
     * Método para validar si los campos del formulario de donaciones son correctos.
     *
     * @return true si los campos son correctos (diferentes de nulo o mayores que 0 segun corresponda), false si no son correctos
     */
    public boolean isValidDonation() {
        return this.title != null && this.description != null && this.image != null && this.idCategory > 0 && this.amount > 0 && this.idMeasurement > 0 && this.idDonor > 0 && this.idmunicipality > 0;
    }

    // getters y setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public int getIdDonor() {
        return idDonor;
    }

    public byte getAmount() {
        return amount;
    }

    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public int getIdMeasurement() {
        return idMeasurement;
    }

    public void setIdMeasurement(int idMeasurement) {
        this.idMeasurement = idMeasurement;
    }

    public int getIdmunicipality() {
        return idmunicipality;
    }

    public static class DonativoFragmentItem extends Fragment {
        private RecyclerView recyclerView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_donativo_item, container, false);

            return view;
        }
    }

    /**
     * Método para comparar dos objetos de tipo Donativo. y determinar si son iguales.
     *
     * @param o objeto a comparar
     * @return true si los objetos son iguales, false si no lo son
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Donativo donativo = (Donativo) o;
        return idCategory == donativo.idCategory && idDonor == donativo.idDonor && amount == donativo.amount && idMeasurement == donativo.idMeasurement && idmunicipality == donativo.idmunicipality && Objects.equals(title, donativo.title) && Objects.equals(description, donativo.description) && Objects.equals(image, donativo.image);
    }

    // Método para obtener el hashcode de un objeto de tipo Donativo.
    // y determinar si dos objetos son iguales.
    @Override
    public int hashCode() {
        return Objects.hash(title, description, image, idCategory, idDonor, amount, idMeasurement, idmunicipality);
    }

    @Override
    public String toString() {
        return "Donativo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", idCategory=" + idCategory +
                ", idDonor=" + idDonor +
                ", amount=" + amount +
                ", idMeasurement=" + idMeasurement +
                ", idmunicipality=" + idmunicipality +
                ", sessionManager=" + sessionManager +
                '}';
    }
}
