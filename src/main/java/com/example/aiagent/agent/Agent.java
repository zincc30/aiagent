package com.example.aiagent.agent;

import java.util.Objects;
import java.util.UUID;

public class Agent {
    private final UUID id = UUID.randomUUID();
    private final String name;

    public Agent(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String instructions() {
        return "You are an AI agent named \"" + name + "\".";
    }
}
