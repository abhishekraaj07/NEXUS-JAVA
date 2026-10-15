package com.nexus.chat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexus.chat.R;
import com.nexus.chat.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        String timeStr = timeFormat.format(new Date(msg.timestamp));

        if (msg.isOwn) {
            holder.layoutOwn.setVisibility(View.VISIBLE);
            holder.layoutPeer.setVisibility(View.GONE);

            holder.tvOwnText.setText(msg.text);
            holder.tvOwnTime.setText(timeStr);
            holder.tvOwnStatus.setText("sent".equals(msg.status) ? " ✓" : " ✓✓");
        } else {
            holder.layoutOwn.setVisibility(View.GONE);
            holder.layoutPeer.setVisibility(View.VISIBLE);

            holder.tvPeerName.setText(msg.senderName != null ? msg.senderName : "Peer");
            holder.tvPeerText.setText(msg.text);
            holder.tvPeerTime.setText(timeStr);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout layoutOwn;
        final LinearLayout layoutPeer;
        final TextView tvOwnText;
        final TextView tvOwnTime;
        final TextView tvOwnStatus;
        final TextView tvPeerName;
        final TextView tvPeerText;
        final TextView tvPeerTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutOwn = itemView.findViewById(R.id.layout_own_message);
            layoutPeer = itemView.findViewById(R.id.layout_peer_message);
            tvOwnText = itemView.findViewById(R.id.tv_own_text);
            tvOwnTime = itemView.findViewById(R.id.tv_own_time);
            tvOwnStatus = itemView.findViewById(R.id.tv_own_status);
            tvPeerName = itemView.findViewById(R.id.tv_peer_name);
            tvPeerText = itemView.findViewById(R.id.tv_peer_text);
            tvPeerTime = itemView.findViewById(R.id.tv_peer_time);
        }
    }
}
