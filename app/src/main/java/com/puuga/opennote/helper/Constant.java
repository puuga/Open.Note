package com.puuga.opennote.helper;

import android.content.Context;

import com.puuga.opennote.R;

/**
 * Created by siwaweswongcharoen on 10/4/2015 AD.
 */
public class Constant {
    public static final String FACEBOOK_IS_LOGIN = "FACEBOOK_IS_LOGIN";
    public static final String FACEBOOK_TOKEN = "FACEBOOK_TOKEN";
    public static final String FACEBOOK_ID = "FACEBOOK_ID";
    public static final String FACEBOOK_NAME = "FACEBOOK_NAME";

    public static final String APP_ID = "APP_ID";

    public static final String API_BASE = "http://128.199.208.34/open.note/";
    public static final String API_MESSAGE = "http://128.199.208.34/open.note/messages.php";
    public static final String API_REGISTER_USER = "http://128.199.208.34/open.note/register_user.php";
    public static final String API_SUBMIT_MESSAGE = "http://128.199.208.34/open.note/submit_message.php";
    public static final String API_USER = "http://128.199.208.34/open.note/user.php";

    public static final float MAP_MIN_ZOOM = 5f;

    public static String APP_NAME( Context context) {
        return context.getResources().getString(R.string.app_name);
    }
}
