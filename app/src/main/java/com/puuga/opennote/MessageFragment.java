package com.puuga.opennote;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puuga.opennote.adapter.MessageAdapter;
import com.puuga.opennote.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private OnMessageFragmentRefresh mOnMessageFragmentRefresh;

    SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerView;
    private List<Message> messageList;

    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initInstances(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnMessageFragmentRefresh = (OnMessageFragmentRefresh) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnMessageFragmentRefresh = null;
    }

    private void initInstances(View view) {
        messageList = new ArrayList<>();

        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), messageList);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        swipeLayout.setOnRefreshListener(this);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(messageAdapter);
    }

    public void setAdapter(List<Message> messageList) {
        if (this.messageList == null || recyclerView == null) {
            initInstances(getView());
        }
        this.messageList.clear();
        this.messageList.addAll(messageList);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        mOnMessageFragmentRefresh.onMessageFragmentRefresh();
    }

    public interface OnMessageFragmentRefresh {
        public void onMessageFragmentRefresh();
    }
}
