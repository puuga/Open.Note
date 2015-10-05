package com.puuga.opennote.manager;

import com.puuga.opennote.helper.Constant;
import com.puuga.opennote.model.Message;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by siwaweswongcharoen on 10/5/2015 AD.
 */
public interface APIService {

    @GET(Constant.API_MESSAGE)
    Call<Message[]> loadMessages();
}
