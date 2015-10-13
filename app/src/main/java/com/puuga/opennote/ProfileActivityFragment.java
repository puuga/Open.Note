package com.puuga.opennote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.manager.APIService;
import com.puuga.opennote.model.Message;
import com.puuga.opennote.model.User;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileActivityFragment extends Fragment {

    TextView tvUserName;
    TextView tvUserEmail;
    TextView tvMessagesCount;
    TextView tvAllMessages;

    User me;

    // Retrofit
    APIService service;

    // SharedPreferences
    SettingHelper settingHelper;

    // Google Analytic
    Tracker mTracker;

    public ProfileActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initRetrofit();
        initSharedPreferences();
        initGoogleAnalytic();

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initInstances(view);
        getMyProfile();

        return view;
    }

    private void initRetrofit() {
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        service = application.getAPIService();
    }

    private void initSharedPreferences() {
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        settingHelper = application.getSettingHelper();
    }

    private void initGoogleAnalytic() {
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    private void initInstances(View view) {
        tvUserName = (TextView) view.findViewById(R.id.tv_user_name);
        tvUserEmail = (TextView) view.findViewById(R.id.tv_user_email);
        tvMessagesCount = (TextView) view.findViewById(R.id.tv_messages_count);
        tvAllMessages = (TextView) view.findViewById(R.id.tv_all_messages);
    }

    private void bindUserToWidget() {
        ((ProfileActivity) getActivity()).bindUserToWidget(me);

        tvUserName.setText(me.name);
        tvUserEmail.setText(me.email);
        tvMessagesCount.setText(getString(R.string.messages_count, me.messages.length));
        String t = "";
        for (Message message : me.messages) {
            t = t.concat(getString(R.string.messages_info,
                    message.getMessage(),
                    message.getLat() + "," + message.getLng(),
                    message.getCreated_at()));
        }
        tvAllMessages.setText(t);
    }

    void getMyProfile() {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Load my profile")
                .build());

        Call<User> call = service.me(settingHelper.getAppId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Response<User> response, Retrofit retrofit) {
                User user = response.body();
                Log.d("me", user.toString());
                me = user;

                bindUserToWidget();
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }
}
