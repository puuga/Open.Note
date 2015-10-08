package com.puuga.opennote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.puuga.opennote.R;
import com.puuga.opennote.model.Message;

import java.util.List;

/**
 * Created by siwaweswongcharoen on 10/8/2015 AD.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_message, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final Message message = messageList.get(position);
        holder.tvMessage.setText(message.getMessage());
        holder.tvUser.setText(message.getUser().name);
        String latlng = message.getLat() + "," + message.getLng();
        holder.tvLatlng.setText(latlng);
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMessage;
        public TextView tvUser;
        public TextView tvLatlng;

        public MessageViewHolder(View itemView) {
            super(itemView);

            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvLatlng = (TextView) itemView.findViewById(R.id.tv_latlng);
        }

    }
}
