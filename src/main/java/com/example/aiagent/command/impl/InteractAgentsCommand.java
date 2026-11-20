package com.example.aiagent.command.impl;

import com.example.aiagent.agent.Agent;
import com.example.aiagent.agent.AgentInteractionService;
import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class InteractAgentsCommand implements Command {
    private final AgentService agentService;
    private final AgentInteractionService interactionService;

    public InteractAgentsCommand(
            AgentService agentService,
            AgentInteractionService interactionService) {
        this.agentService = agentService;
        this.interactionService = interactionService;
    }

    @Override
    public String name() {
        return "/interact";
    }

    @Override
    public String description() {
        return "두 에이전트를 5회 왕복 대화시킵니다";
    }

    @Override
    public CommandResult execute(CommandContext context, String input)
            throws IOException, InterruptedException {
        List<Agent> agents = agentService.listAgents();
        if (agents.size() < 2) {
            context.console().println("/interact를 사용하려면 에이전트가 두 개 이상 필요합니다.");
            return CommandResult.CONTINUE;
        }

        Agent first = selectAgent(context, agents, "첫 번째 에이전트", null);
        if (first == null) {
            return CommandResult.CONTINUE;
        }
        Agent second = selectAgent(context, agents, "두 번째 에이전트", first);
        if (second == null) {
            return CommandResult.CONTINUE;
        }

        context.console().printf("상호작용 시작: %s ↔ %s %n",
                first.name(), second.name());
        try {
            interactionService.interact(first.id(), second.id(),
                    (speaker, message) -> context.console().printf("%s (ID: %s)> %s%n",
                            speaker.name(), speaker.id(), message));
            context.console().println("에이전트 상호작용을 마쳤습니다.");
        } catch (IOException exception) {
            context.console().error(
                    "API 요청이 실패했습니다. 실패한 왕복은 두 대화 기록에 저장하지 않았습니다.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            context.console().error("API 요청이 중단되었습니다. 현재 왕복은 저장하지 않았습니다.");
            return CommandResult.EXIT;
        }
        return CommandResult.CONTINUE;
    }

    private Agent selectAgent(
            CommandContext context,
            List<Agent> agents,
            String prompt,
            Agent excluded) throws IOException {
        context.console().println(prompt + "을(를) 이름 또는 ID로 선택하세요:");
        for (Agent agent : agents) {
            if (excluded == null || !excluded.id().equals(agent.id())) {
                context.console().printf("- %s (ID: %s)%n", agent.name(), agent.id());
            }
        }

        while (true) {
            String identifier = context.console().readLine("이름 또는 ID (/cancel로 취소)> ");
            if (identifier == null || "/cancel".equalsIgnoreCase(identifier.trim())) {
                context.console().println("상호작용을 취소했습니다.");
                return null;
            }
            try {
                Agent selected = agentService.resolveAgent(identifier);
                if (excluded != null && excluded.id().equals(selected.id())) {
                    context.console().println("같은 에이전트를 양쪽에 선택할 수 없습니다.");
                    continue;
                }
                return selected;
            } catch (IllegalArgumentException exception) {
                context.console().println(exception.getMessage());
            }
        }
    }
}
