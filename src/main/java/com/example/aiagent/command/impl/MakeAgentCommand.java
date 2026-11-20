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
        return "이름으로 새 에이전트를 생성합니다";
    }

    @Override
    public CommandResult execute(CommandContext context, String input) throws IOException {
        context.console().println("에이전트 이름을 입력하세요. 취소하려면 /cancel을 입력하세요.");
        while (true) {
            String name = context.console().readLine("이름> ");
            if (name == null || "/cancel".equalsIgnoreCase(name.trim())) {
                context.console().println("에이전트 생성을 취소했습니다.");
                return CommandResult.CONTINUE;
            }
            if (name.isBlank()) {
                context.console().println("이름은 비워 둘 수 없습니다.");
                continue;
            }

            Agent agent = agentService.createAgent(name);
            context.console().printf("에이전트를 만들었습니다: %s (ID: %s)%n",
                    agent.name(), agent.id());
            return CommandResult.CONTINUE;
        }
    }
}
