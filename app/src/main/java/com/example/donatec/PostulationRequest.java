package com.example.donatec;

import java.util.Arrays;

public class PostulationRequest {
    // aributos de la clase
    private int idDonation;
    private String title;
    private String imageURL;
    private String[] messages;
    private boolean acepted;
    private String applicantUsername;
    private String donorUsername;

    // constructor de la clase
    public PostulationRequest(int idDonation, String title, String imageURL, String[] messages, boolean acepted, String applicantUsername, String donorUsername) {
        this.idDonation = idDonation;
        this.title = title;
        this.imageURL = imageURL;
        this.messages = messages;
        this.acepted = acepted;
        this.applicantUsername = applicantUsername;
        this.donorUsername = donorUsername;
    }

    // metodos de acceso

    public int getIdDonation() {
        return idDonation;
    }

    public void setIdDonation(int idDonation) {
        this.idDonation = idDonation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public boolean isAcepted() {
        return acepted;
    }

    public void setAcepted(boolean acepted) {
        this.acepted = acepted;
    }

    public String getApplicantUsername() {
        return applicantUsername;
    }

    public void setApplicantUsername(String applicantUsername) {
        this.applicantUsername = applicantUsername;
    }

    public String getDonorUsername() {
        return donorUsername;
    }

    public void setDonorUsername(String donorUsername) {
        this.donorUsername = donorUsername;
    }

    @Override
    public String toString() {
        return "PostulationRequest{" +
                "idDonation=" + idDonation +
                ", title='" + title + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", messages=" + Arrays.toString(messages) +
                ", acepted=" + acepted +
                ", applicantUsername='" + applicantUsername + '\'' +
                ", donorUsername='" + donorUsername + '\'' +
                '}';
    }
}
