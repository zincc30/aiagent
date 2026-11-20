package com.example.aiagent.llm;

import java.io.IOException;
import java.util.List;

public interface LlmClient {

    LlmResponse generate(List<ChatMessage> messages)
            throws IOException, InterruptedException;

    default LlmResponse generate(String instructions, List<ChatMessage> messages)
            throws IOException, InterruptedException {
        return generate(messages);
    }
}
