package com.puuga.opennote.model;

/**
 * Created by siwaweswongcharoen on 10/6/2015 AD.
 */
public class User {
    private String id;
    private String firstname;
    private String lastname;
    private String name;
    private String email;
    private String facebookId;

    public String toString() {
        return "id: " + id
                + ", firstname:" + firstname
                + ", lastname: " + lastname
                + ", name: " + name
                + ", email: " + email
                + ", facebookId: " + facebookId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}
