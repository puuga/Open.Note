package com.puuga.opennote;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.puuga.opennote.adapter.ProfileAdapter;
import com.puuga.opennote.helper.SettingHelper;
import com.puuga.opennote.manager.APIService;
import com.puuga.opennote.model.Message;
import com.puuga.opennote.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileActivityFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerView;
    private List<Message> messageList;

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
        AnalyticsTrackers.initialize(getActivity());
        AnalyticsTrackers analytics = AnalyticsTrackers.getInstance();
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        mTracker = analytics.get(AnalyticsTrackers.Target.APP);
    }

    private void initInstances(View view) {
        messageList = new ArrayList<>();
        ProfileAdapter profileAdapter = new ProfileAdapter(getActivity(), messageList);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setRefreshing(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(profileAdapter);
    }

    public void setAdapter(List<Message> messageList) {
        this.messageList.clear();
        this.messageList.addAll(messageList);

        ((ProfileAdapter) recyclerView.getAdapter()).setUser(me.name, me.email, me.getUserPictureUrl());
        recyclerView.getAdapter().notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    private void bindUserToWidget() {
        ((ProfileActivity) getActivity()).bindUserToWidget(me);

        List<Message> messageList;
        if (me.messages == null) {
            messageList = new ArrayList<>();
        } else {
            messageList = new ArrayList<>(Arrays.asList(me.messages));
        }
        setAdapter(messageList);
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

    @Override
    public void onRefresh() {
        getMyProfile();
    }
}
