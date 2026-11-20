package com.example.aiagent.command.impl;

import com.example.aiagent.agent.Agent;
import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import com.example.aiagent.llm.LlmResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

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
        return "이름 또는 ID로 에이전트를 선택해 대화합니다";
    }

    @Override
    public boolean availableInChat() {
        return true;
    }

    @Override
    public CommandResult execute(CommandContext context, String input)
            throws IOException, InterruptedException {
        if (!context.chatMode()) {
            return selectAgent(context);
        }

        if (input.isBlank()) {
            return CommandResult.CONTINUE;
        }

        try {
            LlmResponse response = agentService.chat(input);
            Agent agent = agentService.currentAgent();
            context.console().println(agent.name() + "> " + response.content());
            context.console().printf("(tokens: input %d, output %d)%n",
                    response.inputTokens(), response.outputTokens());
        } catch (IOException exception) {
            context.console().error("API 요청에 실패했습니다. API 키와 네트워크 설정을 확인하세요.");
        }
        return CommandResult.CONTINUE;
    }

    private CommandResult selectAgent(CommandContext context) throws IOException {
        List<Agent> agents = agentService.listAgents();
        if (agents.isEmpty()) {
            context.console().println("먼저 /make-agent로 에이전트를 만들어 주세요.");
            return CommandResult.CONTINUE;
        }

        context.console().println("대화할 에이전트를 선택하세요:");
        for (Agent agent : agents) {
            context.console().printf("- %s (ID: %s)%n", agent.name(), agent.id());
        }

        while (true) {
            String identifier = context.console().readLine("이름 또는 ID (/cancel로 취소)> ");
            if (identifier == null || "/cancel".equalsIgnoreCase(identifier.trim())) {
                context.console().println("에이전트 선택을 취소했습니다.");
                return CommandResult.CONTINUE;
            }
            try {
                Agent selected = agentService.selectAgent(identifier);
                context.console().printf("현재 대화 대상: %s (ID: %s)%n",
                        selected.name(), selected.id());
                return CommandResult.ENTER_CHAT;
            } catch (IllegalArgumentException exception) {
                context.console().println(exception.getMessage());
            }
        }
    }
}
