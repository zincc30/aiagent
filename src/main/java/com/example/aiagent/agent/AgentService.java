package com.example.aiagent.agent;

import com.example.aiagent.llm.ChatMessage;
import com.example.aiagent.llm.LlmClient;
import com.example.aiagent.llm.LlmResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentService {
    private final LlmClient llmClient;
    private final AgentManager agentManager;
    private UUID activeAgentId;

    public AgentService(LlmClient llmClient, AgentManager agentManager) {
        this.llmClient = llmClient;
        this.agentManager = agentManager;
    }

    public Agent createAgent(String name, String personality, String interests, String goal) {
        return agentManager.createAgent(name, personality, interests, goal);
    }

    public List<Agent> listAgents() {
        return agentManager.listAgents();
    }

    public Agent resolveAgent(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Enter an agent name or ID");
        }
        String value = identifier.trim();
        Agent selected = null;
        try {
            UUID id = UUID.fromString(value);
            Optional<Agent> byId = agentManager.findById(id);
            if (byId.isPresent()) {
                selected = byId.get();
            }
        } catch (IllegalArgumentException ignored) {
            // The input may be an agent name instead of a UUID.
        }

        if (selected == null) {
            List<Agent> matches = agentManager.findByName(value);
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("No agent found with name or ID: " + value);
            }
            if (matches.size() > 1) {
                String ids = matches.stream()
                        .map(agent -> agent.name() + " (" + agent.id() + ")")
                        .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(
                        "More than one agent has that name. Select by ID: " + ids);
            }
            selected = matches.get(0);
        }
        return selected;
    }

    public synchronized Agent selectAgent(String identifier) {
        Agent selected = resolveAgent(identifier);
        activeAgentId = selected.id();
        return selected;
    }

    public synchronized boolean hasActiveAgent() {
        return activeAgentId != null;
    }

    public synchronized Agent currentAgent() {
        return activeAgentId == null
                ? null
                : agentManager.session(activeAgentId).agent();
    }

    public synchronized LlmResponse chat(String userInput)
            throws IOException, InterruptedException {
        if (activeAgentId == null) {
            throw new IllegalStateException("Select an agent before starting a conversation");
        }
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("userInput must not be blank");
        }

        AgentSession session = agentManager.session(activeAgentId);
        Agent agent = session.agent();
        Conversation conversation = session.conversation();
        ChatMessage userMessage = new ChatMessage("user", userInput);
        List<ChatMessage> requestMessages = new ArrayList<>(conversation.history());
        requestMessages.add(userMessage);

        LlmResponse response = llmClient.generate(agent.instructions(), requestMessages);
        conversation.addMessage(userMessage);
        conversation.addMessage(new ChatMessage("assistant", response.content()));
        return response;
    }

    public synchronized void resetConversation() {
        if (activeAgentId == null) {
            throw new IllegalStateException("No active agent conversation to reset");
        }
        agentManager.session(activeAgentId).conversation().clear();
    }

    public synchronized void leaveChat() {
        activeAgentId = null;
    }

    public List<ChatMessage> conversationHistory(UUID agentId) {
        return agentManager.session(Objects.requireNonNull(agentId, "agentId must not be null"))
                .conversation().history();
    }
}
