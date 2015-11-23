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
import retrofit.http.Query;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public interface APIService {

    @GET(Constant.API_MESSAGE)
    Call<Message[]> loadMessages();

    @GET(Constant.API_MESSAGE)
    Call<Message[]> loadMessages(@Query("lat") String lat,
                                 @Query("lng") String lng);

    @GET(Constant.API_DELETE_MESSAGE)
    Call<Message[]> deleteMessages(@Query("user_id") String user_id,
                                 @Query("message_id") String message_id);

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

    @GET(Constant.API_USER)
    Call<User> me(@Query("id") String id);
}
