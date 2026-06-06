package com.xiaoan.assistant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_ASSISTANT = 1;

    private int type;
    private String content;
    private String time;

    public ChatMessage(int type, String content) {
        this.type = type;
        this.content = content;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.time = sdf.format(new Date());
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }
}
