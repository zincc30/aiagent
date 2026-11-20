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
        return "에이전트 설정을 입력해 생성합니다";
    }

    @Override
    public CommandResult execute(CommandContext context, String input) throws IOException {
        context.console().println("에이전트 설정을 입력하세요. 취소하려면 /cancel을 입력하세요.");
        String name = readSetting(context, "이름", true);
        if (name == null) return CommandResult.CONTINUE;
        String personality = readSetting(context, "성격", true);
        if (personality == null) return CommandResult.CONTINUE;
        String goal = readSetting(context, "목표", true);
        if (goal == null) return CommandResult.CONTINUE;
        String speakingStyle = readSetting(context, "말투 (선택 사항)", false);
        if (speakingStyle == null) return CommandResult.CONTINUE;
        String belief = readSetting(context, "신념 (선택 사항)", false);
        if (belief == null) return CommandResult.CONTINUE;

        Agent agent = new Agent(name, personality, goal, speakingStyle, belief);
        agentService.configureAgent(agent);
        context.console().printf("에이전트를 만들었습니다: %s (ID: %s)%n", agent.name(), agent.id());
        context.console().println("새 에이전트 설정으로 대화 기록을 초기화했습니다.");
        return CommandResult.CONTINUE;
    }

    private String readSetting(CommandContext context, String label, boolean required)
            throws IOException {
        while (true) {
            String value = context.console().readLine(label + "> ");
            if (value == null || "/cancel".equalsIgnoreCase(value.trim())) {
                context.console().println("에이전트 설정을 취소했습니다.");
                return null;
            }
            if (!required || !value.isBlank()) {
                return value.trim();
            }
            context.console().println(label + "은(는) 비워 둘 수 없습니다.");
        }
    }
}
