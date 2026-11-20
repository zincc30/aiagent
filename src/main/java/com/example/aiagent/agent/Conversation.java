package com.example.aiagent.agent;

import com.example.aiagent.llm.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Conversation {
    private final List<ChatMessage> messages = new ArrayList<>();

    public synchronized void addMessage(ChatMessage message) {
        messages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    public synchronized List<ChatMessage> history() {
        return List.copyOf(messages);
    }

    public synchronized void clear() {
        messages.clear();
    }
}
