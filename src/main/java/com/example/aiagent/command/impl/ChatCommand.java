package com.example.aiagent.command.impl;

import com.example.aiagent.agent.Agent;
import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import com.example.aiagent.llm.LlmResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChatCommand implements Command {
    private final AgentService agentService;

    public ChatCommand(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public String name() {
        return "/chat";
    }

    @Override
    public String description() {
        return "에이전트와 대화를 시작합니다";
    }

    @Override
    public boolean availableInChat() {
        return true;
    }

    @Override
    public CommandResult execute(CommandContext context, String input)
            throws IOException, InterruptedException {
        if (!context.chatMode()) {
            if (!agentService.hasAgent()) {
                context.console().println("먼저 /make-agent로 에이전트를 만들어 주세요.");
                return CommandResult.CONTINUE;
            }
            if (!input.isBlank()) {
                context.console().println("/chat은 인수 없이 실행하세요.");
                return CommandResult.CONTINUE;
            }
            Agent agent = agentService.currentAgent();
            context.console().printf("%s (ID: %s)와 대화를 시작합니다.%n", agent.name(), agent.id());
            return CommandResult.ENTER_CHAT;
        }

        if (input.isBlank()) {
            return CommandResult.CONTINUE;
        }

        try {
            LlmResponse response = agentService.chat(input);
            context.console().println(agentService.currentAgent().name() + "> " + response.content());
            context.console().printf("(tokens: input %d, output %d)%n",
                    response.inputTokens(), response.outputTokens());
        } catch (IOException exception) {
            context.console().error("API 요청에 실패했습니다. API 키와 네트워크 설정을 확인하세요.");
        }
        return CommandResult.CONTINUE;
    }
}
