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
        holder.tvMessage.setText(message.message);
        holder.tvUser.setText(message.user.name);
        String latlng = message.lat + "," + message.lng;
        holder.tvLatlng.setText(latlng);
        holder.tvCreatedAt.setText(message.created_at);

        // Load image
        Glide.with(context)
                .load(message.user.getUserPictureUrl())
                .bitmapTransform(new CropCircleTransformation(context))
                .crossFade()
                .into(holder.ivUserPicture);
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivUserPicture;
        public TextView tvMessage;
        public TextView tvUser;
        public TextView tvLatlng;
        public TextView tvCreatedAt;

        public MessageViewHolder(View itemView) {
            super(itemView);

            ivUserPicture = (ImageView) itemView.findViewById(R.id.iv_user_picture);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvLatlng = (TextView) itemView.findViewById(R.id.tv_latlng);
            tvCreatedAt = (TextView) itemView.findViewById(R.id.tv_create_at);
        }

    }

    public class MeViewHolder extends RecyclerView.ViewHolder {

        public TextView tvUserName;
        public TextView tvUserEmail;

        public MeViewHolder(View itemView) {
            super(itemView);

            tvUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = (TextView) itemView.findViewById(R.id.tv_user_email);
        }
    }
}
