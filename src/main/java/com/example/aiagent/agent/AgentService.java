package com.example.aiagent.agent;

import com.example.aiagent.llm.ChatMessage;
import com.example.aiagent.llm.LlmClient;
import com.example.aiagent.llm.LlmResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AgentService {
    private final LlmClient llmClient;
    private final Conversation conversation = new Conversation();
    private Agent agent;

    public AgentService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public synchronized void configureAgent(Agent agent) {
        this.agent = Objects.requireNonNull(agent, "agent must not be null");
        conversation.clear();
    }

    public synchronized boolean hasAgent() {
        return agent != null;
    }

    public synchronized Agent currentAgent() {
        return agent;
    }

    public synchronized LlmResponse chat(String userInput)
            throws IOException, InterruptedException {
        if (agent == null) {
            throw new IllegalStateException("Create an agent before starting a conversation");
        }
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("userInput must not be blank");
        }

        ChatMessage userMessage = new ChatMessage("user", userInput);
        List<ChatMessage> requestMessages = new ArrayList<>(conversation.history());
        requestMessages.add(userMessage);

        LlmResponse response = llmClient.generate(agent.instructions(), requestMessages);
        conversation.addMessage(userMessage);
        conversation.addMessage(new ChatMessage("assistant", response.content()));
        return response;
    }

    public List<ChatMessage> conversationHistory() {
        return conversation.history();
    }

    public synchronized void resetConversation() {
        conversation.clear();
    }
}
