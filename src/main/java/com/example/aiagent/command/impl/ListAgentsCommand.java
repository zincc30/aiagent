package com.example.aiagent.command.impl;

import com.example.aiagent.agent.Agent;
import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAgentsCommand implements Command {
    private final AgentService agentService;

    public ListAgentsCommand(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public String name() {
        return "/list-agents";
    }

    @Override
    public String description() {
        return "생성된 에이전트 목록과 ID를 표시합니다";
    }

    @Override
    public CommandResult execute(CommandContext context, String input) {
        List<Agent> agents = agentService.listAgents();
        if (agents.isEmpty()) {
            context.console().println("생성된 에이전트가 없습니다. /make-agent를 먼저 실행하세요.");
            return CommandResult.CONTINUE;
        }

        context.console().println("생성된 에이전트:");
        for (Agent agent : agents) {
            context.console().printf("- %s (ID: %s)%n", agent.name(), agent.id());
        }
        return CommandResult.CONTINUE;
    }
}
