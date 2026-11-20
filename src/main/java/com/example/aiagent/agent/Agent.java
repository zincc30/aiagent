package com.example.aiagent.agent;

import java.util.Objects;
import java.util.UUID;

public class Agent {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final String personality;

    public Agent(String name, String personality) {
        this.name = required(name, "name");
        this.personality = required(personality, "personality");
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String personality() {
        return personality;
    }

    public String instructions() {
        return "You are an AI agent named \"" + name + "\". "
                + "Your personality is: " + personality + ".";
    }

    private static String required(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
