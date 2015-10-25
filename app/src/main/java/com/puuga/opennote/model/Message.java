package com.puuga.opennote.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public class Message {
    public String id;
    public String message;
    public double lat;
    public double lng;
    public User user;
    public String created_at;
    public double distance_from_my_location;

    private Message() {
    }

    public static Message createMessage() {
        return new Message();
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public Message setMessage(String message) {
        this.message = message;
        return this;
    }

    public Message setLat(float lat) {
        this.lat = lat;
        return this;
    }

    public Message setLng(float lng) {
        this.lng = lng;
        return this;
    }

    public Message setUser(User user) {
        this.user = user;
        return this;
    }

    public Message setCreated_at(String created_at) {
        this.created_at = created_at;
        return this;
    }

    public Date getDateCreatedAt() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
                + ", distance_from_my_location:"+ distance_from_my_location
                + ", created_at:" + created_at
                + ", user:" + user;
    }
}
