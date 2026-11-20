package com.example.aiagent.agent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Agent {
    private final UUID id;
    private final String name;
    private final String personality;
    private final List<String> interests;
    private final String goal;

    public Agent(String name, String personality, String interests, String goal) {
        this(UUID.randomUUID(), name, personality, parseInterests(interests), goal);
    }

    public Agent(UUID id, String name, String personality, List<String> interests, String goal) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = required(name, "name");
        this.personality = required(personality, "personality");
        this.interests = List.copyOf(Objects.requireNonNull(interests, "interests must not be null"));
        this.goal = required(goal, "goal");
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

    public List<String> interests() {
        return interests;
    }

    public String goal() {
        return goal;
    }

    public String instructions() {
        return "You are an AI agent named \"" + name + "\". "
                + "Your personality is: " + personality + ". "
                + "Your interests are: " + String.join(", ", interests) + ". "
                + "Your goal is: " + goal + ".";
    }

    private static String required(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static List<String> parseInterests(String interests) {
        String value = required(interests, "interests");
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
