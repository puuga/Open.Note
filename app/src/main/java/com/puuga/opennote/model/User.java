package com.puuga.opennote.model;

/**
 * Created by siwaweswongcharoen on 10/6/2015 AD.
 */
public class User {
    public String id;
    public String firstname;
    public String lastname;
    public String name;
    public String email;
    public String facebook_id;
    public Message[] messages;

    private User() {
    }

    public static User createUser() {
        return new User();
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public User setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public User setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setFacebookId(String facebookId) {
        this.facebook_id = facebookId;
        return this;
    }

    public User setMessage(Message[] messages) {
        this.messages = messages;
        return this;
    }

    public String getUserPictureUrl() {
        if (facebook_id == null) {
            return null;
        }
        return "https://graph.facebook.com/" + facebook_id + "/picture?type=large";
    }

    public String toString() {
        int messagesCount = messages == null ? -1 : messages.length;
        return "id: " + id
                + ", firstname:" + firstname
                + ", lastname: " + lastname
                + ", name: " + name
                + ", email: " + email
                + ", facebookId: " + facebook_id
                + ", userPictureUrl: " + getUserPictureUrl()
                + ", messages:" + messagesCount;
    }
}
