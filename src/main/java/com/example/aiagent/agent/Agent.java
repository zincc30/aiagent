package com.example.aiagent.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Agent {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final String personality;
    private final String goal;
    private final String speakingStyle;
    private final String belief;

    public Agent(String name, String personality, String goal,
                 String speakingStyle, String belief) {
        this.name = required(name, "name");
        this.personality = required(personality, "personality");
        this.goal = required(goal, "goal");
        this.speakingStyle = optional(speakingStyle);
        this.belief = optional(belief);
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

    public String goal() {
        return goal;
    }

    public String speakingStyle() {
        return speakingStyle;
    }

    public String belief() {
        return belief;
    }

    public String instructions() {
        List<String> settings = new ArrayList<>();
        settings.add("이름: " + name);
        settings.add("성격: " + personality);
        settings.add("목표: " + goal);
        if (!speakingStyle.isBlank()) {
            settings.add("말투: " + speakingStyle);
        }
        if (!belief.isBlank()) {
            settings.add("신념: " + belief);
        }

        return """
                너는 사용자와 대화하는 AI 캐릭터다. 아래 설정을 대화 내내 일관되게 따른다.
                %s
                사용자의 언어로 답하고, 이전 대화 내용을 기억해 자연스럽게 이어서 대화한다.
                """.formatted(String.join("\n", settings));
    }

    private static String required(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }
}

