package com.xiaoan.assistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ChatMessage.TYPE_USER) {
            View view = inflater.inflate(R.layout.item_msg_user, parent, false);
            return new UserMsgHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_msg_assistant, parent, false);
            return new AssistantMsgHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof UserMsgHolder) {
            ((UserMsgHolder) holder).bind(msg);
        } else if (holder instanceof AssistantMsgHolder) {
            ((AssistantMsgHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMsgHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        UserMsgHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMsgContent);
            tvTime = itemView.findViewById(R.id.tvMsgTime);
        }

        void bind(ChatMessage msg) {
            tvContent.setText(msg.getContent());
            tvTime.setText(msg.getTime());
        }
    }

    static class AssistantMsgHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        AssistantMsgHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMsgContent);
            tvTime = itemView.findViewById(R.id.tvMsgTime);
        }

        void bind(ChatMessage msg) {
            tvContent.setText(msg.getContent());
            tvTime.setText(msg.getTime());
        }
    }
}
