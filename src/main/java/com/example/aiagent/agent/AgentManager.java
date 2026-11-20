package com.example.aiagent.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class AgentManager {
    private final Map<UUID, AgentSession> sessions = new LinkedHashMap<>();

    public synchronized Agent createAgent(String name) {
        Agent agent = new Agent(name);
        sessions.put(agent.id(), new AgentSession(agent));
        return agent;
    }

    public synchronized List<Agent> listAgents() {
        return sessions.values().stream()
                .map(AgentSession::agent)
                .toList();
    }

    public synchronized Optional<Agent> findById(UUID id) {
        AgentSession session = sessions.get(id);
        return session == null ? Optional.empty() : Optional.of(session.agent());
    }

    public synchronized List<Agent> findByName(String name) {
        return sessions.values().stream()
                .map(AgentSession::agent)
                .filter(agent -> agent.name().equalsIgnoreCase(name.trim()))
                .toList();
    }

    public synchronized AgentSession session(UUID id) {
        AgentSession session = sessions.get(id);
        if (session == null) {
            throw new IllegalArgumentException("No agent found with ID: " + id);
        }
        return session;
    }
}
