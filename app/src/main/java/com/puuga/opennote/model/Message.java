package com.puuga.opennote.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public class Message {
    private String id;
    private String message;
    private float lat;
    private float lng;
    private User user;
    private String created_at;

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

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Date getDateCreatedAt() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(created_at);
        } catch (ParseException e) {
            Log.d("ParseException", e.getMessage());
        }
        return date;
    }

    public String toString() {
        return "message:" + message
                + ", lat:" + lat
                + ", lng:" + lng
                + ", created_at:" + created_at
                + ", user:" + user;
    }
}
