package com.puuga.opennote.model;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public class Message {
    private String id;
    private String message;
    private float lat;
    private float lng;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }
}
