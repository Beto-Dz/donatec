package com.example.donatec.inicio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Donacion {
    //    atributos
    private int donationId;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private byte amount;
    private String measurement;
    private String published_at;
    private boolean available;
    private String state;
    private String municipality;
    private String username;

    // metodo constructor parametrizado
    public Donacion(int donationId, String title, String category, String description, String imageUrl, byte amount, String measurement, String published_at, boolean available, String state, String municipality, String username) {
        this.donationId = donationId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.amount = amount;
        this.measurement = measurement;
        setPublished_at(published_at);
        this.available = available;
        this.state = state;
        this.municipality = municipality;
        this.username = username;
    }

    // metodos de acceso
    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte getAmount() {
        return amount;
    }

    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getPublished_at() {
        return published_at;
    }

    public void setPublished_at(String published_at) {
        // Formato de fecha recibido del servidor
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
        inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Asegura que sea UTC
        // Formato de fecha deseado
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy - HH:mm 'hrs'", new Locale("es", "ES"));

        try {
            // Parsear la fecha recibida
            Date date = inputFormat.parse(published_at);
            // Convertirla al formato deseado
            this.published_at = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Donativo{" +
                "donationId=" + donationId +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", amount=" + amount +
                ", measurement='" + measurement + '\'' +
                ", published_at='" + published_at + '\'' +
                ", available=" + available +
                ", state='" + state + '\'' +
                ", municipality='" + municipality + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
