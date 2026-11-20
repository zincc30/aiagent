package com.example.aiagent.llm;

public record LlmResponse (String content, int inputTokens, int outputTokens) {
}
