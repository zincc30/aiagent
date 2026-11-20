package com.example.aiagent.command.impl;

import com.example.aiagent.agent.Agent;
import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MakeAgentCommand implements Command {
    private final AgentService agentService;

    public MakeAgentCommand(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public String name() {
        return "/make-agent";
    }

    @Override
    public String description() {
        return "이름, 성격, 관심사, 목표를 입력해 새 에이전트를 생성합니다";
    }

    @Override
    public CommandResult execute(CommandContext context, String input) throws IOException {
        context.console().println(
                "에이전트 이름, 성격, 관심사, 목표를 입력하세요. 취소하려면 /cancel을 입력하세요.");
        String name = readRequired(context, "이름");
        if (name == null) return CommandResult.CONTINUE;
        String personality = readRequired(context, "성격");
        if (personality == null) return CommandResult.CONTINUE;
        String interests = readRequired(context, "관심사");
        if (interests == null) return CommandResult.CONTINUE;
        String goal = readRequired(context, "목표");
        if (goal == null) return CommandResult.CONTINUE;

        Agent agent = agentService.createAgent(name, personality, interests, goal);
        context.console().printf("에이전트를 만들었습니다: %s (ID: %s)%n",
                agent.name(), agent.id());
        return CommandResult.CONTINUE;
    }

    private String readRequired(CommandContext context, String label) throws IOException {
        while (true) {
            String value = context.console().readLine(label + "> ");
            if (value == null || "/cancel".equalsIgnoreCase(value.trim())) {
                context.console().println("에이전트 생성을 취소했습니다.");
                return null;
            }
            if (!value.isBlank()) {
                return value.trim();
            }
            context.console().println(label + "은(는) 비워 둘 수 없습니다.");
        }
    }
}
