package com.example.passsuvidha;

public class PassRequest {
    private String UID;
    private String institute_name;
    private String source;
    private String destination;
    private String pass_type;
    private String pass_validity;
    private String total_distance;
    private String pass_amount;
    private String auth;

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getInstitute_name() {
        return institute_name;
    }

    public void setInstitute_name(String institute_name) {
        this.institute_name = institute_name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPass_type() {
        return pass_type;
    }

    public void setPass_type(String pass_type) {
        this.pass_type = pass_type;
    }

    public String getPass_validity() {
        return pass_validity;
    }

    public void setPass_validity(String pass_validity) {
        this.pass_validity = pass_validity;
    }

    public String getTotal_distance() {
        return total_distance;
    }

    public void setTotal_distance(String total_distance) {
        this.total_distance = total_distance;
    }

    public String getPass_amount() {
        return pass_amount;
    }

    public void setPass_amount(String pass_amount) {
        this.pass_amount = pass_amount;
    }
}
