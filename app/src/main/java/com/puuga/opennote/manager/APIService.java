package com.puuga.opennote.manager;

import com.puuga.opennote.helper.Constant;
import com.puuga.opennote.model.Message;
import com.puuga.opennote.model.User;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public interface APIService {

    @GET(Constant.API_MESSAGE)
    Call<Message[]> loadMessages();

    @POST(Constant.API_REGISTER_USER)
    Call<User> registerUser(@Body User user);

    @FormUrlEncoded
    @POST(Constant.API_REGISTER_USER)
    Call<User> registerUser(@Field("firstname") String firstname,
                            @Field("lastname") String lastname,
                            @Field("name") String name,
                            @Field("email") String email,
                            @Field("facebook_id") String facebook_id);

    @FormUrlEncoded
    @POST(Constant.API_SUBMIT_MESSAGE)
    Call<Message> submitMessage(@Field("user_id") String user_id,
                            @Field("message") String message,
                            @Field("lat") String lat,
                            @Field("lng") String lng);
}
