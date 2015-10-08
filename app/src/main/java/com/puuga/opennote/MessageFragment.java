package com.puuga.opennote;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puuga.opennote.adapter.MessageAdapter;
import com.puuga.opennote.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

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
        messageList = new ArrayList<>();

        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), messageList);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(messageAdapter);

        return view;
    }

    public void setAdapter(List<Message> messageList) {
        this.messageList.clear();
        this.messageList.addAll(messageList);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

}
