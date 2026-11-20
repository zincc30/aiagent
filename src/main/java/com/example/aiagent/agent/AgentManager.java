package com.example.aiagent.agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class AgentManager implements InitializingBean {
    private final Map<UUID, AgentSession> sessions = new LinkedHashMap<>();
    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AgentManager(@Value("${app.agent.config-path}") String configPath) {
        this.configPath = Path.of(configPath);
    }

    @Override
    public synchronized void afterPropertiesSet() {
        loadAgents();
    }

    public synchronized Agent createAgent(
            String name, String personality, String interests, String goal) {
        Agent agent = new Agent(name, personality, interests, goal);
        sessions.put(agent.id(), new AgentSession(agent));
        try {
            saveAgents();
        } catch (IOException e) {
            sessions.remove(agent.id());
            throw new IllegalStateException("Could not save agents to " + configPath, e);
        }
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

    private void loadAgents() {
        if (!Files.exists(configPath)) {
            return;
        }
        try {
            String json = Files.readString(configPath);
            if (json.isBlank()) {
                return;
            }
            AgentFile file = gson.fromJson(json, AgentFile.class);
            if (file == null || file.agents == null) {
                return;
            }
            for (StoredAgent stored : file.agents) {
                Agent agent = new Agent(UUID.fromString(stored.id), stored.name,
                        stored.personality, stored.interests, stored.goal);
                if (sessions.putIfAbsent(agent.id(), new AgentSession(agent)) != null) {
                    throw new IllegalStateException("Duplicate agent ID in " + configPath + ": " + agent.id());
                }
            }
        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            throw new IllegalStateException("Could not load agents from " + configPath, e);
        }
    }

    private void saveAgents() throws IOException {
        Path parent = configPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        AgentFile file = new AgentFile();
        file.agents = sessions.values().stream()
                .map(AgentSession::agent)
                .map(agent -> new StoredAgent(agent.id().toString(), agent.name(),
                        agent.personality(), agent.interests(), agent.goal()))
                .toList();
        Files.writeString(configPath, gson.toJson(file));
    }

    private static final class AgentFile {
        private List<StoredAgent> agents;
    }

    private static final class StoredAgent {
        private String id;
        private String name;
        private String personality;
        private List<String> interests;
        private String goal;

        private StoredAgent(String id, String name, String personality,
                            List<String> interests, String goal) {
            this.id = id;
            this.name = name;
            this.personality = personality;
            this.interests = interests;
            this.goal = goal;
        }
    }
}
