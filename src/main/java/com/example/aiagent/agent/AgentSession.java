package com.example.aiagent.agent;

public final class AgentSession {
    private final Agent agent;
    private final Conversation conversation;

    AgentSession(Agent agent) {
        this.agent = agent;
        this.conversation = new Conversation();
    }

    public Agent agent() {
        return agent;
    }

    public Conversation conversation() {
        return conversation;
    }
}
