package com.puuga.opennote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.puuga.opennote.R;
import com.puuga.opennote.model.Message;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by siwaweswongcharoen on 10/23/2015 AD.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.MainViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String userName;
    private String userEmail;
    private String userPictureUrl;

    private static final int TYPE_PROFILE = 1;
    private static final int TYPE_MESSAGE = 2;

    public ProfileAdapterLongClickListener adapterLongClickListener;

    public ProfileAdapter(Context context,
                          List<Message> messageList,
                          ProfileAdapterLongClickListener adapterLongClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.adapterLongClickListener = adapterLongClickListener;
    }

    public interface ProfileAdapterLongClickListener {
        void recyclerViewLongClick(String messageID);
    }

    //listener passed to viewHolder
    public interface MessageLongClickListener {
        void messageOnLongClick(String messageID);
    }


    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PROFILE:
                return new MeViewHolder(LayoutInflater
                        .from(context).inflate(R.layout.card_me, parent, false));
            case TYPE_MESSAGE:
                return new MessageViewHolder(LayoutInflater
                        .from(context).inflate(R.layout.card_message, parent, false),
                        new MessageLongClickListener() {
                            @Override
                            public void messageOnLongClick(String messageID) {
                                adapterLongClickListener.recyclerViewLongClick(messageID);
                            }
                        });
        }
        return null;
    }

    public void setUser(String userName, String userEmail, String userPictureUrl) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPictureUrl = userPictureUrl;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? TYPE_PROFILE : TYPE_MESSAGE);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_PROFILE:
                setUpProfileCard((MeViewHolder) holder);
                break;
            case TYPE_MESSAGE:
                setUpMessageCard((MessageViewHolder) holder, position-1);
                break;
        }
    }

    private void setUpProfileCard(MeViewHolder holder) {
        try {
            holder.tvUserName.setText(userName);
            holder.tvUserEmail.setText(userEmail);
            holder.tvMessagesCount
                    .setText(context.getString(R.string.messages_count, messageList.size()));
        } catch (Exception ignored) {
        }
    }

    private void setUpMessageCard(MessageViewHolder holder, int position) {
        final Message message = messageList.get(position);
        holder.tvMessage.setText(message.message);
        holder.tvUser.setText(userName);
        String latlng = message.lat + "," + message.lng;
        holder.tvLatlng.setText(latlng);
        holder.tvCreatedAt.setText(message.created_at);
        holder.tvMessageId.setText(message.id);

        // Load image
        Glide.with(context)
                .load(userPictureUrl)
                .bitmapTransform(new CropCircleTransformation(context))
                .crossFade()
                .into(holder.ivUserPicture);
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size()+1;
    }

    public class MessageViewHolder extends MainViewHolder implements View.OnLongClickListener {

        public ImageView ivUserPicture;
        public TextView tvMessage;
        public TextView tvUser;
        public TextView tvLatlng;
        public TextView tvCreatedAt;
        public MessageLongClickListener messageLongClickListener;
        public TextView tvMessageId;

        public MessageViewHolder(View itemView, MessageLongClickListener messageLongClickListener) {
            super(itemView);

            ivUserPicture = (ImageView) itemView.findViewById(R.id.iv_user_picture);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvLatlng = (TextView) itemView.findViewById(R.id.tv_latlng);
            tvCreatedAt = (TextView) itemView.findViewById(R.id.tv_create_at);
            tvMessageId = (TextView) itemView.findViewById(R.id.tv_message_id);
            this.messageLongClickListener = messageLongClickListener;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            String messageId = ((TextView) view.findViewById(R.id.tv_message_id)).getText().toString();
            messageLongClickListener.messageOnLongClick(messageId);
            return false;
        }

    }

    public class MeViewHolder extends MainViewHolder {

        public TextView tvUserName;
        public TextView tvUserEmail;
        public TextView tvMessagesCount;

        public MeViewHolder(View itemView) {
            super(itemView);

            tvUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = (TextView) itemView.findViewById(R.id.tv_user_email);
            tvMessagesCount = (TextView) itemView.findViewById(R.id.tv_messages_count);
        }
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        public MainViewHolder(View v) {
            super(v);
        }


    }
}
