package com.example.aiagent.agent;

import com.example.aiagent.llm.ChatMessage;
import com.example.aiagent.llm.LlmClient;
import com.example.aiagent.llm.LlmResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

@Service
public class AgentInteractionService {
    private static final int ROUND_TRIPS = 5;

    private final AgentManager agentManager;
    private final LlmClient llmClient;

    public AgentInteractionService(AgentManager agentManager, LlmClient llmClient) {
        this.agentManager = agentManager;
        this.llmClient = llmClient;
    }

    public synchronized void interact(
            UUID firstAgentId,
            UUID secondAgentId,
            BiConsumer<Agent, String> onUtterance)
            throws IOException, InterruptedException {
        Objects.requireNonNull(firstAgentId, "firstAgentId must not be null");
        Objects.requireNonNull(secondAgentId, "secondAgentId must not be null");
        Objects.requireNonNull(onUtterance, "onUtterance must not be null");
        if (firstAgentId.equals(secondAgentId)) {
            throw new IllegalArgumentException("Choose two different agents");
        }

        AgentSession first = agentManager.session(firstAgentId);
        AgentSession second = agentManager.session(secondAgentId);
        Agent firstAgent = first.agent();
        Agent secondAgent = second.agent();
        Conversation firstConversation = first.conversation();
        Conversation secondConversation = second.conversation();

        for (int round = 0; round < ROUND_TRIPS; round++) {
            ChatMessage openingPrompt = round == 0
                    ? openingPrompt(secondAgent)
                    : null;
            List<ChatMessage> firstRequest = new ArrayList<>(firstConversation.history());
            if (openingPrompt != null) {
                firstRequest.add(openingPrompt);
            }

            LlmResponse firstResponse = generate(firstAgent, secondAgent, firstRequest);
            ChatMessage firstMessageForSecond = peerMessage(firstAgent, firstResponse.content());

            List<ChatMessage> secondRequest = new ArrayList<>(secondConversation.history());
            secondRequest.add(firstMessageForSecond);
            LlmResponse secondResponse = generate(secondAgent, firstAgent, secondRequest);
            ChatMessage secondMessageForFirst = peerMessage(secondAgent, secondResponse.content());

            // Commit both sides only after both API calls in this round succeed.
            if (openingPrompt != null) {
                firstConversation.addMessage(openingPrompt);
            }
            firstConversation.addMessage(new ChatMessage("assistant", firstResponse.content()));
            firstConversation.addMessage(secondMessageForFirst);

            secondConversation.addMessage(firstMessageForSecond);
            secondConversation.addMessage(new ChatMessage("assistant", secondResponse.content()));

            onUtterance.accept(firstAgent, firstResponse.content());
            onUtterance.accept(secondAgent, secondResponse.content());
        }
    }

    private LlmResponse generate(Agent speaker, Agent partner, List<ChatMessage> messages)
            throws IOException, InterruptedException {
        String instructions = speaker.instructions()
                + " You are speaking with Agent " + partner.name() + " (ID: " + partner.id() + ")."
                + " A user-role message prefixed with [Message from Agent ...] is the partner's"
                + " latest utterance. Respond to that utterance."
                + " A message prefixed with [Start conversation] asks you to open the dialogue.";
        LlmResponse response = llmClient.generate(instructions, messages);
        if (response == null || response.content() == null || response.content().isBlank()) {
            throw new IOException("Agent returned an empty response");
        }
        return response;
    }

    private static ChatMessage openingPrompt(Agent partner) {
        String content = "[Start conversation] Begin a dialogue with Agent "
                + partner.name() + " (ID: " + partner.id() + "). Say your first message.";
        return new ChatMessage("user", content);
    }

    private static ChatMessage peerMessage(Agent speaker, String content) {
        String attributedContent = "[Message from Agent " + speaker.name()
                + " (ID: " + speaker.id() + ")]\n" + content;
        return new ChatMessage("user", attributedContent);
    }
}
